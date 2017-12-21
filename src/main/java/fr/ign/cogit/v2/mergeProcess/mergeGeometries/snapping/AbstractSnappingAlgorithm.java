package fr.ign.cogit.v2.mergeProcess.mergeGeometries.snapping;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ILineString;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPosition;

/**
 * Mieux vaut fair le snapping après la fusion ....
 * @author bcostes
 *
 */
public abstract class AbstractSnappingAlgorithm {

  protected Set<ILineString> lines;
  protected IDirectPosition p1, p2;
  protected Map<ILineString, Double> weights;
  
  public AbstractSnappingAlgorithm(Set<ILineString> lines, IDirectPosition p1, IDirectPosition p2){
    this.lines = new HashSet<ILineString>();
    this.lines.addAll(lines);
    this.p1 = p1;
    this.p2 = p2;
  }

  public AbstractSnappingAlgorithm(Set<ILineString> lines){
    this.lines = new HashSet<ILineString>();
    this.lines.addAll(lines);
    this.init();
  }
  public AbstractSnappingAlgorithm(Set<ILineString> lines, Map<ILineString, Double> weights){
    this.lines = new HashSet<ILineString>();
    this.lines.addAll(lines);
    this.weights = weights;
    this.init();
  }
  public abstract Set<ILineString> transformAll();
  public abstract ILineString transform(ILineString lineMerged);


  private void init(){
    this.order();
    if(this.weights == null){
      this.weights = new HashMap<ILineString, Double>();
    }
    for(ILineString l : this.lines){
      if(!this.weights.containsKey(l)){
        this.weights.put(l, 1.);
      }
    }
    double x1=0, y1 =0, x2 =0, y2 =0;
    double w = 0.;
    for(ILineString l : this.lines){
      x1 += this.weights.get(l) * l.startPoint().getX();
      y1 += this.weights.get(l) * l.startPoint().getY();
      x2 += this.weights.get(l) * l.endPoint().getX();
      y2 += this.weights.get(l) * l.endPoint().getY();
      w += this.weights.get(l);
    }
    x1 /= w;
    y1 /= w;
    x2 /= w;
    y2 /= w;
    this.p1 = new DirectPosition(x1, y1);
    this.p2 = new DirectPosition(x2, y2);
  }
  
  protected void snap(ILineString l){
    l.setControlPoint(0, this.p1);
    l.setControlPoint(l.getControlPoint().size()-1, this.p2);
  }
  
  protected void order(){
    Set<ILineString> tmp = new HashSet<ILineString>();
    IDirectPosition p1 = lines.iterator().next().startPoint(); 
    for(ILineString line: this.lines){
      if(line.startPoint().distance(p1)>line.endPoint().distance(p1)){
        tmp.add(line.reverse());
      }
      else{
        tmp.add(line);
      }
    }
    this.lines.clear();
    this.lines.addAll(tmp);
  }
}
