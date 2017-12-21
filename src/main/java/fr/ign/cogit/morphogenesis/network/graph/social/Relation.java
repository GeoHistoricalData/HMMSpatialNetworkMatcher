package fr.ign.cogit.morphogenesis.network.graph.social;

public class Relation {

  /**
   * id dans la table des liens parisiens géocodés
   */
  private int id;
  private Individu individu1, individu2;
  private String type;
  private int idConnectedComponent;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public Individu getFirst() {
    return individu1;
  }

  public void setFirst(Individu individu1) {
    this.individu1 = individu1;
  }

  public Individu getSecond() {
    return individu2;
  }

  public void setSecond(Individu individu2) {
    this.individu2 = individu2;
  }

  public Relation(int id, Individu individu1, Individu individu2, String type) {
    super();
    this.id = id;
    this.individu1 = individu1;
    this.individu2 = individu2;
    this.type = type;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public boolean equals(Relation rel) {
    return (this.id == rel.getId());
  }

  public void setIdConnectedComponent(int idConnectedComponent) {
    this.idConnectedComponent = idConnectedComponent;
  }

  public int getIdConnectedComponent() {
    return idConnectedComponent;
  }
}
