package fr.ign.cogit.v2.manual.corrections.tag.strokes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.apache.log4j.Logger;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ILineString;
import fr.ign.cogit.geoxygene.contrib.geometrie.Operateurs;
import fr.ign.cogit.geoxygene.feature.DefaultFeature;
import fr.ign.cogit.geoxygene.feature.Population;
import fr.ign.cogit.geoxygene.util.algo.geomstructure.Vector2D;
import fr.ign.cogit.geoxygene.util.attribute.AttributeManager;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileWriter;
import fr.ign.cogit.v2.io.TAGIoManager;
import fr.ign.cogit.v2.tag.STEntity;
import fr.ign.cogit.v2.tag.STGraph;

public class StrokeBuilder {
  private static final Logger logger = Logger.getLogger(StrokeBuilder.class);
  private STGraph stg;
  private Stack<STEntity> all_edges;
  private final double thresold = Math.PI/4;

  public StrokeBuilder(STGraph stg){
    this.stg = stg;
  }


  public Set<ILineString> buildHStructure(){
    Set<ILineString> strokes = new HashSet<ILineString>();
    this.all_edges = new Stack<STEntity>();
    this.all_edges.addAll((this.stg.getEdges()));
    while(!this.all_edges.isEmpty()){
      STEntity eIni = this.all_edges.pop();
      //on va lancer le processus récursif
      List<STEntity> cluster = new ArrayList<STEntity>();
      cluster.add(eIni);
      STEntity nFirst = stg.getEndpoints(eIni).getFirst();
      STEntity nSecond= stg.getEndpoints(eIni).getSecond();
      rec(eIni, nFirst,  true, cluster);
      rec(eIni, nSecond, false,cluster);
      strokes.add(this.concat(cluster));
    }
    return strokes;
  }
  
  public Map<ILineString, Set<STEntity>> buildHStructureMap(){
    Map<ILineString, Set<STEntity>>strokes =new  HashMap<ILineString, Set<STEntity>>();
    this.all_edges = new Stack<STEntity>();
    this.all_edges.addAll((this.stg.getEdges()));
    while(!this.all_edges.isEmpty()){
      STEntity eIni = this.all_edges.pop();
      //on va lancer le processus récursif
      List<STEntity> cluster = new ArrayList<STEntity>();
      cluster.add(eIni);
      STEntity nFirst = stg.getEndpoints(eIni).getFirst();
      STEntity nSecond= stg.getEndpoints(eIni).getSecond();
      rec(eIni, nFirst,  true, cluster);
      rec(eIni, nSecond, false,cluster);
      ILineString line = this.concat(cluster);
      strokes.put(line, new HashSet<STEntity>(cluster));
    }
    return strokes;
  }

  private ILineString concat(List<STEntity> cluster) {
    List<ILineString> l1 = new ArrayList<ILineString>();
    for(STEntity e : cluster){
      l1.add((ILineString)e.getGeometry().toGeoxGeometry());
    }
    ILineString line1 = Operateurs.compileArcs(l1);
    if(line1 == null){
      IPopulation<IFeature> pb =new Population<IFeature>();
      
      System.out.println("----------");
      for(STEntity e: cluster){
        IFeature f = new DefaultFeature(e.getGeometry().toGeoxGeometry());
        AttributeManager.addAttribute(f, "num", cluster.indexOf(e), "Integer");
        pb.add(f);
      }
      ShapefileWriter.write(pb, "/home/bcostes/Bureau/pb.shp");
      logger.error("Null geometry created. Exiting.");
      System.exit(-1);
    }
    return line1;
  }

  public void rec(STEntity eIni, STEntity nFirst, boolean addFirst, List<STEntity> cluster){

    //on cherche un stentity qui est connecté à nFirst et existe à t1 et t2
    Set<STEntity> candidates = new HashSet<STEntity>(stg.getIncidentEdges(nFirst));
    candidates.removeAll(cluster);
    for(STEntity candidate : new HashSet<STEntity>(candidates)){
      if(!this.all_edges.contains(candidate)){
        candidates.remove(candidate);
      }
    }

    if(candidates.isEmpty()){
      //fini
      return;
    }
    //on a au moins un candidat

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
    //on vérifie que c'ets bien l'angle minimal en ce point
    Set<STEntity> incidents = new HashSet<STEntity>(stg.getIncidentEdges(nFirst));
    incidents.removeAll(cluster);
    incidents.remove(candidatMin);
    if(!incidents.isEmpty()){
      // d'autres arcs
      for(STEntity ee: incidents){
        double angle2 = this.getAngle(candidatMin, ee);
        if(angle2 < angleMin){
          //un autre STEntity réalise l'angle min
          return;
        }
      }
    }

    this.all_edges.remove(candidatMin);

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
    rec(candidatMin, nFirst, addFirst, cluster);


    return;
  }

  public double getAngle(STEntity e1, STEntity e2){
    ILineString p = (ILineString)e1.getGeometry().toGeoxGeometry();
    ILineString q = (ILineString)e2.getGeometry().toGeoxGeometry();
    IDirectPosition v1_a;
    IDirectPosition v1_b;
    IDirectPosition v2_a;
    IDirectPosition v2_b;
    if(p.startPoint().equals(q.startPoint()) && p.endPoint().equals(q.endPoint()) ||
        p.startPoint().equals(q.endPoint()) && p.endPoint().equals(q.startPoint())){
      if(p.buffer(0.05).contains(q) && q.buffer(0.05).contains(p))
      return Math.PI;
    }
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
    STGraph stg = TAGIoManager.deserialize("/home/bcostes/Bureau/TAG/etape4/tag_new.tag");

    IPopulation<IFeature> out = new Population<IFeature>();
    StrokeBuilder structBuilder = new StrokeBuilder(stg);
    Set<ILineString> lines = structBuilder.buildHStructure();
    for(ILineString e : lines){
      IFeature f1 = new DefaultFeature(e);
      out.add(f1);
    }

    System.out.println(out.size());
    ShapefileWriter.write(out, "/home/bcostes/Bureau/test.shp");
  }
}
