package fr.ign.cogit.v2.snapshot;

import java.util.HashMap;
import java.util.Map;

import fr.ign.cogit.v2.geometry.LightGeometry;

/**
 * Modèle d'une entité d'un snapshot (arc ou sommet)
 * @author bcostes
 *
 */
public class GraphEntity {

  private int id;
  private LightGeometry geometry;
  private double weight;
  private Map<String, Double> localIndicators;
  private Map<String, String> attributes;
  public static final int NODE = 1000000;
  public static final int EDGE = 2000000;
  private static int _IDNODE = NODE;
  private static int _IDEDGE = EDGE;

  private static int CURRENT_TYPE = GraphEntity.NODE;

  private int type;

  public GraphEntity() {

    this.id = (CURRENT_TYPE == GraphEntity.NODE ? _IDNODE : _IDEDGE);
    this.type = CURRENT_TYPE;
    this.weight = -1;
    this.localIndicators = new HashMap<String, Double>();
    this.attributes = new HashMap<String, String>();
    updateID();
  }

  public GraphEntity(int type) {
    if (type != GraphEntity.EDGE && type != GraphEntity.NODE) {
      return;
    }
    setCurrentType(type);
    this.id = CURRENT_TYPE == GraphEntity.NODE ? _IDNODE : _IDEDGE;
    this.type = CURRENT_TYPE;
    this.weight = -1;
    this.localIndicators = new HashMap<String, Double>();
    this.attributes = new HashMap<String, String>();
    updateID();
  }

  // **************************************************************
  // ********************* Type :arc ou noeud *********************
  // **************************************************************

  public Map<String, String> getAttributes() {
    return attributes;
  }

  public void setAttributes(Map<String, String> attributes) {
    this.attributes = attributes;
  }

  public static int getCurrentType() {
    return CURRENT_TYPE;
  }

  public static void switchCurrentType() {
    if (CURRENT_TYPE == GraphEntity.NODE) {
      CURRENT_TYPE = GraphEntity.EDGE;
    } else {
      CURRENT_TYPE = GraphEntity.NODE;
    }
  }

  public static void setCurrentType(int type) {
    if (type != GraphEntity.EDGE && type != GraphEntity.NODE) {
      return;
    }
    CURRENT_TYPE = type;
  }

  public static void updateID() {
    if (CURRENT_TYPE == GraphEntity.NODE) {
      _IDNODE++;
    } else {
      _IDEDGE++;
    }
  }

  public void setType(int type) {
    this.type = type;
  }

  public int getType() {
    return type;
  }

  public void setId(int id) {
    this.id = id;
  }

  public int getId() {
    return id;
  }

  public void setGeometry(LightGeometry geometry) {
    this.geometry = geometry;
  }

  public LightGeometry getGeometry() {
    return geometry;
  }

  public void setWeight(double weight) {
    this.weight = weight;
  }

  public double getWeight() {
    return weight;
  }

  public void setLocalIndicators(Map<String, Double> localIndicators) {
    this.localIndicators = localIndicators;
  }

  public Map<String, Double> getLocalIndicators() {
    return localIndicators;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof GraphEntity)) {
      return false;
    }
    GraphEntity e = (GraphEntity) o;
    if (e.getType() != this.getType()) {
      return false;
    }
    if (e.getId() != this.getId()) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    if (this.getType() == GraphEntity.NODE) {
      return this.getId();
    } else {
      return 1000000 + this.getId();
    }
  }

  @Override
  public String toString() {
    return Integer.toString(this.getId());
  }

}
