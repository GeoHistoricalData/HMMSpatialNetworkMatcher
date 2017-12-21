package fr.ign.cogit.v2.tag;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.graph.UndirectedSparseMultigraph;
import edu.uci.ics.jung.graph.util.Pair;
import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ILineString;
import fr.ign.cogit.geoxygene.api.spatial.geomaggr.IMultiPoint;
import fr.ign.cogit.geoxygene.api.spatial.geomprim.IPoint;
import fr.ign.cogit.geoxygene.api.spatial.geomroot.IGeometry;
import fr.ign.cogit.geoxygene.contrib.delaunay.TriangulationJTS;
import fr.ign.cogit.geoxygene.contrib.geometrie.Operateurs;
import fr.ign.cogit.geoxygene.feature.DefaultFeature;
import fr.ign.cogit.v2.geometry.LightDirectPosition;
import fr.ign.cogit.v2.geometry.LightGeometry;
import fr.ign.cogit.v2.geometry.LightLineString;
import fr.ign.cogit.v2.indicators.IGlobalIndicator;
import fr.ign.cogit.v2.indicators.ILocalIndicator;
import fr.ign.cogit.v2.io.SnapshotIOManager;
import fr.ign.cogit.v2.mergeProcess.mergeUtility.SplitUtils;
import fr.ign.cogit.v2.snapshot.GraphEntity;
import fr.ign.cogit.v2.snapshot.JungSnapshot;
import fr.ign.cogit.v2.snapshot.SnapshotGraph;
import fr.ign.cogit.v2.snapshot.SnapshotGraph.NORMALIZERS;
import fr.ign.cogit.v2.weightings.IEdgeWeighting;

/**
 * Classe modélisant un STAG (Spatio-Temporal Aggregated Graph) modèle de graphe
 * agrégé temporellement et spatialement
 * 
 * @author bcostes
 *
 */
public class STGraph extends UndirectedSparseMultigraph<STEntity, STEntity>
implements Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  /**
   * Domaine temporel de validité du graphe
   */
  private TemporalDomain temporalDomain;

  /**
   * liste des relations ST maximales
   */
  //  private Set<STLink> stLinks;

  /**
   * Indicateurs globaux de graphe: valeurs pour chaque date
   */
  private Set<STProperty<Double>> globalIndicators;
  /**
   * Indicateurs locaux calculés pour les sommets: leur noms
   */
  private Set<String> nodesLocalIndicators;
  /**
   * Indicateurs locaux calculés pour les arcs: leur noms
   */
  private Set<String> edgesLocalIndicators;

  /**
   * Noms des attributs des STEntity
   */
  private List<String> attributes;

  /**
   * précisions "relatives" des différents snapshots, entre 0 à 1
   */
  private STProperty<Double> accuracies;

  /**
   * Mapping etre les identifiant des arcs d'un jung snapshot et une liste
   * d'id des arcs de self correspondants (utile lorsqu'il existe des sommets
   * fictifs)
   */
  Map<Integer, List<Integer>> mappingSnapshotEdgesId;

  public STGraph() {
    this.globalIndicators = new HashSet<STProperty<Double>>();
    this.nodesLocalIndicators = new HashSet<String>();
    this.edgesLocalIndicators = new HashSet<String>();
    //this.stLinks = new HashSet<STLink>();
    this.attributes = new ArrayList<String>();
    this.accuracies = new STProperty<Double>(STProperty.PROPERTIE_FINAL_TYPES.Attribute, "Accuracies");
  }

  public STGraph(TemporalDomain temporalDomain) {
    super();
    this.temporalDomain = temporalDomain;
    this.globalIndicators = new HashSet<STProperty<Double>>();
    this.nodesLocalIndicators = new HashSet<String>();
    this.edgesLocalIndicators = new HashSet<String>();
    this.accuracies = new STProperty<Double>(STProperty.PROPERTIE_FINAL_TYPES.Attribute, "Accuracies");
    for (FuzzyTemporalInterval t : this.temporalDomain.asList()) {
      this.accuracies.setValueAt(t, 1.);
    }
    //this.stLinks = new HashSet<STLink>();
    this.attributes = new ArrayList<String>();
  }

  // ***************************************************************************
  // *********************** Manipulation des entitées ST
  // **********************
  // ***************************************************************************
  /**
   * Liste des arcs à la date t
   */
  public List<STEntity> getEdgesAt(FuzzyTemporalInterval t) {
    if (!this.temporalDomain.asList().contains(t)) {
      return null;
    }
    List<STEntity> result = new ArrayList<STEntity>();
    for (STEntity edge : this.getEdges()) {
      if (edge.existsAt(t)) {
        result.add(edge);
      }
    }
    return result;
  }

  /**
   * Liste des sommets à la date t
   */
  public List<STEntity> getNodesAt(FuzzyTemporalInterval t) {
    if (!this.temporalDomain.asList().contains(t)) {
      return null;
    }
    List<STEntity> result = new ArrayList<STEntity>();
    for (STEntity node : this.getVertices()) {
      if (node.existsAt(t)) {
        result.add(node);
      }
    }
    return result;
  }

  /**
   * Liste des sommets et des sommet fictifs à la date t
   */
  public List<STEntity> getNodesAndFictiveNodesAt(FuzzyTemporalInterval t) {
    if (!this.temporalDomain.asList().contains(t)) {
      return null;
    }
    List<STEntity> result = new ArrayList<STEntity>();
    for (STEntity node : this.getVertices()) {
      if (node.getGeometryAt(t) != null) {
        result.add(node);
      }
    }
    return result;
  }

  /**
   * Donne les arcs incidents à node à la date t
   * 
   * @param node
   * @param t
   * @return
   */
  public List<STEntity> getIncidentEdgessAt(STEntity node,
      FuzzyTemporalInterval t) {
    List<STEntity> incident = new ArrayList<STEntity>();;
    for(STEntity e : this.getIncidentEdges(node)){
      if(e.existsAt(t)){
        incident.add(e);
      }
    }
    return incident;
  }

  /**
   * Donne les voisins de node à la date t
   */
  public List<STEntity> getNeighborsAt(STEntity node, FuzzyTemporalInterval t) {
    return null;
  }

  // ***************************************************************************
  // ************************ Création des snapshots
  // ***************************
  // ***************************************************************************

  public JungSnapshot getSnapshotAt(FuzzyTemporalInterval t) {
    if (!this.temporalDomain.asList().contains(t)) {
      return null;
    }
    JungSnapshot sG = new JungSnapshot();
    this.mappingSnapshotEdgesId = new HashMap<Integer, List<Integer>>();

    Stack<STEntity> nodes = new Stack<STEntity>();
    Stack<STEntity> edgesDone = new Stack<STEntity>();
    nodes.addAll(this.getNodesAt(t));

    while (!nodes.isEmpty()) {
      // on va chercher les arcs incidents
      STEntity node = nodes.pop();
      Set<STEntity> incidents = new HashSet<STEntity>(
          this.getIncidentEdges(node));
      // on supprime les arcs déja traités
      incidents.removeAll(edgesDone);
      for (STEntity incident : new HashSet<STEntity>(incidents)) {
        if(!incidents.contains(incident)){
          //arrive dans le cas des boucles
          continue;
        }
        // l'arc n'existe pas a t ? probleme ....
        if (!incident.existsAt(t)) {
          continue;
        }
        // l'autre extrémité de l'arc existe-elle à t ?
        STEntity otherNode = null;
        if (this.getEndpoints(incident).getFirst().equals(node)) {
          otherNode = this.getEndpoints(incident).getSecond();
        } else {
          otherNode = this.getEndpoints(incident).getFirst();
        }
        if (otherNode.existsAt(t)) {
          // c'est bon ! on convertit simplement l'arc en graphentity
          GraphEntity gr = incident.toGraphEntity(t);

          List<Integer> lid = new ArrayList<Integer>();
          lid.add(incident.getId());
          this.mappingSnapshotEdgesId.put(gr.getId(), lid);
          sG.addEdge(gr, node.toGraphEntity(t),
              otherNode.toGraphEntity(t));
          edgesDone.add(incident);
        } else {
          // on va devoir reconstituer l'arc car on a un sommet fictif
          Set<STEntity> edges = new HashSet<STEntity>();
          edges.add(incident);
          while (!otherNode.existsAt(t)) {
            Collection<STEntity> newIncidents = new ArrayList<STEntity>(
                this.getIncidentEdges(otherNode));
            newIncidents.remove(incident);
            // normalement il y en a un seul qui existe à t !!!
            for (STEntity e : new ArrayList<STEntity>(newIncidents)) {
              if (!e.existsAt(t)) {
                newIncidents.remove(e);
              }
            }
            if (newIncidents.size() != 1) {
              System.out
              .println("STGraph.getSnapshotAt : incidents edges size must be one");
              System.out.println(t);
              System.out.println();
              System.out.println(newIncidents.size());
              System.out.println(node.getId()+" "+otherNode.getId());
              for(STEntity i: newIncidents){
                System.out.println(i.getId());
              }
              System.out.println(otherNode.getTGeometry());
              System.exit(-1000);
            }
            incident = newIncidents.iterator().next();
            // récupération de l'autre noeud
            if (this.getEndpoints(incident).getFirst()
                .equals(otherNode)) {
              otherNode = this.getEndpoints(incident).getSecond();
            } else {
              otherNode = this.getEndpoints(incident).getFirst();
            }
            edges.add(incident);
            incidents.remove(incident);
          }
          // on fusionne les entitées
          GraphEntity.setCurrentType(GraphEntity.EDGE);
          GraphEntity gr = new GraphEntity();
          gr.setId(edges.iterator().next().getId());
          // mapping entre l'id de l'arc fusion et les id des arcs de
          // self le
          // constituant
          List<Integer> lid = new ArrayList<Integer>();
          for (STEntity e : edges) {
            lid.add(e.getId());
          }
          this.mappingSnapshotEdgesId.put(gr.getId(), lid);
          // concaténation des linestring
          List<ILineString> lLines = new ArrayList<ILineString>();
          for (STEntity e : edges) {
            lLines.add(((LightLineString) e.getGeometryAt(t))
                .toGeoxGeometry());
          }
          gr.setGeometry(new LightLineString(Operateurs.union(lLines,
              0.05).coord()));
          double weight = 0;
          for (STEntity e : edges) {
            weight += e.getWeightAt(t);
          }
          gr.setWeight(weight);

          //attributs
          for(String att: this.getAttributes()){
            Set<String>values = new HashSet<String>();
            for (STEntity e : edges) {
              if(e.getTAttributeByName(att).getValueAt(t) != null){
                values.add(e.getTAttributeByName(att).getValueAt(t) );
              }
            }
            String s = "";
            for(String value : values){
              s+= value +";";
            }
            if(s.length() != 0){
              s = s.substring(0, s.length()-1);
            }   
            gr.getAttributes().put(att, s);
          }
          // Normalement, les indicateurs sont les meme pour tous les
          // arcs à la
          // date t
          STEntity e = edges.iterator().next();
          for (String indicator : this.edgesLocalIndicators) {
            gr.getLocalIndicators().put(indicator,
                e.getIndicatorAt(indicator, t));
          }

          sG.addEdge(gr, node.toGraphEntity(t),
              otherNode.toGraphEntity(t));
          edgesDone.addAll(edges);
        }
      }
      //      for(GraphEntity e : sG.getEdges()){
      //        if(!(e.getWeight() >0)){
      //          System.out.println("AIEEE");
      //          System.out.println(e.getWeight());
      //        }
      //      }
    }

    Transformer<GraphEntity, Double> edgesWeights = new Transformer<GraphEntity, Double>() {
      public Double transform(GraphEntity input) {
        return input.getWeight();
      }
    };
    sG.setEdgesWeights(edgesWeights);
    return sG;
  }

  // **********************************************************************
  // ************************** Autre fonctions ***************************
  // **********************************************************************

  public Map<Integer, List<Integer>> getMappingSnapshotEdgesId() {
    return mappingSnapshotEdgesId;
  }

  public void setMappingSnapshotEdgesId(
      Map<Integer, List<Integer>> mappingSnapshotEdgesId) {
    this.mappingSnapshotEdgesId = mappingSnapshotEdgesId;
  }

  /**
   * Donne l'arbre couvrant de poids minimum à la date t
   */
  public SnapshotGraph minimumSpanningTreeAt(FuzzyTemporalInterval t,
      IEdgeWeighting edges_weights) {
    JungSnapshot snap = this.getSnapshotAt(t);
    SnapshotGraph j = SnapshotIOManager.graph2JungSnapshot(
        snap.arbitraryMinimumSpanningTree(), (edges_weights));
    return j;
  }

  /**
   * Réalise une triangulation de Delaunay sur le réseau. Attention, les
   * sommets et les arcz doivent porter une géométrie (donc ici être de type
   * Graphentity par exemple)
   * 
   * @return
   */
  public SnapshotGraph delaunayTriangulationAt(FuzzyTemporalInterval t,
      IEdgeWeighting edgesWeighting) {
    TriangulationJTS jts = new TriangulationJTS("");
    List<IFeature> listeFeatures = new ArrayList<IFeature>();
    JungSnapshot snap = this.getSnapshotAt(t);
    for (GraphEntity v : snap.getVertices()) {
      listeFeatures.add(new DefaultFeature(v.getGeometry()
          .toGeoxGeometry()));
    }
    jts.importAsNodes(listeFeatures);
    try {
      jts.triangule();
    } catch (Exception e) {
      e.printStackTrace();
    }
    SnapshotGraph gg = SnapshotIOManager.topo2Snapshot(jts, edgesWeighting,
        null);
    jts = null;
    return gg;
  }

  //    public STLink getSTLinkWhenSourceIs(STEntity source) {
  //        for (STLink link : this.stLinks) {
  //            if (link.getSource().equals(source)) {
  //                return link;
  //            }
  //        }
  //        return null;
  //    }
  //
  //    public STLink getSTLinkWhenTargetIs(STEntity target) {
  //        for (STLink link : this.stLinks) {
  //            if (link.getTarget().equals(target)) {
  //                return link;
  //            }
  //        }
  //        return null;
  //    }

  // ********************************************************************
  // *********************** indicateurs ********************************
  // ********************************************************************

  /**
   * Calcul d'un indicateur global pour chaque snapshot
   * @param indicator
   * @param force indique si on veut forcer le calcul (ou re-calcul) pour toutes
   * les dates
   */
  public void graphGlobalIndicator(IGlobalIndicator indicator, boolean force) {
    for(STProperty<Double> stind : this.globalIndicators){
      if(stind.getName().toLowerCase().equals(indicator.getName().toLowerCase())){
        //déja calculé
        for(FuzzyTemporalInterval t: this.temporalDomain.asList()){
          if(stind.getValueAt(t) != null && !force){
            continue;
          }
          double d = graphGlobalIndicatorAt(t, indicator);
          stind.setValueAt(t, d);
        }
        return;
      }
    }
    //dans le cas ou cet indicateur n'a pas été calculé
    STProperty<Double> stglobInd = new STProperty<Double>(STProperty.PROPERTIE_FINAL_TYPES.Indicator, indicator.getName());
    for(FuzzyTemporalInterval t : this.getTemporalDomain().asList()){
      double d = graphGlobalIndicatorAt(t, indicator);
      stglobInd.setValueAt(t, d);
    }
    this.globalIndicators.add(stglobInd);
  }

  protected double graphGlobalIndicatorAt(FuzzyTemporalInterval t, IGlobalIndicator ind){
    JungSnapshot snap = this.getSnapshotAt(t);
    double d = snap.calculateGraphGlobalIndicator(ind);
    return d;
  }

  /**
   * Calcul d'un indicateur local d'ensemble pour les sommets de chaque snapshot
   * @param indicator
   * @param normalize
   * @param force si on veut forcer le calcul (ou re-calcul) pour toutes les dates
   */
  public void nodeLocalIndicator(ILocalIndicator indicator,
      NORMALIZERS normalize, boolean force) {

    if(!this.nodesLocalIndicators.contains(indicator.getName())){
      //c'est la première fois qu'on calcul cet indicateur !
      this.nodesLocalIndicators.add(indicator.getName());
      for(STEntity node : this.getVertices()){
        STProperty<Double> newInd = new STProperty<Double>(STProperty.PROPERTIE_FINAL_TYPES.Indicator, indicator.getName());
        node.getTIndicators().add(newInd);
      }
    }
    for (FuzzyTemporalInterval t : this.temporalDomain.asList()) {
      if(this.getNodesAt(t).isEmpty()){
        continue;
      }
      STEntity firstNode = this.getNodesAt(t).iterator().next();
      //on a forcement deja crée un indicateur pour ce sommet
      //déja calculé pour cette date ?
      if(firstNode.getTIndicatorByName(indicator.getName()).getValueAt(t) != null){
        // oui déja pour cette date. 
        //on recalcul ? 
        if(force){
          this.nodeLocalIndicatorAt(t, indicator, normalize);
        }
      }
      else{
        //non pas pour cette date
        this.nodeLocalIndicatorAt(t, indicator, normalize);
      }

    }
  }


  /**
   * Calcul d'un indicateur local d'ensemble pour les arcs de chaque snapshot
   * @param indicator
   * @param normalize
   * @param force si on veut forcer le calcul (ou re-calcul) pour toutes les dates
   */
  public void edgeLocalIndicator(ILocalIndicator indicator,
      NORMALIZERS normalize, boolean force) {

    if(!this.edgesLocalIndicators.contains(indicator.getName())){
      //c'est la première fois qu'on calcul cet indicateur !
      this.edgesLocalIndicators.add(indicator.getName());
      for(STEntity edge : this.getEdges()){
        STProperty<Double> newInd = new STProperty<Double>(STProperty.PROPERTIE_FINAL_TYPES.Indicator, indicator.getName());
        edge.getTIndicators().add(newInd);
      }
    }
    for (FuzzyTemporalInterval t : this.temporalDomain.asList()) {
      if(this.getEdgesAt(t).isEmpty()){
        continue;
      }
      STEntity firstEdge = this.getEdgesAt(t).iterator().next();
      //on a forcement deja crée un indicateur pour ce sommet
      //déja calculé pour cette date ?
      if(firstEdge.getTIndicatorByName(indicator.getName()).getValueAt(t) != null){
        // oui déja pour cette date. 
        //on recalcul ? 
        if(force){
          this.edgeLocalIndicatorAt(t, indicator, normalize);
        }
      }
      else{
        //non pas pour cette date
        this.edgeLocalIndicatorAt(t, indicator, normalize);
      }

    }
  }



  /**
   * Calcul d'un indicateur local de voisinage pour les sommets de chaque snapshot
   * @param indicator
   * @param normalize
   * @param k
   * @param force si on veut forcer le calcul (ou re-calcul) pour toutes les dates
   */
  public void neighborhoodNodeLocalIndicator(ILocalIndicator indicator,
      int k, NORMALIZERS normalize, boolean force) {
    String indName = k +"_" + indicator.getName();
    if(!this.nodesLocalIndicators.contains(indicator.getName())){
      //c'est la première fois qu'on calcul cet indicateur !
      this.nodesLocalIndicators.add(indName);
      for(STEntity node : this.getVertices()){
        STProperty<Double> newInd = new STProperty<Double>(STProperty.PROPERTIE_FINAL_TYPES.Indicator, indName);
        node.getTIndicators().add(newInd);
      }
    }
    for (FuzzyTemporalInterval t : this.temporalDomain.asList()) {
      STEntity firstNode = this.getNodesAt(t).iterator().next();
      if(this.getNodesAt(t).isEmpty()){
        continue;
      }
      //on a forcement deja crée un indicateur pour ce sommet
      //déja calculé pour cette date ?
      if(firstNode.getTIndicatorByName(indName).getValueAt(t) != null){
        // oui déja pour cette date. 
        //on recalcul ? 
        if(force){
          this.neighborhoodNodeLocalIndicatorAt(t, indicator, k, normalize);
        }
      }
      else{
        //non pas pour cette date
        this.neighborhoodNodeLocalIndicatorAt(t, indicator, k, normalize);
      }

    }
  }


  /**
   * Calcul d'un indicateur local de voisinage pour les arcs de chaque snapshot
   * @param indicator
   * @param normalize
   * @param k
   * @param force si on veut forcer le calcul (ou re-calcul) pour toutes les dates
   */
  public void neighborhoodEdgeLocalIndicator(ILocalIndicator indicator,
      int k, NORMALIZERS normalize, boolean force) {
    String indName = k + "_" + indicator.getName() ;
    if(!this.edgesLocalIndicators.contains(indicator.getName())){
      //c'est la première fois qu'on calcul cet indicateur !
      this.edgesLocalIndicators.add(indName);
      for(STEntity edge : this.getEdges()){
        STProperty<Double> newInd = new STProperty<Double>(STProperty.PROPERTIE_FINAL_TYPES.Indicator, indName);
        edge.getTIndicators().add(newInd);
      }
    }
    for (FuzzyTemporalInterval t : this.temporalDomain.asList()) {
      if(this.getEdgesAt(t).isEmpty()){
        continue;
      }
      STEntity firstEdge = this.getEdgesAt(t).iterator().next();
      //on a forcement deja crée un indicateur pour ce sommet
      //déja calculé pour cette date ?
      if(firstEdge.getTIndicatorByName(indName).getValueAt(t) != null){
        // oui déja pour cette date. 
        //on recalcul ? 
        if(force){
          this.neighborhoodEdgeLocalIndicatorAt(t, indicator, k, normalize);
        }
      }
      else{
        //non pas pour cette date
        this.neighborhoodEdgeLocalIndicatorAt(t, indicator, k, normalize);
      }

    }
  }

  /**
   * Calcul de l'indicateur pour les sommets
   * 
   * @param indicator
   */
  protected void nodeLocalIndicatorAt(FuzzyTemporalInterval t,
      ILocalIndicator indicator, NORMALIZERS normalize) {
    JungSnapshot snap = this.getSnapshotAt(t);

    Map<GraphEntity, Double> values = snap.calculateNodeCentrality(
        indicator, normalize);
    for (STEntity node : this.getVertices()) {
      if (!node.existsAt(t)) {
        continue;
      }
      // récupération du graphentity
      GraphEntity gr = null;
      for (GraphEntity grr : snap.getVertices()) {
        if (grr.getId() == node.getId()) {
          gr = grr;
          break;
        }
      }
      node.setIndicatorAt(indicator.getName(), t, values.get(gr));
    }
  }

  /**
   * Calcul de l'indicateur pour les sommets
   * 
   * @param indicator
   */
  public void neighborhoodNodeLocalIndicatorAt(FuzzyTemporalInterval t,
      ILocalIndicator indicator, int k, NORMALIZERS normalize) {

    JungSnapshot snap = this.getSnapshotAt(t);
    Map<GraphEntity, Double> values = snap
        .calculateNeighborhoodNodeCentrality(indicator, k, normalize);

    for (STEntity node : this.getVertices()) {
      if (!node.existsAt(t)) {
        continue;
      }
      // récupération du graphentity
      GraphEntity gr = null;
      for (GraphEntity grr : snap.getVertices()) {
        if (grr.getId() == node.getId()) {
          gr = grr;
          break;
        }
      }
      node.setIndicatorAt(k +"_" + indicator.getName(),
          t, values.get(gr));
    }

  }

  /**
   * Calcul de l'indicateur pour les arcs
   * 
   * @param indicator
   */
  protected void edgeLocalIndicatorAt(FuzzyTemporalInterval t,
      ILocalIndicator indicator, NORMALIZERS normalize) {
    this.edgesLocalIndicators.add(indicator.getName());

    JungSnapshot snap = this.getSnapshotAt(t);

    Map<GraphEntity, Double> values = snap.calculateEdgeCentrality(
        indicator, normalize);
    for (STEntity edge : this.getEdges()) {
      if (!edge.existsAt(t)) {
        continue;
      }
      // récupération du graphentity
      GraphEntity gr = null;
      for (GraphEntity grr : snap.getEdges()) {
        if (this.mappingSnapshotEdgesId.get(grr.getId()).contains(
            edge.getId())) {
          gr = grr;
          break;
        }
      }
      edge.setIndicatorAt(indicator.getName(), t, values.get(gr));
    }

  }

  /**
   * Calcul de l'indicateur pour les arcs
   * 
   * @param indicator
   */
  protected void neighborhoodEdgeLocalIndicatorAt(FuzzyTemporalInterval t,
      ILocalIndicator indicator, int k, NORMALIZERS normalize) {



    this.edgesLocalIndicators.add(
        k +"_" + indicator.getName());
    JungSnapshot snap = this.getSnapshotAt(t);

    Map<GraphEntity, Double> values = snap
        .calculateNeighborhoodEdgeCentrality(indicator, k, normalize);

    for (STEntity edge : this.getEdges()) {
      if (!edge.existsAt(t)) {
        continue;
      }
      // récupération du graphentity
      GraphEntity gr = null;
      for (GraphEntity grr : snap.getEdges()) {
        if (this.mappingSnapshotEdgesId.get(grr.getId()).contains(
            edge.getId())) {
          gr = grr;
          break;
        }
      }
      edge.setIndicatorAt(k +"_" + indicator.getName(),
          t, values.get(gr));
    }

  }

  public void updateGeometries() {
    for (STEntity node : this.getVertices()) {
      node.updateGeometry(this);
    }
    for (STEntity edge : this.getEdges()) {
      edge.updateGeometry(this);
    }
  }

  // ***************************************************************************
  // ****************************
  // Accesseurs************************************
  // ***************************************************************************

  public Set<STProperty<Double>> getGlobalIndicators() {
    return globalIndicators;
  }

  public void setGlobalIndicators(
      Set<STProperty<Double>> globalIndicators) {
    this.globalIndicators = globalIndicators;
  }

  public Set<String> getNodesLocalIndicators() {
    return nodesLocalIndicators;
  }

  public void setNodesLocalIndicators(
      Set<String> nodesLocalIndicators) {
    this.nodesLocalIndicators = nodesLocalIndicators;
  }

  public Set<String> getEdgesLocalIndicators() {
    return edgesLocalIndicators;
  }

  public void setEdgesLocalIndicators(
      Set<String> edgesLocalIndicators) {
    this.edgesLocalIndicators = edgesLocalIndicators;
  }

  public void setTemporalDomain(TemporalDomain temporalDomain) {
    this.temporalDomain = temporalDomain;
  }

  public TemporalDomain getTemporalDomain() {
    return temporalDomain;
  }

  //    public void setStLinks(Set<STLink> stLinks) {
  //        this.stLinks = stLinks;
  //    }
  //
  //    public Set<STLink> getStLinks() {
  //        return stLinks;
  //    }

  public List<String> getAttributes() {
    return attributes;
  }

  public void setAttributes(List<String> attributes) {
    this.attributes = attributes;
  }

  /**
   * Ajout d'un attribut String pour les STEntity
   * @param attribute
   */
  public void addAttribute(String attribute) {
    if (!this.attributes.contains(attribute)) {
      this.attributes.add(attribute);
      for (STEntity e : this.getEdges()) {
        STProperty<String> statt = new STProperty<String>(STProperty.PROPERTIE_FINAL_TYPES.Attribute, attribute,
            this.temporalDomain.asList());
        Map<FuzzyTemporalInterval, String> values = new HashMap<FuzzyTemporalInterval, String>();
        for (FuzzyTemporalInterval t : this.temporalDomain.asList()) {
          values.put(t, null);
        }
        statt.setValues(values);
        e.getTAttributes().add(statt);
      }
    }
  }



  public STProperty<Double> getAccuracies() {
    return accuracies;
  }

  public void setAccuracies(STProperty<Double> accuracies) {
    this.accuracies = accuracies;
  }

  public void updateFuzzyTemporalInterval(FuzzyTemporalInterval told,
      FuzzyTemporalInterval tnew) {
    if (tnew.equals(told)) {
      return;
    }
    // domaine temporel
    this.temporalDomain.updateFuzzyTemporalInterval(told, tnew);
    // entités
    for (STEntity node : this.getVertices()) {
      node.updateFuzzyTemporalInterval(told, tnew);
    }
    for (STEntity edge : this.getEdges()) {
      edge.updateFuzzyTemporalInterval(told, tnew);
    }
    // st links
    //        for (STLink link : this.stLinks) {
    //            link.updateFuzzyTemporalInterval(told, tnew);
    //        }
    // global indicateurs
    for(STProperty<Double> globInd : this.globalIndicators){
      globInd.updateFuzzyTemporalInterval(told, tnew);
    }
    // accuracies
    this.accuracies.updateFuzzyTemporalInterval(told, tnew);
  }


  public void addFuzzyTemporalInterval(FuzzyTemporalInterval t, double accuracy) {
    if (this.temporalDomain.asList().contains(t)) {
      return;
    }
    // domaine temporel
    this.temporalDomain.asList().add(t);
    // entités
    for (STEntity node : this.getVertices()) {
      node.addFuzzyTemporalInterval(t);
    }
    for (STEntity edge : this.getEdges()) {
      edge.addFuzzyTemporalInterval(t);
    }

    // global indicateurs
    for(STProperty<Double> globInd : this.globalIndicators){
      globInd.setValueAt(t, null);
    }         // indicateurs locaux de noeuds
    // accuracies
    this.accuracies.setValueAt(t,accuracy);
  }

  /**
   * Reset les identifiants des sommets et arcs ST
   */
  public void resetIds(){
    Map<STEntity, STEntity> oldNodes = new HashMap<STEntity, STEntity>();
    int cpt = STEntity.NODE;
    STEntity.setCurrentType(STEntity.NODE);
    for(STEntity node: this.getVertices()){
      STEntity newNode = new STEntity(node.getTimeSerie());
      newNode.setId(cpt);
      newNode.setGeometry(node.getGeometry());
      newNode.setTAttributes(node.getTAttributes());
      newNode.setTGeometries(node.getTGeometry());
      newNode.setTIndicators(node.getTIndicators());
      newNode.setTWeight(node.getTWeight());
      oldNodes.put(node,newNode);
      cpt++;
    }
    cpt = STEntity.EDGE;
    STEntity.setCurrentType(STEntity.EDGE);
    Set<STEntity> edges = new HashSet<STEntity>(this.getEdges());
    Map<STEntity, Pair<STEntity>> nodes = new HashMap<STEntity, Pair<STEntity>>();
    for(STEntity edge: new ArrayList<STEntity>(this.getEdges())){
      nodes.put(edge, new Pair<STEntity>(oldNodes.get(this.getEndpoints(edge).getFirst()),
          oldNodes.get(this.getEndpoints(edge).getSecond())));
      this.removeEdge(edge);
    }
    for(STEntity n: oldNodes.keySet()){
      this.removeVertex(n);
    }

    for(STEntity edge: edges){
      STEntity newEdge = new STEntity(edge.getTimeSerie());
      newEdge.setId(cpt);
      newEdge.setGeometry(edge.getGeometry());
      newEdge.setTAttributes(edge.getTAttributes());
      newEdge.setTGeometries(edge.getTGeometry());
      newEdge.setTIndicators(edge.getTIndicators());
      newEdge.setTWeight(edge.getTWeight());
      this.addEdge(newEdge, nodes.get(edge).getFirst(), nodes.get(edge).getSecond());
      cpt++;
    }
    STEntity.updateIDS(this.getVertexCount()+1, this.getEdgeCount() +1);
  }

  /**
   * Rend le STAG planaire
   * A utiliser avec modération : n'est valable que pour une géométrie donnée du STAG
   * TODO : Si on change sa géométrie, il faut récupérer les sommets fictifs a toute date (ce qui correspond
   * au sommet rajoutés pour la planarité) et refusionner les arcs correspondant, puis rendre a nouveau 
   * planaire pour la nouvelle géométrie.
   */
  public void setPlanar(){

    Set<STEntity> newVertex = new HashSet<STEntity>();
    Stack<STEntity> edges = new Stack<STEntity>();
    edges.addAll(this.getEdges());
    Map<IGeometry, STEntity> mapping = new HashMap<IGeometry, STEntity>();
    while(!edges.isEmpty()){
      STEntity edge = edges.pop();
      for(STEntity edgeInt: edges){
        //on enleve les voisins
        if(edge.getGeometry().toGeoxGeometry().intersects(edgeInt.getGeometry().toGeoxGeometry())){
          IGeometry intersection = edge.getGeometry().toGeoxGeometry().intersection(edgeInt.getGeometry().toGeoxGeometry());
          if(intersection instanceof IPoint){
            
            // TODO: pour le moment on suppose qu'on  n'intersecte pas sur un sommet...
            IDirectPosition pint = ((IPoint) intersection).getPosition();
            
            if(intersection.distance(this.getEndpoints(edge).getFirst().getGeometry().toGeoxGeometry()) < 0.001){
              continue;
            }
            
            if(intersection.distance(this.getEndpoints(edge).getSecond().getGeometry().toGeoxGeometry()) < 0.001){
              continue;
            }
            
            if(intersection.distance(this.getEndpoints(edgeInt).getFirst().getGeometry().toGeoxGeometry()) < 0.001){
              continue;
            }
            
            if(intersection.distance(this.getEndpoints(edgeInt).getSecond().getGeometry().toGeoxGeometry()) < 0.001){
              continue;
            }
            //on crée un nouveau sommet
            STEntity.setCurrentType(STEntity.NODE);

            STEntity newVertice = new STEntity();
            //fictif a toute date
            for(FuzzyTemporalInterval t: this.temporalDomain.asList()){
              newVertice.existsAt(t,false);
            }
            newVertice.setGeometry((LightGeometry) new LightDirectPosition(pint));
            newVertex.add(newVertice);
            mapping.put(newVertice.getGeometry().toGeoxGeometry(), newVertice);
          }
          else if(intersection instanceof IMultiPoint){
            for(IPoint p : ((IMultiPoint)intersection).getList()){
              IDirectPosition pint =  p.getPosition();
              
              if(p.distance(this.getEndpoints(edge).getFirst().getGeometry().toGeoxGeometry()) < 0.001){
                continue;
              }
              
              if(p.distance(this.getEndpoints(edge).getSecond().getGeometry().toGeoxGeometry()) < 0.001){
                continue;
              }
              
              if(p.distance(this.getEndpoints(edgeInt).getFirst().getGeometry().toGeoxGeometry()) < 0.001){
                continue;
              }
              
              if(p.distance(this.getEndpoints(edgeInt).getSecond().getGeometry().toGeoxGeometry()) < 0.001){
                continue;
              }

              //on crée un nouveau sommet
              STEntity.setCurrentType(STEntity.NODE);

              STEntity newVertice = new STEntity();
              //fictif a toute date
              for(FuzzyTemporalInterval t: this.temporalDomain.asList()){
                newVertice.existsAt(t,false);
              }
              newVertice.setGeometry((LightGeometry) new LightDirectPosition(pint));
              newVertex.add(newVertice);
              mapping.put(newVertice.getGeometry().toGeoxGeometry(), newVertice);

            }
          }
        }
      }
    }

    System.out.println(newVertex.size());

    for(STEntity edge: new HashSet<STEntity>(this.getEdges())){
      Set<STEntity> vertexOk = new HashSet<STEntity>();
      for(IGeometry g: mapping.keySet()){
        if(edge.getGeometry().toGeoxGeometry().distance(g)<0.001){
          vertexOk.add(mapping.get(g));
        }
      }
      Set<STEntity>edgesTarget = new HashSet<STEntity>();
      edgesTarget.add(edge);
      for (STEntity nodeSource : vertexOk) {
        // récupération de l'arc ST le plus proche
        STEntity edgeClose = null;
        double dmin = Double.MAX_VALUE;
        for (STEntity e : edgesTarget) {
          double d = e.getGeometry().toGeoxGeometry()
              .distance(nodeSource.getGeometry().toGeoxGeometry());
          if (d < dmin) {
            dmin = d;
            edgeClose = e;
          }
        }
        STEntity[] edgesSplit = SplitUtils.fictiveSplitEdgeCurviligne(edgeClose, nodeSource, this);
        if (edgesSplit == null) {
          continue;
        }
        edgesTarget.remove(edgeClose);
        edgesTarget.add(edgesSplit[0]);
        edgesTarget.add(edgesSplit[1]);
      }
    }
    
    //this.updateGeometries();
  }



}
