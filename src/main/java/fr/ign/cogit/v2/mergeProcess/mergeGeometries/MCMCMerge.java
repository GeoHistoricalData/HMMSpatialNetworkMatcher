package fr.ign.cogit.v2.mergeProcess.mergeGeometries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPositionList;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ILineString;
import fr.ign.cogit.geoxygene.contrib.geometrie.Operateurs;
import fr.ign.cogit.geoxygene.generalisation.GaussianFilter;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPosition;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPositionList;
import fr.ign.cogit.geoxygene.spatial.coordgeom.GM_LineString;
import fr.ign.cogit.v2.utils.GeometryUtils;

public class MCMCMerge extends AbstractMergeAlgorithm{


  private final double resolution = 10;
  private final double resolution2 = 5;

  private final int MAX_STABILITY_ITERATION = 100;
  private final double epsilon = 0.00001;
  private int iterations = 0; 
  // distance minimale entre deux points
  private double dmin, dmax;

  public MCMCMerge(Set<ILineString> lines) {
    super(lines);
    // TODO Auto-generated constructor stub
  }
  public MCMCMerge(Set<ILineString> lines,  Map<ILineString, Double> weights) {
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
    double lemax = 0;
    for (ILineString ll : lines) {
      if (ll.length() > lemax) {
        lemax = ll.length();
      }
    }
    int nbPoints = (int)(lemax) ;
    this.dmin = Double.MAX_VALUE;
    for(ILineString l : lines){
      if(l.length()/nbPoints <dmin){
        dmin = l.length() / nbPoints;
      }
    }
    this.dmin *= this.dmin;

//    int nbPoints = -1;
//    //ligne la plus résolue
//    for(ILineString l :this.lines){
//      if(l.getControlPoint().size() > nbPoints){
//        nbPoints = l.getControlPoint().size();
//      }
//    }
//    this.dmax = Double.MIN_VALUE;
//    this.dmin = Double.MAX_VALUE;
//    for(ILineString l :this.lines){
//    for(int i=0;i < l.getControlPoint().size()-1; i++){
//      double d = l.getControlPoint(i).distance(l.getControlPoint(i+1));
//      if(d > this.dmax){
//        this.dmax = d;
//      }
//      if(d<this.dmin){
//        this.dmin = d;
//      }
//    }
//    }

    IDirectPositionList generatedPoints = new DirectPositionList();
    List<Double> evaluation = new ArrayList<Double>();
    initialization(generatedPoints, lines.iterator().next(), nbPoints);

    for(int i=0; i< generatedPoints.size(); i++){
      evaluation.add(0.);
    }
    // TODO Auto-generated method stub
    while(true){
      List<Double> oldEvaluation = new ArrayList<Double>();
      oldEvaluation.addAll(evaluation);
      for(int i=0; i< generatedPoints.size(); i++){
        // calcul d'un nouveau point aléatoirement : coordonnées polaires
        // angle entre 0 et Pi
        double teta = Math.random() *  Math.PI;
        //rayon : gaussienne centré en le point courrant, max: 5m
        Random random = new Random();
        IDirectPosition p =null;
        while(true){
          double radius = random.nextGaussian() * 0.01;
          double x = generatedPoints.get(i).getX() + radius * Math.cos(teta);
          double y = generatedPoints.get(i).getY() + radius * Math.sin(teta);
          p = new DirectPosition(x,y);
          break;
        }
        IDirectPositionList ltemp = new DirectPositionList();
        ltemp.addAll(generatedPoints);
        ltemp.remove(generatedPoints.get(i));
        double d= evaluate(p, ltemp);
        double oldd = evaluation.get(i);
        double alpha = d / oldd;
        if(alpha >=1){
          // on remplace
          generatedPoints.set(i, p);
          evaluation.set(i, d);
        }
        else{
          //on accpete avec proba alpha
          double proba = Math.random();
          if(proba <= 1-alpha){
            // on remplace
            generatedPoints.set(i, p);
            evaluation.set(i, d);
          }
        }
      }
      // on continue ? 
      this.iterations ++;
      double gap = 0;
      for(int i=0; i< evaluation.size(); i++){
        gap += Math.abs(evaluation.get(i) - oldEvaluation.get(i));
      }
      if(gap < epsilon){
        if(this.iterations >= MAX_STABILITY_ITERATION){
          break;
        }
      }
      else{
        iterations = 0;
      }
    }  
    GeometryUtils.filterLowAngles(generatedPoints, 5.);
    ILineString l =  GaussianFilter.gaussianFilter(new GM_LineString(generatedPoints), 5, 5);
    return l;
    }

  private double evaluate(IDirectPosition p, IDirectPositionList generatedPoints) {
    // TODO Auto-generated method stub
    //distances aux lignes

    double xg =0, yg = 0;
    for(ILineString l: lines){
      IDirectPosition pproj = Operateurs.projection(p, l);
      xg += pproj.getX();
      yg += pproj.getY();
    }
    xg /= ((double)lines.size());
    yg /= ((double)lines.size());
    double d =  (p.getX() - xg) * (p.getX()-xg) + (p.getY() -yg) * (p.getY() - yg);
    //double d = (new DirectPosition(xg,yg)).distance(p);
    double proba1 = Math.exp(-(d)/(2 * this.resolution * this.resolution));

    //    //distance au point le plus proche
    double dMin = Double.MAX_VALUE;
    for(IDirectPosition pp: generatedPoints){
      double dist = (pp.getX() - p.getX()) * (pp.getX() - p.getX()) +
          (pp.getY() - p.getY())* (pp.getY() - p.getY());
      if(dist<dMin){
        dMin = dist;
      }
    }
    double proba2 = 0.;
    if(dMin> this.dmin){
      proba2 = 1.;
    }
    else{
      proba2 = Math.exp((dMin-this.dmin)/(2*this.resolution2 * this.resolution2));
    }

    return (proba1*proba2);
  }


  private void initialization(IDirectPositionList generatedPoints, ILineString line, int nbPoints){
    double pas = line.length() / nbPoints;
    ILineString le= Operateurs.resampling(line, pas);
    generatedPoints.addAll(le.getControlPoint());
  }
}
