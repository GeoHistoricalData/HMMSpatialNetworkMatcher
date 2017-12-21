package fr.ign.cogit.v2.indicators.dynamics.eventsfinder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import nrc.fuzzy.XValuesOutOfOrderException;
import nrc.fuzzy.YValueOutOfRangeException;
import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung.graph.util.Pair;
import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.feature.DefaultFeature;
import fr.ign.cogit.geoxygene.feature.Population;
import fr.ign.cogit.geoxygene.util.attribute.AttributeManager;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileWriter;
import fr.ign.cogit.morphogenesis.network.utils.ConnectedComponents;
import fr.ign.cogit.v2.indicators.ILocalIndicator;
import fr.ign.cogit.v2.indicators.local.BetweennessCentrality;
import fr.ign.cogit.v2.io.SnapshotIOManager;
import fr.ign.cogit.v2.io.TAGIoManager;
import fr.ign.cogit.v2.snapshot.GraphEntity;
import fr.ign.cogit.v2.snapshot.JungSnapshot;
import fr.ign.cogit.v2.strokes.Stroke;
import fr.ign.cogit.v2.tag.FuzzyTemporalInterval;
import fr.ign.cogit.v2.tag.STEntity;
import fr.ign.cogit.v2.tag.STGraph;

public class EventsFinder {

  private STGraph stag;
  private FuzzyTemporalInterval t1, t2;
  private Set<Stroke> constructions, destructions;
  private Set<EventsHypothesis> hypothesis;
  private Set<STEntity> edges;
  private JungSnapshot snapDestructions, snapConstructions;
  Map<STEntity, GraphEntity> mappingEdges1, mappingEdges2;


  public EventsFinder(STGraph stag, FuzzyTemporalInterval t1, FuzzyTemporalInterval t2){
    this.stag = stag;
    this.t1 = t1;
    this.t2 = t2;
    this.hypothesis = new HashSet<EventsHypothesis>();
    this.constructions = new HashSet<Stroke>();
    this.destructions = new HashSet<Stroke>();
  }

  public void findEvents(Set<STEntity> edges, ILocalIndicator ind){

    this.edges = edges;

    // les transformations
    Set<STEntity> ec = new HashSet<STEntity>();
    Set<STEntity> ed = new HashSet<STEntity>();
    for(STEntity e : this.stag.getEdges()){
      if(!e.existsAt(t1) && e.existsAt(t2)){
        ec.add(e);
      }
      else if(e.existsAt(t1) && !e.existsAt(t2)){
        ed.add(e);
      }
    }
    //oàn regroupe en fonction du nom
    Map<String, Set<STEntity>> mapc = new HashMap<String, Set<STEntity>>();
    Map<String, Set<STEntity>> mapd = new HashMap<String, Set<STEntity>>();
    for(STEntity e: ec){
      String name = e.getTAttributeByName("name").getValueAt(t2);
      if(name == null || name.equals("")){
        name = "null";
      }
      if(mapc.containsKey(name)){
        mapc.get(name).add(e);
      }
      else{
        Set<STEntity> s = new HashSet<STEntity>();
        s.add(e);
        mapc.put(name,s);
      }
    }
    for(STEntity e: ed){
      String name = e.getTAttributeByName("name").getValueAt(t1);
      if(name == null || name.equals("")){
        name = "null";
      }
      if(mapd.containsKey(name)){
        mapd.get(name).add(e);
      }
      else{
        Set<STEntity> s = new HashSet<STEntity>();
        s.add(e);
        mapd.put(name,s);
      }
    }
    //les strokes par nom
    for(String name : mapc.keySet()){
      Stroke s =new Stroke();
      s.getEntities().addAll(mapc.get(name));
      this.constructions.add(s);
    }
    for(String name : mapd.keySet()){
      Stroke s =new Stroke();
      s.getEntities().addAll(mapd.get(name));
      this.destructions.add(s);
    }

    System.out.println("Stroke construction : " + this.constructions.size() + " (/" + ec.size() + ")");
    System.out.println("Stroke destruction : " + this.destructions.size() + " (/" + ed.size() + ")");
    // ec.clear();
    //ec = null;
    // ed.clear();
    //  ed = null;
    mapc.clear();
    mapc = null;
    mapd.clear();
    mapd = null;


//    List<Stroke> l = new ArrayList<Stroke>(this.constructions);
//    l = l.subList(0, 50);
//    this.constructions = new HashSet<Stroke>();
//    this.constructions.addAll(l);
//    l = new ArrayList<Stroke>(this.destructions);
//    l = l.subList(0, 10);
//    this.destructions = new HashSet<Stroke>();
//    this.destructions.addAll(l);

    //création des structures locales
    this.initLocalGraphs();

    System.out.println("Local graphs initialized ... ");

    //    
    Map<Integer, GraphEntity> mappingId = new HashMap<Integer, GraphEntity>();
    for(GraphEntity node : snapConstructions.getVertices()){
      mappingId.put(node.getId(), node);
    }
    Map<Integer, GraphEntity> mappingId2 = new HashMap<Integer, GraphEntity>();
    for(GraphEntity node : snapDestructions.getVertices()){
      mappingId2.put(node.getId(), node);
    }


    //    //calculs des indicateurs initiaux et stockage des ppcs
    int iConstruction = 0;
    this.hypothesis = new HashSet<EventsHypothesis>();
    //on commencde par les constructions
    //on va stocker les ppc qui concernent les constructions
    Map<Integer, Set<int[]>> ppcConstructions = new HashMap<Integer, Set<int[]>>();
    Set<int[]> pathsEdges = new HashSet<int[]>();
    DijkstraShortestPath<GraphEntity, GraphEntity> ppc = new DijkstraShortestPath<GraphEntity, GraphEntity>(this.snapConstructions, this.snapConstructions.getEdgesWeights());

    Stack<GraphEntity> nodes = new Stack<GraphEntity>();
    nodes.addAll(this.snapConstructions.getVertices());
    while(!nodes.isEmpty()){
      System.out.println(nodes.size());
      GraphEntity node1 = nodes.pop();
      for(GraphEntity node2: nodes){
        List<GraphEntity> path = ppc.getPath(node1, node2);
        int[] t = {node1.getId(), node2.getId()};
        for(Stroke sconstruction: this.constructions){
          for(STEntity construction: sconstruction.getEntities()){
            GraphEntity geconstruction = this.mappingEdges1.get(construction);
            if(path.contains(geconstruction)){
              //on a un ppc qui passe par un arc construit
              if(ppcConstructions.containsKey(geconstruction.getId())){
                ppcConstructions.get(geconstruction.getId()).add(t);
              }
              else{
                Set<int[]> set = new  HashSet<int[]>();
                set.add(t);
                ppcConstructions.put(geconstruction.getId(), set);
              }
            }
          }
        }
        //passe-til par un des arcs de edges
        boolean ok = false;
        for(STEntity e: this.edges){
          GraphEntity ge = this.mappingEdges1.get(e);
          if(path.contains(ge)){
            ok = true;
            break;
          }
        }
        if(ok){
          iConstruction ++;
          pathsEdges.add(t);
        }
      }
      ppc.reset(node1);
    }


    ppc.reset();
    System.out.println("PPC calculation done for constructions... ");
    System.out.println("Calculating hypothesis impact ... ");

    int cpt =0;
    for(Stroke sconstruction: this.constructions){
      cpt++;


      System.out.print(cpt + "/" +this.constructions.size()+"...");
      EventsHypothesis hyp = new EventsHypothesis();
      hyp.getEventsConstructions().add(sconstruction);
      //récupération du graphe
      JungSnapshot snapHyp = this.generateTemporaryGraph(hyp);
      if(!this.goodHypothesis(snapHyp)){
        continue;
      }


      // on détermine quels sont les ppc concernés par l'hypothèse
      Set<int[]> ppcConstructionsHyp = new HashSet<int[]>();
      for(Stroke s : hyp.getEventsConstructions()){
        for(STEntity ste: s.getEntities()){
          // graphe,ntity
          GraphEntity gste = this.mappingEdges1.get(ste);
          //ppc concernés ? 
          if(ppcConstructions.containsKey(gste.getId())){
            ppcConstructionsHyp.addAll(ppcConstructions.get(gste.getId()));
          }
        }
      }


      //on va recalculer les ppc pour ces relation sur le graphe hypothèse
      int plusEdges =0, minusEdges = 0;
      DijkstraShortestPath<GraphEntity, GraphEntity> ppcLocal = new DijkstraShortestPath<GraphEntity, GraphEntity>(snapHyp,snapHyp.getEdgesWeights());
      ppcLocal.enableCaching(true);

      for(int[] relation : ppcConstructionsHyp){
        if(!snapHyp.containsVertex(mappingId.get(relation[0])) || ! snapHyp.containsVertex(mappingId.get(relation[1]))){
          continue;
        }
        List<GraphEntity> pathLocal = ppcLocal.getPath(mappingId.get(relation[0]), mappingId.get(relation[1]));
        //ce ppc passe t'il par un arc de edge ?
        boolean pass = false;
        for(STEntity e: this.edges){
          GraphEntity ge = this.mappingEdges1.get(e);
          if(pathLocal.contains(ge)){
            pass = true;
            break;
          }
        }
        if(pass){
          //y passait-il avant ? 
          if(pathsEdges.contains(relation)){
            //rien de nouveau
          }
          else{
            //il y passait pas avant !
            plusEdges ++;
          }
        }
        else{
          //ne passe pas par un arc de edges.
          // mais avant ?
          if(pathsEdges.contains(relation)){
            //il y passait !
            minusEdges ++;
          }
          else{
            //rien de nouveau
          }
        }
      }
      //calcul du nouveal indicareur
      int iConstructionLocal = iConstruction + plusEdges - minusEdges;
      hyp.setScore(Math.abs(iConstructionLocal - iConstruction));
      // System.out.println("score : " +Math.abs(iConstructionLocal - iConstruction));
      hypothesis.add(hyp);

    }

    System.out.println("Constructions hypothesis done.");
    ppcConstructions.clear();
    ppc.reset();
    pathsEdges.clear();

    //meme travail sur les destructions


    //calculs des indicateurs initiaux et stockage des ppcs
    int iDestruction = 0;
    //on va stocker les ppc qui concernent les destructions
    Map<Integer, Set<int[]>> ppcDestructions= new HashMap<Integer, Set<int[]>>();
    Set<int[]> pathsEdges2 = new HashSet<int[]>();
    ppc = new DijkstraShortestPath<GraphEntity, GraphEntity>(this.snapDestructions, this.snapDestructions.getEdgesWeights());
    nodes = new Stack<GraphEntity>();
    nodes.addAll(this.snapDestructions.getVertices());
    while(!nodes.isEmpty()){
      System.out.println(nodes.size());
      GraphEntity node1 = nodes.pop();
      for(GraphEntity node2: nodes){
        List<GraphEntity> path = ppc.getPath(node1, node2);
        int[] t = {node1.getId(), node2.getId()};
        for(Stroke sdestruction: this.destructions){
          for(STEntity destruction: sdestruction.getEntities()){
            GraphEntity gedestruction = this.mappingEdges2.get(destruction);
            if(path.contains(gedestruction)){
              //on a un ppc qui passe par un arc construit
              if(ppcDestructions.containsKey(gedestruction.getId())){
                ppcDestructions.get(gedestruction.getId()).add(t);
              }
              else{
                Set<int[]> set = new  HashSet<int[]>();
                set.add(t);
                ppcDestructions.put(gedestruction.getId(), set);
              }
            }
          }
        }
        //passe-til par un des arcs de edges
        boolean ok = false;
        for(STEntity e: this.edges){
          GraphEntity ge = this.mappingEdges2.get(e);
          if(path.contains(ge)){
            ok = true;
            break;
          }
        }
        if(ok){
          iDestruction ++;
          pathsEdges2.add(t);
        }
      }
      ppc.reset(node1);
    }


    ppc.reset();
    System.out.println("PPC calculation done for destructions... ");
    System.out.println("Calculating hypothesis impact ... ");

    cpt =0;
    for(Stroke sdestructions: this.destructions){
      cpt++;
      System.out.print(cpt + "/" +this.destructions.size()+"...");
      EventsHypothesis hyp = new EventsHypothesis();
      hyp.getEventsDestructions().add(sdestructions);
      //récupération du graphe
      JungSnapshot snapHyp = this.generateTemporaryGraph(hyp);
      if(!this.goodHypothesis(snapHyp)){
        continue;
      }


      // on détermine quels sont les ppc concerns par l'hypothèse
      Set<int[]> ppcDestructionsHyp = new HashSet<int[]>();
      for(Stroke s : hyp.getEventsDestructions()){
        for(STEntity ste: s.getEntities()){
          // graphe,ntity
          GraphEntity gste = this.mappingEdges2.get(ste);
          //ppc concernés ? 
          if(ppcDestructions.containsKey(gste.getId())){
            ppcDestructionsHyp.addAll(ppcDestructions.get(gste.getId()));
          }
        }
      }



      //on va recalculer les ppc pour ces relation sur le graphe hypothèse
      int plusEdges =0, minusEdges = 0;
      DijkstraShortestPath<GraphEntity, GraphEntity> ppcLocal = new DijkstraShortestPath<GraphEntity, GraphEntity>(snapHyp,snapHyp.getEdgesWeights());
      ppcLocal.enableCaching(true);


      for(int[] relation : ppcDestructionsHyp){
        if(!snapHyp.containsVertex(mappingId2.get(relation[0])) || ! snapHyp.containsVertex(mappingId2.get(relation[1]))){
          continue;
        }

        List<GraphEntity> pathLocal = ppcLocal.getPath(mappingId2.get(relation[0]), mappingId2.get(relation[1]));
        //ce ppc passe t'il par un arc de edge ?
        boolean pass = false;
        for(STEntity e: this.edges){
          GraphEntity ge = this.mappingEdges2.get(e);
          if(pathLocal.contains(ge)){
            pass = true;
            break;
          }
        }
        if(pass){
          //y passait-il avant ? 
          if(pathsEdges2.contains(relation)){
            //rien de nouveau
          }
          else{
            //il y passait pas avant !
            plusEdges ++;
          }
        }
        else{
          //ne passe pas par un arc de edges.
          // mais avant ?
          if(pathsEdges2.contains(relation)){
            //il y passait !
            minusEdges ++;
          }
          else{
            //rien de nouveau
          }
        }
      }
      //calcul du nouveal indicareur
      int iDestructionLocal = iDestruction + plusEdges - minusEdges;
      hyp.setScore(Math.abs(iDestructionLocal - iDestruction));
      // System.out.println("score : " +Math.abs(iDestructionLocal - iDestruction));

      hypothesis.add(hyp);

    }

  }

  private void initLocalGraphs(){

    //snapshot des constructions
    this.snapConstructions = new JungSnapshot();
    this.mappingEdges1 =  new HashMap<STEntity, GraphEntity>();
    JungSnapshot snapt2 = this.stag.getSnapshotAt(this.t2);
    for(GraphEntity ge: snapt2.getEdges()){
      snapConstructions.addEdge(ge, snapt2.getEndpoints(ge));
    }
    for(GraphEntity ge: snapConstructions.getEdges()){
      for(Stroke s:  this.constructions){
        for(STEntity e :s.getEntities()){
          if(this.stag.getMappingSnapshotEdgesId().get(ge.getId()).contains(e.getId())){ 
            this.mappingEdges1.put(e, ge);
            continue;
          }
        }
        for(STEntity e : this.edges){
          if(this.stag.getMappingSnapshotEdgesId().get(ge.getId()).contains(e.getId())){ 
            this.mappingEdges1.put(e, ge);
            continue;
          }
        }
      }
    }

    this.snapConstructions.setEdgesWeights(snapt2.getEdgesWeights());


    //snapshot des destructions
    this.snapDestructions = new JungSnapshot();
    this.mappingEdges2 =  new HashMap<STEntity, GraphEntity>();
    JungSnapshot snapt1 = this.stag.getSnapshotAt(this.t1);
    for(GraphEntity ge: snapt1.getEdges()){
      snapDestructions.addEdge(ge, snapt1.getEndpoints(ge));
    }
    for(GraphEntity ge: snapDestructions.getEdges()){
      for(Stroke s:  this.destructions){
        for(STEntity e :s.getEntities()){
          if(this.stag.getMappingSnapshotEdgesId().get(ge.getId()).contains(e.getId())){ 
            this.mappingEdges2.put(e, ge);
            continue;
          }
        }
        for(STEntity e : this.edges){
          if(this.stag.getMappingSnapshotEdgesId().get(ge.getId()).contains(e.getId())){ 
            this.mappingEdges2.put(e, ge);
            continue;
          }
        }
      }
    }
    this.snapDestructions.setEdgesWeights(snapt1.getEdgesWeights());


  }

  /**
   * Génère un graphe temporaire sur le quel on a ajouté / détruit les arcs de l'hypothèse
    @param hyp
   * @return
   */
  private JungSnapshot generateTemporaryGraph(EventsHypothesis hyp){
    JungSnapshot snapHyp = new JungSnapshot();
    if(hyp.getEventsConstructions().isEmpty()){
      //graphe pour destructions
      for(GraphEntity ge: this.snapDestructions.getEdges()){
        snapHyp.addEdge(ge, snapDestructions.getEndpoints(ge));
      }
      snapHyp.setEdgesWeights(snapDestructions.getEdgesWeights());
      //suppression des destructions
      for(Stroke s : hyp.getEventsDestructions()){
        for(STEntity e:  s.getEntities()){
          //graphEntity
          GraphEntity ge = this.mappingEdges2.get(e);
          snapHyp.removeEdge(ge);
        }
      }
      for(GraphEntity v: new HashSet<GraphEntity>(snapHyp.getVertices())){
        if(snapHyp.getDegre(v) == 0){
          snapHyp.removeVertex(v);
        }
      }
    }
    else{
      //graphe ppour constructions
      for(GraphEntity ge: this.snapConstructions.getEdges()){
        snapHyp.addEdge(ge, snapConstructions.getEndpoints(ge));
      }
      snapHyp.setEdgesWeights(snapConstructions.getEdgesWeights());

      //suppression des construction
      for(Stroke s : hyp.getEventsConstructions()){
        for(STEntity e:  s.getEntities()){
          //graphEntity
          GraphEntity ge = this.mappingEdges1.get(e);
          snapHyp.removeEdge(ge);
        }
      }
      for(GraphEntity v: new HashSet<GraphEntity>(snapHyp.getVertices())){
        if(snapHyp.getDegre(v) == 0){
          snapHyp.removeVertex(v);
        }
      }
    }
    return snapHyp;
  }

  public static List<int[]> combinaisons(int size){
    boolean[] elements = new boolean[size];
    //liste construite par la recursion  
    int[] liste = new int[size];
    List<int[]> result = new ArrayList<int[]>();
    permut(0, size, elements, liste, result);
    return result;
  }

  public static void permut(int rank, int size, boolean[]elements, int[]liste,List<int[]> result)  {

    if (rank>=size) {
      // la liste est construite -> FIN 
      System.out.println(liste[0]+
          " "+liste[1]+ " "+ liste[2]);
      result.add(Arrays.copyOf(liste, size));
      return;
    }

    // parcours les elements
    for(int i=0;i<size;i++) {
      // deja utilisé -> suivant
      if (elements[i]) continue;
      // sinon on choisi cet element
      elements[i]=true;
      // on l'ajoute a la liste
      liste[rank]=i;
      // on construit le reste de la liste par recursion
      permut(rank+1, size, elements, liste, result);
      // on libere cet element
      elements[i]=false;
    }

  }

  /**
   * Vérifie si une hypothèse est réalisable
   * @param hyp
   * @return
   */
  private boolean goodHypothesis(JungSnapshot snap){
    //on va vérifier que la suppression et l'ajout d'arc n'entraine pas l'augmentation du nombre
    // de compsantes connexes du graphe
    ConnectedComponents<GraphEntity, GraphEntity> cc = new ConnectedComponents<GraphEntity, GraphEntity>(snap);
    if(cc.buildConnectedComponents().size() > 1){
      return false;
    }
    return true;
  }

  public static void main(String agrs[]) throws XValuesOutOfOrderException, YValueOutOfRangeException{

    String inputStg ="/media/bcostes/Data/Benoit/these/analyses/TAG/TAG_v2/final_ind/tag_corrected_ind.tag";
    STGraph stg = TAGIoManager.deserialize(inputStg);





    //  int id = 24384;
    // int id = 28789;
    List<Integer> ids = Arrays.asList(27497,27703,25136,30076,28231,28212,28213,28216,28207,28206,28204,32172,32173,30903,30902,32241,32240,32238);

    //FuzzyTemporalInterval t1 = new FuzzyTemporalInterval(new double[]{1784,1785,1789,1791},new double[]{0,1,1,0}, 4);
    FuzzyTemporalInterval t1 =new FuzzyTemporalInterval(new double[]{1848,1849,1849,1850},new double[]{0,1,1,0}, 4);
    //FuzzyTemporalInterval t1 = new FuzzyTemporalInterval(new double[]{1825,1827,1836,1839},new double[]{0,1,1,0}, 4);
    FuzzyTemporalInterval t2  = new FuzzyTemporalInterval(new double[]{1870,1871,1871,1872},new double[]{0,1,1,0}, 4);




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

    EventsFinder ddd = new EventsFinder(stg, t1, t2);
    ddd.findEvents(edges, new BetweennessCentrality());


    List<EventsHypothesis> hyp = new ArrayList<EventsHypothesis>(ddd.getHypothesis());
    Collections.sort(hyp);
    Collections.reverse(hyp);
    EventsHypothesis hyp1 = hyp.get(0);
    System.out.println(hyp1.toString());


    IPopulation<IFeature> out = new Population<IFeature>();
    int cpt= 0;
    for(EventsHypothesis hypp: hyp){
      cpt++;
      double score = hypp.getScore();
      for(Stroke s: hypp.getEventsConstructions()){
        for(STEntity e : s.getEntities()){
          IFeature f = new DefaultFeature(e.getGeometry().toGeoxGeometry());
          AttributeManager.addAttribute(f, "hypothesis", cpt, "Integer");
          AttributeManager.addAttribute(f, "score", score, "Double");
          out.add(f);
        }
      }
      for(Stroke s: hypp.getEventsDestructions()){
        for(STEntity e : s.getEntities()){
          IFeature f = new DefaultFeature(e.getGeometry().toGeoxGeometry());
          AttributeManager.addAttribute(f, "hypothesis", cpt, "Integer");
          AttributeManager.addAttribute(f, "score", score, "Double");
          out.add(f);
        }
      }
    }

    ShapefileWriter.write(out, "/home/bcostes/Bureau/hypothesis.shp");


  }

  public Set<EventsHypothesis> getHypothesis() {
    return hypothesis;
  }

  public void setHypothesis(Set<EventsHypothesis> hypothesis) {
    this.hypothesis = hypothesis;
  }

}
