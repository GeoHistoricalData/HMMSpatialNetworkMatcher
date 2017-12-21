package fr.ign.cogit.v2.tag.enrichment.stgeocoding.evidence;

import java.util.ArrayList;
import java.util.List;

import fr.ign.cogit.geoxygene.matching.dst.evidence.codec.EvidenceCodec;
import fr.ign.cogit.geoxygene.matching.dst.operators.CombinationAlgos;
import fr.ign.cogit.geoxygene.matching.dst.util.Pair;
import fr.ign.cogit.v2.tag.FuzzyTemporalInterval;
import fr.ign.cogit.v2.tag.enrichment.stgeocoding.TextualAdress;

public class TemporalDistanceSource extends GeocodeSource {

  private final double alpha = 20.;



  public TemporalDistanceSource() throws Exception {
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
      float distance = (float)Math.abs(FuzzyTemporalInterval.ChengFuzzyRank(h.getDecoratedFeature().date) -
          reference.getDate());
      
      double d = Math.exp(-distance / alpha);
      
//      if(distance <= this.minX){
//        distance = this.maxY;
//      }
//      else if(distance > this.maxX) {
//        distance = this.minY;
//      }
//      else{
//        float a= (this.maxY - this.minY)/(this.minX - this.maxX);
//        float b = this.maxY - a * this.minX;
//        distance = a*distance + b;
//      }
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
    return "Temporal distance";
  }
}
