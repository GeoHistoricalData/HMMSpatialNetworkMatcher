package fr.ign.cogit.v2.tag;

import nrc.fuzzy.FuzzySet;
import nrc.fuzzy.XValuesOutOfOrderException;
import nrc.fuzzy.YValueOutOfRangeException;


public class FuzzyTemporalInterval extends FuzzySet implements Comparable<FuzzyTemporalInterval>{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public FuzzyTemporalInterval(double[] ds, double[] ds2, int i) throws XValuesOutOfOrderException, YValueOutOfRangeException {
      super(ds, ds2,i);
    }
    
    public boolean equals(Object t) {
        return ((FuzzySet)(this)).equals((FuzzySet)t);
    };
    
    @Override
    public int hashCode() {
        String s = "";
        for(int i=0; i<numPoints; i++)
        {
            s+=Double.toString(this.getPoint(i).getX()) + Double.toString(this.getPoint(i).getY()) ;
           
        }
        return s.hashCode();
    }
    
    public static double ChengFuzzyRank(FuzzyTemporalInterval f) {
      double[] fc = f.centroid();
      double rf = Math.sqrt(fc[0] * fc[0] + fc[1] * fc[1]);
      return  Math.abs(rf);
    }

    public double[] centroid() {
      double a = this.getX(0);
      double d = this.getX(this.size()-1);
      double b =0;
      double c = 0;
      if(this.size() == 3){
        b = this.getX(1);
        c = b;
      }
      else{
        b = this.getX(1);
        
        c= this.getX(2);
      }
      double w= this.getY(1);
      double x0 = (w * (d * d - 2 * c * c + 2 * b
          * b - a * a + d * c - a * b) + 3*(c*c-b*b))
          / (3 * w * (d - c + b - a) + 6 * (c - b));

      double y0 = (w / 3.0)
          * (1.0 + (((b + c) - (a + d) * (1.0 - w)) / ((b
              + c - a - d) + 2 * (a + d) * w)));
      double[] pt = new double[2];
      pt[0] = x0;
      pt[1] = y0;
      return pt;
    }
    

    

    @Override
    public int compareTo(FuzzyTemporalInterval o) {
      double a = ChengFuzzyRank(this);
      double b= ChengFuzzyRank(o);
      if(a < b){
        return -1;
      }
      if(a > b){
        return 1;
      }
      if(a == b){
        return 0;
      }
      return 0;
    }
//
//    @Override
//    public int compareTo(FuzzyTemporalInterval o) {
//       double a1 = this.getX(0);
//       double a2 = o.getX(0);
//       if(a1 < a2){
//           return -1;
//       }
//       else if(a1>a2){
//           return 1;
//       }
//       double d1 = this.getX(3);
//       double d2 = o.getX(3);
//       if(d1 < d2){
//           return -1;
//       }
//       else if(d1>d2){
//           return 1;
//       }
//       double b1 = this.getX(1);
//       double b2 = o.getX(1);
//       if(b1 < b2){
//           return -1;
//       }
//       else if(b1>b2){
//           return 1;
//       }
//       double c1 = this.getX(2);
//       double c2 = o.getX(2);
//       if(c1 < c2){
//           return -1;
//       }
//       else if(c1>c2){
//           return 1;
//       }
//
//      return 0;
//
//    }
    
    public static void main(String args[]){
      FuzzyTemporalInterval t1;
      try {
        t1 = new FuzzyTemporalInterval(new double[]{1825,1827,1836,1839},new double[]{0,1,1,0}, 4);
        FuzzyTemporalInterval t2 = new FuzzyTemporalInterval(new double[]{1848,1849,1849,1850},new double[]{0,1,1,0}, 4);
        FuzzyTemporalInterval t3  =  new FuzzyTemporalInterval(new double[]{1808,1810,1836,1853},new double[]{0,1,1,0}, 4);
        FuzzyTemporalInterval t4  =  new FuzzyTemporalInterval(new double[]{1887,1888,1888,1889},new double[]{0,1,1,0}, 4);
   
        System.out.println(FuzzyTemporalInterval.ChengFuzzyRank(t1));
        System.out.println(FuzzyTemporalInterval.ChengFuzzyRank(t2));
        System.out.println(FuzzyTemporalInterval.ChengFuzzyRank(t3));


      } catch (XValuesOutOfOrderException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (YValueOutOfRangeException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

    }

}
