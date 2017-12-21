package fr.ign.cogit.v2.mergeProcess.mergeGeometries;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.geotools.legend.DefaultGlyphFactory;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPositionList;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ILineString;
import fr.ign.cogit.geoxygene.contrib.geometrie.Operateurs;
import fr.ign.cogit.geoxygene.feature.DefaultFeature;
import fr.ign.cogit.geoxygene.feature.Population;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPosition;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPositionList;
import fr.ign.cogit.geoxygene.spatial.coordgeom.GM_LineString;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileWriter;

/**
 * Mieux vaut snapper avant la fusion..
 * @author bcostes
 *
 */
public class CurvilinearHomologousPointsMerge extends AbstractMergeAlgorithm{

  public CurvilinearHomologousPointsMerge(Set<ILineString> lines) {
    super(lines);
    // TODO Auto-generated constructor stub
  }
  public CurvilinearHomologousPointsMerge(Set<ILineString> lines,  Map<ILineString, Double> weights) {
    super(lines, weights);
  }
  @Override
  public ILineString merge() {
    // TODO Auto-generated method stub
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
    for (ILineString l : this.lines) {
      if (l.length() > lemax) {
        lemax = l.length();
      }
    }

    int nbPts =  (int) ((double) lemax / 1);
    // resampling of linestrings
    Map<ILineString, Double> lE = new HashMap<ILineString, Double>();
    for (ILineString l : this.lines) {
      double pasVariable = l.length() / (double) (nbPts);
      lE.put(Operateurs.resampling(l, pasVariable), this.weights.get(l));
    }
    int min = Integer.MAX_VALUE;
    for (ILineString ll : lE.keySet()) {
      System.out.println(ll.getControlPoint().size()+"/"+nbPts);
      if(ll.getControlPoint().size()<min){
        min = ll.getControlPoint().size();
      }
    }

    IDirectPositionList lprovE = new DirectPositionList();
    for (int i = 0; i < min ; i++) {
      IDirectPosition pi = new DirectPosition();
      double newx = 0, newy = 0;
      double weight = 0.;
      for (ILineString ll : lE.keySet()) {
        newx +=  lE.get(ll) * ll.getControlPoint(i).getX();
        newy +=  lE.get(ll)* ll.getControlPoint(i).getY();
        weight+= lE.get(ll);
      }
      pi.setX(newx /weight);
      pi.setY(newy / weight);
      lprovE.add(pi);
    }
    return this.finalize(new GM_LineString(lprovE));
  }

}
