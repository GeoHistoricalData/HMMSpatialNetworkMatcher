package fr.ign.cogit.v2.manual.corrections.tag.criterions;

import java.util.Arrays;
import java.util.Map;

import nrc.fuzzy.FuzzySet;
import nrc.fuzzy.XValuesOutOfOrderException;
import nrc.fuzzy.YValueOutOfRangeException;
import v2.gh.Observation;
import v2.tagging.TaggingMassFunction;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ILineString;
import fr.ign.cogit.geoxygene.api.spatial.geomroot.IGeometry;
import fr.ign.cogit.v2.manual.corrections.tag.strokes.UpperHierarchicalStructureBuilder;

public class SinuosityMassFunction implements TaggingMassFunction{

        FuzzySet continuation_filiation_error_filter;
        Map<IGeometry, ILineString> mapping;

        public SinuosityMassFunction() {

            try {
                double[] cxval = new double[] { 0, 0.005,
                        0.01 };
                double[] cyval = new double[] { 0, 0, 1. };
                this.continuation_filiation_error_filter = new FuzzySet(cxval, cyval, 3);


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

            double sin1 = Double.parseDouble(to.getAttribute("sinuosity").toString());
            double sin2 = Double.parseDouble(ho.getAttribute("sinuosity").toString());
            
            double value = Math.abs(sin1 - sin2);
            
            if(Double.isInfinite(sin1) || Double.isInfinite(sin2)){
                if(Arrays.equals(hypothesis, new byte[] { 1, 1})){
                    return 1f;
                }
                return 0.f;
            } 


            if (Arrays.equals(hypothesis, new byte[] { 1, 0})) {
                return (float) this.continuation_filiation_error_filter.getMembership(value);
            } 
            else if(Arrays.equals(hypothesis, new byte[] { 1, 1})){
                return (float)( 1f - this.continuation_filiation_error_filter.getMembership(value));
            }
//            else if(Arrays.equals(hypothesis, new byte[] { 1, 0, 0 })) {
//                return (float) this.continuation_filiation_error_filter.getMembership(value);
//            } 
//            else if(Arrays.equals(hypothesis, new byte[] { 0, 1, 0 })) {
//                return (float) this.continuation_filiation_error_filter.getMembership(value);
//            } 
//            else if(Arrays.equals(hypothesis, new byte[] { 0, 0, 1 })) {
//                return (float) this.continuation_filiation_error_filter.getMembership(value);
//            } 
            return 0;

        }
        public String toString(){
            return "Sinuosity";
        }
}
