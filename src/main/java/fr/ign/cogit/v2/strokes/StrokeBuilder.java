package fr.ign.cogit.v2.strokes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Stack;

import edu.uci.ics.jung.graph.util.Pair;
import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ILineString;
import fr.ign.cogit.geoxygene.contrib.geometrie.Operateurs;
import fr.ign.cogit.geoxygene.feature.DefaultFeature;
import fr.ign.cogit.geoxygene.feature.Population;
import fr.ign.cogit.geoxygene.util.algo.geomstructure.Vector2D;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileWriter;
import fr.ign.cogit.v2.geometry.LightLineString;
import fr.ign.cogit.v2.geometry.LightMultipleGeometry;
import fr.ign.cogit.v2.io.TAGIoManager;
import fr.ign.cogit.v2.snapshot.GraphEntity;
import fr.ign.cogit.v2.snapshot.JungSnapshot;
import fr.ign.cogit.v2.tag.FuzzyTemporalInterval;
import fr.ign.cogit.v2.tag.STEntity;
import fr.ign.cogit.v2.tag.STGraph;

public class StrokeBuilder {

  /**
   *  la date du snapshot pour lequel on veut reconstituer les strokes
   */
  private FuzzyTemporalInterval t;
  /**
   * Le stgraph
   */
  private STGraph stgraph;
  private double deflection;
  private String attribute;
  final static double prec = 2;
  final static double maxGap = 7.5; //7.5 m d'écart pour les LightMLultipleGeometry correspondant
  // à une intersection détaillée

  public StrokeBuilder(STGraph stg, FuzzyTemporalInterval t, double deflection, String attribute){
    this.stgraph = stg;
    this.t = t;
    this.deflection = deflection;
    this.network = this.stgraph.getSnapshotAt(t);
    this.attribute = attribute;
  }

  /**
   * les arcs à t
   */
  private JungSnapshot network;



  /**
   * Convenience method that build strokes from a set of linear objects.
   * @param objects
   * @param mapping : if not null, build the mapping between the sets of arcs
   *          and the strokes
   * @return
   */
  public List<Stroke> buildStroke() {
    List<List<GraphEntity>> roads_arcs = buildLinesClusters();
    List<Stroke> roads = new ArrayList<Stroke>();
    for (List<GraphEntity> road_arcs : roads_arcs) {
      ILineString strokeG = mergeStroke(road_arcs);
      if (strokeG == null) {
        throw new NullPointerException("A STROKE WAS BUILT NULL");
      }
      Set<STEntity> stroke_objects = new HashSet<STEntity>();
      for(GraphEntity gg: road_arcs){
        List<Integer> ids = this.stgraph.getMappingSnapshotEdgesId().get(gg.getId());
        for(STEntity ee: this.stgraph.getEdgesAt(t)){
          if(ids.contains(ee.getId())){
            stroke_objects.add(ee);
          }
        }
      }
      Stroke stroke = new Stroke();
      stroke.setGeom(strokeG);
      stroke.setEntities(stroke_objects);
      roads.add(stroke);
    }

    return roads;
  }



  /**
   * Sort and merge linestrings
   */
  private ILineString mergeStroke(List<GraphEntity> arcs) {
    List<ILineString> road = new ArrayList<ILineString>();
    for (GraphEntity a : arcs) {
      road.add((ILineString)a.getGeometry().toGeoxGeometry());
    }
    ILineString line = Operateurs.compileArcs(road);
    if(line == null){
      System.out.println(road.toString());
    }
    return line;
  }

  private void sort(List<ILineString> roads) {

    Collections.sort(roads, new Comparator<ILineString>() {
      @Override
      public int compare(ILineString o1, ILineString o2) {
        if (o1.startPoint().equals(o2.endPoint())) {
          return 1;
        } else if (o1.endPoint().equals(o2.startPoint())) {
          return -1;
        }
        return 0;
      }
    });
  }

  /**
   * Build the strokes accorded to the desired principle : <br/>
   * 1 : Every Best Fit 2 : Self Best Fit 3 : Self Fit
   * @param t the planar graph of the Observation
   * @param threshold : the angle from which we "break" a stroke.
   * @param principle
   * @return
   */
  private List<List<GraphEntity>> buildLinesClusters() {
    List<List<GraphEntity>> result = new ArrayList<List<GraphEntity>>();
    List<GraphEntity> arcs = new ArrayList<GraphEntity>(this.network.getEdges());
    Stack<GraphEntity> processed = new Stack<GraphEntity>();
    GraphEntity random = arcs.get(new Random().nextInt(arcs.size()));
    while (random != null) {
      if (!processed.contains(random)) {
        processed.add(random);
        String attributeValue = "";
        if(this.attribute != null && !this.attribute.equals("")){
          attributeValue = random.getAttributes().get(this.attribute);
        }
        List<GraphEntity> clusterFrom = new ArrayList<GraphEntity>();
        search_ebf(random, -1, processed,
            clusterFrom, attributeValue);

        List<GraphEntity> clusterTo = new ArrayList<GraphEntity>();
        search_ebf(random, 1, processed, clusterTo, attributeValue);

        List<GraphEntity> road = new ArrayList<GraphEntity>();
        Collections.reverse(clusterFrom);
        road.addAll(clusterFrom);
        road.add(random);
        road.addAll(clusterTo);
        result.add(road);
      }
      List<GraphEntity> untagged = new ArrayList<GraphEntity>();
      untagged.addAll(arcs);
      untagged.removeAll(processed);
      if (!untagged.isEmpty()) {
        random = untagged.get(new Random().nextInt(untagged.size()));
      } else {
        random = null;
      }
    }
    return result;
  }

  /**
   * Build a stroke based on every best fit -1 = FROM 1 = TO
   * @param old_s
   * @param _dir
   * @param clusterFromprocessed
   * @param threshold
   * @param cluster
   * @param old_point
   */
  private void search_ebf(GraphEntity old_s, int direction,
      Stack<GraphEntity> processed, List<GraphEntity> cluster, String attributeValue) {
    GraphEntity search_point = null;
    if (direction == 1) {
      search_point = this.network.getEndpoints(old_s).getSecond();
    } else {
      search_point = this.network.getEndpoints(old_s).getFirst();
    }
    List<GraphEntity> searched = new ArrayList<GraphEntity>();
    List<GraphEntity> selected = new ArrayList<GraphEntity>();
    searched.addAll(this.network.getIncidentEdges(search_point));
    searched.remove(old_s);
    searched.removeAll(processed);
    if (searched.isEmpty()) {
      return;
    }
    double[] old_remain_angles = new double[searched.size()];
    int i = 0;
    for (GraphEntity remain : searched) {
      old_remain_angles[i] = deflectionAngle((ILineString)old_s.getGeometry().toGeoxGeometry(), 
          (ILineString)remain.getGeometry().toGeoxGeometry());
      i++;
    }
    i = 0;
    if (searched.size() > 1) {
      if(this.attribute != null && !this.attribute.equals("") && attributeValue != null){
        for (GraphEntity pair_a : searched) {
          if(pair_a.getAttributes().get(this.attribute)!= null && pair_a.getAttributes().get(this.attribute).equals(attributeValue)){
            selected.add(pair_a);
          }
        }
      }
      else{
        for (GraphEntity pair_a : searched) {
          boolean isMin = true;
          for (GraphEntity pair_b : searched) {
            if (!pair_a.equals(pair_b)) {
              double angle = deflectionAngle((ILineString)pair_a.getGeometry().toGeoxGeometry(),
                  (ILineString) pair_b.getGeometry().toGeoxGeometry());
              if (old_remain_angles[searched.indexOf(pair_a)] > angle) {
                isMin = false;
                break;
              }
            }
          }
          if (isMin) {
            selected.add(pair_a);
          }
        }
      }
    } else if (searched.size() == 1) {
      selected.add(searched.get(0));
    } else {
      return;
    }
    double min = Double.MAX_VALUE;
    GraphEntity choice = null;
    for (GraphEntity a : selected) {
      int id = searched.indexOf(a);
      if (old_remain_angles[id] < min) {
        min = old_remain_angles[id];
        choice = a;
      }
    }
    if (min < this.deflection && choice != null) {
      // on regarde si c'est un multiple geometry
      if(search_point.getGeometry() instanceof LightMultipleGeometry){
        LightMultipleGeometry multipleG = (LightMultipleGeometry)search_point.getGeometry();
        if(multipleG.getLightLineString().size()>1 || multipleG.getLightLineString().isEmpty()){
          // géométrie complexe, probablement une place
          return;
        }
        // sinon on est sur une intersection détaillée
        // on regarde la longueur de la lineString
        ILineString lM = multipleG.getLightLineString().get(0).toGeoxGeometry();
        if(lM.length() >= maxGap){
          // plus de  X m d'écart
          //sinon on test l'angle
          double angleM = this.deflectionAngle((ILineString)choice.getGeometry().toGeoxGeometry(), lM);
          if(angleM > this.deflection){
            return;
          }
        }

      }

      cluster.add(choice);
      processed.add(choice);
      if (this.network.getEndpoints(old_s).getSecond()
          .equals(this.network.getEndpoints(choice).getSecond())
          || this.network.getEndpoints(old_s).getFirst()
          .equals(this.network.getEndpoints(choice).getFirst())) {
        GraphEntity n = this.network.getEndpoints(choice).getSecond();                
        Pair<GraphEntity> ends = this.network.getEndpoints(choice);
        this.network.removeEdge(choice);
        choice.setGeometry(new LightLineString(
            ((ILineString)choice.getGeometry().toGeoxGeometry()).reverse().coord()));
        this.network.addEdge(choice, n,ends.getFirst());
      }
      search_ebf(choice, direction, processed, cluster, attributeValue);
    }
  }



  /**
   * Computes the deflection angle between a and b
   * @param a
   * @param b
   * @return
   */
  private double deflectionAngle(ILineString p, ILineString q) {
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


  public static void main(String args[]){
    STGraph stgraph = TAGIoManager.
        deserialize("/media/bcostes/Data/Benoit/these/analyses/TAG/TAG_v2/final/tag_corrected_ind.tag");



    StrokeBuilder strokeBuilder = new StrokeBuilder
        (stgraph, stgraph.getTemporalDomain().asList().get(stgraph.getTemporalDomain().asList().get(0).size()-1), Math.PI /4., "");
    List<Stroke> strokes =strokeBuilder.buildStroke();
    IPopulation<IFeature> out = new Population<IFeature>();
    for(Stroke s: strokes){
      out.add(new DefaultFeature(s.getGeom()));
    }
    ShapefileWriter.write(out, "/home/bcostes/Bureau/strokes.shp");

  }

}
