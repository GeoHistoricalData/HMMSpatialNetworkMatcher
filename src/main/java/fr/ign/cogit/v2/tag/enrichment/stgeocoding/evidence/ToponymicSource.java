package fr.ign.cogit.v2.tag.enrichment.stgeocoding.evidence;

import java.util.ArrayList;
import java.util.List;

import fr.ign.cogit.geoxygene.matching.dst.evidence.codec.EvidenceCodec;
import fr.ign.cogit.geoxygene.matching.dst.operators.CombinationAlgos;
import fr.ign.cogit.geoxygene.matching.dst.util.Pair;
import fr.ign.cogit.v2.tag.enrichment.stgeocoding.TextualAdress;
import geocodage.LexicalTools;

public class ToponymicSource extends GeocodeSource {

  private final double gamma = 1;


  public ToponymicSource() throws Exception {
  }

  public List<Pair<byte[], Float>> evaluate(TextualAdress reference,
      final List<GeocodeHypothesis> candidates, EvidenceCodec<GeocodeHypothesis> codec) {

    List<Pair<byte[], Float>> weightedfocalset = new ArrayList<Pair<byte[], Float>>();

    float sum = 0;
    for (GeocodeHypothesis h : candidates) {
      float distance = this.compute(reference, h);
        distance =this.compute(reference, h);
       // System.out.println(distance);
        byte[] encoded = codec.encode(new GeocodeHypothesis[] { h });
        weightedfocalset.add(new Pair<byte[], Float>(encoded, distance));
        sum += distance;

    }
    for (Pair<byte[], Float> st : weightedfocalset) {
      st.setSecond(st.getSecond() / sum);
    }
    CombinationAlgos.sortKernel(weightedfocalset);
    return weightedfocalset;
  }

  private float compute(TextualAdress reference, GeocodeHypothesis h) {
  double d = LexicalTools.lexicalSimilarityCoeff(LexicalTools.parse2(reference.getOriginalName()), 
        LexicalTools.parse2(h.getDecoratedFeature().name));
    
     return  (float)Math.exp(-d / gamma);
  }

  @Override
  public String getName() {
    // TODO using I18N
    return "Topnymic source";
  }




}

