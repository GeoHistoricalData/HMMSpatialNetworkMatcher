package fr.ign.cogit.v2.mergeProcess.mergeGeometries.snapping;

import java.util.Set;

import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPositionList;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ILineString;
import fr.ign.cogit.geoxygene.generalisation.GaussianFilter;
import fr.ign.cogit.geoxygene.spatial.coordgeom.GM_LineString;
import fr.ign.cogit.v2.utils.GeometryUtils;

public class ElasticTransformation extends AbstractSnappingAlgorithm{

  private double influence_radius = 8.;

  public ElasticTransformation(Set<ILineString> lines, double influence_radius) {
    super(lines);
    this.influence_radius = influence_radius;
    // TODO Auto-generated constructor stub
  }
  public ElasticTransformation(Set<ILineString> lines) {
    super(lines);
    // TODO Auto-generated constructor stub
  }
  public ElasticTransformation(Set<ILineString> lines, IDirectPosition p1, IDirectPosition p2,double influence_radius){
    super(lines, p1, p2);
    this.influence_radius = influence_radius;
  }
  public ElasticTransformation(Set<ILineString> lines, IDirectPosition p1, IDirectPosition p2){
    super(lines, p1, p2);
  }
  @Override
  public Set<ILineString> transformAll() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ILineString transform(ILineString le) {
    // TODO Auto-generated method stub
    //ILineString le = Operateurs.resampling(lineMerged, 0.5);
    IDirectPosition startP = le.startPoint();
    IDirectPosition endP = le.endPoint();
    double t1x = p1.getX() - startP.getX();
    double t1y = p1.getY() - startP.getY();
    double t2x = p2.getX() - endP.getX();
    double t2y = p2.getY() - endP.getY();

    for(IDirectPosition p: le.getControlPoint()){
      //calcul du déplacement
      double d1 = p.distance(startP);
      double d2 = p.distance(endP);
      double coeff1 = 0.;
      if(d1<this.influence_radius){
        coeff1 = (Math.cos(d1*Math.PI / this.influence_radius)+1.) / 2;
      }
      double coeff2 = 0.;
      if(d2<this.influence_radius){
        coeff2 = (Math.cos(d2*Math.PI / this.influence_radius)+1.) / 2;
      }
      System.out.println(coeff1 +" "+ coeff2+" "+ d1+" "+ d2);
      double w = (coeff1 + coeff2) ==0 ? 1 : coeff1 + coeff2 ;
      double x = p.getX() + t1x * coeff1  + t2x * coeff2 ; 
      double y = p.getY() + t1y * coeff1  + t2y * coeff2 ; 
      p.setCoordinate(x, y);
    }
    IDirectPositionList list = le.getControlPoint();
   // GeometryUtils.filterLowAngles(list, 5.);
    return GaussianFilter.gaussianFilter(new GM_LineString(list),2,5);
   //return new GM_LineString(list);
  }//

}
