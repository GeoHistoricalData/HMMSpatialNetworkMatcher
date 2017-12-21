package fr.ign.cogit.v2.indicators.dynamics.eventsfinder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import nrc.fuzzy.XValuesOutOfOrderException;
import nrc.fuzzy.YValueOutOfRangeException;
import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.feature.DefaultFeature;
import fr.ign.cogit.geoxygene.feature.Population;
import fr.ign.cogit.geoxygene.util.attribute.AttributeManager;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileWriter;
import fr.ign.cogit.v2.io.TAGIoManager;
import fr.ign.cogit.v2.snapshot.GraphEntity;
import fr.ign.cogit.v2.snapshot.JungSnapshot;
import fr.ign.cogit.v2.tag.FuzzyTemporalInterval;
import fr.ign.cogit.v2.tag.STEntity;
import fr.ign.cogit.v2.tag.STGraph;

public class NegativeBetweennessCentrality {

  private Set<STEntity> edges;
  private FuzzyTemporalInterval t1, t2;
  private STGraph stag;

  public NegativeBetweennessCentrality(STGraph stag, Set<STEntity> edges, FuzzyTemporalInterval t1, FuzzyTemporalInterval t2) throws Exception{
    if(edges.isEmpty()){
      throw new Exception("edges.size() must be at least 1.");
    }
    this.t1 = t1;
    this.t2 = t2;
    this.edges = edges;
    this.stag = stag;
    for(STEntity e : edges){
      if(!e.existsAt(t1) || !e.existsAt(t2)){
        throw new Exception("Edges must exist at "+ t1.toString()+" and at "+ t2.toString());
      }
    }
    // si plus d'un arc on regarde la continuité
    if(this.edges.size() > 1){
      Set<STEntity> nodes = new HashSet<STEntity>();
      for(STEntity e: this.edges){
        if(nodes.contains(this.stag.getEndpoints(e).getFirst())){
          nodes.remove(this.stag.getEndpoints(e).getFirst());
        }
        else{
          nodes.add(this.stag.getEndpoints(e).getFirst());
        }
        if(nodes.contains(this.stag.getEndpoints(e).getSecond())){
          nodes.remove(this.stag.getEndpoints(e).getSecond());
        }
        else{
          nodes.add(this.stag.getEndpoints(e).getSecond());
        }
      }
      if(nodes.size() != 2){
        //les arcs ne forment pas une voie continue
        throw new Exception("Edges must be a continuous street.");
      }
    }
  }

  /**
   * La centralité intermédiaire négative relativement à x pour les arcs du réseau à t1
   * identifie les arcs à t1 par lesquels transitaient majoritairement les relations qui passaient
   * également par x à t1 mais qui n'y passent plus à t2. En gros, quels étaient les arcs de
   * t1 qui acheminaient majoritairement les ppc vers x et qui ne passent plus par x à t2.
   * On cherche les relations qui passaient par tous les arcs à t14 et aucun à t2
   * @return
   */
  public  Map<STEntity, Double>  getFirstNegativeBetweennessCentrality(){
    Map<STEntity, Double> result = new HashMap<STEntity, Double>();
    for (STEntity v : this.stag.getEdgesAt(this.t1)) {
      result.put(v, 0.);
    }
    JungSnapshot snap1 = this.stag.getSnapshotAt(t1);
    Map<STEntity, GraphEntity> mappingNodes1 = new HashMap<STEntity, GraphEntity>();
    for(STEntity se: this.stag.getNodesAt(t1)){
      for(GraphEntity ge : snap1.getVertices()){
        if(ge.getId() == se.getId()){
          mappingNodes1.put(se, ge);
          break;
        }
      }
    }
    Map<STEntity, GraphEntity> mapping1 = new HashMap<STEntity, GraphEntity>();
    for(STEntity se: this.stag.getEdgesAt(t1)){
      for(GraphEntity ge : snap1.getEdges()){
        if(this.stag.getMappingSnapshotEdgesId().get(ge.getId()).contains(se.getId())){
          mapping1.put(se, ge);
          break;
        }
      }
    }

    Map<GraphEntity, List<STEntity>> mappingReverse1 = new HashMap<GraphEntity, List<STEntity>>();
    for(GraphEntity ge : snap1.getEdges()){
      List<STEntity> l = new ArrayList<STEntity>();
      for(STEntity se: this.stag.getEdgesAt(t1)){
        if(this.stag.getMappingSnapshotEdgesId().get(ge.getId()).contains(se.getId())){
          l.add(se);
        }
      }
      mappingReverse1.put(ge, l);
    }
    JungSnapshot snap2 = this.stag.getSnapshotAt(t2);
    Map<STEntity, GraphEntity> mappingNodes2 = new HashMap<STEntity, GraphEntity>();
    for(STEntity se: this.stag.getNodesAt(t2)){
      for(GraphEntity ge : snap2.getVertices()){
        if(ge.getId() == se.getId()){
          mappingNodes2.put(se, ge);
          break;
        }
      }
    }

    Map<STEntity, GraphEntity> mapping2 = new HashMap<STEntity, GraphEntity>();
    for(STEntity se: this.stag.getEdgesAt(t2)){
      for(GraphEntity ge : snap2.getEdges()){
        if(this.stag.getMappingSnapshotEdgesId().get(ge.getId()).contains(se.getId())){
          mapping2.put(se, ge);
          break;
        }
      }
    }

    Map<GraphEntity, List<STEntity>> mappingReverse2 = new HashMap<GraphEntity, List<STEntity>>();
    for(GraphEntity ge : snap2.getEdges()){
      List<STEntity> l = new ArrayList<STEntity>();
      for(STEntity se: this.stag.getEdgesAt(t2)){
        if(this.stag.getMappingSnapshotEdgesId().get(ge.getId()).contains(se.getId())){
          l.add(se);
        }
      }
      mappingReverse2.put(ge, l);
    }


    DijkstraShortestPath<GraphEntity, GraphEntity> ppc1 = new DijkstraShortestPath<GraphEntity, GraphEntity>(snap1, snap1.getEdgesWeights());
    DijkstraShortestPath<GraphEntity, GraphEntity> ppc2 = new DijkstraShortestPath<GraphEntity, GraphEntity>(snap2, snap2.getEdgesWeights());


    //on regarde les ppc entre sommets existants aux deux dates
    Stack<STEntity> nodes = new Stack<STEntity>();
    for(STEntity n: this.stag.getVertices()){
      if(n.existsAt(t1) && n.existsAt(t2)){
        nodes.add(n);
      }
    }
    while(!nodes.isEmpty()){
      System.out.println(nodes.size());
      STEntity stnode1 = nodes.pop();
      //récupération du graphentity correspondant
      GraphEntity node11 = mappingNodes1.get(stnode1);
      GraphEntity node21 = mappingNodes2.get(stnode1);
      for(STEntity stnode2 : nodes){
        GraphEntity node12 = mappingNodes1.get(stnode2);
        GraphEntity node22 = mappingNodes2.get(stnode2);

        //chemin entre node11 et node12 sur le graphe snap1
        Set<GraphEntity> path1= new HashSet<GraphEntity>(ppc1.getPath(node11, node12));
        //chemin entre node21 et node22 sur le graphe snap2
        Set<GraphEntity> path2= new HashSet<GraphEntity>(ppc2.getPath(node21, node22));
        // on regarde si ce ppc nous interesse: passe-t-il par tous les arc de this.edges a t1? 
        boolean ok = true;
        for(STEntity stedge : this.edges){
          //récupération du graphentity
          GraphEntity edge1 = mapping1.get(stedge);
          if(!path1.contains(edge1)){
            ok = false;
            break;
          }
        }
        if(ok){
          ok = false;
          for(STEntity stedge : this.edges){
            //récupération du graphentity
            GraphEntity edge2 = mapping2.get(stedge);
            if(path2.contains(edge2)){
              ok = true;
              break;
            }
          }
          if(ok){
            continue;
          }
          //on a un ppc qui passe par tous les arcs de edges à t1 mais pas à t2
          //on regarde comment le ppc diverge entre t1 et t2
          Set<STEntity> set1 = new HashSet<STEntity>();
          for(GraphEntity e: path1){
            set1.addAll(mappingReverse1.get(e));
          }
          Set<STEntity> set2 = new HashSet<STEntity>();
          for(GraphEntity e: path2){
            set2.addAll(mappingReverse2.get(e));
          }
          for(STEntity e1: set1){
            if(/*!set2.contains(e1) &&*/ !edges.contains(e1)){
              //on ne s'interesse pas aux arcs de edges
              result.put(e1, result.get(e1)+1);
            }
          }
        }

      }

      ppc1.reset(node11);
      ppc2.reset(node21);
    }

    return result;
  }
  
  /**
   *    * On cherche les relations qui passaient par au moins un arc à t1, et plus aucun à t2
   */
  public  Map<STEntity, Double>  getPartialFirstNegativeBetweennessCentrality(){
    Map<STEntity, Double> result = new HashMap<STEntity, Double>();
    for (STEntity v : this.stag.getEdgesAt(this.t1)) {
      result.put(v, 0.);
    }
    JungSnapshot snap1 = this.stag.getSnapshotAt(t1);
    Map<STEntity, GraphEntity> mappingNodes1 = new HashMap<STEntity, GraphEntity>();
    for(STEntity se: this.stag.getNodesAt(t1)){
      for(GraphEntity ge : snap1.getVertices()){
        if(ge.getId() == se.getId()){
          mappingNodes1.put(se, ge);
          break;
        }
      }
    }
    Map<STEntity, GraphEntity> mapping1 = new HashMap<STEntity, GraphEntity>();
    for(STEntity se: this.stag.getEdgesAt(t1)){
      for(GraphEntity ge : snap1.getEdges()){
        if(this.stag.getMappingSnapshotEdgesId().get(ge.getId()).contains(se.getId())){
          mapping1.put(se, ge);
          break;
        }
      }
    }

    Map<GraphEntity, List<STEntity>> mappingReverse1 = new HashMap<GraphEntity, List<STEntity>>();
    for(GraphEntity ge : snap1.getEdges()){
      List<STEntity> l = new ArrayList<STEntity>();
      for(STEntity se: this.stag.getEdgesAt(t1)){
        if(this.stag.getMappingSnapshotEdgesId().get(ge.getId()).contains(se.getId())){
          l.add(se);
        }
      }
      mappingReverse1.put(ge, l);
    }
    JungSnapshot snap2 = this.stag.getSnapshotAt(t2);
    Map<STEntity, GraphEntity> mappingNodes2 = new HashMap<STEntity, GraphEntity>();
    for(STEntity se: this.stag.getNodesAt(t2)){
      for(GraphEntity ge : snap2.getVertices()){
        if(ge.getId() == se.getId()){
          mappingNodes2.put(se, ge);
          break;
        }
      }
    }

    Map<STEntity, GraphEntity> mapping2 = new HashMap<STEntity, GraphEntity>();
    for(STEntity se: this.stag.getEdgesAt(t2)){
      for(GraphEntity ge : snap2.getEdges()){
        if(this.stag.getMappingSnapshotEdgesId().get(ge.getId()).contains(se.getId())){
          mapping2.put(se, ge);
          break;
        }
      }
    }

    Map<GraphEntity, List<STEntity>> mappingReverse2 = new HashMap<GraphEntity, List<STEntity>>();
    for(GraphEntity ge : snap2.getEdges()){
      List<STEntity> l = new ArrayList<STEntity>();
      for(STEntity se: this.stag.getEdgesAt(t2)){
        if(this.stag.getMappingSnapshotEdgesId().get(ge.getId()).contains(se.getId())){
          l.add(se);
        }
      }
      mappingReverse2.put(ge, l);
    }


    DijkstraShortestPath<GraphEntity, GraphEntity> ppc1 = new DijkstraShortestPath<GraphEntity, GraphEntity>(snap1, snap1.getEdgesWeights());
    DijkstraShortestPath<GraphEntity, GraphEntity> ppc2 = new DijkstraShortestPath<GraphEntity, GraphEntity>(snap2, snap2.getEdgesWeights());


    //on regarde les ppc entre sommets existants aux deux dates
    Stack<STEntity> nodes = new Stack<STEntity>();
    for(STEntity n: this.stag.getVertices()){
      if(n.existsAt(t1) && n.existsAt(t2)){
        nodes.add(n);
      }
    }
    while(!nodes.isEmpty()){
      System.out.println(nodes.size());
      STEntity stnode1 = nodes.pop();
      //récupération du graphentity correspondant
      GraphEntity node11 = mappingNodes1.get(stnode1);
      GraphEntity node21 = mappingNodes2.get(stnode1);
      for(STEntity stnode2 : nodes){
        GraphEntity node12 = mappingNodes1.get(stnode2);
        GraphEntity node22 = mappingNodes2.get(stnode2);

        //chemin entre node11 et node12 sur le graphe snap1
        Set<GraphEntity> path1= new HashSet<GraphEntity>(ppc1.getPath(node11, node12));
        //chemin entre node21 et node22 sur le graphe snap2
        Set<GraphEntity> path2= new HashSet<GraphEntity>(ppc2.getPath(node21, node22));
        // on regarde si ce ppc nous interesse: passe-t-il par au moins un des arc de this.edges a t1? 
        boolean ok = false;
        for(STEntity stedge : this.edges){
          //récupération du graphentity
          GraphEntity edge1 = mapping1.get(stedge);
          if(path1.contains(edge1)){
            ok = true;
            break;
          }
        }
        if(ok){
          ok = false;
          for(STEntity stedge : this.edges){
            //récupération du graphentity
            GraphEntity edge2 = mapping2.get(stedge);
            if(path2.contains(edge2)){
              ok = true;
              break;
            }
          }
          if(ok){
            continue;
          }
          //on a un ppc qui passe par tous les arcs de edges à t1 mais pas à t2
          //on regarde comment le ppc diverge entre t1 et t2
          Set<STEntity> set1 = new HashSet<STEntity>();
          for(GraphEntity e: path1){
            set1.addAll(mappingReverse1.get(e));
          }
          Set<STEntity> set2 = new HashSet<STEntity>();
          for(GraphEntity e: path2){
            set2.addAll(mappingReverse2.get(e));
          }
          for(STEntity e1: set1){
            if(/*!set2.contains(e1) &&*/ !edges.contains(e1)){
              //on ne s'interesse pas aux arcs de edges
              result.put(e1, result.get(e1)+1);
            }
          }
        }

      }

      ppc1.reset(node11);
      ppc2.reset(node21);
    }

    return result;
  }
  /**
   * La centralité intermédiaire négative relativement à x pour les arcs du réseau à t2
   * identifie les arcs à t2 par lesquels transitent les relations qui passaient par x à t1
   * mais plus à t2. En gros, quelles sont les structures qui ont « absorbées » les ppc qui
   * passaient par x à t1 mais n'y passe plus à t2.
   * @return
   */
  public  Map<STEntity, Double>  getSecondeNegativeBetweennessCentrality(){
    Map<STEntity, Double> result = new HashMap<STEntity, Double>();
    for (STEntity v : this.stag.getEdgesAt(this.t2)) {
      result.put(v, 0.);
    }
    JungSnapshot snap1 = this.stag.getSnapshotAt(t1);
    Map<STEntity, GraphEntity> mappingNodes1 = new HashMap<STEntity, GraphEntity>();
    for(STEntity se: this.stag.getNodesAt(t1)){
      for(GraphEntity ge : snap1.getVertices()){
        if(ge.getId() == se.getId()){
          mappingNodes1.put(se, ge);
          break;
        }
      }
    }
    Map<STEntity, GraphEntity> mapping1 = new HashMap<STEntity, GraphEntity>();
    for(STEntity se: this.stag.getEdgesAt(t1)){
      for(GraphEntity ge : snap1.getEdges()){
        if(this.stag.getMappingSnapshotEdgesId().get(ge.getId()).contains(se.getId())){
          mapping1.put(se, ge);
          break;
        }
      }
    }

    Map<GraphEntity, List<STEntity>> mappingReverse1 = new HashMap<GraphEntity, List<STEntity>>();
    for(GraphEntity ge : snap1.getEdges()){
      List<STEntity> l = new ArrayList<STEntity>();
      for(STEntity se: this.stag.getEdgesAt(t1)){
        if(this.stag.getMappingSnapshotEdgesId().get(ge.getId()).contains(se.getId())){
          l.add(se);
        }
      }
      mappingReverse1.put(ge, l);
    }
    JungSnapshot snap2 = this.stag.getSnapshotAt(t2);
    Map<STEntity, GraphEntity> mappingNodes2 = new HashMap<STEntity, GraphEntity>();
    for(STEntity se: this.stag.getNodesAt(t2)){
      for(GraphEntity ge : snap2.getVertices()){
        if(ge.getId() == se.getId()){
          mappingNodes2.put(se, ge);
          break;
        }
      }
    }

    Map<STEntity, GraphEntity> mapping2 = new HashMap<STEntity, GraphEntity>();
    for(STEntity se: this.stag.getEdgesAt(t2)){
      for(GraphEntity ge : snap2.getEdges()){
        if(this.stag.getMappingSnapshotEdgesId().get(ge.getId()).contains(se.getId())){
          mapping2.put(se, ge);
          break;
        }
      }
    }

    Map<GraphEntity, List<STEntity>> mappingReverse2 = new HashMap<GraphEntity, List<STEntity>>();
    for(GraphEntity ge : snap2.getEdges()){
      List<STEntity> l = new ArrayList<STEntity>();
      for(STEntity se: this.stag.getEdgesAt(t2)){
        if(this.stag.getMappingSnapshotEdgesId().get(ge.getId()).contains(se.getId())){
          l.add(se);
        }
      }
      mappingReverse2.put(ge, l);
    }


    DijkstraShortestPath<GraphEntity, GraphEntity> ppc1 = new DijkstraShortestPath<GraphEntity, GraphEntity>(snap1, snap1.getEdgesWeights());
    DijkstraShortestPath<GraphEntity, GraphEntity> ppc2 = new DijkstraShortestPath<GraphEntity, GraphEntity>(snap2, snap2.getEdgesWeights());


    //on regarde les ppc entre sommets existants aux deux dates
    Stack<STEntity> nodes = new Stack<STEntity>();
    for(STEntity n: this.stag.getVertices()){
      if(n.existsAt(t1) && n.existsAt(t2)){
        nodes.add(n);
      }
    }
    while(!nodes.isEmpty()){
      System.out.println(nodes.size());
      STEntity stnode1 = nodes.pop();
      //récupération du graphentity correspondant
      GraphEntity node11 = mappingNodes1.get(stnode1);
      GraphEntity node21 = mappingNodes2.get(stnode1);
      for(STEntity stnode2 : nodes){
        GraphEntity node12 = mappingNodes1.get(stnode2);
        GraphEntity node22 = mappingNodes2.get(stnode2);

        //chemin entre node11 et node12 sur le graphe snap1
        Set<GraphEntity> path1= new HashSet<GraphEntity>(ppc1.getPath(node11, node12));
        //chemin entre node21 et node22 sur le graphe snap2
        Set<GraphEntity> path2= new HashSet<GraphEntity>(ppc2.getPath(node21, node22));
        // on regarde si ce ppc nous interesse: passe-t-il par tous les arc de this.edges a t1? 
        boolean ok = true;
        for(STEntity stedge : this.edges){
          //récupération du graphentity
          GraphEntity edge1 = mapping1.get(stedge);
          if(!path1.contains(edge1)){
            ok = false;
            break;
          }
        }
        if(ok){
          ok = false;
          for(STEntity stedge : this.edges){
            //récupération du graphentity
            GraphEntity edge2 = mapping2.get(stedge);
            if(path2.contains(edge2)){
              ok = true;
              break;
            }
          }
          if(ok){
            continue;
          }
          //on a un ppc qui passe par tous les arcs de edges à t1 mais pas à t2
          //on regarde comment le ppc diverge entre t1 et t2
          Set<STEntity> set1 = new HashSet<STEntity>();
          for(GraphEntity e: path1){
            set1.addAll(mappingReverse1.get(e));
          }
          Set<STEntity> set2 = new HashSet<STEntity>();
          for(GraphEntity e: path2){
            set2.addAll(mappingReverse2.get(e));
          }

          for(STEntity e2: set2){
            if(/*!set1.contains(e2) &&*/ !edges.contains(e2)){
              //on ne s'interesse pas aux arcs de edges
              result.put(e2, result.get(e2)+1);
            }
          }
        }

      }

      ppc1.reset(node11);
      ppc2.reset(node21);
    }

    return result;
  }
  
  public  Map<STEntity, Double>  getPartialSecondeNegativeBetweennessCentrality(){
    Map<STEntity, Double> result = new HashMap<STEntity, Double>();
    for (STEntity v : this.stag.getEdgesAt(this.t2)) {
      result.put(v, 0.);
    }
    JungSnapshot snap1 = this.stag.getSnapshotAt(t1);
    Map<STEntity, GraphEntity> mappingNodes1 = new HashMap<STEntity, GraphEntity>();
    for(STEntity se: this.stag.getNodesAt(t1)){
      for(GraphEntity ge : snap1.getVertices()){
        if(ge.getId() == se.getId()){
          mappingNodes1.put(se, ge);
          break;
        }
      }
    }
    Map<STEntity, GraphEntity> mapping1 = new HashMap<STEntity, GraphEntity>();
    for(STEntity se: this.stag.getEdgesAt(t1)){
      for(GraphEntity ge : snap1.getEdges()){
        if(this.stag.getMappingSnapshotEdgesId().get(ge.getId()).contains(se.getId())){
          mapping1.put(se, ge);
          break;
        }
      }
    }

    Map<GraphEntity, List<STEntity>> mappingReverse1 = new HashMap<GraphEntity, List<STEntity>>();
    for(GraphEntity ge : snap1.getEdges()){
      List<STEntity> l = new ArrayList<STEntity>();
      for(STEntity se: this.stag.getEdgesAt(t1)){
        if(this.stag.getMappingSnapshotEdgesId().get(ge.getId()).contains(se.getId())){
          l.add(se);
        }
      }
      mappingReverse1.put(ge, l);
    }
    JungSnapshot snap2 = this.stag.getSnapshotAt(t2);
    Map<STEntity, GraphEntity> mappingNodes2 = new HashMap<STEntity, GraphEntity>();
    for(STEntity se: this.stag.getNodesAt(t2)){
      for(GraphEntity ge : snap2.getVertices()){
        if(ge.getId() == se.getId()){
          mappingNodes2.put(se, ge);
          break;
        }
      }
    }

    Map<STEntity, GraphEntity> mapping2 = new HashMap<STEntity, GraphEntity>();
    for(STEntity se: this.stag.getEdgesAt(t2)){
      for(GraphEntity ge : snap2.getEdges()){
        if(this.stag.getMappingSnapshotEdgesId().get(ge.getId()).contains(se.getId())){
          mapping2.put(se, ge);
          break;
        }
      }
    }

    Map<GraphEntity, List<STEntity>> mappingReverse2 = new HashMap<GraphEntity, List<STEntity>>();
    for(GraphEntity ge : snap2.getEdges()){
      List<STEntity> l = new ArrayList<STEntity>();
      for(STEntity se: this.stag.getEdgesAt(t2)){
        if(this.stag.getMappingSnapshotEdgesId().get(ge.getId()).contains(se.getId())){
          l.add(se);
        }
      }
      mappingReverse2.put(ge, l);
    }


    DijkstraShortestPath<GraphEntity, GraphEntity> ppc1 = new DijkstraShortestPath<GraphEntity, GraphEntity>(snap1, snap1.getEdgesWeights());
    DijkstraShortestPath<GraphEntity, GraphEntity> ppc2 = new DijkstraShortestPath<GraphEntity, GraphEntity>(snap2, snap2.getEdgesWeights());


    //on regarde les ppc entre sommets existants aux deux dates
    Stack<STEntity> nodes = new Stack<STEntity>();
    for(STEntity n: this.stag.getVertices()){
      if(n.existsAt(t1) && n.existsAt(t2)){
        nodes.add(n);
      }
    }
    while(!nodes.isEmpty()){
      System.out.println(nodes.size());
      STEntity stnode1 = nodes.pop();
      //récupération du graphentity correspondant
      GraphEntity node11 = mappingNodes1.get(stnode1);
      GraphEntity node21 = mappingNodes2.get(stnode1);
      for(STEntity stnode2 : nodes){
        GraphEntity node12 = mappingNodes1.get(stnode2);
        GraphEntity node22 = mappingNodes2.get(stnode2);

        //chemin entre node11 et node12 sur le graphe snap1
        Set<GraphEntity> path1= new HashSet<GraphEntity>(ppc1.getPath(node11, node12));
        //chemin entre node21 et node22 sur le graphe snap2
        Set<GraphEntity> path2= new HashSet<GraphEntity>(ppc2.getPath(node21, node22));
        // on regarde si ce ppc nous interesse: passe-t-il par au moins un des arcs de this.edges a t1? 
        boolean ok = false;
        for(STEntity stedge : this.edges){
          //récupération du graphentity
          GraphEntity edge1 = mapping1.get(stedge);
          if(path1.contains(edge1)){
            ok = true;
            break;
          }
        }
        if(ok){
          ok = false;
          for(STEntity stedge : this.edges){
            //récupération du graphentity
            GraphEntity edge2 = mapping2.get(stedge);
            if(path2.contains(edge2)){
              ok = true;
              break;
            }
          }
          if(ok){
            continue;
          }
          //on a un ppc qui passe par tous les arcs de edges à t1 mais pas à t2
          //on regarde comment le ppc diverge entre t1 et t2
          Set<STEntity> set1 = new HashSet<STEntity>();
          for(GraphEntity e: path1){
            set1.addAll(mappingReverse1.get(e));
          }
          Set<STEntity> set2 = new HashSet<STEntity>();
          for(GraphEntity e: path2){
            set2.addAll(mappingReverse2.get(e));
          }

          for(STEntity e2: set2){
            if(/*!set1.contains(e2) &&*/ !edges.contains(e2)){
              //on ne s'interesse pas aux arcs de edges
              result.put(e2, result.get(e2)+1);
            }
          }
        }

      }

      ppc1.reset(node11);
      ppc2.reset(node21);
    }

    return result;
  }

  public static void main(String agrs[]) throws XValuesOutOfOrderException, YValueOutOfRangeException{



    String inputStg ="/media/bcostes/Data/Benoit/these/analyses/TAG/TAG_v2/final_ind/tag_corrected_ind.tag";
    String date1 ="1870";
    String date2 ="1888";
    String outS = "RStMartin";
    STGraph stg = TAGIoManager.deserialize(inputStg);

    //  int id = 24384;
    // int id = 28789;
    List<Integer> ids = Arrays.asList(27107,26482,31342,31343,25453,25454,32296,32298,32299,25460,25461,25459);

    //FuzzyTemporalInterval t1 = new FuzzyTemporalInterval(new double[]{1784,1785,1789,1791},new double[]{0,1,1,0}, 4);
  //  FuzzyTemporalInterval t1 =new FuzzyTemporalInterval(new double[]{1848,1849,1849,1850},new double[]{0,1,1,0}, 4);
    //FuzzyTemporalInterval t1 = new FuzzyTemporalInterval(new double[]{1825,1827,1836,1839},new double[]{0,1,1,0}, 4);
    FuzzyTemporalInterval t1  = new FuzzyTemporalInterval(new double[]{1870,1871,1871,1872},new double[]{0,1,1,0}, 4);
    FuzzyTemporalInterval t2= new FuzzyTemporalInterval(new double[]{1887,1888,1888,1889},new double[]{0,1,1,0}, 4);
    
    
    STEntity edge = null;
    Set<STEntity> edges = new HashSet<STEntity>();
    for(Integer id: ids){
      for(STEntity e : stg.getEdges()){
        if(e.getId() == id){
          // edge = e;
          edges.add(e);
          break;
        }
      }
    }
    //    Set<STEntity> edges = new HashSet<STEntity>();
    //    //edges.add(edge);
    //    for(STEntity e: stg.getEdges()){
    //      if(e.existsAt(t1) && e.existsAt(t2)){
    //        edges.add(e);
    //      }
    //    }


    try{
      NegativeBetweennessCentrality ddd = new NegativeBetweennessCentrality(stg, edges, t1, t2);
      Map<STEntity, Double> result = ddd.getFirstNegativeBetweennessCentrality();
      IPopulation<IFeature> out = new Population<IFeature>();
      for(STEntity e: stg.getEdgesAt(t1)){
        IFeature f = new DefaultFeature(e.getGeometry().toGeoxGeometry());
        if(result.containsKey(e)){
          AttributeManager.addAttribute(f, "c_drop", result.get(e), "Double");
        }
        out.add(f);
      }
      ShapefileWriter.write(out, "/home/bcostes/Bureau/nbc_"+outS+"_"+date1+".shp");


      result = ddd.getSecondeNegativeBetweennessCentrality();
      out = new Population<IFeature>();
      for(STEntity e: stg.getEdgesAt(t2)){
        IFeature f = new DefaultFeature(e.getGeometry().toGeoxGeometry());
        if(result.containsKey(e)){
          AttributeManager.addAttribute(f, "c_drop", result.get(e), "Double");
        }
        out.add(f);
      }
      ShapefileWriter.write(out, "/home/bcostes/Bureau/nbc_"+outS+"_"+date2+".shp");
    }

    catch(Exception e){
      e.printStackTrace();
    }

  }

}
