package fr.ign.cogit.morphogenesis.network.clustering;

import java.util.List;

public abstract class AbstractClustering {

  protected String name;
  protected int nbclasses;

  public AbstractClustering(int nbclasses) {
    this.name = this.getClass().getSimpleName();
    this.nbclasses = nbclasses;
  }

  public String getName() {
    return this.name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public abstract List<List<Double>> cluster(List<? extends Number> values);

}
