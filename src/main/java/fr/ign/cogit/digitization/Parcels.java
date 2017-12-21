package fr.ign.cogit.digitization;

import java.util.List;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPositionList;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ILineString;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IPolygon;
import fr.ign.cogit.geoxygene.contrib.cartetopo.Arc;
import fr.ign.cogit.geoxygene.contrib.cartetopo.CarteTopo;
import fr.ign.cogit.geoxygene.contrib.cartetopo.Face;
import fr.ign.cogit.geoxygene.contrib.geometrie.Vecteur;
import fr.ign.cogit.geoxygene.feature.DefaultFeature;
import fr.ign.cogit.geoxygene.feature.Population;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPosition;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPositionList;
import fr.ign.cogit.geoxygene.spatial.coordgeom.GM_LineString;
import fr.ign.cogit.geoxygene.spatial.coordgeom.GM_Polygon;
import fr.ign.cogit.geoxygene.spatial.geomprim.GM_Point;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileReader;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileWriter;

public class Parcels {


    public static IDirectPosition translate(IDirectPosition p1, IDirectPosition p2, boolean dir, IPolygon face, double t){
        double x1 = p1.getX();
        double y1 = p1.getY();
        double x2 = p2.getX();
        double y2 = p2.getY();

        Vecteur n1 = new Vecteur(y1-y2, x2-x1);
        n1 = n1.getNormalised();
        Vecteur n2 = new Vecteur(y2-y1, x1-x2);
        n2 = n2.getNormalised();
        IDirectPosition pnew1 = new DirectPosition(p1.getX() + n1.multConstante(t).getX(), p1.getY() + n1.multConstante(t).getY());
        IDirectPosition pnew2 = new DirectPosition(p1.getX() + n2.multConstante(t).getX(), p1.getY() + n2.multConstante(t).getY());

        if(face.contains(new GM_Point(pnew1))){
            if(face.contains(new GM_Point(pnew2))){
                if((new GM_Point(pnew1)).distance(face.exteriorLineString())
                        >(new GM_Point(pnew2)).distance(face.exteriorLineString())){
                    return pnew1;
                }
                else{
                    return pnew2;
                }
            }
            else{
                return pnew1;
            }
        }
        else if(face.contains(new GM_Point(pnew2))){
            return pnew2;
        }
        else{
            if((new GM_Point(pnew1)).distance(face.exteriorLineString())
                   <(new GM_Point(pnew2)).distance(face.exteriorLineString())){
                return pnew1;
            }
            else{
                return pnew2;
            }
        }



    }

    private static ILineString shrinkLineString(ILineString line, IPolygon face, double t){
        IDirectPositionList list = new DirectPositionList();


        for(int i=1; i< line.coord().size(); i++){
            IDirectPosition p1 = line.getControlPoint(i-1);
            IDirectPosition p2 = line.getControlPoint(i);
            IDirectPosition p3 = null;
            if(i == line.getControlPoint().size()-1){
                p3 = line.getControlPoint(1);
            }
            else{
                p3 = line.getControlPoint(i+1);
            }
            IDirectPosition pnew1 = Parcels.translate(p2, p1, false, face, t);
            IDirectPosition pnew2 = Parcels.translate(p2, p3, true, face, t);
            double x = (pnew1.getX() + pnew2.getX()) / 2.;
            double y = (pnew1.getY() + pnew2.getY()) / 2.;
            IDirectPosition pnew = new DirectPosition(x, y);
            list.add(pnew);
        }
        list.add(0, list.get(list.size()-1));

        return new GM_LineString(list);
    }


    private static void shrinkPolygone(Face f, double t){
        ILineString line = new GM_LineString(f.getGeometrie().exteriorLineString().getPositive().coord());
        line = Parcels.shrinkLineString(line, f.getGeometrie(), t);
        f.setGeometrie(new GM_Polygon(line));
    }

    public static void main(String args[]){

        String shp ="/media/bcostes/Data/Benoit/these/donnees/vecteur/filaires/filaires_corriges/verniquet_l93_utf8_corr.shp";
        IPopulation<IFeature> pop = ShapefileReader.read(shp);
        CarteTopo map = new CarteTopo("");
        IPopulation<Arc> popArcs = map.getPopArcs();
        for(IFeature f: pop){
            Arc a  =popArcs.nouvelElement();
            a.setGeom(new GM_LineString(f.getGeom().coord()));
            a.addCorrespondant(f);
        }
        map.creeTopologieArcsNoeuds(0.);
        map.creeNoeudsManquants(0.);
        map.filtreArcsDoublons();
        map.filtreNoeudsIsoles();
        map.rendPlanaire(0.);
        map.creeTopologieFaces();

        List<Face> faces = map.getListeFaces();
        IPopulation<IFeature> out = new Population<IFeature>();
        for(Face f: faces){
            Parcels.shrinkPolygone(f, 5.);
            out.add(f);
        }
        ShapefileWriter.write(out, "/home/bcostes/Bureau/faces_2.shp");


    }

}
