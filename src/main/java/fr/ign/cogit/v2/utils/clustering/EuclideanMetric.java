package fr.ign.cogit.v2.utils.clustering;

public class EuclideanMetric implements Metric{

    @Override
    public double Distance(double[] firstVector, double[] secondVector) {
        if(firstVector.length != secondVector.length){
            throw new Error("Vectors do not have the same dimension.");
        }
        else{
            double d= 0;
            for(int i=0; i< firstVector.length; i++){
                d += (firstVector[i] - secondVector[i])*(firstVector[i] - secondVector[i]);
            }  
            return Math.sqrt(d);
        }
    }

}
