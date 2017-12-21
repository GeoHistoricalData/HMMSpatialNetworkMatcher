package fr.ign.cogit.v2.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.uci.ics.jung.graph.UndirectedGraph;
import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPositionList;
import fr.ign.cogit.geoxygene.api.spatial.geomprim.IOrientableCurve;
import fr.ign.cogit.geoxygene.api.spatial.geomroot.IGeometry;
import fr.ign.cogit.geoxygene.feature.DefaultFeature;
import fr.ign.cogit.geoxygene.feature.Population;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPositionList;
import fr.ign.cogit.geoxygene.spatial.coordgeom.GM_LineString;
import fr.ign.cogit.geoxygene.spatial.geomaggr.GM_MultiCurve;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileWriter;
import fr.ign.cogit.v2.io.TAGIoManager;
import fr.ign.cogit.v2.snapshot.GraphEntity;
import fr.ign.cogit.v2.snapshot.JungSnapshot;
import fr.ign.cogit.v2.tag.FuzzyTemporalInterval;
import fr.ign.cogit.v2.tag.STGraph;

/**
 * Adopted from JGraphT for use with Jung.
 * The BronKerbosch algorithm is an algorithm for finding maximal cliques in an undirected graph
 * This algorithmn is taken from Coenraad Bron- Joep Kerbosch in 1973.
 * This works on undirected graph
 *  See {@linktourl  See http://en.wikipedia.org/wiki/Bron%E2%80%93Kerbosch_algorithm}
 * @author Reuben Doetsch
 *
 * @param <V> vertex class of graph
 * @param <E> edge class of graph
 */
public class BronKerboschCliqueFinder<V, E>
{
  //~ Instance fields --------------------------------------------------------

  private final UndirectedGraph<V, E> graph;

  private Collection<Set<V>> cliques;

  //~ Constructors -----------------------------------------------------------

  /**
   * Creates a new clique finder. Make sure this is a simple graph.
   *
   * @param graph the graph in which cliques are to be found; graph must be
   * simple
   */
  public BronKerboschCliqueFinder(UndirectedGraph<V, E> graph)
  {

    this.graph = graph;
  }

  //~ Methods ----------------------------------------------------------------

  /**
   * Finds all maximal cliques of the graph. A clique is maximal if it is
   * impossible to enlarge it by adding another vertex from the graph. Note
   * that a maximal clique is not necessarily the biggest clique in the graph.
   *
   * @return Collection of cliques (each of which is represented as a Set of
   * vertices)
   */
  public Collection<Set<V>> getAllMaximalCliques()
  {
    // TODO:  assert that graph is simple

    cliques = new ArrayList<Set<V>>();
    List<V> potential_clique = new ArrayList<V>();
    List<V> candidates = new ArrayList<V>();
    List<V> already_found = new ArrayList<V>();
    candidates.addAll(graph.getVertices());
    findCliques(potential_clique, candidates, already_found);
    return cliques;
  }

  /**
   * Finds the biggest maximal cliques of the graph.
   *
   * @return Collection of cliques (each of which is represented as a Set of
   * vertices)
   */
  public Collection<Set<V>> getBiggestMaximalCliques()
  {
    // first, find all cliques
    getAllMaximalCliques();

    int maximum = 0;
    Collection<Set<V>> biggest_cliques = new ArrayList<Set<V>>();
    for (Set<V> clique : cliques) {
      if (maximum < clique.size()) {
        maximum = clique.size();
      }
    }
    for (Set<V> clique : cliques) {
      if (maximum == clique.size()) {
        biggest_cliques.add(clique);
      }
    }
    return biggest_cliques;
  }

  private void findCliques(
      List<V> potential_clique,
      List<V> candidates,
      List<V> already_found)
  {
    List<V> candidates_array = new ArrayList<V>(candidates);
    if (!end(candidates, already_found)) {
      // for each candidate_node in candidates do
      for (V candidate : candidates_array) {
        List<V> new_candidates = new ArrayList<V>();
        List<V> new_already_found = new ArrayList<V>();

        // move candidate node to potential_clique
        potential_clique.add(candidate);
        candidates.remove(candidate);

        // create new_candidates by removing nodes in candidates not
        // connected to candidate node
        for (V new_candidate : candidates) {
          if (graph.isNeighbor(candidate, new_candidate))
          {
            new_candidates.add(new_candidate);
          } // of if
        } // of for

        // create new_already_found by removing nodes in already_found
        // not connected to candidate node
        for (V new_found : already_found) {
          if (graph.isNeighbor(candidate, new_found)) {
            new_already_found.add(new_found);
          } // of if
        } // of for

        // if new_candidates and new_already_found are empty
        if (new_candidates.isEmpty() && new_already_found.isEmpty()) {
          // potential_clique is maximal_clique
          cliques.add(new HashSet<V>(potential_clique));
        } // of if
        else {
          // recursive call
          findCliques(
              potential_clique,
              new_candidates,
              new_already_found);
        } // of else

        // move candidate_node from potential_clique to already_found;
        already_found.add(candidate);
        potential_clique.remove(candidate);
      } // of for
    } // of if
  }

  private boolean end(List<V> candidates, List<V> already_found)
  {
    // if a node in already_found is connected to all nodes in candidates
    boolean end = false;
    int edgecounter;
    for (V found : already_found) {
      edgecounter = 0;
      for (V candidate : candidates) {
        if (graph.isNeighbor(found, candidate)) {
          edgecounter++;
        } // of if
      } // of for
      if (edgecounter == candidates.size()) {
        end = true;
      }
    } // of for
    return end;
  }


  public static void main(String args[]){
    STGraph stg = TAGIoManager.deserialize("/media/bcostes/Data/Benoit/these/analyses/TAG/TAG_v2/construction/etape4/tag_new.tag");
    List<FuzzyTemporalInterval> times = stg.getTemporalDomain().asList();
    Collections.sort(times);

    for(FuzzyTemporalInterval t : times){
      int cpt=0;
      Population<IFeature>out = new Population<IFeature>();

      JungSnapshot j = stg.getSnapshotAt(t);
      System.out.println(j.getVertexCount()+" " +j.getEdgeCount());
      BronKerboschCliqueFinder<GraphEntity, GraphEntity> b= 
          new BronKerboschCliqueFinder<GraphEntity,GraphEntity>(j);
      Collection<Set<GraphEntity>> cliques = b.getAllMaximalCliques();
      System.out.println(cliques.size());
      for(Set<GraphEntity> clique : cliques){
        if(clique.size()>2){
          cpt++;
          IGeometry geom = new GM_MultiCurve<IOrientableCurve>();
          for(GraphEntity e : clique){
            for(GraphEntity ee : clique){
              if(e.equals(ee)){
                continue;
              }
              IDirectPositionList l = new DirectPositionList();
              l.add(e.getGeometry().toGeoxGeometry().coord().get(0));
              l.add(ee.getGeometry().toGeoxGeometry().coord().get(0));
              geom = geom.union(new GM_LineString(l));
            }
          }
          out.add(new DefaultFeature(geom));
        }
      }
      System.out.println(t+" "+ cpt);
   // ShapefileWriter.write(out, "/media/bcostes/Data/Benoit/these/analyses/TAG/TAG_v2/analyses_indicateurs/cliques_" + t.getX(0)+".shp");

    }
  }

}
