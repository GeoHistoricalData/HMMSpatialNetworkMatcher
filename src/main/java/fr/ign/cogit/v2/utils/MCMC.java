package fr.ign.cogit.v2.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.shape.random.RandomPointsBuilder;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPositionList;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ILineString;
import fr.ign.cogit.geoxygene.api.spatial.geomroot.IGeometry;
import fr.ign.cogit.geoxygene.contrib.geometrie.Operateurs;
import fr.ign.cogit.geoxygene.feature.DefaultFeature;
import fr.ign.cogit.geoxygene.feature.Population;
import fr.ign.cogit.geoxygene.generalisation.GaussianFilter;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPosition;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPositionList;
import fr.ign.cogit.geoxygene.spatial.coordgeom.GM_LineString;
import fr.ign.cogit.geoxygene.spatial.geomaggr.GM_MultiPoint;
import fr.ign.cogit.geoxygene.spatial.geomprim.GM_Point;
import fr.ign.cogit.geoxygene.util.attribute.AttributeManager;
import fr.ign.cogit.geoxygene.util.conversion.JtsGeOxygene;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileReader;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileWriter;

public class MCMC {

  private Set<ILineString> lines;
  private IDirectPosition p1, p2;
  private final double resolution = 8;
  private final int MAX_STABILITY_ITERATION = 50;
  private final double epsilon = 0.0001;
  private int iterations = 0; 
  private final double NB_POINTS_RATIO = 0.3; // nombre de points / m (ex: 0.25 => 1 point tous les 4m)1
  // distance minimale entre deux points
  private double dmin;


  public ILineString run(Set<ILineString> lines, IDirectPosition p1, IDirectPosition p2){
    this.lines = lines;
    this.p1 = p1;
    this.p2 = p2;
    //ShapefileWriter.write(out, "/home/bcostes/Bureau/test_poly.shp");
    // les gaussiennes
    //int nbPoints = (int)(lines.iterator().next().length() * NB_POINTS_RATIO) +1;
    double lemax = 0;
    for (ILineString ll : lines) {
      if (ll.length() > lemax) {
        lemax = ll.length();
      }
    }
    int nbPoints = (int) (0.25*(double) lemax / 1);
    this.dmin = Double.MAX_VALUE;
    for(ILineString l : lines){
      if(l.length()/nbPoints <dmin){
        dmin = l.length() / nbPoints;
      }
    }
    this.dmin *= this.dmin;


    IDirectPositionList generatedPoints = new DirectPositionList();
    List<Double> evaluation = new ArrayList<Double>();
    initialization(generatedPoints, lines.iterator().next(), nbPoints);

    for(int i=0; i< generatedPoints.size(); i++){
      evaluation.add(0.);
    }
    mcmc(generatedPoints, evaluation, nbPoints);
    return this.finalize(generatedPoints);
  }

  private ILineString finalize(IDirectPositionList generatedPoints){
    if(p1 != null && p2 != null) {
      generatedPoints.set(0, p1);
      generatedPoints.set(generatedPoints.size()-1, p2);
    }

    ILineString l = new GM_LineString(generatedPoints);
    //return l;
    generatedPoints =  GaussianFilter.gaussianFilter(l, 4, 5).coord();
    GeometryUtils.filterLowAngles(generatedPoints, 5);
    GeometryUtils.filterLargeAngles(generatedPoints, 150);
    
    return new GM_LineString(generatedPoints);
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
    

    
    dMin = this.dmin / dMin;
    
    double proba3 = Math.exp(-(dMin)/(2*10*10));
    return (proba1*proba3);
  }

  private void mcmc(IDirectPositionList generatedPoints,  List<Double> evaluation,
      int nbPoints) {
    //pour chaque point
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
      double energie = 0;
      for(int i=0; i< evaluation.size(); i++){
        gap += Math.abs(evaluation.get(i) - oldEvaluation.get(i));
        energie += evaluation.get(i);
      }
      //System.out.println(energie+" "+ gap+ " "+ iterations+" "+ evaluation.size()+" "+ generatedPoints.size());
      if(gap < epsilon){
        if(this.iterations >= MAX_STABILITY_ITERATION){
          return;
        }
      }
      else{
        iterations = 0;
      }
    }
  }

  private void initialization(IDirectPositionList generatedPoints, ILineString line, int nbPoints){
    double pas = line.length() / nbPoints;
    ILineString le= Operateurs.resampling(line, pas);
    generatedPoints.addAll(le.getControlPoint());
    //    RandomPointsBuilder generator = new RandomPointsBuilder(new GeometryFactory());
    //    generator.setNumPoints(nbPoints);
    //    try {
    //      generator.setExtent(JtsGeOxygene.makeJtsGeom(line.buffer(10)));
    //      GeometryCollection points = ( GeometryCollection)generator.getGeometry();
    //      for(int i=0; i< points.getNumGeometries(); i++)
    //      {
    //        Point p = (Point)points.getGeometryN(i);
    //        generatedPoints.add(new DirectPosition(p.getX(), p.getY()));
    //      }
    //    } catch (Exception e) {
    //      // TODO Auto-generated catch block
    //      e.printStackTrace();
    //    }
  }

  public static void main(String[] args) {
    // TODO Auto-generated method stub
    MCMC mcmc = new MCMC();

    IPopulation<IFeature> pop = ShapefileReader.read(
        "/home/bcostes/Bureau/test_merge/lines.shp");
    Set<ILineString> lines = new HashSet<ILineString>();
    for(IFeature f :pop){
      lines.add(new GM_LineString(f.getGeom().coord()));
    }
    
//    IDirectPosition p1 = lines.iterator().next().startPoint();
//    IDirectPosition p2 = lines.iterator().next().endPoint();
//
//
//    if (p1.equals(p2)) {
//      // simple translation
//      for (ILineString l : lines) {
//        double tx = p1.getX() - l.getControlPoint(0).getX();
//        double ty = p1.getY() - l.getControlPoint(0).getY();
//        for (int i = 0; i < l.coord().size(); i++) {
//          l.setControlPoint(i, new DirectPosition(l.getControlPoint(i).getX()
//              + tx, l.getControlPoint(i).getY() + ty));
//        }
//      }
//    } else {
//      lines = GeometryUtils.helmert(lines, p1, p2);
//    }
    //    
        RandomPointsBuilder generator = new RandomPointsBuilder(new GeometryFactory());
        generator.setNumPoints(100000);
        IDirectPositionList points = new DirectPositionList();
        for(ILineString l  : lines){
          points.addAll(l.getControlPoint());
        }
        GM_MultiPoint pointsM = new GM_MultiPoint(points);
        IGeometry convexHull = pointsM.convexHull();
      IPopulation<IFeature> out2 = new Population<IFeature>();
    
        try {
          generator.setExtent(JtsGeOxygene.makeJtsGeom(convexHull));
          GeometryCollection points2 = ( GeometryCollection)generator.getGeometry();
          for(int i=0; i< points2.getNumGeometries(); i++)
          {
            Point p = (Point)points2.getGeometryN(i);
            IDirectPosition pp  = new DirectPosition(p.getX(), p.getY());
            IFeature f = new DefaultFeature(new GM_Point(pp));
            double xg =0, yg = 0;
            for(ILineString l: lines){
              IDirectPosition pproj = Operateurs.projection(pp, l);
              xg +=pproj.getX();
              yg +=  pproj.getY();
            }
            xg /= ((double)lines.size());
            yg /= ((double)lines.size());
            double d = Math.sqrt((pp.getX()-xg) * (pp.getX()-xg) + (pp.getY()-yg)*(pp.getY()-yg));
    
            AttributeManager.addAttribute(f, "dist", d, "Double");
            out2.add(f);
          }
        } catch (Exception e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }

   // ILineString lineMerged = mcmc.run(lines, lines.iterator().next().startPoint(), lines.iterator().next().endPoint());
    ILineString lineMerged = mcmc.run(lines,null, null);


    IPopulation<IFeature> out = new Population<IFeature>();
    out.add(new DefaultFeature(lineMerged));
    ShapefileWriter.write(out2, "/home/bcostes/Bureau/test_points.shp");

    ShapefileWriter.write(out, "/home/bcostes/Bureau/test_out.shp");
    
    


  }

}
