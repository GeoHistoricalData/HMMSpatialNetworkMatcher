package fr.ign.cogit.v2.utils.clustering;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.feature.DefaultFeature;
import fr.ign.cogit.geoxygene.feature.Population;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPosition;
import fr.ign.cogit.geoxygene.spatial.geomprim.GM_Point;
import fr.ign.cogit.geoxygene.util.attribute.AttributeManager;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileReader;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileWriter;

public class KMeanMain {

    public static void main(String[] args) {

        IPopulation<IFeature> points = ShapefileReader.read("/home/bcostes/Bureau/points.shp");


        double[][] Tableau = new double[points.size()][2];
        int cpt=0;
        for(IFeature f: points){
            IDirectPosition p= f.getGeom().coord().get(0);
            Tableau[cpt][0]  = p.getX(); 
            Tableau[cpt][1]  = p.getY(); 
            cpt++;
        }


        int nbKMeans = 3;
        int nbClusters = 5;
        double Epsilon = 0.000001;
        boolean UnIndividuParClusterMinimum = true;
        boolean CentrerReduire = false;


                KMeans m = new KMeans(new EuclideanMetric());
                m.Calculer(Tableau, Epsilon, UnIndividuParClusterMinimum, CentrerReduire, "/home/bcostes/Bureau/cah.txt");



//
//                        KMeans m= new KMeans(new EuclideanMetric());
//                        m.Calculer(Tableau, nbClusters, Epsilon, UnIndividuParClusterMinimum, CentrerReduire, null);
                        
                        
                        
                System.out.println(m.getInertieIntraClasse());
                System.out.println(m.getInertieInterClasse());
                System.out.println(m.getNbIterations());
                System.out.println(m.getNbIndividus());
                System.out.println(m.getClusterResultat().length);
        //
        //        m.ComputeBestRepresentant();
        //
        //        System.out.println(m.getBestRepresentant().length);
        //
        //        IPopulation<IFeature> out = new Population<IFeature>();
        //        for(int i=0; i< points.size(); i++){
        //            IFeature f = new DefaultFeature(new GM_Point(new DirectPosition(Tableau[i][0], Tableau[i][1])));
        //            AttributeManager.addAttribute(f,"classe", m.getClusterResultat()[i], "Integer");
        //            if(i == m.getBestRepresentant()[m.getClusterResultat()[i]]){
        //                AttributeManager.addAttribute(f,"best", true, "Boolean");
        //            }
        //            else{
        //                AttributeManager.addAttribute(f,"best", false, "Boolean");
        //            }
        //            out.add(f);
        //        }
        //        ShapefileWriter.write(out, "/home/bcostes/Bureau/classif2.shp");
        //
        //        System.exit(0);

       // KMeansFormesFortes clusterer = new KMeansFormesFortes(new EuclideanMetric());
        //
        //
        //        clusterer.calculateAllStablePartition(Tableau, nbKMeans, nbClusters, Epsilon, UnIndividuParClusterMinimum, CentrerReduire);
        //        clusterer.exportStablePartitions("/home/bcostes/Bureau/stablepartitions.txt");

//        clusterer.Calculer(Tableau, nbKMeans, nbClusters,
//                Epsilon, UnIndividuParClusterMinimum, CentrerReduire);
//        IPopulation<IFeature> out = new Population<IFeature>();
//        for(int i=0; i< points.size(); i++){
//            IFeature f = new DefaultFeature(new GM_Point(new DirectPosition(Tableau[i][0], Tableau[i][1])));
//            AttributeManager.addAttribute(f,"classe", clusterer.getKMeansResultat().getClusterResultat()[i], "Integer");
//            out.add(f);
//        }
//        ShapefileWriter.write(out, "/home/bcostes/Bureau/classif3.shp");
//                System.out.println(clusterer.getKMeansResultat().getInertieIntraClasse());
//                System.out.println(clusterer.getKMeansResultat().getInertieInterClasse());
//
//        System.exit(0);

    }

}
