package fr.ign.cogit.v2.manual.corrections.tag.criterions;

import java.util.Arrays;

import nrc.fuzzy.FuzzySet;
import nrc.fuzzy.XValuesOutOfOrderException;
import nrc.fuzzy.YValueOutOfRangeException;
import v2.gh.Observation;
import v2.tagging.TaggingMassFunction;
import fr.ign.cogit.geoxygene.matching.dst.sources.text.SimilaritiesDist;

public class StreetNamesMassFunction  implements TaggingMassFunction{

    FuzzySet continuation_filiation_filter;
    FuzzySet continuation_filiation_error_filter;


    public StreetNamesMassFunction() {
        try {
            double[] cxval = new double[] { 0, 1./4.,
                    1./3. };
            double[] cyval = new double[] { 1, 1, 0 };
            this.continuation_filiation_filter = new FuzzySet(cxval, cyval, 3);

            double[] fxval = new double[] { 1./4.,
                    1./3.};
            double[] fyval = new double[] { 0, 1,};
            this.continuation_filiation_error_filter = new FuzzySet(fxval, fyval, 2);

        } catch (XValuesOutOfOrderException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (YValueOutOfRangeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    @Override
    public float massOf(byte[] hypothesis, Observation to, Observation ho){



        String name1 = to.getAttribute("NOM_ENTIER").toString();
        String name2 = ho.getAttribute("NOM_ENTIER").toString();
        
        
        double value = 0.;
        if(name1 == null || name2 == null || name1.equals("") || name2.equals("")){
            return 0.5f;
        }
        else{
            SimilaritiesDist d =new SimilaritiesDist("", "");
            value = d.compute(name1, name2);
        }



        if (Arrays.equals(hypothesis, new byte[] { 1, 1, 0 })) {
            return (float) this.continuation_filiation_filter.getMembership(value);
        } else if (Arrays.equals(hypothesis, new byte[] { 1, 1, 1 })) {
            return (float) this.continuation_filiation_error_filter.getMembership(value);
        } 

        return 0;

    }
    public String toString(){
        return "StreetNames";
    }
}
