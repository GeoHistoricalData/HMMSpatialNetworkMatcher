package fr.ign.cogit.v2.lineage;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import fr.ign.cogit.v2.tag.STEntity;


public class STGroupe implements Serializable {

  private Set<STEntity> nodes;
  private Set<STEntity> edges;

  public STGroupe() {
    this.setNodes(new HashSet<STEntity>());
    this.setEdges(new HashSet<STEntity>());
  }

  public void setNodes(Set<STEntity> nodes) {
    this.nodes = nodes;
  }

  public Set<STEntity> getNodes() {
    return nodes;
  }

  public void setEdges(Set<STEntity> edges) {
    this.edges = edges;
  }

  public Set<STEntity> getEdges() {
    return edges;
  }

  public boolean contains(STGroupe g) {
    for (STEntity node : g.getNodes()) {
      if (!this.getNodes().contains(node)) {
        return false;
      }
    }
    for (STEntity edge : g.getEdges()) {
      if (!this.getEdges().contains(edge)) {
        return false;
      }
    }
    return true;
  }

  public boolean strictlyContains(STGroupe g) {
    if (this.getNodes().size() <= g.getNodes().size()) {
      return false;
    }
    if (this.getEdges().size() <= g.getEdges().size()) {
      return false;
    }
    return this.contains(g);
  }

  public boolean equals(STGroupe g) {
    if (g.getNodes().size() != this.getNodes().size()
        || g.getEdges().size() != this.getEdges().size()) {
      return false;
    }
    for (STEntity node : this.getNodes()) {
      if (!g.getNodes().contains(node)) {
        return false;
      }
    }
    for (STEntity edge : this.getEdges()) {
      if (!g.getEdges().contains(edge)) {
        return false;
      }
    }
    return true;
  }

  public boolean isContainedBy(STGroupe g) {
    for (STEntity node : this.getNodes()) {
      if (!g.getNodes().contains(node)) {
        return false;
      }
    }
    for (STEntity edge : this.getEdges()) {
      if (!g.getEdges().contains(edge)) {
        return false;
      }
    }
    return true;
  }

  public boolean isStriclyContainedBy(STGroupe g) {
    if (g.getNodes().size() <= this.getNodes().size()) {
      return false;
    }
    if (g.getEdges().size() <= this.getEdges().size()) {
      return false;
    }
    return this.isContainedBy(g);
  }

  public int size() {
    return (this.getNodes().size() + this.getEdges().size());
  }

  /**
   * Remplacement d'une entité par une autre
   * @param entity
   * @param newEntity
   */
  public void update(STEntity entity, STEntity newEntity) {
    if (entity.getType() == STEntity.NODE) {
      this.nodes.remove(entity);
    } else {
      this.edges.remove(entity);
    }
    if (newEntity.getType() == STEntity.NODE) {
      this.nodes.add(newEntity);
    } else {
      this.edges.add(newEntity);
    }
  }

  public void update(STEntity entity, List<STEntity> newEntities) {
    if (entity.getType() == STEntity.NODE) {
      this.nodes.remove(entity);
    } else {
      this.edges.remove(entity);
    }
    for (STEntity newEntity : newEntities) {
      if (newEntity.getType() == STEntity.NODE) {
        this.nodes.add(newEntity);
      } else {
        this.edges.add(newEntity);
      }
    }
  }

}
