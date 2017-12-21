package fr.ign.cogit.v2.mergeProcess.mergeGeometries;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPositionList;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ILineString;
import fr.ign.cogit.geoxygene.contrib.geometrie.Operateurs;
import fr.ign.cogit.geoxygene.contrib.geometrie.Vecteur;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPosition;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPositionList;
import fr.ign.cogit.geoxygene.spatial.coordgeom.GM_LineString;

/**
 * Snapping avant fusion
 * @author bcostes
 *
 */
public class DichotomicSegmentationMerge extends AbstractMergeAlgorithm{
  
  private final double epsilon = 0.0000001;
  
  public DichotomicSegmentationMerge(Set<ILineString> lines) {
    super(lines);
    // TODO Auto-generated constructor stub
  }
  public DichotomicSegmentationMerge(Set<ILineString> lines,  Map<ILineString, Double> weights) {
    super(lines, weights);
  }
  @Override
  public ILineString merge() {
    if(this.lines.size() == 1){
      return this.finalize((ILineString)this.lines.iterator().next());
    }
    if(this.weights == null){
      // on privilégie uniquement la pondération indiquée
      this.weights = new HashMap<ILineString, Double>();
    }
    for(ILineString l : this.lines){
      if(!weights.containsKey(l) || weights.get(l) <0)
        this.weights.put(l, 1.);
    }
    // TODO Auto-generated method stub
    double lemax = 0;
    for (ILineString ll : this.lines) {
      if (ll.length() > lemax) {
        lemax = ll.length();
      }
    }
    int nbPoints = (int) (0.5*(double) lemax / 1);
    IDirectPositionList list = new DirectPositionList();
    double x1=0, y1 =0, x2 =0, y2 =0;
    double w = 0.;
    for(ILineString l : this.lines){
      x1 += l.startPoint().getX();
      y1 +=  l.startPoint().getY();
      x2 +=  l.endPoint().getX();
      y2 +=  l.endPoint().getY();
      w += 1;
    }
    x1 /= w;
    y1 /= w;
    x2 /= w;
    y2 /= w;
    IDirectPosition p1 = new DirectPosition(x1, y1);
    IDirectPosition p2 = new DirectPosition(x2, y2);
    list.add(p1);
    list.add(p2);
    ILineString l = new GM_LineString(list);
    IDirectPositionList generatedPoints = Operateurs.resampling(l, l.length() / nbPoints).coord();
    
    //vecteurs normaux
    Vecteur v = new Vecteur((p1.getY() - p2.getY()) / (p2.getX() - p1.getX()),  1);
    v =v.getNormalised();
    double nx = v.getX();
    double ny = v.getY();
    for(int i=0; i< nbPoints; i++){
      IDirectPosition p = generatedPoints.get(i);
      IDirectPositionList startL = new DirectPositionList();
      startL.add(new DirectPosition(p.getX() - nx * 500, p.getY() - ny * 500));
      startL.add(new DirectPosition(p.getX() + nx * 500, p.getY() + ny * 500));
      ILineString startLine = new GM_LineString(startL);
      startL = Operateurs.resampling(startLine, startLine.length() / 50.).coord();
      while(true){
        double oldEvaluation = Double.MAX_VALUE;
        int indexMin = -1;
        for(int j = 0; j< startL.size(); j++){
          double d = evaluate(startL.get(j), generatedPoints);
          if(d < oldEvaluation){
            oldEvaluation = d;
            indexMin = j;
          }
        }
        if(indexMin != 0 && indexMin < startL.size()-1){
          IDirectPositionList newstartL = new DirectPositionList();
          newstartL.add(startL.get(indexMin-1));
          newstartL.add(startL.get(indexMin));
          newstartL.add(startL.get(indexMin+1));
          startLine = new GM_LineString(newstartL);
          startL = Operateurs.resampling(startLine, startLine.length() / 50.).coord();
          if(startLine.length()< epsilon){
            p = Operateurs.milieu(startLine);
            generatedPoints.set(i, p);
            break;
          }
        }
        else{
          p = Operateurs.milieu(startLine);
          break;
        }    
      }

    }
    
    return finalize(new GM_LineString(generatedPoints));
    }
  
  private double evaluate(IDirectPosition p, IDirectPositionList generatedPoints) {
    double xg =0, yg = 0;
    for(ILineString l: this.lines){
      IDirectPosition pproj = Operateurs.projection(p, l);
      xg += pproj.getX();
      yg += pproj.getY();
    }
    xg /= ((double)this.lines.size());
    yg /= ((double)this.lines.size());
    double d =  (p.getX() - xg) * (p.getX()-xg) + (p.getY() -yg) * (p.getY() - yg);
    return d;
  }
}
