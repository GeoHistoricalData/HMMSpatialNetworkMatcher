package fr.ign.cogit.v2.manual.corrections.tag.criterions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import nrc.fuzzy.FuzzySet;
import nrc.fuzzy.XValuesOutOfOrderException;
import nrc.fuzzy.YValueOutOfRangeException;
import v2.gh.Observation;
import v2.tagging.TaggingMassFunction;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ILineString;
import fr.ign.cogit.geoxygene.contrib.geometrie.Distances;

public class FrechetMassFunction implements TaggingMassFunction{
    //Frechet criteria = new Frechet(0.1);

    FuzzySet continuation_filter;
    FuzzySet filiation_filter;
    FuzzySet error_filter;

    public FrechetMassFunction(float delta_distorsion,
        float max_continuation_value, float min_error_value) {
      try {
        double[] cxval = new double[] { 0, max_continuation_value,
            max_continuation_value + delta_distorsion, };
        double[] cyval = new double[] { 1, 1, 0 };
        this.continuation_filter = new FuzzySet(cxval, cyval, 3);

        double[] fxval = new double[] { max_continuation_value,
            delta_distorsion + max_continuation_value,
            min_error_value + delta_distorsion };
        double[] fyval = new double[] { 0, 1, 0 };
        this.filiation_filter = new FuzzySet(fxval, fyval, 3);

        double[] exval = new double[] {
            delta_distorsion + max_continuation_value,
            min_error_value + delta_distorsion };
        double[] eyval = new double[] { 0, 1 };

        this.error_filter = new FuzzySet(exval, eyval, 2);
      } catch (XValuesOutOfOrderException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (YValueOutOfRangeException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }

    @Override
    public float massOf(byte[] hypothesis, Observation to, Observation ho) {
      Collection<Observation> tail = new ArrayList<Observation>();
      tail.add(to);
      Collection<Observation> head = new ArrayList<Observation>();
      head.add(ho);
      float value = this.evaluate((ILineString) to.getGeometry(),
          (ILineString) ho.getGeometry());
      

      if (Arrays.equals(hypothesis, new byte[] { 1, 0, 0 })) {
        return (float) this.continuation_filter.getMembership(value);
      } else if (Arrays.equals(hypothesis, new byte[] { 0, 1, 0 })) {
        return (float) this.filiation_filter.getMembership(value);
      } else if (Arrays.equals(hypothesis, new byte[] { 0, 0, 1 })) {
        return (float) this.error_filter.getMembership(value);
      }
      return 0;
    }

    private float evaluate(ILineString left, ILineString right) {
  //  Double std = Distances.hausdorff(left, right);
  //  Double reverse = Distances.hausdorff(left, right.reverse());
//      return (float) Math.min(std, reverse);
      Double std = Math.min(Distances.premiereComposanteHausdorff(left, right),
          Distances.premiereComposanteHausdorff(left, right.reverse()));
      Double reverse = Math.min(
          Distances.premiereComposanteHausdorff(right, left),
          Distances.premiereComposanteHausdorff(right, left.reverse()));
  //  Double std = Distances.hausdorff(left, right);
  //  Double reverse = Distances.hausdorff(left, right.reverse());
  //   Double std =
  //   fr.ign.cogit.geoxygene.distance.Frechet.partialFrechet(right,left );
  //   Double reverse =
  //   fr.ign.cogit.geoxygene.distance.Frechet.partialFrechet(left,right );
      return (float) Math.min(std, reverse);
    }

    public String toString() {
      return "FrechetFunc";
    }

}
