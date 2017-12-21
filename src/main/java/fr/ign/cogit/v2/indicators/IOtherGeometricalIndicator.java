package fr.ign.cogit.v2.indicators;

import java.util.List;

import fr.ign.cogit.v2.snapshot.JungSnapshot;

public abstract class IOtherGeometricalIndicator {
  protected String name;

  public String getName() {
    return this.name;
  }

  public IOtherGeometricalIndicator() {
  }

  public abstract List<Double> calculateGeometricalIndicator(JungSnapshot graph);

}
