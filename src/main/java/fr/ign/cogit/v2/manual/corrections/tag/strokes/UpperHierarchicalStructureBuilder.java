package fr.ign.cogit.v2.manual.corrections.tag.strokes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import nrc.fuzzy.XValuesOutOfOrderException;
import nrc.fuzzy.YValueOutOfRangeException;
import edu.uci.ics.jung.graph.util.Pair;
import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ILineString;
import fr.ign.cogit.geoxygene.contrib.geometrie.Angle;
import fr.ign.cogit.geoxygene.contrib.geometrie.Operateurs;
import fr.ign.cogit.geoxygene.feature.DefaultFeature;
import fr.ign.cogit.geoxygene.feature.Population;
import fr.ign.cogit.geoxygene.util.algo.geomstructure.Vector2D;
import fr.ign.cogit.geoxygene.util.attribute.AttributeManager;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileWriter;
import fr.ign.cogit.v2.io.TAGIoManager;
import fr.ign.cogit.v2.tag.FuzzyTemporalInterval;
import fr.ign.cogit.v2.tag.STEntity;
import fr.ign.cogit.v2.tag.STGraph;

/**
 * Construit des espèces de "strokes" à partir d'un STentity, de deux dates et éventuellement d'un
 * attribut de continuité
 * On aggrége les stentity successif si ils existent aux deux dates, avec l'angle min ou l'attribut de
 * continuité
 * @author bcostes
 *
 */
public class UpperHierarchicalStructureBuilder {

    private STGraph stg;
    private String attribute;
    private String att1, att2;
    private List<STEntity> cluster;
    private final double thresold = Math.PI/10;
    public UpperHierarchicalStructureBuilder(STGraph stg,
            String attribute){
        this.stg = stg;
        this.attribute = attribute;   
    }


    public Pair<ILineString> buildHStructure(STEntity eIni, FuzzyTemporalInterval t1, FuzzyTemporalInterval t2){
        if(!eIni.existsAt(t1) || !eIni.existsAt(t2)){
            return null;
        }

        //on va lancer le processus récursif
        this.cluster = new ArrayList<STEntity>();
        cluster.add(eIni);
        STEntity nFirst = stg.getEndpoints(eIni).getFirst();
        STEntity nSecond= stg.getEndpoints(eIni).getSecond();

        if(attribute != null){
            System.out.println(eIni.getTAttributes());
            this.att1 = eIni.getTAttributeByName(attribute).getValueAt(t1);
            this.att2 = eIni.getTAttributeByName(attribute).getValueAt(t2);
        }
        rec(eIni, nFirst, t1, t2, true);
        rec(eIni, nSecond, t1, t2, false);
        System.out.println(t1+" " +t2);
        return this.concat(cluster, t1, t2);
    }

    private Pair<ILineString> concat(List<STEntity> cluster, FuzzyTemporalInterval t1, FuzzyTemporalInterval t2) {
        List<ILineString> l1 = new ArrayList<ILineString>();
        List<ILineString> l2 = new ArrayList<ILineString>();
        for(STEntity e : cluster){
            l1.add((ILineString)e.getGeometryAt(t1).toGeoxGeometry());
            l2.add((ILineString)e.getGeometryAt(t2).toGeoxGeometry());
        }
        ILineString line1 = Operateurs.compileArcs(l1);
        ILineString line2 = Operateurs.compileArcs(l2);
        if(line1 == null || line2 == null){
            return null;
        }
        return new Pair<ILineString>(line1, line2);
    }

    public void rec(STEntity eIni, STEntity nFirst, FuzzyTemporalInterval t1, FuzzyTemporalInterval t2, boolean addFirst){

        //on cherche un stentity qui est connecté à nFirst et existe à t1 et t2
        Set<STEntity> candidates = new HashSet<STEntity>(stg.getIncidentEdges(nFirst));
        candidates.removeAll(cluster);
        for(STEntity e: new HashSet<STEntity>(candidates)){
            if(!e.existsAt(t1) || !e.existsAt(t2)){
                candidates.remove(e);
            }
        }
        if(candidates.isEmpty()){
            //fini
            return;
        }
        //on a au moins un candidat
        STEntity candidateForName1 = null, candidateForName2 = null;
        for(STEntity candidate: candidates){
            //un seul, on le prend si même attribut
            if(att1 != null){
                String newAtt1 = candidate.getTAttributeByName(attribute).getValueAt(t1);
                if(newAtt1 != null && newAtt1.equals(att1) && candidateForName1 == null){
                    candidateForName1 = candidate;
                }
            }
            if(att2 != null){
                String newAtt2 = candidate.getTAttributeByName(attribute).getValueAt(t2);
                if(newAtt2 != null && newAtt2.equals(att2) && candidateForName2 == null){
                    candidateForName2 = candidate;
                }
            }
        }
        //meme candidats ?
        if(candidateForName1 != null && candidateForName2 != null){
            if(candidateForName1.equals(candidateForName2)){
                //super! 
                //on a vérifié les conditions de continuité
                if(addFirst){
                    cluster.add(0, candidateForName1);
                }
                else{
                    cluster.add(cluster.size(), candidateForName1);
                }
                if(stg.getEndpoints(candidateForName1).getFirst().equals(nFirst)){
                    nFirst = stg.getEndpoints(candidateForName1).getSecond();
                }
                else{
                    nFirst = stg.getEndpoints(candidateForName1).getFirst();
                }
                rec(candidateForName1, nFirst, t1, t2,  addFirst);
            }
            else{
                return;
            }
        }
        else if(candidateForName1 != null){
            //on prend si angle < PI/3
            double angle = this.getAngle(eIni, candidateForName1);
            if(angle>thresold){
                return;
            }
            //on a vérifié les conditions de continuité
            if(addFirst){
                cluster.add(0, candidateForName1);
            }
            else{
                cluster.add(cluster.size(), candidateForName1);
            }
            if(stg.getEndpoints(candidateForName1).getFirst().equals(nFirst)){
                nFirst = stg.getEndpoints(candidateForName1).getSecond();
            }
            else{
                nFirst = stg.getEndpoints(candidateForName1).getFirst();
            }
            rec(candidateForName1, nFirst, t1, t2,  addFirst);
        }
        else if(candidateForName2 != null){
            double angle = this.getAngle(eIni, candidateForName2);
            if(angle>thresold){
                return;
            }
            //on a vérifié les conditions de continuité
            if(addFirst){
                cluster.add(0, candidateForName2);
            }
            else{
                cluster.add(cluster.size(), candidateForName2);
            }
            if(stg.getEndpoints(candidateForName2).getFirst().equals(nFirst)){
                nFirst = stg.getEndpoints(candidateForName2).getSecond();
            }
            else{
                nFirst = stg.getEndpoints(candidateForName2).getFirst();
            }
            rec(candidateForName2, nFirst, t1, t2,  addFirst);
        }
        else{
            // un des deux ou les deux n'ont pas de candidats
            //on prend l'angle min si angle < Math.PI/3
            STEntity candidatMin = null;
            double angleMin = Double.MAX_VALUE;
            for(STEntity candidate: candidates){
                double angle = this.getAngle(eIni, candidate);
                if(angle < angleMin){
                    angleMin = angle;
                    candidatMin = candidate;
                }

            }
            if(angleMin>thresold){
                return;
            }
            //on a vérifié les conditions de continuité
            if(addFirst){
                cluster.add(0, candidatMin);
            }
            else{
                cluster.add(cluster.size(), candidatMin);
            }
            if(stg.getEndpoints(candidatMin).getFirst().equals(nFirst)){
                nFirst = stg.getEndpoints(candidatMin).getSecond();
            }
            else{
                nFirst = stg.getEndpoints(candidatMin).getFirst();
            }
            rec(candidatMin, nFirst, t1, t2,  addFirst);
        }

        return;
    }

    public double getAngle(STEntity e1, STEntity e2){
        ILineString p = (ILineString)e1.getGeometry().toGeoxGeometry();
        ILineString q = (ILineString)e2.getGeometry().toGeoxGeometry();
        IDirectPosition v1_a;
        IDirectPosition v1_b;
        IDirectPosition v2_a;
        IDirectPosition v2_b;
        if(p.endPoint().equals(q.endPoint())){
            v1_a = p.coord().get(p.coord().size()-2);
            v1_b = p.endPoint();
            v2_a = q.endPoint();
            v2_b = q.coord().get(q.coord().size()-2);
        }else if(p.startPoint().equals(q.startPoint())){
            v1_a = p.coord().get(1);
            v1_b = p.startPoint();
            v2_a = q.startPoint();
            v2_b = q.coord().get(1);
        }else if(p.endPoint().equals(q.startPoint())){
            v1_a = p.coord().get(p.coord().size()-2);
            v1_b = p.endPoint();
            v2_a = q.startPoint();
            v2_b = q.coord().get(1);
        }else{
            v1_a = p.coord().get(1);
            v1_b = p.startPoint();
            v2_a = q.endPoint();
            v2_b = q.coord().get(q.coord().size()-2);
        }
        // TODO Il faudrait utiliser un lissage, mais celui ci a tendance à creer
        // des effets de bord en simplifiant trop les linestrings "coudées" et
        // influe trop sur l'angle final.
        Vector2D v1 = new Vector2D(v1_a, v1_b);
        Vector2D v2 = new Vector2D(v2_a, v2_b);
        return v1.angleVecteur(v2).getValeur();

    }

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        STGraph stg = TAGIoManager.deserialize("/home/bcostes/Bureau/test/etape1/tag_new.tag");
        try {
            FuzzyTemporalInterval t1 =new FuzzyTemporalInterval(new double[]{1825,1827,1836,1839},new double[]{0,1,1,0}, 4) ;
            FuzzyTemporalInterval t2 =   new FuzzyTemporalInterval(new double[]{1870,1871,1871,1872},new double[]{0,1,1,0}, 4);
            IPopulation<IFeature> out = new Population<IFeature>();
            UpperHierarchicalStructureBuilder structBuilder = new UpperHierarchicalStructureBuilder(stg, "name");
            for(STEntity e : stg.getEdges()){
                if(e.getId() == 8627){
                    Pair<ILineString> lines = structBuilder.buildHStructure(e, t1, t2);
                    IFeature f1 = new DefaultFeature(lines.getFirst());
                    IFeature f2 = new DefaultFeature(lines.getSecond());
                    double s = f1.getGeom().length() / (lines.getFirst().startPoint().distance(lines.getFirst().endPoint()));
                    AttributeManager.addAttribute(f1, "s", s, "Double");
                    s = f2.getGeom().length() / (lines.getSecond().startPoint().distance(lines.getSecond().endPoint()));
                    AttributeManager.addAttribute(f2, "s", s, "Double");
                    
                    Angle a1 = Operateurs.directionPrincipale(lines.getFirst().coord());
                    
                    Angle a2 = Operateurs.directionPrincipale(lines.getSecond().coord());

                    System.out.println(Angle.ecart(a1, a2).getValeur()* 180./Math.PI);
                    
                    out.add(f1);
                    out.add(f2);
                    break;
                }
            }

            ShapefileWriter.write(out, "/home/bcostes/Bureau/test.shp");

        } catch (XValuesOutOfOrderException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (YValueOutOfRangeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
