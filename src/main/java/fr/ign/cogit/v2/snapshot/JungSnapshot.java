package fr.ign.cogit.v2.snapshot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.apache.commons.collections15.Transformer;
import org.apache.log4j.Logger;

import cern.colt.matrix.impl.SparseDoubleMatrix2D;
import edu.uci.ics.jung.algorithms.filters.Filter;
import edu.uci.ics.jung.algorithms.filters.KNeighborhoodFilter;
import edu.uci.ics.jung.algorithms.filters.KNeighborhoodFilter.EdgeType;
import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung.algorithms.shortestpath.MinimumSpanningForest2;
import edu.uci.ics.jung.algorithms.shortestpath.UnweightedShortestPath;
import edu.uci.ics.jung.graph.DelegateForest;
import edu.uci.ics.jung.graph.DelegateTree;
import edu.uci.ics.jung.graph.Forest;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseMultigraph;
import fr.ign.cogit.v2.indicators.IGlobalIndicator;
import fr.ign.cogit.v2.indicators.ILocalIndicator;
import fr.ign.cogit.v2.indicators.IOtherGeometricalIndicator;
import fr.ign.cogit.v2.indicators.ShortestPath;
import fr.ign.cogit.v2.utils.Statistics;

/**
 * Classe étendant UndirectedSparseMultigraph
 * Modèle d'un multigraphe non orienté
 * les arcs et les sommets sont des GraphEntity
 * @author bcostes
 *
 */
public class JungSnapshot extends
UndirectedSparseMultigraph<GraphEntity, GraphEntity> {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * Valuation des arcs (longueurs, temps de parcours, etC.)
     */
    private Transformer<GraphEntity, Double> edgesWeights = null;
    /**
     * Poids des sommets
     */
    private Transformer<GraphEntity, Double> nodesWeights = null;

    /**
     * stockage des ppc: cf
     * @see [Gleyze, 2005]
     */
    private double[] distances; // les distances
    //  private int[] nbrsp; // les nombres de shortest path
    // private int[] previousEdges; // indexes des arcs précédents selon les ppc
    // depuis un sommet vers un autre sommet
    // private int[] previousNodes; // indexes des sommet précédents selon les
    // ppc
    // depuis un sommet vers un autre sommet
     private Map<GraphEntity, Integer> nodesIndexes; // indexes des sommets
     private Map<GraphEntity, Integer> edgesIndexes; // indexes des arcs
     private Map<Integer, GraphEntity> indexesNodes; // indexes des sommets
     private Map<Integer, GraphEntity> indexesEdges; // indexes des arcs
    // private int dmax = -1; // degré max

    private DijkstraShortestPath<GraphEntity, GraphEntity> sp;

    /**
     * Logger
     */
    private final Logger logger = Logger.getLogger(JungSnapshot.class);

    // **************************************************************
    // ******************* Constructeur ***************************
    // **************************************************************

    public JungSnapshot() {
        super();
    }

    // **************************************************************
    // ******************* Shortest Paths ***************************
    // **************************************************************

    /**
     * Poids des arcs
     */
    public Transformer<GraphEntity, Double> getEdgesWeights() {
        return edgesWeights;
    }

    public void setEdgesWeights(Transformer<GraphEntity, Double> edgesWeights) {
        this.edgesWeights = edgesWeights;
        // on doit reset les ppc car on change la pondération des arcs
        this.resetShortestPaths();
    }

    public void setNodesWeights(Transformer<GraphEntity, Double> nodesWeights) {
        this.nodesWeights = nodesWeights;
    }

    /**
     * Poids des sommets
     * @return
     */
    public Transformer<GraphEntity, Double> getNodesWeights() {
        return nodesWeights;
    }

    /**
     * Donne le sommet d'index index dans la table de mapping
     * @param index
     * @return
     */
        public GraphEntity getNodeByIndex(int index) {
            if (this.indexesNodes == null) {
                this.indexesNodes = new HashMap<Integer, GraphEntity>();
                int cpt = 0;
                for (GraphEntity v : this.nodesIndexes.keySet()) {
                    this.indexesNodes.put(this.nodesIndexes.get(v), v);
                    cpt++;
                }
            }
            return this.indexesNodes.get(index);
        }

    /**
     * Donne l'arc d'index index dans la table de mapping
     * @param index
     * @return
     */
        public GraphEntity getEdgeByIndex(int index) {
            if (this.indexesEdges == null) {
                this.indexesEdges = new HashMap<Integer, GraphEntity>();
                int cpt = 0;
                for (GraphEntity e : this.edgesIndexes.keySet()) {
                    this.indexesEdges.put(this.edgesIndexes.get(e), e);
                    cpt++;
                }
            }
            return this.indexesEdges.get(index);
        }

    /**
     * Renvoie l'index du sommet vertex dans la table de mapping
     * @param vertex
     * @return
     */
        public int getNodeIndex(GraphEntity vertex) {
            if (this.nodesIndexes == null) {
                this.nodesIndexes = new HashMap<GraphEntity, Integer>();
                int cpt = 0;
                for (GraphEntity v : this.getVertices()) {
                    this.nodesIndexes.put(v, cpt);
                    cpt++;
                }
            }
            return this.nodesIndexes.get(vertex);
        }

    /**
     * Renvoie l'index de l'arc edge dans la table de mapping
     * @param edge
     * @return
     */
        public int getEdgeIndex(GraphEntity edge) {
            if (this.edgesIndexes == null) {
                this.edgesIndexes = new HashMap<GraphEntity, Integer>();
                int cpt = 0;
                for (GraphEntity e : this.getEdges()) {
                    this.edgesIndexes.put(e, cpt);
                    cpt++;
                }
            }
            return this.edgesIndexes.get(edge);
        }

    /**
     * Distance ppc entre les sommets d'index index1 et index2 dans la table de
     * mapping
     * @param index1
     * @param index2
     * @return
     */
        public double getDistance(int index1, int index2) {
            if (this.distances == null) {
                return -1;
            }
            if (index1 == index2) {
                return 0.;
            }
            if (index1 > index2) {
                return this.getDistance(index2, index1);
            }
    
            int id = index1 * (2 * this.getVertexCount() - index1 - 1) / 2
                    + (index2 - index1 - 1);
            return this.distances[id];
        }

    /**
     * Distance ppc entre les sommets v1 et v2
     * @param v1
     * @param v2
     * @return
     */
    public double getDistance(GraphEntity v1, GraphEntity v2) {
                double d = this.getDistance(this.getNodeIndex(v1), this.getNodeIndex(v2));
                return d;
        //return this.sp.getDistance(v1, v2).doubleValue();
    }
    
//    public DijkstraShortestPath<GraphEntity, GraphEntity> getSP(){
//        return this.sp;
//    }

    /**
     * Nombre de ppc entre les somets d'index index1 et index2 dans la table de
     * mapping
     * @param index1
     * @param index2
     * @return
     */
//        public int getNumberShortestPath(int index1, int index2) {
//            if (this.nbrsp == null) {
//                return -1;
//            }
//            if (index1 == index2) {
//                return 1;
//            }
//            if (index1 > index2) {
//                return this.getNumberShortestPath(index2, index1);
//            }
//    
//            int id = index1 * (2 * this.getVertexCount() - index1 - 1) / 2
//                    + (index2 - index1 - 1);
//            return this.nbrsp[id];
//        }

    /**
     * Nombre de ppc entre les sommets v1 et v2
     * @param v1
     * @param v2
     * @return
     */
//        public int getNumberShortestPath(GraphEntity v1, GraphEntity v2) {
//            return this.getNumberShortestPath(this.getNodeIndex(v1),
//                    this.getNodeIndex(v2));
//        }

    /**
     * Index dans la table de mapping des sommets précédents du sommet d'index
     * index 1 dans les ppc entre index1 et index 2
     * @param index1
     * @param index2
     * @return
     */
    //    public int[] getPreviousNodesIndexes(int index1, int index2) {
    //        if (this.previousNodes == null) {
    //            return null;
    //        }
    //        if (index1 == index2) {
    //            return new int[0];
    //        }
    //        int dmax = this.getDegreMax();
    //        int n = this.getVertexCount();
    //        int[] resultTmp = new int[dmax];
    //        int id = index1 * dmax * n + dmax * index2;
    //        //id *= dmax;
    //        int cpt = 0;
    //        for (int i = 0; i < resultTmp.length; i++) {
    //            resultTmp[i] = this.previousNodes[id + i];
    //            if (resultTmp[i] != -1) {
    //                cpt++;
    //            }
    //        }
    //        int[] result = new int[cpt];
    //        for (int i = 0; i < resultTmp.length; i++) {
    //            if (resultTmp[i] != -1) {
    //                result[i] = resultTmp[i];
    //            }
    //        }
    //        return result;
    //    }

    /**
     * Index dans la table de mapping des arcs précédents du sommet d'index index
     * 1 dans les ppc entre index1 et index 2
     * @param index1
     * @param index2
     * @return
     */
    //    public int[] getPreviousEdgesIndexes(int index1, int index2) {
    //        if (this.previousEdges == null) {
    //            return null;
    //        }
    //        if (index1 == index2) {
    //            return null;
    //        }
    //        int dmax = this.getDegreMax();
    //        int n = this.getVertexCount();
    //        int[] resultTmp = new int[dmax];
    //        int id = index1 * dmax * n + dmax * index2;
    //        int cpt = 0;
    //        for (int i = 0; i < resultTmp.length; i++) {
    //            resultTmp[i] = this.previousEdges[id + i];
    //            if (resultTmp[i] != -1) {
    //                cpt++;
    //            }
    //        }
    //        int[] result = new int[cpt];
    //        for (int i = 0; i < resultTmp.length; i++) {
    //            if (resultTmp[i] != -1) {
    //                result[i] = resultTmp[i];
    //            }
    //        }
    //        return result;
    //    }
    //
    //    public List<GraphEntity> getPreviousNodes(GraphEntity v1, GraphEntity v2) {
    //        int index1 = this.getNodeIndex(v1);
    //        int index2 = this.getNodeIndex(v2);
    //        int id[] = this.getPreviousNodesIndexes(index1, index2);
    //        if (id == null) {
    //            return null;
    //        }
    //        List<GraphEntity> result = new ArrayList<GraphEntity>();
    //        for (int i = 0; i < id.length; i++) {
    //            result.add(this.getNodeByIndex(id[i]));
    //        }
    //        return result;
    //    }
    //
    //    public List<GraphEntity> getPreviousEdges(GraphEntity v1, GraphEntity v2) {
    //        int index1 = this.getNodeIndex(v1);
    //        int index2 = this.getNodeIndex(v2);
    //
    //        int id[] = this.getPreviousEdgesIndexes(index1, index2);
    //
    //        if (id == null) {
    //            return null;
    //        }
    //        List<GraphEntity> result = new ArrayList<GraphEntity>();
    //        for (int i = 0; i < id.length; i++) {
    //            result.add(this.getEdgeByIndex(id[i]));
    //        }
    //        return result;
    //    }

    /**
     * Donne la (les) successions d'arcs entre v1 et v2 selon les ppc
     * @param v1
     * @param v2
     * @return
     */
    //    public List<List<GraphEntity>> getShortestPaths(GraphEntity v1, GraphEntity v2) {
    //        if (v1.equals(v2)) {
    //            return null;
    //        }
    //        List<List<GraphEntity>> result = new ArrayList<List<GraphEntity>>();
    //        List<GraphEntity> previous = this.getPreviousEdges(v1, v2);
    //        for (GraphEntity e : previous) {
    //            List<GraphEntity> p = new ArrayList<GraphEntity>();
    //            p.add(e);
    //            this.spRec(v1, this.getOpposite(v2, e), p, result);
    //        }
    //        return result;
    //    }


//    public List<GraphEntity> getShortestPaths(GraphEntity v1, GraphEntity v2) {
//        if (v1.equals(v2)) {
//            return null;
//        }
//        return this.sp.getPath(v1, v2);
//    }

    /**
     * Méthode récursive permettant de reconstituer les ppc depuis les tables
     * previousEdges et previousNodes
     * @param v1
     * @param opposite
     * @param p
     * @param result
     */
    //    private void spRec(GraphEntity v1, GraphEntity opposite, List<GraphEntity> p,
    //            List<List<GraphEntity>> result) {
    //
    //        if (v1.equals(opposite)) {
    //            result.add(p);
    //            return;
    //        }
    //        List<GraphEntity> previous = this.getPreviousEdges(v1, opposite);
    //        for (GraphEntity e : previous) {
    //            List<GraphEntity> pp = new ArrayList<GraphEntity>();
    //            pp.addAll(p);
    //            pp.add(e);
    //            this.spRec(v1, this.getOpposite(opposite, e), pp, result);
    //        }
    //    }
    //
    //    public void setDistance(double[] distances) {
    //        this.distances = distances;
    //    }

    /**
     * met en cache les calculs des ppc
     */
    public void cacheShortestPaths() {
                if (this.distances != null) {
                    if (logger.isInfoEnabled()) {
                        logger.info("Shortest distances already cached.");
                    }
                    return;
                }
                if (logger.isInfoEnabled()) {
                    logger.info("Caching shortest distances ...");
                }
        
                // calcul des structurs des ppc par l'algo de Brandes
                ShortestPath sp = new ShortestPath();
                sp.calculate(this);
                this.distances = sp.getDistances();
               // this.nbrsp = sp.getNbrsp();
               // this.previousEdges = sp.getPreviousEdges();
               // this.previousNodes = sp.getPreviousNodes();
                
                sp.reset();
                sp = null;

//        if (this.sp != null) {
//            if (logger.isInfoEnabled()) {
//                logger.info("Shortest distances already cached.");
//            }
//            return;
//        }
//        if (logger.isInfoEnabled()) {
//            logger.info("Caching shortest distances ...");
//        }
//
//        this.sp = new DijkstraShortestPath<GraphEntity, GraphEntity>(this, this.getEdgesWeights());
    }

    public void resetShortestPaths() {
                this.distances = null;
              //  this.nbrsp = null;
               // this.previousEdges = null;
               // this.previousNodes = null;
//        if(this.sp != null){
//            this.sp.reset();
//            this.sp = null;
//            System.gc();
//        }
    }

        public double[] getDistances() {
            return distances;
        }

    // **************************************************************
    // ***************** Indicateurs globaux ************************
    // **************************************************************

    public double calculateGraphGlobalIndicator(IGlobalIndicator indicator) {
        logger.info("Calculating graph " + indicator.getName() + " ... ");
        return indicator.calculate(this);
    }

    // **************************************************************
    // ****************** Indicateurs locaux ************************
    // **************************************************************

    public Map<GraphEntity, Double> calculateNodeCentrality(
            ILocalIndicator indicator, SnapshotGraph.NORMALIZERS normalize) {
        if (logger.isInfoEnabled()) {
            logger.info("Calculating nodes " + indicator.getName() + " ... ");
        }
        if (normalize.equals(SnapshotGraph.NORMALIZERS.MINMAX)) {
            Statistics<GraphEntity> stat = new Statistics<GraphEntity>();
            return stat.normalize(indicator.calculateNodeCentrality(this,false));
        } else if(normalize.equals(SnapshotGraph.NORMALIZERS.CONVENTIONAL)){
            return indicator.calculateNodeCentrality(this, true);
        }
        else if(normalize.equals(SnapshotGraph.NORMALIZERS.NONE)){
            return indicator.calculateNodeCentrality(this, false);
        }
        else{
            return null;
        }
        // return indicator.calculateNodeCentrality(this);

    }

    public Map<GraphEntity, Double> calculateEdgeCentrality(
            ILocalIndicator indicator, SnapshotGraph.NORMALIZERS normalize) {
        if (logger.isInfoEnabled()) {
            logger.info("Calculating edges " + indicator.getName() + " ... ");
        }
        if (normalize.equals(SnapshotGraph.NORMALIZERS.MINMAX)) {
            Statistics<GraphEntity> stat = new Statistics<GraphEntity>();
            return stat.normalize(indicator.calculateEdgeCentrality(this,false));
        } else if(normalize.equals(SnapshotGraph.NORMALIZERS.CONVENTIONAL)){
            return indicator.calculateEdgeCentrality(this, true);
        }
        else if(normalize.equals(SnapshotGraph.NORMALIZERS.NONE)){
            return indicator.calculateEdgeCentrality(this, false);
        }
        else{
            return null;
        }
        // return indicator.calculateEdgeCentrality(this);

    }

    public Map<GraphEntity, Double> calculateNeighborhoodNodeCentrality(
            ILocalIndicator indicator, int k, SnapshotGraph.NORMALIZERS normalize) {
        if (logger.isInfoEnabled()) {
            logger.info("Calculating nodes " + indicator.getName() + " ... ");
        }
        if (normalize.equals(SnapshotGraph.NORMALIZERS.MINMAX)) {
            Statistics<GraphEntity> stat = new Statistics<GraphEntity>();
            return stat.normalize(indicator.calculateNeighborhoodNodeCentrality(this,
                    k,false));
        } else if(normalize.equals(SnapshotGraph.NORMALIZERS.CONVENTIONAL)){
            return indicator.calculateNeighborhoodNodeCentrality(this,
                    k, true);
        }
        else if(normalize.equals(SnapshotGraph.NORMALIZERS.NONE)){
            return indicator.calculateNeighborhoodNodeCentrality(this,
                    k, false);
        }
        else{
            return null;
        }
        // return indicator.calculateNodeCentrality(this);

    }

    public Map<GraphEntity, Double> calculateNeighborhoodEdgeCentrality(
            ILocalIndicator indicator, int k, SnapshotGraph.NORMALIZERS normalize) {
        if (logger.isInfoEnabled()) {
            logger.info("Calculating edges " + indicator.getName() + " ... ");
        }
        if (normalize.equals(SnapshotGraph.NORMALIZERS.MINMAX)) {
            Statistics<GraphEntity> stat = new Statistics<GraphEntity>();
            return stat.normalize(indicator.calculateNeighborhoodEdgeCentrality(this,
                    k,false));
        } else if(normalize.equals(SnapshotGraph.NORMALIZERS.CONVENTIONAL)){
            return indicator.calculateNeighborhoodEdgeCentrality(this,
                    k, true);
        }
        else if(normalize.equals(SnapshotGraph.NORMALIZERS.NONE)){
            return indicator.calculateNeighborhoodEdgeCentrality(this,
                    k, false);
        }
        else{
            return null;
        }

        // return indicator.calculateEdgeCentrality(this);

    }

    public List<Double> calculateGeometricalIndicator(
            IOtherGeometricalIndicator indicator) {
        return indicator.calculateGeometricalIndicator(this);
    }

    public void resetAll() {
        this.resetShortestPaths();
        //        this.edgesIndexes.clear();
        //        this.nodesIndexes.clear();
        //        this.indexesEdges.clear();
        //        this.indexesNodes.clear();
        this.edgesWeights = null;
        this.getEdges().clear();
        this.getVertices().clear();
    }

    // **************************************************************
    // ***************** Autres fonctionnalités *********************
    // **************************************************************

    /**
     * Calcul le degré d'un sommet
     */
    public int getDegre(GraphEntity v) {
        return this.getIncidentEdges(v).size();
    }

    /**
     * Donne le degré max des sommets de self
     * @return
     */
    public int getDegreMax() {
        int dm = -1;
        for (GraphEntity v : this.getVertices()) {
            int d = this.getDegre(v);
            if (d > dm) {
                dm = d;
            }
        }
        return dm;
    }

    /**
     * Matrice d'adjacence
     * @return
     */
//    public SparseDoubleMatrix2D getUnweightedAdjacencyMatrix() {
//        int n = this.getVertexCount();
//        SparseDoubleMatrix2D A = new SparseDoubleMatrix2D(n, n);
//        for (int i = 0; i < n - 1; i++) {
//            for (int j = i + 1; j < n; j++) {
//                if (this.isNeighbor(this.getNodeByIndex(i), this.getNodeByIndex(j))) {
//                    A.setQuick(i, j, 1);
//                } else {
//                    A.setQuick(i, j, 0.);
//                }
//                A.setQuick(j, i, A.getQuick(i, j));
//            }
//            A.setQuick(i, i, 0);
//        }
//        return A;
//    }

    /**
     * Matrice d'adjacence pondérée
     * @return
     */
//    public SparseDoubleMatrix2D getWeightedAdjacencyMatrix() {
//        if (this.getEdgesWeights() == null) {
//            return this.getUnweightedAdjacencyMatrix();
//        }
//        int n = this.getVertexCount();
//        SparseDoubleMatrix2D A = new SparseDoubleMatrix2D(n, n);
//        for (int i = 0; i < n - 1; i++) {
//            for (int j = i + 1; j < n; j++) {
//                if (this.isNeighbor(this.getNodeByIndex(i), this.getNodeByIndex(j))) {
//                    // arcs reliants i et j
//                    Collection<GraphEntity> edges = this.findEdgeSet(
//                            this.getNodeByIndex(i), this.getNodeByIndex(j));
//                    // on prend le plus court
//                    double dmin = Double.MAX_VALUE;
//                    for (GraphEntity e : edges) {
//                        if (this.getEdgesWeights().transform(e) < dmin) {
//                            dmin = this.getEdgesWeights().transform(e);
//                        }
//                    }
//                    A.setQuick(i, j, dmin);
//                } else {
//                    A.setQuick(i, j, 0.);
//                }
//                A.setQuick(j, i, A.getQuick(i, j));
//            }
//            A.setQuick(i, i, 0);
//        }
//        return A;
//    }

    /**
     * Renvoie la foret couvrante de poids minimale (tous les arbres couvrants de
     * poids minimal)
     * @return
     */
    public Forest<GraphEntity, GraphEntity> minimumSpanningForest() {
        MinimumSpanningForest2<GraphEntity, GraphEntity> mm = new MinimumSpanningForest2<GraphEntity, GraphEntity>(
                this, new DelegateForest<GraphEntity, GraphEntity>(),
                DelegateTree.<GraphEntity, GraphEntity> getFactory(),
                this.getEdgesWeights());
        return mm.getForest();
    }

    /**
     * Renvoie le premier arbre couvrant de poids minimum de la foret couvrante de
     * poids minimum
     * @return
     */
    public Graph<GraphEntity, GraphEntity> arbitraryMinimumSpanningTree() {
        Forest<GraphEntity, GraphEntity> forest = this.minimumSpanningForest();
        if (forest == null || forest.getTrees().isEmpty()) {
            return null;
        }
        Graph<GraphEntity, GraphEntity> tree = forest.getTrees().iterator().next();
        return tree;
    }

    /**
     * Donne le k-voisinage de v (les sommets dont la distance topologique à v est
     * inférieure ou égale à k)
     * @param v
     * @param k
     * @return
     */
    public List<GraphEntity> getKNeighborhood(GraphEntity v, int k) {
        if (k <= 1) {
            return null;
        }
        if (k == 1) {
            if(v.getType() == GraphEntity.NODE){
                return new ArrayList<GraphEntity>(this.getNeighbors(v));
            }
            else{
                return new ArrayList<GraphEntity>(this.getDual().getNeighbors(v));
            }
        }
        if(v.getType() == GraphEntity.NODE){
            Filter<GraphEntity, GraphEntity> filter = new KNeighborhoodFilter<GraphEntity, GraphEntity>(
                    v, k, EdgeType.IN_OUT);
            UndirectedSparseMultigraph<GraphEntity, GraphEntity> neighborhood = (UndirectedSparseMultigraph<GraphEntity, GraphEntity>) filter
                    .transform(this);
            List<GraphEntity> r = new ArrayList<GraphEntity>(neighborhood.getVertices());
            r.remove(v);
            return r;
        }
        else{
            Filter<GraphEntity, String> filter = new KNeighborhoodFilter<GraphEntity, String>(
                    v, k, EdgeType.IN_OUT);
            UndirectedSparseMultigraph<GraphEntity, String> neighborhood = (UndirectedSparseMultigraph<GraphEntity, String>) filter
                    .transform(this.getDual());
            List<GraphEntity> r = new ArrayList<GraphEntity>(neighborhood.getVertices());
            r.remove(v);
            return r;  
        }
    }

    /**
     * Donne les k-voisins de v (les sommets dont la distance topologique à v est
     * strictement égale à k)
     * @param v
     * @param k
     * @return
     */
    public List<GraphEntity> getKNeighbors(GraphEntity v, int k) {
        if (k <= 1) {
            return null;
        }
        if (k == 1) {
            if(v.getType() == GraphEntity.NODE){
                return new ArrayList<GraphEntity>(this.getNeighbors(v));
            }
            else{
                return new ArrayList<GraphEntity>(this.getDual().getNeighbors(v));
            }
        }
        if(v.getType() == GraphEntity.NODE){
            Filter<GraphEntity, GraphEntity> filter = new KNeighborhoodFilter<GraphEntity, GraphEntity>(
                    v, k, EdgeType.IN_OUT);
            UndirectedSparseMultigraph<GraphEntity, GraphEntity> neighborhood = (UndirectedSparseMultigraph<GraphEntity, GraphEntity>) filter
                    .transform(this);
            List<GraphEntity> r = new ArrayList<GraphEntity>(neighborhood.getVertices());
            r.remove(v);
            // ppc avec une distance topologique
            UnweightedShortestPath<GraphEntity, GraphEntity> sp = new UnweightedShortestPath<GraphEntity, GraphEntity>(
                    neighborhood);
            List<GraphEntity> result = new ArrayList<GraphEntity>(r);
            for (GraphEntity w : r) {
                if (sp.getDistance(v, w).intValue() != k) {
                    result.remove(w);
                }
            }
            return result;
        }
        else{
            Filter<GraphEntity, String> filter = new KNeighborhoodFilter<GraphEntity, String>(
                    v, k, EdgeType.IN_OUT);
            UndirectedSparseMultigraph<GraphEntity, String> neighborhood = (UndirectedSparseMultigraph<GraphEntity, String>) filter
                    .transform(this.getDual());
            List<GraphEntity> r = new ArrayList<GraphEntity>(neighborhood.getVertices());
            r.remove(v);
            // ppc avec une distance topologique
            UnweightedShortestPath<GraphEntity, String> sp = new UnweightedShortestPath<GraphEntity, String>(
                    neighborhood);
            List<GraphEntity> result = new ArrayList<GraphEntity>(r);
            for (GraphEntity w : r) {
                if (sp.getDistance(v, w).intValue() != k) {
                    result.remove(w);
                }
            }
            return result;
        }

    }

    /**
     * Compute the dual graph
     * Les sommets sont les arcs du graph primal
     * @return
     */
    public UndirectedSparseMultigraph<GraphEntity, String> getDual(){
        UndirectedSparseMultigraph<GraphEntity, String> dual = new UndirectedSparseMultigraph<GraphEntity, String>();
        Stack<GraphEntity>edges = new Stack<GraphEntity>();
        Set<GraphEntity>dones = new HashSet<GraphEntity>();
        edges.addAll(this.getEdges());
        while(!edges.isEmpty()){
            GraphEntity e = edges.pop();
            dones.add(e);
            Set<GraphEntity> connectedE = new HashSet<GraphEntity>();
            connectedE.addAll(this.getIncidentEdges(this.getEndpoints(e).getFirst()));
            connectedE.addAll(this.getIncidentEdges(this.getEndpoints(e).getSecond()));
            connectedE.removeAll(dones);
            for(GraphEntity ee : connectedE){
                String edge = Integer.toString(e.getId()) + "-" +  Integer.toString(ee.getId());
                dual.addEdge(edge, e,ee);
            }
        }
        return dual;
    }

}
