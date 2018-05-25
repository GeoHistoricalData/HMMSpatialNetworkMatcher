package fr.ign.cogit.HMMSpatialNetworkMatcher.spatial_impl.spatial_hmm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import fr.ign.cogit.HMMSpatialNetworkMatcher.api.IHiddenState;
import fr.ign.cogit.HMMSpatialNetworkMatcher.api.IHiddenStateCollection;
import fr.ign.cogit.HMMSpatialNetworkMatcher.api.IObservation;
import fr.ign.cogit.HMMSpatialNetworkMatcher.matching.core.ParametersSet;
import fr.ign.cogit.HMMSpatialNetworkMatcher.spatial_impl.utils.Combinations;
import fr.ign.cogit.geoxygene.feature.Population;
/**
 * Implementation IHiddenStateCollection based on a Population of HiddenState
 * @author bcostes
 *
 */
public class HiddenStatePopulation extends Population<FeatHiddenState> implements IHiddenStateCollection{

  /**
   * Filter this population using euclidean distance threshold
   * and spatial index.
   * Return HiddenState that are less than SELECTION_THRESHOLD meters from obs, and clusters of
   * these HiddenState (CompositeHiddenState).
   */
  @Override
  public Collection<IHiddenState> filter(IObservation obs) {
    FeatObservation o = (FeatObservation) obs;
    Collection<FeatHiddenState> resultTmp = new ArrayList<FeatHiddenState>();
    resultTmp.addAll(this.select(o.getGeom(), ParametersSet.get().SELECTION_THRESHOLD));
    Combinations<FeatHiddenState> combinationsStates = new Combinations<>();
    List<List<FeatHiddenState>> combinations =  combinationsStates.getAllCombinations(resultTmp);
    Collection<IHiddenState> result = new ArrayList<IHiddenState>();
    for(List<FeatHiddenState> hdl : combinations) {
      if(hdl.size() == 1) {
        result.add(hdl.get(0));
      }
      else {
        result.add(new CompositeHiddenState(hdl));
      }
    }
    return result;
  }
  @Override
  public List<IHiddenState> toList() {
    List<IHiddenState> list = new ArrayList<>();
    list.addAll(this.getElements());
    return list;
  }


}
