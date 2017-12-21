package fr.ign.cogit.v2.indicators.dynamics.eventsfinder;

import java.util.HashSet;
import java.util.Set;

import fr.ign.cogit.v2.strokes.Stroke;
import fr.ign.cogit.v2.tag.STEntity;

/**
 * "Hypothèse d'événement", hypothèse d'un groupe d'arcs susceptibles d'avoir eu l'impact max sur 
 * l'écolution de la centralité d'un arc ou groupe d'arcs 
 * @author bcostes
 *
 */
public class EventsHypothesis implements Comparable<EventsHypothesis>{

  private double score;
  private Set<Stroke> eventsConstructions;
  private Set<Stroke> eventsDestructions;

  public EventsHypothesis(){
    this.score = -1;
    this.eventsConstructions = new HashSet<Stroke>();
    this.eventsDestructions = new HashSet<Stroke>();
  }

  public double getScore() {
    return score;
  }
  public void setScore(double score) {
    this.score = score;
  }
  public Set<Stroke> getEventsConstructions() {
    return eventsConstructions;
  }
  public void setEventsConstructions(Set<Stroke> eventsConstructions) {
    this.eventsConstructions = eventsConstructions;
  }
  public Set<Stroke> getEventsDestructions() {
    return eventsDestructions;
  }
  public void setEventsDestructions(Set<Stroke> eventsDestructions) {
    this.eventsDestructions = eventsDestructions;
  }
  @Override
  public int compareTo(EventsHypothesis o) {
    return Double.compare(this.score, o.getScore());
  }


  public String toString(){

    //  public String toString(){
    String s ="-------------- EVENTS HYPOTHESIS ----------------\n";
    s+="SCORE : " + this.getScore()+"\n";
    s+= "** Constructions **\n";
    for(Stroke se: this.eventsConstructions){
      for(STEntity e : se.getEntities()){
        s+=e.getId()+"\n";
      }
    }
    s+= "** Destructions **\n";
    for(Stroke se: this.eventsDestructions){
      for(STEntity e : se.getEntities()){
        s+=e.getId()+"\n";
      }
    }
    return s;
  }



}
