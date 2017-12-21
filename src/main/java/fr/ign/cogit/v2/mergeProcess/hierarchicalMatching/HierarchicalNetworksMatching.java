package fr.ign.cogit.v2.mergeProcess.hierarchicalMatching;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.apache.commons.math3.stat.descriptive.rank.Median;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IFeatureCollection;
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPositionList;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IEnvelope;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ILineString;
import fr.ign.cogit.geoxygene.api.spatial.geomroot.IGeometry;
import fr.ign.cogit.geoxygene.contrib.cartetopo.Arc;
import fr.ign.cogit.geoxygene.contrib.cartetopo.CarteTopo;
import fr.ign.cogit.geoxygene.contrib.cartetopo.Noeud;
import fr.ign.cogit.geoxygene.contrib.geometrie.Angle;
import fr.ign.cogit.geoxygene.contrib.geometrie.Distances;
import fr.ign.cogit.geoxygene.contrib.geometrie.Operateurs;
import fr.ign.cogit.geoxygene.distance.Frechet;
import fr.ign.cogit.geoxygene.feature.DefaultFeature;
import fr.ign.cogit.geoxygene.feature.Population;
import fr.ign.cogit.geoxygene.matching.hmmm.HMMMapMatcher;
import fr.ign.cogit.geoxygene.matching.hmmm.HMMMapMatcher.Node;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPosition;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPositionList;
import fr.ign.cogit.geoxygene.spatial.coordgeom.GM_LineString;
import fr.ign.cogit.geoxygene.spatial.coordgeom.GM_Polygon;
import fr.ign.cogit.geoxygene.spatial.geomprim.GM_Point;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileReader;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileWriter;
import fr.ign.cogit.geoxygene.util.index.Tiling;

public class HierarchicalNetworksMatching {

  static double sigmaZ = 5;
  static double selection = 10.;
  static double beta =1.;
  static double distanceLimit = 7.;

  static double angleMax = 5;
  //  double sigmaZ = 100.;
  //  double selection = 250.;
  //  double beta =1.;
  //  double distanceLimit = 100.;



  private Map<Arc, Set<Arc>> matching = new HashMap<Arc, Set<Arc>>();

  private CarteTopo networkRef, networkComp, networkRefLocal, networkCompLocal;

  public HierarchicalNetworksMatching(String shp1, String shp2, boolean vector_georeferencing, boolean auto_thresholding, double max_dist_auto_threshold){
    IPopulation<IFeature> in = ShapefileReader.read(shp1);
    this.networkRef = new CarteTopo("network ref");
    IPopulation<Arc> arcsRef = this.networkRef.getPopArcs();
    for(IFeature f: in){
      Arc a = arcsRef.nouvelElement();
      a.setGeom(new GM_LineString(f.getGeom().coord()));
      //a.addCorrespondant(f);
    }
    this.networkRef.creeTopologieArcsNoeuds(0);
    this.networkRef.creeNoeudsManquants(0);
    this.networkRef.rendPlanaire(0);
   // this.networkRef.filtreNoeudsSimples();
    for(Arc a : this.networkRef.getPopArcs()){
      a.addCorrespondant(new DefaultFeature((ILineString)a.getGeometrie().clone()));
    }
    //    this.networkRef.creeTopologieArcsNoeuds(0);
    //    this.networkRef.rendPlanaire(0);

    // ShapefileWriter.write(this.networkRef.getPopArcs(), "/home/bcostes/Bureau/score_matching/verniquet_vasserot/verniquet.shp");

    in= ShapefileReader.read(shp2);
    this.networkComp = new CarteTopo("network comp");
    IPopulation<Arc> arcsComp = this.networkComp.getPopArcs();
    for(IFeature f: in){
      Arc a = arcsComp.nouvelElement();
      a.setGeom(new GM_LineString(f.getGeom().coord()));
      // a.addCorrespondant(f);
    }
    this.networkComp.creeTopologieArcsNoeuds(0);
    this.networkComp.creeNoeudsManquants(0);
    this.networkComp.rendPlanaire(0);
    //this.networkComp.filtreNoeudsSimples();
    for(Arc a : this.networkComp.getPopArcs()){
      a.addCorrespondant(new DefaultFeature((ILineString)a.getGeometrie().clone()));
    }
    //    this.networkComp.creeTopologieArcsNoeuds(0);
    //    this.networkComp.rendPlanaire(0);

   // ShapefileWriter.write(this.networkComp.getPopArcs(), "/home/bcostes/Bureau/score_matching/jacoubet_poubelle/poubelle.shp");
    //  System.exit(0);

    this.networkRef.getPopArcs().initSpatialIndex(Tiling.class, false);
    this.networkComp.getPopArcs().initSpatialIndex(Tiling.class, false);


    if(vector_georeferencing){
      if(max_dist_auto_threshold <0){
        max_dist_auto_threshold = 30.;
      }
      VectorGeoreferencing vg = new VectorGeoreferencing(this.networkRef, this.networkComp, true, max_dist_auto_threshold);
      vg.vector_georeferencing();
    }


    if(auto_thresholding){
      if(max_dist_auto_threshold <=0){
        max_dist_auto_threshold = 30.;
      }
      this.auto_thresholding(max_dist_auto_threshold);
    }


    // System.out.println(this.networkRef.getPopArcs().size()+" "+ this.networkComp.getPopArcs().size());

    IEnvelope env = arcsRef.envelope();
    double xmin = env.minX();
    double xmax = env.maxX();
    double ymin = env.minY();
    double ymax = env.maxY();

    double pas = 1500.;

   // double pas = 200.;
    Map<Arc, Set<Arc>> mapArcsFinal = new HashMap<Arc, Set<Arc>>();
    int size = (int)((ymax - ymin + 10) / pas);
    int cpt = 1;
    for(double y = (ymin - 10); y< ymax; y += pas){
      System.out.println(cpt + " / " + size);
      cpt++;
      for(double x = (xmin - 10); x<xmax; x += pas){

        IDirectPositionList list = new DirectPositionList();
        list.add(new DirectPosition(x-20,y-20));
        list.add(new DirectPosition(x+pas+20,y-20));
        list.add(new DirectPosition(x+pas+20, y+pas+20));
        list.add(new DirectPosition(x-20,y+pas+20));
        list.add(new DirectPosition(x-20,y-20));

        IGeometry envTmp = new GM_Polygon(new GM_LineString(list));


        this.networkRefLocal = new CarteTopo("network ref Local");
        this.networkRefLocal.getPopArcs().addAll(this.networkRef.getPopArcs().select(envTmp));
        this.networkRefLocal.creeTopologieArcsNoeuds(0);
        this.networkRefLocal.creeNoeudsManquants(0);
        this.networkRefLocal.rendPlanaire(0);


        this.networkCompLocal = new CarteTopo("network comp Local");
        this.networkCompLocal.getPopArcs().addAll(this.networkComp.getPopArcs().select(envTmp));
        this.networkCompLocal.creeTopologieArcsNoeuds(0);
        this.networkCompLocal.creeNoeudsManquants(0);
        this.networkCompLocal.rendPlanaire(0);

        if(this.networkCompLocal.getPopArcs().isEmpty() || this.networkRefLocal.getPopArcs().isEmpty()){
          continue;
        }


        this.networkRefLocal.getPopArcs().initSpatialIndex(Tiling.class, false);
        this.networkCompLocal.getPopArcs().initSpatialIndex(Tiling.class, false);


        //Map<HMMCluster, Set<HMMCluster>> matchingC = this.matchClusters();
        // System.out.println(matchingC.size());



        Map<Arc, Set<Arc>> mapArcs = this.match(/*matchingC*/);

        CarteTopo t = this.networkCompLocal;
        this.networkCompLocal = this.networkRefLocal;
        this.networkRefLocal = t;



        //matchingC = this.matchClusters();
        //System.out.println(matchingC.size());



        Map<Arc, Set<Arc>> mapArcs2 = this.match(/*matchingC*/);




        for(Arc a: this.networkCompLocal.getPopArcs()){
          if(!mapArcs.containsKey(a)){
            continue;
          }
          for(Arc a2: mapArcs.get(a)){
            if(!mapArcs2.containsKey(a2)){
              continue;
            }
            if(mapArcs2.get(a2).contains(a)){
              if(mapArcsFinal.containsKey(a)){
                mapArcsFinal.get(a).add(a2);
                this.matching.get(a).add(a2);
              }
              else{
                Set<Arc> set = new HashSet<Arc>();
                set.add(a2);
                mapArcsFinal.put(a, set);
                this.matching.put(a, set);
              }
            }
          }
        }  



        for(Arc a: mapArcs.keySet()){
          for(Arc a2: mapArcs.get(a)){
            if(!mapArcs2.containsKey(a2) || !mapArcs2.get(a2).contains(a)){
              // ok si orientation compatible et duistance
              if(Math.min(Distances.premiereComposanteHausdorff(a.getGeometrie(), a2.getGeometrie()),
                  Distances.premiereComposanteHausdorff(a2.getGeometrie(), a.getGeometrie())) < this.selection){
                ILineString lineRef = new GM_LineString(a.getGeometrie().getControlPoint());
                IDirectPosition p11 = lineRef.startPoint();
                IDirectPosition p12 = lineRef.endPoint();
                ILineString lineComp =  new GM_LineString(a2.getGeometrie().getControlPoint());
                IDirectPosition p21 = lineComp.startPoint();
                IDirectPosition p22 = lineComp.endPoint();
                IDirectPosition pmin1 = null, pmin2 = null;
                if(p11.distance(p21) < Math.min(p11.distance(p22),
                    Math.min(p12.distance(p21), p12.distance(p22)))){
                  pmin1 = p11;
                  pmin2 = p21;
                }
                else if(p11.distance(p22) < Math.min(p11.distance(p21),
                    Math.min(p12.distance(p21), p12.distance(p22)))){
                  pmin1 = p11;
                  pmin2 = p22;
                }
                else if(p12.distance(p21) < Math.min(p11.distance(p22),
                    Math.min(p11.distance(p21), p12.distance(p22)))){
                  pmin1 = p12;
                  pmin2 = p21;
                }
                else{
                  pmin1 = p12;
                  pmin2 = p22;
                }
                if(pmin1.equals(p12)){
                  lineRef = lineRef.reverse();
                }
                if(pmin2.equals(p22)){
                  lineComp = lineComp.reverse();
                }

                Angle angleRef = Operateurs.directionPrincipaleOrientee(Operateurs.resampling(lineRef.coord(),5));
                Angle angleComp = Operateurs.directionPrincipaleOrientee(Operateurs.resampling(lineComp.coord(),5));
                double value =Angle.ecart(angleRef, angleComp).getValeur();
                if (Math.abs(value)*180./Math.PI < angleMax){


                  //                  //dernière vérif: longueur si pas impasse
                  //                   if((a.getNoeudIni().getEntrants().size()+ a.getNoeudIni().getSortants().size() != 1) &&
                  //                       (a.getNoeudFin().getEntrants().size()+ a.getNoeudFin().getSortants().size() !=1)){
                  //                     double length = a2.longueur();
                  //                     double length2 =0;
                  //                     for(Arc aa: mapArcs.get(a)){
                  //                       length += aa.longueur();
                  //                     }
                  //                     if(length < length2/1.5 || length > 1.5*length2){
                  //                       continue;
                  //                     }
                  //                   }



                  //c'est bon
                  if(this.matching.containsKey(a)){
                    this.matching.get(a).add(a2);
                  }
                  else{
                    Set<Arc>set = new HashSet<Arc>();
                    set.add(a2);
                    this.matching.put(a, set);
                  }
                }
              }
            }
          }
        }


        for(Arc a2: mapArcs2.keySet()){
          for(Arc a: mapArcs2.get(a2)){
            if(!mapArcs.containsKey(a) || !mapArcs.get(a).contains(a2)){
              // ok si orientation compatible et duistance
              if(Math.min(Distances.premiereComposanteHausdorff(a.getGeometrie(), a2.getGeometrie()),
                  Distances.premiereComposanteHausdorff(a2.getGeometrie(), a.getGeometrie()))< this.selection){
                ILineString lineRef = new GM_LineString(a.getGeometrie().getControlPoint());
                IDirectPosition p11 = lineRef.startPoint();
                IDirectPosition p12 = lineRef.endPoint();
                ILineString lineComp =  new GM_LineString(a2.getGeometrie().getControlPoint());
                IDirectPosition p21 = lineComp.startPoint();
                IDirectPosition p22 = lineComp.endPoint();
                IDirectPosition pmin1 = null, pmin2 = null;
                if(p11.distance(p21) < Math.min(p11.distance(p22),
                    Math.min(p12.distance(p21), p12.distance(p22)))){
                  pmin1 = p11;
                  pmin2 = p21;
                }
                else if(p11.distance(p22) < Math.min(p11.distance(p21),
                    Math.min(p12.distance(p21), p12.distance(p22)))){
                  pmin1 = p11;
                  pmin2 = p22;
                }
                else if(p12.distance(p21) < Math.min(p11.distance(p22),
                    Math.min(p11.distance(p21), p12.distance(p22)))){
                  pmin1 = p12;
                  pmin2 = p21;
                }
                else{
                  pmin1 = p12;
                  pmin2 = p22;
                }
                if(pmin1.equals(p12)){
                  lineRef = lineRef.reverse();
                }
                if(pmin2.equals(p22)){
                  lineComp = lineComp.reverse();
                }

                Angle angleRef = Operateurs.directionPrincipaleOrientee(Operateurs.resampling(lineRef.coord(),5));
                Angle angleComp = Operateurs.directionPrincipaleOrientee(Operateurs.resampling(lineComp.coord(),5));
                double value =Angle.ecart(angleRef, angleComp).getValeur();
                if (Math.abs(value)*180./Math.PI < angleMax){

                  //dernière vérif: longueur si pas impasse
                  //                   if((a2.getNoeudIni().getEntrants().size()+ a2.getNoeudIni().getSortants().size() != 1) &&
                  //                       (a2.getNoeudFin().getEntrants().size()+ a2.getNoeudFin().getSortants().size() !=1)){
                  //                     double length = a2.longueur();
                  //                     double length2 =0;
                  //                     for(Arc aa: mapArcs2.get(a2)){
                  //                       length += aa.longueur();
                  //                     }
                  //                     if(length < length2/1.5 || length > 1.5*length2){
                  //                       continue;
                  //                     }
                  //                   }


                  //c'est bon
                  if(this.matching.containsKey(a)){
                    this.matching.get(a).add(a2);
                  }
                  else{
                    Set<Arc>set = new HashSet<Arc>();
                    set.add(a2);
                    this.matching.put(a, set);
                  }
                }
              }
            }
          }
        }




        //        this.networkCompLocal.nettoyer();
        //        this.networkRefLocal.nettoyer();
        //        this.networkCompLocal = null;
        //        this.networkRefLocal = null;




      }
    }

    this.matching = mapArcsFinal;


  }


  private void auto_thresholding(double max_dist) {
    Map<Arc, Set<Arc>> result = new HashMap<Arc, Set<Arc>>();
    int cpt=0;
    for(Arc aref: this.networkComp.getPopArcs()){
      cpt++;
      System.out.println(cpt+" / " + this.networkComp.getPopArcs().size());
      result.put(aref, new HashSet<Arc>());
      Collection<Arc> candidates = this.networkRef.getPopArcs().select(aref.getGeom(), (max_dist+0.1*max_dist));
      for(Arc acomp: candidates){
        if(Frechet.partialFrechet(aref.getGeometrie(), acomp.getGeometrie()) > max_dist){
          continue;
        }
        ILineString lineRef = aref.getGeometrie();
        IDirectPosition p11 = lineRef.startPoint();
        IDirectPosition p12 = lineRef.endPoint();
        ILineString lineComp = acomp.getGeometrie();
        IDirectPosition p21 = lineComp.startPoint();
        IDirectPosition p22 = lineComp.endPoint();
        IDirectPosition pmin1 = null, pmin2 = null;
        if(p11.distance(p21) < Math.min(p11.distance(p22),
            Math.min(p12.distance(p21), p12.distance(p22)))){
          pmin1 = p11;
          pmin2 = p21;
        }
        else if(p11.distance(p22) < Math.min(p11.distance(p21),
            Math.min(p12.distance(p21), p12.distance(p22)))){
          pmin1 = p11;
          pmin2 = p22;
        }
        else if(p12.distance(p21) < Math.min(p11.distance(p22),
            Math.min(p11.distance(p21), p12.distance(p22)))){
          pmin1 = p12;
          pmin2 = p21;
        }
        else{
          pmin1 = p12;
          pmin2 = p22;
        }
        if(pmin1.equals(p12)){
          lineRef = lineRef.reverse();
        }
        if(pmin2.equals(p22)){
          lineComp = lineComp.reverse();
        }

        Angle angleRef = Operateurs.directionPrincipaleOrientee(lineRef.coord());
        Angle angleComp = Operateurs.directionPrincipaleOrientee(lineComp.coord());
        double value =Angle.ecart(angleRef, angleComp).getValeur();
        if (Math.abs(value)*180./Math.PI > 30.){
          continue;
        }
        result.get(aref).add(acomp);
      }
    }
    List<Double> values = new ArrayList<Double>();
    List<Double> valuesBeta = new ArrayList<Double>();
    List<Double> angles = new ArrayList<Double>();
    
    for(Arc a : result.keySet()){
      for(Arc a2: result.get(a)){
        ILineString le = Operateurs.resampling(a2.getGeometrie(), 10.);
        for(IDirectPosition p: le.getControlPoint()){
          if(p.equals(le.startPoint()) || p.equals(le.endPoint())){
            continue;
          }
          IDirectPosition proj = Operateurs.projection(p, a.getGeometrie());
          if(proj.equals(a.getGeometrie().startPoint()) || proj.equals(a.getGeometrie().endPoint())){
            continue;
          }
          double d = p.distance(proj);
          if(d > 1){
            values.add(d);
          }
        }
        Angle angleRef = Operateurs.directionPrincipaleOrientee(Operateurs.resampling(a.getGeometrie().coord(),5));
        Angle angleComp = Operateurs.directionPrincipaleOrientee(Operateurs.resampling(a2.getGeometrie().coord(),5));
        double value =Angle.ecart(angleRef, angleComp).getValeur();
        angles.add(Math.abs(value)*180./Math.PI);
      }
    }
    Median m = new Median();
    double[]valuesT = new double[values.size()];
    for(int i=0; i < valuesT.length; i++){
      valuesT[i] = values.get(i);
    }
    sigmaZ = m.evaluate(valuesT);

    Mean moy = new Mean();
    StandardDeviation std = new StandardDeviation();
    distanceLimit = moy.evaluate(valuesT);
    selection = 2*std.evaluate(valuesT) + distanceLimit;
    beta = 1.;

    
    FileWriter fw;
    try {
      fw = new FileWriter("/home/bcostes/Bureau/angles.txt");
      BufferedWriter bw = new BufferedWriter(fw);
      String s ="";
      for(Double d: angles){
        s+= d+ "\n";
      }
      bw.write(s);
      bw.flush();
      bw.close();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    System.out.println(sigmaZ +" "+ beta+" "+ selection+" "+ distanceLimit);
  }


  public Map<Arc, Set<Arc>> getMatching() {
    return matching;
  }



  public void setMatching(Map<Arc, Set<Arc>> matching) {
    this.matching = matching;
  }


  class MapMatching extends HMMMapMatcher{

    public MapMatching(IFeatureCollection<? extends IFeature> gpsPop,
        IFeatureCollection<? extends IFeature> network, double sigmaZ,
        double selection, double beta, double distanceLimit) {
      super(gpsPop, network, sigmaZ, selection, beta, distanceLimit);
    }
    public MapMatching(IFeatureCollection<? extends IFeature> gpsPop,
        IFeatureCollection<? extends IFeature> network, double sigmaZ,
        double selection, double beta, double distanceLimit, CarteTopo t) {
      super(gpsPop, network, sigmaZ, selection, beta, distanceLimit);
      this.networkMap = t;
    }


    @Override
    protected void importNetwork(IFeatureCollection<? extends IFeature> network) {
      // TODO Auto-generated method stub
    }

  } 


  public Map<Arc, Set<Arc>> match(/*Map<HMMCluster, Set<HMMCluster>> clustersMatching*/){
    Map<Arc, Set<Arc>> mapArcs = new HashMap<Arc, Set<Arc>>();
    List<HMMCluster> clustersRef = HMMCluster.buildStroke(this.networkRefLocal.getListeArcs(), Math.PI/4.);


    //    IPopulation<IFeature> strokes = new Population<IFeature>();
    //    for(HMMCluster c : clustersRef){
    //      strokes.add(new DefaultFeature(c.geom));
    //    }
    //    ShapefileWriter.write(strokes, "/home/bcostes/Bureau/HMM_matching/strokes_ref.shp");
    //    


    MapMatching matcher = new MapMatching(new Population<IFeature>(), this.networkCompLocal.getPopArcs()/*t.getPopArcs()*/, this.sigmaZ,
        this.selection, this.beta, this.distanceLimit, this.networkCompLocal/*t*/);


    for(HMMCluster clusterRef: clustersRef){

      //      CarteTopo t = new CarteTopo("f");
      //      for(HMMCluster c: clustersMatching.get(clusterRef)){
      //        t.getPopArcs().addAll(c.getCluster());
      //      }
      //      t.creeTopologieArcsNoeuds(0);
      //      t.creeNoeudsManquants(0);
      //      t.rendPlanaire(0);
      //      t.getPopArcs().initSpatialIndex(Tiling.class, false);


      IFeatureCollection<IFeature> gpsPop = new Population<IFeature>();
      IFeatureCollection<IFeature> gpsPopReverse = new Population<IFeature>();


      Map<IFeature, Arc> mapF = new HashMap<IFeature, Arc>();
      for(Arc a: clusterRef.getCluster()){

        //sampling thresold
        double d= Double.MAX_VALUE;
        Collection<Arc> neighbourhood =   this.networkCompLocal.getPopArcs().select(a.getGeom(), 2 * this.selection);
        for(IFeature f: neighbourhood){
          if(f.getGeom().length()< d){
            d = f.getGeom().length();
          }
        }
        double length = a.longueur();
        double samplingT = Math.max(1., Math.min(d/2., length/5.));    

        ILineString le = Operateurs.resampling(a.getGeometrie(), 
            samplingT);
        for(int i=1; i< le.getControlPoint().size()-1; i++){
          IDirectPosition pt = le.getControlPoint(i);
          IFeature f = new DefaultFeature(new GM_Point(pt));
          mapF.put(f, a);
          gpsPop.add(f);
        }
      }



      List<IFeature> arcsReverse = new ArrayList<IFeature>(gpsPop);
      Collections.reverse(arcsReverse);
      gpsPopReverse.addAll(gpsPop);   




      IFeatureCollection<? extends IFeature> popPts = matcher.getPoints();
      popPts.clear();
      @SuppressWarnings("unchecked")
      Population<IFeature> p = (Population<IFeature>)popPts;
      p.addAll(gpsPop);


      Map<IFeature, Arc> matchDirect= new HashMap<IFeature, Arc>();
      Map<IFeature, Arc> matchReverse= new HashMap<IFeature, Arc>();

      while(gpsPop.size() >2 ){
        Node result = matcher.computeTransitions();

        if(result == null || result.getStates() == null || result.getStates().isEmpty() ||
            matcher.getPoints().size() < 2){
          //on supprime le premier points
          Arc ll = mapF.get(gpsPop.get(0));
          Set<IFeature> pts = new HashSet<IFeature>();
          for(IFeature pp: mapF.keySet()){
            if(mapF.get(pp).equals(ll)){
              pts.add(pp);
            }
          }
          gpsPop.removeAll(pts);
          popPts = matcher.getPoints();
          popPts.clear();
          @SuppressWarnings("unchecked")
          Population<IFeature>p1 = (Population<IFeature>)popPts;
          p1.addAll(gpsPop);
          continue;
        }
        for(int i=0; i< matcher.getPoints().size(); i++){
          Arc matchedArcComp = result.getStates().get(i);
          //récupération du stroke apparié
          matchDirect.put(matcher.getPoints().get(i), matchedArcComp);
        }
        gpsPop.removeAll(matcher.getPoints());
        popPts = matcher.getPoints();
        popPts.clear();
        @SuppressWarnings("unchecked")
        Population<IFeature> p1 = (Population<IFeature>)popPts;
        p1.addAll(gpsPop);
      }
      popPts = matcher.getPoints();
      popPts.clear();
      @SuppressWarnings("unchecked")
      Population<IFeature> p2= (Population<IFeature>)popPts;
      p2.addAll(gpsPopReverse);



      while(gpsPopReverse.size() >2 ){
        Node result = matcher.computeTransitions();
        if(result == null || result.getStates() == null || result.getStates().isEmpty() ||
            matcher.getPoints().size() < 2){
          //on supprime le premier points
          Arc ll = mapF.get(gpsPopReverse.get(0));
          Set<IFeature> pts = new HashSet<IFeature>();
          for(IFeature pp: mapF.keySet()){
            if(mapF.get(pp).equals(ll)){
              pts.add(pp);
            }
          }
          gpsPopReverse.removeAll(pts);
          popPts = matcher.getPoints();
          popPts.clear();
          @SuppressWarnings("unchecked")
          Population<IFeature>p1 = (Population<IFeature>)popPts;
          p1.addAll(gpsPopReverse);
          continue;
        }
        for(int i=0; i< matcher.getPoints().size(); i++){
          Arc matchedArcComp = result.getStates().get(i);
          matchReverse.put(matcher.getPoints().get(i), matchedArcComp);
        }
        gpsPopReverse.removeAll(matcher.getPoints());
        popPts = matcher.getPoints();
        popPts.clear();
        @SuppressWarnings("unchecked")
        Population<IFeature> p1 = (Population<IFeature>)popPts;
        p1.addAll(gpsPopReverse);
      }



      for(IFeature pt: mapF.keySet()){
        Arc arcRef = mapF.get(pt);
        Arc a1 = matchDirect.get(pt);
        Arc a2 = matchReverse.get(pt);
        if(a1 == null || a2 == null){
          continue;
        }
        if(a1.getGeometrie().equals(a2.getGeometrie())){
          Arc arccomp = matchDirect.get(pt);
          if(mapArcs.containsKey(arcRef)){
            mapArcs.get(arcRef).add(arccomp );
          }
          else{
            Set<Arc> set = new HashSet<Arc>();
            set.add(arccomp);
            mapArcs.put(arcRef, set);
          }          
        }
      }
    }


    for(Arc arcRef: mapArcs.keySet()){
      for(Arc arcComp : new ArrayList<Arc>(mapArcs.get(arcRef))){
        ILineString lineRef = new GM_LineString(arcRef.getGeometrie().getControlPoint());
        IDirectPosition p11 = lineRef.startPoint();
        IDirectPosition p12 = lineRef.endPoint();
        ILineString lineComp =  new GM_LineString(arcComp.getGeometrie().getControlPoint());
        IDirectPosition p21 = lineComp.startPoint();
        IDirectPosition p22 = lineComp.endPoint();
        IDirectPosition pmin1 = null, pmin2 = null;
        if(p11.equals(p21)){
          pmin1 = p11;
          pmin2 = p21;
        }
        else if(p11.equals(p22)){
          pmin1 = p11;
          pmin2 = p22;
        }
        else if(p12.equals(p21)){
          pmin1 = p12;
          pmin2 = p21;
        }
        else if(p12.equals(p22)){
          pmin1 = p12;
          pmin2 = p22;
        }
        else if(p11.distance(p21) < Math.min(p11.distance(p22),
            Math.min(p12.distance(p21), p12.distance(p22)))){
          pmin1 = p11;
          pmin2 = p21;
        }
        else if(p11.distance(p22) < Math.min(p11.distance(p21),
            Math.min(p12.distance(p21), p12.distance(p22)))){
          pmin1 = p11;
          pmin2 = p22;
        }
        else if(p12.distance(p21) < Math.min(p11.distance(p22),
            Math.min(p11.distance(p21), p12.distance(p22)))){
          pmin1 = p12;
          pmin2 = p21;
        }
        else{
          pmin1 = p12;
          pmin2 = p22;
        }
        if(pmin1.equals(p12)){
          lineRef = lineRef.reverse();
        }
        if(pmin2.equals(p22)){
          lineComp = lineComp.reverse();
        }

        Angle angleRef = Operateurs.directionPrincipaleOrientee(Operateurs.resampling(lineRef.coord(),5));
        Angle angleComp = Operateurs.directionPrincipaleOrientee(Operateurs.resampling(lineComp.coord(),5));
        double value =Angle.ecart(angleRef, angleComp).getValeur();
        if (Math.abs(value)*180./Math.PI > 2*angleMax){
          mapArcs.get(arcRef).remove(arcComp);
        }
      }
    }


    Population<IFeature> out = new Population<IFeature>();
    for(Arc a: mapArcs.keySet()){
      IDirectPosition ppp1 = Operateurs.milieu((a.getGeometrie()));
      for(Arc aa: mapArcs.get(a)){
        IDirectPosition ppp2 = Operateurs.milieu((aa.getGeometrie()));
        ILineString lll = new GM_LineString(new DirectPositionList(Arrays.asList(ppp1,ppp2)));
        out.add(new DefaultFeature(lll));
      }
    }

    return mapArcs;
  }

  public  Map<Noeud, Set<Noeud>> nodeMatching( Map<Arc, Set<Arc>> edgesMatching){
    Map<Noeud, Set<Noeud>> nodeMatching = new HashMap<Noeud, Set<Noeud>>();

    Set<Noeud> set1 = new HashSet<Noeud>();
    for(Arc a: edgesMatching.keySet()){
      set1.add(a.getNoeudFin());
      set1.add(a.getNoeudIni());
    }
    Population<Noeud> pop1 = new Population<Noeud>();
    pop1.addAll(set1);
    Set<Noeud> set2 = new HashSet<Noeud>();
    for(Set<Arc> S: edgesMatching.values()){
      for(Arc a: S){
        set2.add(a.getNoeudFin());
        set2.add(a.getNoeudIni());
      }
    }
    Population<Noeud> pop2 = new Population<Noeud>();
    pop2.addAll(set2);


     pop1.initSpatialIndex(Tiling.class, false);
     pop2.initSpatialIndex(Tiling.class, false);

    for(Noeud n1: pop1){
      Collection<Noeud> nodes = pop2.select(n1.getGeom(), selection );
      List<Arc> arcs1 = new ArrayList<Arc>();
      arcs1.addAll(n1.getEntrants());
      arcs1.addAll(n1.getSortants());

      for(Noeud n2: nodes){
        //candiats? 
        List<Arc> arcs2 = new ArrayList<Arc>();
        arcs2.addAll(n2.getEntrants());
        arcs2.addAll(n2.getSortants());
        if(arcs1.size()> arcs2.size()+1 || arcs1.size()< arcs2.size()-1){
          continue;
        }
        boolean ok = true;
        boolean matched = false;
        for(Arc a1: arcs1){
          if(edgesMatching.containsKey(a1)){
            matched = true;
            //l'arc est apparié ! avec au moins un arc de arc2 ?
            boolean ok2 = false;
            for(Arc a2: arcs2){
              if(edgesMatching.get(a1).contains(a2)) {
                ok2 = true;
                break;
              }
            }
            if(!ok2){
              ok = false;
              break;
            }
          }
          if(!ok){
            break;
          }
        }
        if(ok && matched){
          if(nodeMatching.containsKey(n1)){
            nodeMatching.get(n1).add(n2);
          }
          else{
            Set<Noeud> s = new HashSet<Noeud>();
            s.add(n2);
            nodeMatching.put(n1, s);
          }
        }
      }
    }
    
    for(Noeud n1: pop2){
      Collection<Noeud> nodes = pop1.select(n1.getGeom(), selection );
      List<Arc> arcs1 = new ArrayList<Arc>();
      arcs1.addAll(n1.getEntrants());
      arcs1.addAll(n1.getSortants());

      for(Noeud n2: nodes){
        //candiats? 
        List<Arc> arcs2 = new ArrayList<Arc>();
        arcs2.addAll(n2.getEntrants());
        arcs2.addAll(n2.getSortants());
        if(arcs1.size()> arcs2.size()+1 || arcs1.size()< arcs2.size()-1){
          continue;
        }
        boolean ok = true;
        boolean matched = false;
        for(Arc a1: arcs1){
          boolean containvalue = false;
          for(Arc a : edgesMatching.keySet()){
            if(edgesMatching.get(a).contains(a1)){
              containvalue = true;
              break;
            }
          }
          if(containvalue){
            matched = true;
            //l'arc est apparié ! avec au moins un arc de arc2 ?
            boolean ok2 = false;
            for(Arc a2: arcs2){
              if(edgesMatching.containsKey(a2) && edgesMatching.get(a2).contains(a1)) {
                ok2 = true;
                break;
              }
            }
            if(!ok2){
              ok = false;
              break;
            }
          }
          if(!ok){
            break;
          }
        }
        if(ok && matched){
          if(nodeMatching.containsKey(n2)){
            nodeMatching.get(n2).add(n1);
          }
          else{
            Set<Noeud> s = new HashSet<Noeud>();
            s.add(n1);
            nodeMatching.put(n2, s);
          }
        }
      }
    }
    return nodeMatching;
  }




  public static void main(String[] args) {
    // TODO Auto-generated method stub


    String shp2 = "/home/bcostes/Bureau/stag_1854/stag/snapshot_1853.0_1855.0_edges.shp";
    // String shp2 = "/media/bcostes/Data/Benoit/these/donnees/vecteur/filaires/filaires_corriges/poubelle_TEMPORAIRE_emprise_utf8_L93_v2.shp";
    // String shp1 = "/home/bcostes/Bureau/test2.shp";

    String shp1 = "/home/bcostes/Bureau/stag_1854/tag_new_filter_edges.shp";
    // String shp2 = "/home/bcostes/Bureau/edgesG22.shp";
    // String shp2 = "/media/bcostes/Data/Benoit/these/donnees/vecteur/filaires/filaires_corriges/jacoubet_l93_utf8.shp";

    // String shp1 = "/media/bcostes/Data/Benoit/qualite/donnees/cassini/grenoble/hydro/bdcarto/troncons_hydro_enrichi.shp";
    //  String shp2 = "/media/bcostes/Data/Benoit/qualite/donnees/cassini/grenoble/hydro/cassini/hydro_lineaire_corrected_enrichi.shp";
    //
    //    //    
    //    String shp1 = "/home/bcostes/Bureau/t2.shp";
    //    String shp2 = "/home/bcostes/Bureau/t3.shp";

  /*  IPopulation<IFeature> zoneRef = ShapefileReader.read("/home/bcostes/Bureau/score_matching/jacoubet_poubelle/zone1/zone_jacoubet.shp");
    IPopulation<IFeature> popRef = ShapefileReader.read("/home/bcostes/Bureau/score_matching/jacoubet_poubelle/jacoubet.shp");
    IPopulation<IFeature> popComp = ShapefileReader.read("/home/bcostes/Bureau/score_matching/jacoubet_poubelle/poubelle.shp");
    IPopulation<IFeature> matchingManuel = ShapefileReader.read("/home/bcostes/Bureau/score_matching/jacoubet_poubelle/zone1/manual_matching.shp");

     for(double d = 50;d<=100; d+= 10){
      HierarchicalNetworksMatching.beta = d;



      long t0 = System.currentTimeMillis();*/
    HierarchicalNetworksMatching matcher = new HierarchicalNetworksMatching(shp1, shp2, false, false, 10.);
   /* long t = System.currentTimeMillis();

    System.out.println("time : " + (t-t0));*/




    Map<Arc, Set<Arc>> mapArcs2 = matcher.getMatching();

    Map<Noeud, Set<Noeud>> mapnodes = matcher.nodeMatching(mapArcs2);

    IPopulation<IFeature> out2  = new Population<IFeature>();



    for(Arc cref: mapArcs2.keySet()){
      for(IFeature fref: cref.getCorrespondants()){
        IDirectPosition p = Operateurs.milieu(new GM_LineString(fref.getGeom().coord()));
        for(Arc ccomp : mapArcs2.get(cref)){
          for(IFeature fcomp: ccomp.getCorrespondants()){

            IDirectPositionList list = new DirectPositionList();
            IDirectPosition p2 = Operateurs.milieu(new GM_LineString(fcomp.getGeom().coord()));


            IDirectPosition pproj1 = Operateurs.projection(p, new GM_LineString(fcomp.getGeom().coord()));
            IDirectPosition pproj2 = Operateurs.projection(p2, new GM_LineString(fref.getGeom().coord()));

            if(p.distance(pproj1) < p2.distance(pproj2)){
              if(pproj1.equals( new GM_LineString(fcomp.getGeom().coord()).startPoint()) || pproj1.equals( new GM_LineString(fcomp.getGeom().coord()).endPoint())){
                pproj1 = p2;
              }
              list.add(p);
              list.add(pproj1);
            }
            else{
              if(pproj2.equals(new GM_LineString(fref.getGeom().coord()).startPoint()) || pproj2.equals(new GM_LineString(fref.getGeom().coord()).endPoint())){
                pproj2 = p;
              }
              list.add(p2);
              list.add(pproj2);
            }

            out2.add(new DefaultFeature(new GM_LineString(list)));
          }
        }
      }
    }



    //    for(Arc cref: mapArcs2.keySet()){
    //      IDirectPosition p = Operateurs.milieu(cref.getGeometrie());
    //      for(Arc ccomp : mapArcs2.get(cref)){
    //        IDirectPositionList list = new DirectPositionList();
    //        IDirectPosition p2 = Operateurs.milieu(ccomp.getGeometrie());
    //
    //
    //        IDirectPosition pproj1 = Operateurs.projection(p, ccomp.getGeometrie());
    //        IDirectPosition pproj2 = Operateurs.projection(p2, cref.getGeometrie());
    //
    //        if(p.distance(pproj1) < p2.distance(pproj2)){
    //          if(pproj1.equals( ccomp.getGeometrie().startPoint()) || pproj1.equals(ccomp.getGeometrie().endPoint())){
    //            pproj1 = p2;
    //          }
    //          list.add(p);
    //          list.add(pproj1);
    //        }
    //        else{
    //          if(pproj2.equals(cref.getGeometrie().startPoint()) || pproj2.equals(cref.getGeometrie().endPoint())){
    //            pproj2 = p;
    //          }
    //          list.add(p2);
    //          list.add(pproj2);
    //        }
    //
    //        out2.add(new DefaultFeature(new GM_LineString(list)));
    //      }
    //
    //    }


    for(Noeud cref: mapnodes.keySet()){
      IDirectPosition p = cref.getCoord();
      for(Noeud ccomp : mapnodes.get(cref)){
        IDirectPositionList list = new DirectPositionList();
        IDirectPosition p2 = ccomp.getCoord();
        list.add(p);
        list.add(p2);

        out2.add(new DefaultFeature(new GM_LineString(list)));
      }
    }
      
      System.out.println(out2.size());


    //  ShapefileWriter.write(out, "/home/bcostes/Bureau/HMM_matching/arcs_matching_final.shp");
    // ShapefileWriter.write(out2, "/home/bcostes/Bureau/score_matching/param/match.shp");
    ShapefileWriter.write(out2, "/home/bcostes/Bureau/stag_1854/matching.shp");


   


     /*IPopulation<IFeature> matchingAuto = ShapefileReader.read("/home/bcostes/Bureau/score_matching/param/match.shp");


      Scores s = new Scores(popRef, popComp);
      s.init(matchingAuto, matchingManuel, popRef, popComp, zoneRef);
      System.out.println(d+" " + s.getAccuracy()+" "+ s.getRecall());
*/






    //      ShapefileWriter.write(HierarchicalNetworksMatching.outMatching, "/home/bcostes/Bureau/HMM_matching/ptsgps_matching_final.shp");
    //    ShapefileWriter.write(HierarchicalNetworksMatching.outEnv, "/home/bcostes/Bureau/HMM_matching/enveloppes.shp");













    //    IPopulation<IFeature> outGpsMatching = new Population<IFeature>();
    //
    //
    //    for(Arc cref: mapArcs2.keySet()){
    //      int cpt = 1;
    //      ILineString le = Operateurs.resampling(cref.getGeometrie(), cref.getGeometrie().length()/20.);
    //      for(int i=0; i< le.getControlPoint().size()-1; i++){
    //        if(i!=0){
    //          if(le.getControlPoint(i).equals(le.getControlPoint(i-1))){
    //            continue;
    //          }
    //        }
    //        
    //        List<ILineString> lines = new ArrayList<ILineString>();
    //        for(Arc ccomp : mapArcs2.get(cref)){
    //          lines.add(ccomp.getGeometrie());
    //        }
    //        ILineString lineMerged = Operateurs.union(lines);
    //        IDirectPosition pproj = Operateurs.projection(le.getControlPoint(i), lineMerged);
    //        
    //        
    //        IFeature f1 = new DefaultFeature(new GM_Point(le.getControlPoint(i)));
    //        AttributeManager.addAttribute(f1, "reference", true, "Boolean");
    //        AttributeManager.addAttribute(f1, "number", cpt, "Integer");
    //        IFeature f2 = new DefaultFeature(new GM_Point(pproj));
    //        AttributeManager.addAttribute(f2, "reference", false, "Boolean");
    //        AttributeManager.addAttribute(f2, "number", cpt, "Integer");
    //        
    //        outGpsMatching.add(f1);
    //        outGpsMatching.add(f2);
    //        
    //        cpt++;
    //      }
    //
    //    }
    //    
    //      ShapefileWriter.write(outGpsMatching, "/home/bcostes/Bureau/HMM_matching/outGpsMatching.shp");


    //}




  }

}
