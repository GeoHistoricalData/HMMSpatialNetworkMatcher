package fr.ign.cogit.v2.utils.streetsWidth;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import book.set.Hash;
import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPositionList;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ILineString;
import fr.ign.cogit.geoxygene.api.spatial.geomroot.IGeometry;
import fr.ign.cogit.geoxygene.contrib.geometrie.Operateurs;
import fr.ign.cogit.geoxygene.contrib.geometrie.Vecteur;
import fr.ign.cogit.geoxygene.feature.DefaultFeature;
import fr.ign.cogit.geoxygene.feature.Population;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPosition;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPositionList;
import fr.ign.cogit.geoxygene.spatial.coordgeom.GM_LineString;
import fr.ign.cogit.geoxygene.spatial.geomprim.GM_Point;
import fr.ign.cogit.geoxygene.util.attribute.AttributeManager;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileReader;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileWriter;
import fr.ign.cogit.geoxygene.util.index.Tiling;
import fr.ign.cogit.v2.io.SnapshotIOManager;
import fr.ign.cogit.v2.snapshot.GraphEntity;
import fr.ign.cogit.v2.snapshot.JungSnapshot;
import fr.ign.cogit.v2.weightings.LengthEdgeWeighting;

public class StreetsWidth {

    public static Map<GraphEntity, Double> getStreetsWidth(JungSnapshot snap, 
            IPopulation<IFeature> polygons){
        Map<GraphEntity, Double> widths = new HashMap<GraphEntity, Double>();
        for(GraphEntity e: snap.getEdges()){
            widths.put(e,-1.);
        }
        polygons.initSpatialIndex(Tiling.class, false);
        for(GraphEntity e : snap.getEdges()){
            // géométrie de la rue
            ILineString le = (ILineString)e.getGeometry().toGeoxGeometry();
            Collection<IFeature> closestPolygons = polygons.select(le, 0);
            if(!closestPolygons.isEmpty()){
                // on regarde si cette intersection contient au moins la moitié de la ligne
                Iterator<IFeature> it = closestPolygons.iterator();
                IGeometry union = it.next().getGeom();
                while(it.hasNext()){
                    union = union.union(it.next().getGeom());
                }
                if(union != null && union.intersection(le) != null && union.intersection(le).length() > 0.75* le.length()){
                    continue;
                }
            }
            List<Double> values = new ArrayList<Double>();
            for(int i=30; i<=61; i+=5){
                IDirectPosition point = Operateurs.pointEnAbscisseCurviligne(le, le.length() * ((double)i)/100.);
                //milieu
                closestPolygons = polygons.select(point, 0);
                if(closestPolygons.isEmpty()){
                    // milieu pas dans un ilot
                    double v = StreetsWidth.getNormalWidth(point, le, i, polygons);
                    if(v >0){
                        values.add(v);
                    }
                }             
            }

            if(values.size() != 0){
                //moyenne, écart type
                double moyenne = 0;
                double sigma = 0;
                for (Double t : values) {
                    moyenne += t;
                }
                moyenne /= (double) values.size();
                for (Double t : values) {
                    sigma += (moyenne - t) * (moyenne - t);
                }
                sigma /= (double) values.size();
                sigma = Math.sqrt(sigma);
                //on retire tout ce qui est supérieur à moyenne +- 3sigma
                double width =0;
                double cpt=0.;
                for (Double t : values) {
                    if(t <= moyenne + 2*sigma && t >= moyenne - 2*sigma){
                        width += t;
                        cpt += 1.;
                    }
                }
                widths.put(e, width / cpt);
                // widths.put(e, moyenne);

            }

        }

        return widths;
    }


    private static double getNormalWidth(IDirectPosition middle, ILineString line, double abscisse, IPopulation<IFeature> polygons){      
        //on le milieu et un point juste avant


        IDirectPosition before = Operateurs.pointEnAbscisseCurviligne(line, line.length() * ((double)(abscisse-1.)/100.) );
        //vecteur normaux
        double x1 = middle.getX();
        double y1 = middle.getY();
        double x2 = before.getX();
        double y2 = before.getY();

        //vecteurs normaux
        Vecteur n1 = new Vecteur(y1-y2, x2-x1);
        n1 = n1.getNormalised();
        Vecteur n2 = new Vecteur(y2-y1, x1-x2);
        n2 = n2.getNormalised();
        IDirectPosition pnew1 = new DirectPosition(middle.getX() + n1.multConstante(70).getX(), middle.getY() + n1.multConstante(70).getY());
        IDirectPosition pnew2 = new DirectPosition(middle.getX() + n2.multConstante(70).getX(), middle.getY() + n2.multConstante(70).getY());

        //intersections avec les ilots
        IDirectPositionList l1 = new DirectPositionList();
        l1.add(middle);
        l1.add(pnew1);
        IDirectPositionList l2 = new DirectPositionList();
        l2.add(middle);
        l2.add(pnew2);
        ILineString line1 = new GM_LineString(l1);
        ILineString line2 = new GM_LineString(l2);
        Collection<IFeature> closestPolygons1 = polygons.select(line1,0);
        Collection<IFeature> closestPolygons2 = polygons.select(line2,0);
        for(IFeature f: new ArrayList<IFeature>(closestPolygons1)){
            if(!f.getGeom().intersects(line1) /*|| f.getGeom().intersection(line1)  == null*/){
                closestPolygons1.remove(f);
            }
        }
        for(IFeature f: new ArrayList<IFeature>(closestPolygons2)){
            if(!f.getGeom().intersects(line2) /*|| f.getGeom().intersection(line2)  == null*/){
                closestPolygons2.remove(f);
            }
        }
        if(closestPolygons1.isEmpty() || closestPolygons2.isEmpty() ){
            return StreetsWidth.getDirectedWidth(middle, line, abscisse, polygons);
        }
        //sens 1
        double d1 = -1.;
        IFeature close1 = null;
        if(closestPolygons1.size() == 1){
            //intersection
            close1 = closestPolygons1.iterator().next();
            d1 = close1.getGeom().distance(new GM_Point(middle));
        }
        else{
            Iterator<IFeature> itF = closestPolygons1.iterator();
            close1 = itF.next();
            d1 =  close1.getGeom().distance(new GM_Point(middle));
            while(itF.hasNext()){
                IFeature pol =itF.next();
                // intersetion  = pol.getGeom().intersection(line1);
                double d= -1;
                d =  pol.getGeom().distance(new GM_Point(middle));
                if(d < d1){
                    d1 = d;
                    close1 = pol;
                }
            }

        }
        //sens 2
        double d2 = -1.;
        IFeature close2 = null;
        if(closestPolygons2.size() == 1){
            //intersection
            close2 = closestPolygons2.iterator().next();
            d2 = close2.getGeom().distance(new GM_Point(middle));
        }
        else{
            Iterator<IFeature> itF = closestPolygons2.iterator();
            close2 = itF.next();
            d2 = close2.getGeom().distance(new GM_Point(middle));

            while(itF.hasNext()){
                IFeature pol =itF.next();
                double d= -1;
                d = pol.getGeom().distance(new GM_Point(middle));
                if(d < d2){
                    d2 = d;
                    close2 = pol;
                }
            }
        }    
        if(close1.equals(close2)){
            IGeometry intersetion1  = close1.getGeom().intersection(line1);
            IGeometry intersetion2  = close1.getGeom().intersection(line2);
            d1 = intersetion1.distance(new GM_Point(middle));
            d2 = intersetion2.distance(new GM_Point(middle));
        }



        double dNormal = d1+d2;
        double dDirected = StreetsWidth.getDirectedWidth(middle, line, abscisse, polygons);
        if(dDirected >0){
            return Math.min(dNormal, dDirected);
        }
        else{
            return dNormal;
        }

    }

    private static double getDirectedWidth(IDirectPosition middle, ILineString line, double abscisse, IPopulation<IFeature> polygons){      
        //on le milieu et un point juste avant


        IDirectPosition before = Operateurs.pointEnAbscisseCurviligne(line, line.length() * ((double)(abscisse-1.)/100.) );
        //vecteur normaux
        double x1 = middle.getX();
        double y1 = middle.getY();
        double x2 = before.getX();
        double y2 = before.getY();

        //vecteurs directeurs
        Vecteur n1 = new Vecteur(x2-x1, y2-y1);
        n1 = n1.getNormalised();
        Vecteur n2 = new Vecteur(x1-x2, y1-y2);
        n2 = n2.getNormalised();
        IDirectPosition pnew1 = new DirectPosition(middle.getX() + n1.multConstante(70).getX(), middle.getY() + n1.multConstante(70).getY());
        IDirectPosition pnew2 = new DirectPosition(middle.getX() + n2.multConstante(70).getX(), middle.getY() + n2.multConstante(70).getY());

        //intersections avec les ilots
        IDirectPositionList l1 = new DirectPositionList();
        l1.add(middle);
        l1.add(pnew1);
        IDirectPositionList l2 = new DirectPositionList();
        l2.add(middle);
        l2.add(pnew2);
        ILineString line1 = new GM_LineString(l1);
        ILineString line2 = new GM_LineString(l2);
        Collection<IFeature> closestPolygons1 = polygons.select(line1,0);
        Collection<IFeature> closestPolygons2 = polygons.select(line2,0);
        for(IFeature f: new ArrayList<IFeature>(closestPolygons1)){
            if(!f.getGeom().intersects(line1) /*|| f.getGeom().intersection(line1)  == null*/){
                closestPolygons1.remove(f);
            }
        }
        for(IFeature f: new ArrayList<IFeature>(closestPolygons2)){
            if(!f.getGeom().intersects(line2) /*|| f.getGeom().intersection(line2)  == null*/){
                closestPolygons2.remove(f);
            }
        }
        if(closestPolygons1.isEmpty() || closestPolygons2.isEmpty() ){
            return -1;
        }
        //sens 1
        double d1 = -1.;
        IFeature close1 = null;
        if(closestPolygons1.size() == 1){
            //intersection
            close1 = closestPolygons1.iterator().next();
            d1 = close1.getGeom().distance(new GM_Point(middle));
        }
        else{
            Iterator<IFeature> itF = closestPolygons1.iterator();
            close1 = itF.next();
            d1 =  close1.getGeom().distance(new GM_Point(middle));
            while(itF.hasNext()){
                IFeature pol =itF.next();
                // intersetion  = pol.getGeom().intersection(line1);
                double d= -1;
                d =  pol.getGeom().distance(new GM_Point(middle));
                if(d < d1){
                    d1 = d;
                    close1 = pol;
                }
            }

        }
        //sens 2
        double d2 = -1.;
        IFeature close2 = null;
        if(closestPolygons2.size() == 1){
            //intersection
            close2 = closestPolygons2.iterator().next();
            d2 = close2.getGeom().distance(new GM_Point(middle));
        }
        else{
            Iterator<IFeature> itF = closestPolygons2.iterator();
            close2 = itF.next();
            d2 = close2.getGeom().distance(new GM_Point(middle));

            while(itF.hasNext()){
                IFeature pol =itF.next();
                double d= -1;
                d = pol.getGeom().distance(new GM_Point(middle));
                if(d < d2){
                    d2 = d;
                    close2 = pol;
                }
            }
        }    
        if(close1.equals(close2)){
            IGeometry intersetion1  = close1.getGeom().intersection(line1);
            IGeometry intersetion2  = close1.getGeom().intersection(line2);
            d1 = intersetion1.distance(new GM_Point(middle));
            d2 = intersetion2.distance(new GM_Point(middle));
        }


        return (d1+d2);

    }


    public static void main(String args[]){
        IPopulation<IFeature> polygons = ShapefileReader.read(
                "/media/bcostes/Data/Benoit/these/donnees/vecteur/parcelaire/verniquet/verniquet_ilots.shp");
        JungSnapshot snap =  SnapshotIOManager.shp2Snapshot(
                "/media/bcostes/Data/Benoit/these/donnees/vecteur/filaires/filaires_corriges/verniquet_l93_utf8_corr.shp",
                new LengthEdgeWeighting(),null, false);

        Map<GraphEntity, Double> widths = StreetsWidth.getStreetsWidth(snap, polygons);
        IPopulation<IFeature> out = new Population<IFeature>();
        for(GraphEntity e: widths.keySet()){
            IFeature f = new DefaultFeature(e.getGeometry().toGeoxGeometry());
            AttributeManager.addAttribute(f, "warnings", widths.get(e), "Double");
            out.add(f);
        }   
        ShapefileWriter.write(out, "/home/bcostes/Bureau/width.shp");
    

        polygons.clear();
        polygons = ShapefileReader.read(
                "/media/bcostes/Data/Benoit/these/donnees/vecteur/parcelaire/BDPARCELAIRE/PARCELLE_L93.SHP");
        Map<GraphEntity, Double> widths2 = StreetsWidth.getStreetsWidth(snap, polygons);
        out = new Population<IFeature>();
        for(GraphEntity e: widths2.keySet()){
            IFeature f = new DefaultFeature(e.getGeometry().toGeoxGeometry());
            AttributeManager.addAttribute(f, "warnings", widths2.get(e), "Double");
            out.add(f);
        }   
        ShapefileWriter.write(out, "/home/bcostes/Bureau/width2.shp");

        Map<GraphEntity, Double> warnings = new HashMap<GraphEntity, Double>();
        for(GraphEntity e : snap.getEdges()){
            if( widths.get(e)>0 && widths2.get(e)>0){
                double d1 = widths.get(e);
                double d2 = widths2.get(e);
                if(d1 <= 6){
                    if(d2 > 2 * d1 ){
                        warnings.put(e, d2-d1);
                    }  
                }
                else if(d1> 6 && d1 <= 10){
                    if(d2 > 1.75 * d1 ){
                        warnings.put(e, d2-d1);
                    }    
                }
                else{
                    if(d2 > 1.5 * d1 ){
                        warnings.put(e, d2-d1);
                    }  
                }

            }
        }



        out = new Population<IFeature>();
        for(GraphEntity e: warnings.keySet()){
            IFeature f = new DefaultFeature(e.getGeometry().toGeoxGeometry());
            AttributeManager.addAttribute(f, "warnings", warnings.get(e), "Double");
            out.add(f);
        }   
        ShapefileWriter.write(out, "/home/bcostes/Bureau/warnings2.shp");
    }


}
