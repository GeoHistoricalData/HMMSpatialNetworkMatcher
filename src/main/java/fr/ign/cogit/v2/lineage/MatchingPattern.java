package fr.ign.cogit.v2.lineage;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import fr.ign.cogit.v2.tag.STEntity;
import fr.ign.cogit.v2.tag.STLink;
import fr.ign.cogit.v2.tag.FuzzyTemporalInterval;

public class MatchingPattern {
  public List<Integer> sources;
  public List<Integer> targets;

  public MatchingPattern(List<Integer> sources, List<Integer> targets) {
    this.sources = sources;
    this.targets = targets;
  }

  public MatchingPattern(List<Integer> sources, int target) {
    this.sources = sources;
    this.targets = new ArrayList<Integer>();
    this.targets.add(target);
  }

  public MatchingPattern(int source, List<Integer> targets) {
    this.sources = new ArrayList<Integer>();
    this.sources.add(source);
    this.targets = targets;
  }

  public MatchingPattern(int source, int target) {
    this.sources = new ArrayList<Integer>();
    this.sources.add(source);
    this.targets = new ArrayList<Integer>();
    this.targets.add(target);
  }

  /**
   * Cherche un pattern d'appariement ex : (Node, Node), ou (Node, {Node, Edge,
   * Node)}
   * @param sources
   * @param targets
   * @return
   */
  public static List<MatchingLink> findMatchingPattern(MatchingPattern pattern,
      List<MatchingLink> links) {
    List<MatchingLink> result = new ArrayList<MatchingLink>();
    int sourcesN = 0;
    int sourcesE = 0;
    int targetN = 0;
    int targetE = 0;
    for (Integer source : pattern.sources) {
      if (source == STEntity.NODE) {
        sourcesN++;
      } else if (source == STEntity.EDGE) {
        sourcesE++;
      }
    }
    for (Integer target : pattern.targets) {
      if (target == STEntity.NODE) {
        targetN++;
      } else if (target == STEntity.EDGE) {
        targetE++;
      }
    }
    for (MatchingLink link : links) {
      if (link.getSources().getNodes().size() == sourcesN
          && link.getSources().getEdges().size() == sourcesE
          && link.getTargets().getNodes().size() == targetN
          && link.getTargets().getEdges().size() == targetE) {
        result.add(link);
      }
    }
    return result;
  }

  /**
   * Cherche un pattern d'appariement ex : (Node, Node), ou (Node, {Node, Edge,
   * Node)}
   * @param sources
   * @param targets
   * @return
   */
  public static List<MatchingLink> findMatchingPattern(MatchingPattern pattern,
      List<MatchingLink> links, FuzzyTemporalInterval t1, FuzzyTemporalInterval t2) {
    List<MatchingLink> result = new ArrayList<MatchingLink>();
    int sourcesN = 0;
    int sourcesE = 0;
    int targetN = 0;
    int targetE = 0;
    for (Integer source : pattern.sources) {
      if (source == STEntity.NODE) {
        sourcesN++;
      } else if (source == STEntity.EDGE) {
        sourcesE++;
      }
    }
    for (Integer target : pattern.targets) {
      if (target == STEntity.NODE) {
        targetN++;
      } else if (target == STEntity.EDGE) {
        targetE++;
      }
    }
    for (MatchingLink link : links) {
      if (!link.getDateSource().equals(t1) || !link.getDateTarget().equals(t2)) {
        continue;
      }
      if (link.getSources().getNodes().size() == sourcesN
          && link.getSources().getEdges().size() == sourcesE
          && link.getTargets().getNodes().size() == targetN
          && link.getTargets().getEdges().size() == targetE) {
        result.add(link);
      }
    }
    return result;
  }

  /**
   * Cherche un pattern d'appariement ex : (Node, Node), ou (Node, {Node, Edge,
   * Node)}
   * @param sources
   * @param targets
   * @return
   */
  public static List<MatchingLink> findMatchingPattern(MatchingPattern pattern,
      Set<MatchingLink> links, FuzzyTemporalInterval ttarget) {
    List<MatchingLink> result = new ArrayList<MatchingLink>();
    int sourcesN = 0;
    int sourcesE = 0;
    int targetN = 0;
    int targetE = 0;
    for (Integer source : pattern.sources) {
      if (source == STEntity.NODE) {
        sourcesN++;
      } else if (source == STEntity.EDGE) {
        sourcesE++;
      }
    }
    for (Integer target : pattern.targets) {
      if (target == STEntity.NODE) {
        targetN++;
      } else if (target == STEntity.EDGE) {
        targetE++;
      }
    }
    for (MatchingLink link : links) {
      if (!link.getDateTarget().equals(ttarget)) {
        continue;
      }
      if (link.getSources().getNodes().size() == sourcesN
          && link.getSources().getEdges().size() == sourcesE
          && link.getTargets().getNodes().size() == targetN
          && link.getTargets().getEdges().size() == targetE) {
        result.add(link);
      }
    }
    return result;
  }
}
