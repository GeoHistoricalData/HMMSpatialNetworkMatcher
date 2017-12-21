package fr.ign.cogit.v2.mergeProcess.mergeGeometries;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPositionList;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ILineString;
import fr.ign.cogit.geoxygene.generalisation.GaussianFilter;
import fr.ign.cogit.geoxygene.spatial.coordgeom.GM_LineString;
import fr.ign.cogit.v2.utils.GeometryUtils;

/**
 * Classe gérant différents algorithmes de fusion de polylignes
 * On suppose :
 * 1- les polylignes sont appariées (appariements 1:1)
 * 2- elles sont déjà pré-découpée : elles sont globalement de la même longueur
 *  et on ne gère pas les cas ou un bout d'une polyligne n'est pas fusionné
 * @author bcostes
 *
 */
public abstract class AbstractMergeAlgorithm {

  protected Set<ILineString> lines;
  protected Map<ILineString, Double> weights;
  public AbstractMergeAlgorithm(Set<ILineString> lines){
    this.lines = new HashSet<ILineString>();
    this.lines.addAll(lines);
    this.order();
  }
  public AbstractMergeAlgorithm(Set<ILineString> lines, Map<ILineString, Double> weights){
    this.lines = new HashSet<ILineString>();
    this.lines.addAll(lines);
    this.weights = weights;
    this.order();
  }
  public abstract ILineString merge();

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
  
  protected ILineString finalize(ILineString lineMerged){
    IDirectPositionList list = lineMerged.coord();
    GeometryUtils.filterLowAngles(list, 5.);
    ILineString l =  GaussianFilter.gaussianFilter(new GM_LineString(list), 2, 5);
    return l;
   // return lineMerged;
  }

}
