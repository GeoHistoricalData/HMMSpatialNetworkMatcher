package fr.ign.cogit.v2.mergeProcess.mergeGeometries;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPositionList;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ILineString;
import fr.ign.cogit.geoxygene.contrib.appariement.EnsembleDeLiens;
import fr.ign.cogit.geoxygene.contrib.appariement.Lien;
import fr.ign.cogit.geoxygene.contrib.geometrie.Distances;
import fr.ign.cogit.geoxygene.contrib.geometrie.Operateurs;
import fr.ign.cogit.geoxygene.distance.Frechet;
import fr.ign.cogit.geoxygene.feature.DefaultFeature;
import fr.ign.cogit.geoxygene.feature.Population;
import fr.ign.cogit.geoxygene.generalisation.GaussianFilter;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPosition;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPositionList;
import fr.ign.cogit.geoxygene.spatial.coordgeom.GM_LineString;
import fr.ign.cogit.geoxygene.spatial.geomprim.GM_Point;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileWriter;
import fr.ign.cogit.v2.utils.GeometryUtils;

/**
 * Reprend l'approche proposée par Devogèle pour fusionner deux LineString.
 * Pour plus de deux lineString, la méthode de diffusion des poids serait à revoir ...
 * @author bcostes
 *
 */
public class MatchingHomologousPointsMerge extends AbstractMergeAlgorithm{

  private double thresold = Double.MAX_VALUE;

  private ILineString lineRef = null;

  public MatchingHomologousPointsMerge(Set<ILineString> lines, double  thresold) {
    super(lines);
  }
  public MatchingHomologousPointsMerge(Set<ILineString> lines) {
    super(lines);
  }
  public MatchingHomologousPointsMerge(Set<ILineString> lines,  Map<ILineString, Double> weights, double  thresold) {
    super(lines, weights);
  }
  @Override
  public ILineString merge() {
    if(this.lines.size() == 1){
      return this.finalize((ILineString)this.lines.iterator().next());
    }
        Set<ILineString> newLines = new HashSet<ILineString>();
        for(ILineString l: this.lines){
          l = Operateurs.resampling(l, 5.);
          newLines.add(l);
        }
        this.lines = newLines;
    if(this.weights == null){
      // on privilégie uniquement la pondération indiquée
      this.weights = new HashMap<ILineString, Double>();
    }
    for(ILineString l : this.lines){
      if(!weights.containsKey(l) || weights.get(l) <0)
        this.weights.put(l, 1.);
    }

    Iterator<ILineString> it = lines.iterator();
    this.lineRef = (ILineString)it.next();

    ILineString lineMerged = this.merge(it);
    double s = 0;
    for(Double d :this.weights.values()){
      s += d;
    }
    s/=((double)this.lines.size());
    this.weights.put(lineMerged, s);
//
    while(true){
      it = lines.iterator();
      this.lineRef = lineMerged;
      lineMerged =  this.merge(it);
      this.weights.put(lineMerged, s);
      if(Frechet.discreteFrechet(this.lineRef, lineMerged) == 0){
        break;
      }
    }
     //return lineMerged;
    IDirectPositionList list = lineMerged.coord();
    GeometryUtils.filterLowAngles(list, 5.);
    ILineString l =  GaussianFilter.gaussianFilter(lineMerged, 2, 5);
    return l;
  }


  private ILineString merge(Iterator<ILineString> it){
    //ligne la plus détaillée
    //  Matching des points par distance min
    EnsembleDeLiens liens = new EnsembleDeLiens();
    IPopulation<IFeature> points1 = new Population<IFeature>();
    Map<IFeature, Integer> map1 = new HashMap<IFeature, Integer>();
    int cpt=0;
    for(IDirectPosition p1: this.lineRef.getControlPoint()){
      points1.add(new DefaultFeature(new GM_Point(p1)));
      map1.put(points1.get(points1.size()-1), cpt);
      cpt++;
    }
    IPopulation<IFeature> points2 = new Population<IFeature>();
    Map<IFeature, Integer> map2 = new HashMap<IFeature, Integer>();
    Map<IFeature, Double> map3 = new HashMap<IFeature, Double>();

    while(it.hasNext()){
      ILineString l2 = (ILineString)it.next();

      cpt=0;
      IPopulation<IFeature> points2Tmp = new Population<IFeature>();
      for(IDirectPosition p2: l2.getControlPoint()){
        points2Tmp.add(new DefaultFeature(new GM_Point(p2)));
        map2.put(points2Tmp.get(points2Tmp.size()-1), cpt);
        map3.put(points2Tmp.get(points2Tmp.size()-1), this.weights.get(l2));

        cpt++;
      }
      for(IFeature f1: points1){
        Lien lien = liens.nouvelElement();
        lien.addObjetRef(f1);
        double dmin = Double.MAX_VALUE;
        IFeature fMatched = null;
        for(IFeature f2: points2Tmp){
          if(f1.getGeom().distance(f2.getGeom())<= this.thresold){
            if(dmin > f1.getGeom().distance(f2.getGeom())){
              dmin = f1.getGeom().distance(f2.getGeom());
              fMatched = f2;
            }
          }
        }
        if(fMatched == null){
          liens.remove(lien);
        }
        else{
          lien.addObjetComp(fMatched);
        }
      }
      //on regarde quelles entités de line2 ne sont pas encore matchées
      a: for(IFeature f2: points2Tmp){
        for(Lien lien: liens){
          if(lien.getObjetsComp().contains(f2)){
            continue a;
          }
        }
        Lien lien = liens.nouvelElement();
        double dmin = Double.MAX_VALUE;
        lien.addObjetComp(f2);
        IFeature fMatched = null;  
        for(IFeature f1: points1){
          if(f1.getGeom().distance(f2.getGeom())<= this.thresold){
            if(dmin > f1.getGeom().distance(f2.getGeom())){
              dmin = f1.getGeom().distance(f2.getGeom());
              fMatched = f1;
            }      
          }
        }
        if(fMatched == null){
          liens.remove(lien);
        }
        else{
          lien.addObjetRef(fMatched);
        }
      }
      points2.addAll(points2Tmp);
    }
    // calcul des composantes connexes pour regroupper les liens concernants les mêmes objets
    EnsembleDeLiens liensGrouped = liens.regroupeLiens(points1, points2);
//     liensGrouped.creeGeometrieDesLiens();
//    ShapefileWriter.write(liensGrouped, "/home/bcostes/Bureau/test_merge/test.shp");
    IDirectPositionList points = new DirectPositionList();
    double w1 = this.weights.get(this.lineRef);

    for(Lien lien: liensGrouped){
      //pour chaque appariement de points (1:1, 1:N, ou M:1)

      double w = 0;
      double x =0, y = 0;
      for(IFeature f : lien.getObjetsRef()){
        x += w1 * f.getGeom().coord().get(0).getX();
        y += w1 * f.getGeom().coord().get(0).getY();
        w += w1;
      }
      for(IFeature f : lien.getObjetsComp()){
        double w2 = map3.get(f);
        x += w2 * f.getGeom().coord().get(0).getX();
        y += w2 * f.getGeom().coord().get(0).getY();
        w += w2;
      }
      x /= w;
      y /= w;

      IDirectPosition p = new DirectPosition(x, y);
      points.add(p);
    }

  

  //Finalisation
  //return this.finalize(new GM_LineString(points));
  return new GM_LineString(points);
}

}
