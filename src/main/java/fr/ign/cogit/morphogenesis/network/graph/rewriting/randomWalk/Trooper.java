package fr.ign.cogit.morphogenesis.network.graph.rewriting.randomWalk;

import java.util.Stack;

public class Trooper<E> {

  private E current;
  private E lastVisited;
  private Stack<E> visited;

  public Trooper(E initialize) {
    this.visited = new Stack<E>();
    this.current = initialize;
    this.lastVisited = current;
    this.addVisitedPosition(initialize);
  }

  public void setCurrentPosition(E current) {
    this.lastVisited = this.current;
    this.current = current;
    this.addVisitedPosition(this.current);
  }

  public E getCurrentPosition() {
    return this.current;
  }

  public void addVisitedPosition(E position) {
    this.visited.push(position);
  }

  public void setVisitedPositions(Stack<E> visited) {
    this.visited = visited;
  }

  public Stack<E> getVisitedPositions() {
    return visited;
  }

  public void setLastVisitedPosition(E lastVisited) {
    this.lastVisited = lastVisited;
  }

  public E lastVisitedPosition() {
    return lastVisited;
  }

}
