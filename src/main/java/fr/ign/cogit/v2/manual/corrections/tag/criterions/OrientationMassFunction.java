package fr.ign.cogit.v2.manual.corrections.tag.criterions;

import java.util.Arrays;

import nrc.fuzzy.FuzzySet;
import nrc.fuzzy.XValuesOutOfOrderException;
import nrc.fuzzy.YValueOutOfRangeException;
import v2.gh.Observation;
import v2.tagging.TaggingMassFunction;

public class OrientationMassFunction implements TaggingMassFunction{
    //Frechet criteria = new Frechet(0.1);

    FuzzySet continuation_filiation_error_filter;


    public OrientationMassFunction() {
        try {
            double[] cxval = new double[] { 0, 5,
                   10 };
            double[] cyval = new double[] { 0, 0, 1 };
            this.continuation_filiation_error_filter = new FuzzySet(cxval, cyval, 3);

//            double[] fxval = new double[] { 15,
//                    30};
//            double[] fyval = new double[] { 0, 1,};
//            this.filiation_error_filter = new FuzzySet(fxval, fyval, 2);

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

   //     Angle a1 = Operateurs.directionPrincipale(to.getGeom().coord());
     //   Angle a2 = Operateurs.directionPrincipale(ho.getGeom().coord());
       // double value = Angle.ecart(a1, a2).getValeur()* 180./Math.PI;
        
        double value =  Double.parseDouble(to.getAttribute("orientation").toString());
        
        if (Arrays.equals(hypothesis, new byte[] { 1, 0 })) {
            return (float) this.continuation_filiation_error_filter.getMembership(value);
       }else if (Arrays.equals(hypothesis, new byte[] { 0, 1})) {
           return (float) (1f- this.continuation_filiation_error_filter.getMembership(value));
        } 
        return 0;
    }


    public String toString() {
      return "OrientationFunc";
    }
}