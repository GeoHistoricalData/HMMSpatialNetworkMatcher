package fr.ign.cogit.v2.tag.enrichment.stgeocoding.evidence;

import java.util.ArrayList;
import java.util.List;

import fr.ign.cogit.geoxygene.matching.dst.evidence.codec.EvidenceCodec;
import fr.ign.cogit.geoxygene.matching.dst.operators.CombinationAlgos;
import fr.ign.cogit.geoxygene.matching.dst.util.Pair;
import fr.ign.cogit.v2.tag.enrichment.stgeocoding.GeocodeType;
import fr.ign.cogit.v2.tag.enrichment.stgeocoding.TextualAdress;

public class GeocodeTypeSource  extends GeocodeSource {
  
  private final double beta = 1;

  public GeocodeTypeSource() throws Exception {
  }

  /**
   * Evaluation.
   */
  @Override
  public List<Pair<byte[], Float>> evaluate(TextualAdress reference,
      final List<GeocodeHypothesis> candidates, EvidenceCodec<GeocodeHypothesis> codec) {
    
 
    List<Pair<byte[], Float>> weightedfocalset = new ArrayList<Pair<byte[], Float>>();

    float sum = 0;
    for (GeocodeHypothesis h : candidates) {
      float distance = 0f;
      if (h.getDecoratedFeature().type == GeocodeType.EXACT) {
        distance = 1f;
      }
      else if (h.getDecoratedFeature().type == GeocodeType.INTERPOLATION) {
        distance = 2f;
      }
      else if (h.getDecoratedFeature().type == GeocodeType.POOR_INTERPOLATION) {
        distance = 3f;
      }
      else if (h.getDecoratedFeature().type == GeocodeType.STREET) {
        distance = 4f;
      }
      else if (h.getDecoratedFeature().type == GeocodeType.STREET_INF) {
        distance = 5f;
      }
      else if (h.getDecoratedFeature().type == GeocodeType.STREET_SUP) {
        distance = 6f;
      }
      else if (h.getDecoratedFeature().type == GeocodeType.UNCERTAIN) {
        distance = 7f;
      }
      
      double d =Math.exp(-distance / beta);
      
      byte[] encoded = codec.encode(new GeocodeHypothesis[] { h });
      weightedfocalset.add(new Pair<byte[], Float>(encoded, (float)d));
      sum += distance;
    }
    for (Pair<byte[], Float> st : weightedfocalset) {
      st.setSecond(st.getSecond() / sum);
    }
    CombinationAlgos.sortKernel(weightedfocalset);

    // System.out.println("   " + Arrays.toString(weightedfocalset.get(0).getFirst()));
    return weightedfocalset;
  }

  @Override
  public String getName() {
    // TODO using I18N
    return "Geocode type accuracy";
  }



}
