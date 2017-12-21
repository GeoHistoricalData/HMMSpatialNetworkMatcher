package fr.ign.cogit.v2.indicators;

import fr.ign.cogit.v2.snapshot.JungSnapshot;

public abstract class IGlobalIndicator {

  protected String name;

  public IGlobalIndicator() {
  }

  public String getName() {
    return this.name;
  }

  public abstract double calculate(JungSnapshot graph);

}
