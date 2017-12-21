package fr.ign.cogit.morphogenesis.network.graph.rewriting.randomWalk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.Logger;

import fr.ign.cogit.morphogenesis.network.graph.rewriting.AbstractGraph;

public class RandomWalk<E, V> {

  private Logger logger = Logger.getLogger(RandomWalk.class);
  private int nb_troopers;
  private AbstractGraph<E, V> g;
  private List<Trooper<E>> troopers;
  private Map<E, Integer> map;

  public static int STEP_MAX = 100;

  public RandomWalk(AbstractGraph<E, V> g, int nb_troopers) {
    this.g = g;
    this.nb_troopers = nb_troopers;
    this.troopers = new ArrayList<Trooper<E>>();
    this.initialize();
  }

  private void initialize() {
    while (this.troopers.size() != this.nb_troopers) {
      Random r = new Random();
      int index = r.nextInt(g.getVertexCount());
      @SuppressWarnings("unchecked")
      Trooper<E> trooper = new Trooper<E>((E) g.getVertices().toArray()[index]);
      this.troopers.add(trooper);
    }
  }

  public void setMap(Map<E, Integer> map) {
    this.map = map;
  }

  public Map<E, Integer> map() {
    return map;
  }

  public void run() {
    int step = 1;
    while (step < STEP_MAX) {
      if (logger.isInfoEnabled()) {
        logger.info("Random Walk step " + step + " / " + STEP_MAX);
      }
      for (Trooper<E> trooper : this.troopers) {
        // on récupère la position courante du trooper
        E vertex = (E) trooper.getCurrentPosition();
        // on récupère les noeuds connectés
        ArrayList<E> candidats = new ArrayList<E>(g.getNeighbors(vertex));
        // on élimine la dernière position
        if (candidats.size() == 1) {
          @SuppressWarnings("unchecked")
          E nextVertex = (E) candidats.toArray()[0];
          trooper.setCurrentPosition(nextVertex);
          continue;
        }
        candidats.remove(trooper.lastVisitedPosition());
        if (candidats.size() == 1) {
          @SuppressWarnings("unchecked")
          E nextVertex = (E) candidats.toArray()[0];
          trooper.setCurrentPosition(nextVertex);
          continue;
        }
        // on tire un nombre aléatoire entre 0 et candidats.size() -1
        Random r = new Random();
        int index = r.nextInt(candidats.size() - 1);
        @SuppressWarnings("unchecked")
        E nextVertex = (E) candidats.toArray()[index];
        trooper.setCurrentPosition(nextVertex);
      }
      step++;
    }
    // à la fin du process on fait les comptes
    this.map = new HashMap<E, Integer>();
    for (Trooper<E> trooper : this.troopers) {
      for (E vertex : trooper.getVisitedPositions()) {
        if (map.containsKey(vertex)) {
          map.put(vertex, map.get(vertex) + 1);
        } else {
          map.put(vertex, 1);
        }
      }
    }
    for (E vertex : g.getVertices()) {
      if (!this.map.containsKey(vertex)) {
        this.map.put(vertex, 0);
      }
    }
  }

  public int getVertexScore(E vertex) {
    return this.map.get(vertex);
  }

}
