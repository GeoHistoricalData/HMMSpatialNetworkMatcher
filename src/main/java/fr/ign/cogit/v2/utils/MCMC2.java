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
import fr.ign.cogit.geoxygene.contrib.geometrie.Vecteur;
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

public class MCMC2 {

  private Set<ILineString> lines;
  private IDirectPosition p1, p2;
  private final double resolution = 5;
  private final int MAX_STABILITY_ITERATION = 100;
  private final double epsilon = 0.0000001;
  private int iterations = 0; 
  // distance minimale entre deux points


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
    int nbPoints = (int) (0.5*(double) lemax / 1);


    List<Double> evaluation = new ArrayList<Double>();

    IDirectPositionList generatedPoints   = mcmc(evaluation, nbPoints);
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

    return proba1;
  }

  private IDirectPositionList mcmc(List<Double> evaluation,
      int nbPoints) {
    //pour chaque point
    IDirectPositionList list = new DirectPositionList();
    list.add(this.p1);
    list.add(this.p2);
    ILineString l = new GM_LineString(list);
    IDirectPositionList generatedPoints = Operateurs.resampling(l, l.length() / nbPoints).coord();
    for(int i=0; i< generatedPoints.size(); i++){
      evaluation.add(0.);
    }
    IDirectPositionList points = new DirectPositionList();
    for(ILineString ll  : lines){
      points.addAll(ll.getControlPoint());
    }
    GM_MultiPoint pointsM = new GM_MultiPoint(points);
    IGeometry convexHull = pointsM.convexHull();
//    IPopulation<IFeature> out = new Population<IFeature>();
//    for(IDirectPosition p: generatedPoints){
//      out.add(new DefaultFeature(new GM_Point(p)));
//    }
//    ShapefileWriter.write(out, "/home/bcostes/Bureau/gbthqr.shp");
    //vecteurs normaux
    Vecteur v = new Vecteur((this.p1.getY() - this.p2.getY()) / (this.p2.getX() - this.p1.getX()),  1);
    v =v.getNormalised();
    double nx = v.getX();
    double ny = v.getY();
    for(int i=0; i< nbPoints; i++){
      IDirectPosition p = generatedPoints.get(i);
      IDirectPositionList startL = new DirectPositionList();
      startL.add(new DirectPosition(p.getX() - nx * 250, p.getY() - ny * 250));
      startL.add(new DirectPosition(p.getX() + nx * 250, p.getY() + ny * 250));
      ILineString startLine = new GM_LineString(startL);
      startL = Operateurs.resampling(startLine, startLine.length() / 50.).coord();


      while(true){
        double oldEvaluation = Double.MIN_VALUE;
        int indexMin = -1;
        for(int j = 0; j< startL.size(); j++){
          double d = evaluate(startL.get(j), generatedPoints);
          if(d > oldEvaluation){
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
    
    return generatedPoints;
    
  }

  

  public static void main(String[] args) {
    // TODO Auto-generated method stub
    MCMC2 mcmc = new MCMC2();

    IPopulation<IFeature> pop = ShapefileReader.read(
        "/media/bcostes/Data/Benoit/these/analyses/TAG/merge/play_data/play5.shp");
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
    generator.setNumPoints(50000);
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

    ILineString lineMerged = mcmc.run(lines, lines.iterator().next().startPoint(), lines.iterator().next().endPoint());
    //ILineString lineMerged = mcmc.run(lines,null, null);


    IPopulation<IFeature> out = new Population<IFeature>();
    out.add(new DefaultFeature(lineMerged));
    ShapefileWriter.write(out2, "/home/bcostes/Bureau/test_points.shp");

    ShapefileWriter.write(out, "/home/bcostes/Bureau/test_out.shp");




  }

}
