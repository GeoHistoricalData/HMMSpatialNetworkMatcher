package fr.ign.cogit.v2.indicators.sensibility.grid;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.collections15.Factory;
import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.algorithms.generators.random.ErdosRenyiGenerator;
import edu.uci.ics.jung.algorithms.shortestpath.DistanceStatistics;
import edu.uci.ics.jung.graph.UndirectedGraph;
import edu.uci.ics.jung.graph.UndirectedSparseMultigraph;
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPositionList;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ILineString;
import fr.ign.cogit.geoxygene.contrib.cartetopo.Arc;
import fr.ign.cogit.geoxygene.contrib.cartetopo.CarteTopo;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPosition;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPositionList;
import fr.ign.cogit.geoxygene.spatial.coordgeom.GM_LineString;
import fr.ign.cogit.v2.io.SnapshotIOManager;
import fr.ign.cogit.v2.snapshot.SnapshotGraph;
import fr.ign.cogit.v2.weightings.LengthEdgeWeighting;

public class RegularGridGenerator {

  /**
   * création d'un jung graph, sous forme d'une grille régulière n*m, avec un pas
   * @param n
   * @param m
   * @param thresoldx
   * @param thresoldy
   * @param p0 point supérieur gauche
   * @return
   */
  public static SnapshotGraph generateRegularGirdNetwork(int n, int m, double thresoldx, double thresoldy, IDirectPosition p0){
    if(n<2 || m<2){
      return null;
    }
    if(p0 == null){
      p0 = new DirectPosition(0,0);
    }
    //création des linstring
    Set<ILineString> lines = new HashSet<ILineString>();
    double x0  =p0.getX();
    double y0 = p0.getY();
    //on fait les lignes verticales d'abord
    for(int j=0; j< m; j++){
      // point ligne 0
      IDirectPosition p1 = new DirectPosition(x0 + j * thresoldx, y0);
      //point ligne m-1
      IDirectPosition p2 = new DirectPosition(x0 + j * thresoldx, y0 + (m-1) * thresoldy);
      //ligne
      IDirectPositionList l = new DirectPositionList();
      l.add(p1);
      l.add(p2);
      lines.add(new GM_LineString(l));
    }
    //puis les lignes horizontales
    for(int i=0; i< n; i++){
      // point colonne 0
      IDirectPosition p1 = new DirectPosition(x0 , y0 + i * thresoldy);
      //point ligne m-1
      IDirectPosition p2 = new DirectPosition(x0 + (n-1) * thresoldx, y0 + i * thresoldy);
      //ligne
      IDirectPositionList l = new DirectPositionList();
      l.add(p1);
      l.add(p2);
      lines.add(new GM_LineString(l));
    }
    //les arcs diagonaux
    for(int i=0; i< n-1; i++){
      for(int j=0; j< m-1; j++){
        IDirectPosition p1 = new DirectPosition(x0 + i*thresoldx, y0+j*thresoldy);
        IDirectPosition p2 = new DirectPosition(x0 + (i+1)*thresoldx, y0+(j+1)*thresoldy);
        //ligne
        IDirectPositionList l = new DirectPositionList();
        l.add(p1);
        l.add(p2);
        lines.add(new GM_LineString(l));
      }
    }
    //création de la carte topo
    CarteTopo map = new CarteTopo("void");
    IPopulation<Arc> arcs = map.getPopArcs();
    for(ILineString l  :lines){
      Arc a = arcs.nouvelElement();
      a.setGeom(l);
    }
    map.creeTopologieArcsNoeuds(0);
    map.creeNoeudsManquants(0);
    map.rendPlanaire(0);
    return SnapshotIOManager.topo2Snapshot(map, new LengthEdgeWeighting(), null);
  }



  public static void main(String[] args) {
    Factory<Integer> vertexFactory = new Factory<Integer>() { // My vertex factory
      int n;
      public Integer create() {
        return n++;
      }
    };
    Factory<Integer> edgeFactory = new Factory<Integer>() { // My edge factory
      int n;
      public Integer create() {
        return n++;
      }
    };
    Factory<UndirectedGraph<Integer, Integer>> graphFactory = new Factory<UndirectedGraph<Integer,Integer>>() {
      @Override
      public UndirectedGraph<Integer, Integer> create() {
        // TODO Auto-generated method stub
        return new UndirectedSparseMultigraph<Integer, Integer>();
      }
    };
    
  String s ="";
  for(int n = 10; n<=10000; n+=100){
    ErdosRenyiGenerator<Integer, Integer> e = new ErdosRenyiGenerator<Integer, Integer>(graphFactory,
        vertexFactory, edgeFactory, n, 0.5);
    UndirectedSparseMultigraph<Integer, Integer> ss = (UndirectedSparseMultigraph) e.create();
    double d =0;
    Transformer<Integer, Double> dist = DistanceStatistics.averageDistances(ss);
    for(Integer vertex : ss.getVertices()){
       d += dist.transform(vertex);
    }
    d/=((double)ss.getVertexCount());
    double degree=0;
    for(Integer vertex : ss.getVertices()){
      degree += ss.getIncidentEdges(vertex).size();
   }
    degree/=((double)ss.getVertexCount());

    System.out.println(ss.getVertexCount()+" "+ ss.getEdgeCount());
    s += ss.getVertexCount()+";"+d+";"+degree+"\n";
  }
  try {
    FileWriter fw = new FileWriter("/home/bcostes/Bureau/test_grille/random.txt");
    BufferedWriter bw = new BufferedWriter(fw);
    bw.write(s);
    bw.flush();
    bw.close();
  } catch (IOException e) {
    // TODO Auto-generated catch block
    e.printStackTrace();
  }
    
  

    // SnapshotGraph snap = RegularGridGenerator.generateRegularGirdNetwork(50, 50, 1, 1, null);
    // SnapshotIOManager.snapshot2Shp(snap, "/home/bcostes/Bureau/test_grille/test.shp", SnapshotIOManager.NODE_AND_EDGE);

//    String s ="";
//    for(int n = 2; n<=20; n+=1){
//      SnapshotGraph snap = RegularGridGenerator.generateRegularGirdNetwork(n, n, 1, 1, null);
//      System.out.println(n);
//
//      double d = snap.calculateGraphGlobalIndicator(new AveragePathLength());
//      s += snap.getVertexCount()+";"+d+"\n";
//    }
//    try {
//      FileWriter fw = new FileWriter("/home/bcostes/Bureau/test_grille/grid.txt");
//      BufferedWriter bw = new BufferedWriter(fw);
//      bw.write(s);
//      bw.flush();
//      bw.close();
//    } catch (IOException e) {
//      // TODO Auto-generated catch block
//      e.printStackTrace();
//    }

  }

}
