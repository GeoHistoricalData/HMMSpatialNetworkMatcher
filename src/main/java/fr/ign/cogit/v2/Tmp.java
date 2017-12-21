package fr.ign.cogit.v2;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import nrc.fuzzy.XValuesOutOfOrderException;
import nrc.fuzzy.YValueOutOfRangeException;
import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ILineString;
import fr.ign.cogit.geoxygene.feature.DefaultFeature;
import fr.ign.cogit.geoxygene.feature.Population;
import fr.ign.cogit.geoxygene.util.attribute.AttributeManager;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileWriter;
import fr.ign.cogit.v2.io.TAGIoManager;
import fr.ign.cogit.v2.manual.corrections.tag.strokes.StrokeBuilder;
import fr.ign.cogit.v2.patterns.AppearanceSTPattern;
import fr.ign.cogit.v2.patterns.ReincarnationSTPattern;
import fr.ign.cogit.v2.tag.FuzzyTemporalInterval;
import fr.ign.cogit.v2.tag.STEntity;
import fr.ign.cogit.v2.tag.STGraph;
import fr.ign.cogit.v2.tag.STProperty;

public class Tmp {

  public static List<String> NAMES = new ArrayList<String>(
      Arrays.asList("passage","impasse","pas", "imp", "cul", "ruelle","allee","cloitre",
          "cour","port","preau","cul-de-sac",
          "all","cite","crs","gal","galerie","i","rle","sq"));  
  @SuppressWarnings("deprecation")
  public static void main(String[] args) throws XValuesOutOfOrderException, YValueOutOfRangeException {
    //    ParametresApp params;
    //
    //    params = new ParametresApp();
    //    params.debugBilanSurObjetsGeo = true;
    //    // fait buguer l'algo car rond points déja traités en amont
    //    params.varianteChercheRondsPoints = false;
    //    params.debugTirets = false;
    //    params.distanceArcsMax = 15;
    //    params.distanceArcsMin = 5;
    //    params.distanceNoeudsMax = 15;
    //    params.distanceNoeudsImpassesMax = 15;
    //    params.varianteRedecoupageNoeudsNonApparies = true;
    //    // this.params.varianteForceAppariementSimple = true;
    //    params.varianteRedecoupageNoeudsNonApparies_DistanceNoeudArc = 15;
    //    params.varianteRedecoupageNoeudsNonApparies_DistanceProjectionNoeud = 5;
    //
    //
    //    IPopulation<IFeature> ref = ShapefileReader.read("/media/bcostes/Data/Benoit/these/donnees/vecteur/filaires/filaires_corriges/verniquet_l93_utf8_corr.shp");
    //    params.populationsArcs1.add(ref);
    //    IPopulation<IFeature> comp = ShapefileReader.read("/media/bcostes/Data/Benoit/these/donnees/vecteur/filaires/filaires_corriges/1871_L93_utf8_emprise.shp");
    //    params.populationsArcs2.add(comp);
    //
    ////    ReseauApp reseauRef = new ReseauApp();
    ////    ReseauApp reseauComp = AppariementIO.importData(params, false);
    //    EnsembleDeLiens liens = AppariementIO.appariementDeJeuxGeo(params, new ArrayList<ReseauApp>());
    //
    //    ShapefileWriter.write(liens, "/home/bcostes/Bureau/HMM_matching/devogele.shp");
    //    System.exit(0);


    //    IPopulation<IFeature> in = ShapefileReader.read("/media/bcostes/Data/Benoit/these/donnees/vecteur/filaires/filaires_corriges/georoute_1999_emprise_L93.shp");
    //    IPopulation<IFeature> out = new Population<IFeature>();
    //    
    //    CarteTopo t = new CarteTopo("");
    //    IPopulation<Arc>arcs = t.getPopArcs();
    //    
    //    for(IFeature f: in){
    //      Arc a= arcs.nouvelElement();
    //      a.setGeom(new GM_LineString(f.getGeom().coord()));
    //    }
    //    t.rendPlanaire(0.5);
    //    t.creeTopologieArcsNoeuds(0.5);
    //    t.creeTopologieFaces();
    //    
    //    System.out.println(t.getPopFaces().size());
    //    for(Face  f: t.getPopFaces()){
    //      IGeometry g = f.getGeom();
    //      double miller = 4.*Math.PI * g.area() / (g.length() * g.length());
    //      if(miller > 0.9){
    //        out.add(f);
    //      }
    //    }
    //    ShapefileWriter.write(out, "/home/bcostes/Bureau/rpts.shp");
    //    System.exit(0);

    //    String out = "";
    //    List<String> files = Arrays.asList("/media/bcostes/Data/Benoit/these/analyses/TAG/TAG_v2/final/"
    //        + "tag_corrected_edges.shp");
    //    for(String file : files){
    //      JungSnapshot s = SnapshotIOManager.shp2Snapshot(file, new LengthEdgeWeighting(), null, false);
    //      out += s.calculateGraphGlobalIndicator(new Alpha()) +";";
    //      out += s.calculateGraphGlobalIndicator(new AveragePathLength()) +";";
    //      out +=s.calculateGraphGlobalIndicator(new Beta()) +";";
    //      out +=s.calculateGraphGlobalIndicator(new ClusteringCoefficient()) +";";
    //      out +=s.calculateGraphGlobalIndicator(new Density()) +";";
    //      out +=s.calculateGraphGlobalIndicator(new DetourIndex()) +";";
    //      out += s.calculateGraphGlobalIndicator(new Diameter()) +";";
    //      out +=s.calculateGraphGlobalIndicator(new Gamma()) +";";
    //      out += s.calculateGraphGlobalIndicator(new MeanEdgeLength()) +";";
    //      out +=s.calculateGraphGlobalIndicator(new MeanEdgesOrientation()) +";";
    //      out += s.calculateGraphGlobalIndicator(new MeanNodeDegree()) +";";
    //      out += s.calculateGraphGlobalIndicator(new Mu()) +";";
    //      out +=s.calculateGraphGlobalIndicator(new Pi()) +"\n";
    //      out += s.getVertexCount()+" "+ s.getEdgeCount()+"\n";
    //    }
    //    System.out.println(out);
    //    FileWriter fw;
    //    try {
    //      fw = new FileWriter("/media/bcostes/Data/Benoit/these/analyses/indicateurs/globaux/globaux_stag_as_a_graph.txt");
    //      BufferedWriter bw = new BufferedWriter(fw);
    //      bw.write(out);
    //      bw.flush();
    //      bw.close();
    //
    //    } catch (IOException e) {
    //      // TODO Auto-generated catch block
    //      e.printStackTrace();
    //    }


    //    String shpDeleteVerniquet = "/home/bcostes/Bureau/TAG/corrections_patterns/etape6/delete_edges_verniquet.shp";
    //    String shpDeleteJacoubet = "/home/bcostes/Bureau/TAG/corrections_patterns/etape6/delete_edges_jacoubet.shp";
    //    String shpDelete1849= "/home/bcostes/Bureau/TAG/corrections_patterns/etape6/delete_edges_1849.shp";
    //    String shpDelete1871= "/home/bcostes/Bureau/TAG/corrections_patterns/etape6/delete_edges_1871.shp";
    // String shpDelete= "/home/bcostes/Bureau/TAG/corrections_patterns/etape-finale/delete_edges.shp";
    //    
    // String shpAddVerniquet = "/home/bcostes/Bureau/TAG/corrections_patterns/finale/add_edges_verniquet.shp";
    //    String shpAddJacoubet = "/home/bcostes/Bureau/TAG/corrections_patterns/etape6/add_edges_jacoubet.shp";
    //  String shpAdd1849 = "/home/bcostes/Bureau/TAG/corrections_patterns/etape-finale/add_edges_1849.shp";
    //    String shpAdd1871 = "/home/bcostes/Bureau/TAG/corrections_patterns/etape7/add_edges_1871.shp";
    //    String shpAdd1889= "/home/bcostes/Bureau/TAG/corrections_patterns/etape7/add_edges_1889.shp";


    //    String inputStg ="/home/bcostes/Bureau/TAG/etape4/tag_new.tag";
    //    String repNew ="/home/bcostes/Bureau/deleteme";
    //
    //    //    String outputStg ="/home/bcostes/Bureau/TAG/corrections_patterns/test/tag_corrected.shp";
    //    //    String outputStg2 ="/home/bcostes/Bureau/TAG/corrections_patterns/test/tag_corrected.tag";
    //    //
    //    //
    //    STGraph stg = TAGIoManager.deserialize(inputStg);
    //
    //
    //    IPopulation<IFeature> pop = new Population<IFeature>();
    //
    //
    //
    //    ReincarnationSTPattern pattern = new ReincarnationSTPattern();       
    //    for(STEntity edge : stg.getEdges()){
    //      STProperty<Boolean> ts = edge.getTimeSerie();
    //      if(pattern.find(ts)){
    //        // System.out.println("Réincarnation ! ");
    //        DefaultFeature f = new DefaultFeature(edge.getGeometry().toGeoxGeometry());
    //        pop.add(f);
    //      }
    //    }
    //    System.out.println(pop.size());
    //    ShapefileWriter.write(pop, repNew+"/reincarnations.shp");
    //    pop.clear();
    //
    //
    //    AppearanceSTPattern pattern2 = new AppearanceSTPattern();       
    //    for(STEntity edge : stg.getEdges()){
    //      STProperty<Boolean> ts = edge.getTimeSerie();
    //
    //      if(pattern2.find(ts)){
    //        DefaultFeature f = new DefaultFeature(edge.getGeometry().toGeoxGeometry());
    //        pop.add(f);
    //      }        
    //    }      
    //    System.out.println(pop.size());
    //    ShapefileWriter.write(pop, repNew + "/appearances.shp");
    //
    //    pop.clear();
    //
    //
    //    LifeSTPattern pattern3 = new LifeSTPattern();       
    //    for(STEntity edge : stg.getEdges()){
    //      STProperty<Boolean> ts = edge.getTimeSerie();
    //
    //      if(pattern3.find(ts)){
    //        DefaultFeature f = new DefaultFeature(edge.getGeometry().toGeoxGeometry());
    //        pop.add(f);
    //      }        
    //    }      
    //    System.out.println(pop.size());
    //    ShapefileWriter.write(pop, repNew + "/birth.shp");
    //
    //
    //    pop.clear();
    //
    //
    //    DeathSTPattern pattern4 = new DeathSTPattern();       
    //    for(STEntity edge : stg.getEdges()){
    //      STProperty<Boolean> ts = edge.getTimeSerie();
    //
    //      if(pattern4.find(ts)){
    //        DefaultFeature f = new DefaultFeature(edge.getGeometry().toGeoxGeometry());
    //        pop.add(f);
    //      }        
    //    }      
    //    System.out.println(pop.size());
    //    ShapefileWriter.write(pop, repNew + "/death.shp");
    //    pop.clear();




    //
    //        Map<FuzzyTemporalInterval, Double> accuraciesMap = new HashMap<FuzzyTemporalInterval, Double>();
    //        accuraciesMap.put(new FuzzyTemporalInterval(new double[]{1784,1785,1789,1791},new double[]{0,1,1,0}, 4),
    //            1.);
    //        /* accuraciesMap.put(new FuzzyTemporalInterval(new double[]{1808,1810,1836,1853},new double[]{0,1,1,0}, 4),
    //                    1.);*/ //0.7
    //        accuraciesMap.put(new FuzzyTemporalInterval(new double[]{1825,1827,1836,1839},new double[]{0,1,1,0}, 4),
    //            0.);
    //        accuraciesMap.put(new FuzzyTemporalInterval(new double[]{1848,1849,1849,1850},new double[]{0,1,1,0}, 4),
    //            1.);
    //        accuraciesMap.put(new FuzzyTemporalInterval(new double[]{1870,1871,1871,1872},new double[]{0,1,1,0}, 4),
    //            0.);
    //        accuraciesMap.put(new FuzzyTemporalInterval(new double[]{1887,1888,1888,1889},new double[]{0,1,1,0}, 4),
    //            1.);
    //        STProperty<Double> accuracies = new STProperty<Double>(STProperty.PROPERTIE_FINAL_TYPES.Attribute, "Accuracies");
    //        accuracies.setValues(accuraciesMap);
    //        stg.setAccuracies(accuracies);
    //        stg.updateGeometries();
    //        TAGIoManager.exportTAG(stg, "/home/bcostes/Bureau/deleteme/tag.shp");
    //    
    //    
    //    for(STEntity e : stg.getEdges()){
    //      for(FuzzyTemporalInterval t : stg.getTemporalDomain().asList()){    
    //        if(e.existsAt(t)){
    //          e.setWeightAt(t, e.getGeometryAt(t).toGeoxGeometry().length());
    //        }
    //      }
    //    }
    //    
    //    for(STEntity node : stg.getVertices()){
    //      for(FuzzyTemporalInterval t : stg.getTemporalDomain().asList()){
    //        Set<STEntity> incidents = new HashSet<STEntity>(stg.getIncidentEdgessAt(node, t));
    //        if(incidents.size() == 2 && node.existsAt(t)){
    //          //il devrait être fictif
    //          node.existsAt(t, false);
    //        }
    //      }
    //    }


    //TAGIoManager.serializeXml(stg, "/home/bcostes/Bureau/TAG/corrections_patterns/test/tag_corrected.xml");



    //    for(STEntity e :stg.getEdges()){
    //      for(FuzzyTemporalInterval  t :stg.getTemporalDomain().asList()){
    //        if(!e.existsAt(t)){
    //          continue;
    //        }
    //        ILineString l = (ILineString)e.getGeometryAt(t).toGeoxGeometry();
    //        IDirectPositionList ll = l.coord();
    //       // GeometryUtils.filterLowAngles(ll, 5);
    //       GeometryUtils.filterLargeAngles(ll,150);
    //        e.setGeometryAt(t,new LightLineString(ll));
    //      }
    //    }
    //    stg.updateGeometries();


    //DeleteEdge.deleteEdge(stg, shpDelete);

    /// FuzzyTemporalInterval t;
    ///try {
    // t = new FuzzyTemporalInterval(new double[]{1825,1827,1836,1839},new double[]{0,1,1,0}, 4);
    // t = new FuzzyTemporalInterval
    // (new double[]{1848,1849,1849,1850},new double[]{0,1,1,0}, 4);
    //t = new FuzzyTemporalInterval(new double[]{1887,1888,1888,1889},new double[]{0,1,1,0}, 4);
    //    t = new FuzzyTemporalInterval(new double[]{1784,1785,1789,1791},new double[]{0,1,1,0}, 4);
    //    DeleteEdge.deleteEdgeAt(stg, t, shpDeleteVerniquet);
    //    t = new FuzzyTemporalInterval(new double[]{1825,1827,1836,1839},new double[]{0,1,1,0}, 4);
    //    DeleteEdge.deleteEdgeAt(stg, t, shpDeleteJacoubet);
    //    t = new FuzzyTemporalInterval(new double[]{1848,1849,1849,1850},new double[]{0,1,1,0}, 4);
    //    DeleteEdge.deleteEdgeAt(stg, t, shpDelete1849);
    //    t = new FuzzyTemporalInterval(new double[]{1870,1871,1871,1872},new double[]{0,1,1,0}, 4);
    //    DeleteEdge.deleteEdgeAt(stg, t, shpDelete1871);


    // t = new FuzzyTemporalInterval(new double[]{1784,1785,1789,1791},new double[]{0,1,1,0}, 4);
    //  CorrectionInterpolation.addDateToEdge(stg, t, shpAddVerniquet);
    //    t = new FuzzyTemporalInterval(new double[]{1825,1827,1836,1839},new double[]{0,1,1,0}, 4);
    //    CorrectionInterpolation.addDateToEdge(stg, t, shpAddJacoubet);
    // t = new FuzzyTemporalInterval(new double[]{1848,1849,1849,1850},new double[]{0,1,1,0}, 4);
    // CorrectionInterpolation.addDateToEdge(stg, t, shpAdd1849);
    //    t = new FuzzyTemporalInterval(new double[]{1870,1871,1871,1872},new double[]{0,1,1,0}, 4);
    //    CorrectionInterpolation.addDateToEdge(stg, t, shpAdd1871);
    //    t = new FuzzyTemporalInterval(new double[]{1887,1888,1888,1889},new double[]{0,1,1,0}, 4);
    //    CorrectionInterpolation.addDateToEdge(stg, t, shpAdd1889);

    //    
    //    String shpLiens = "/home/bcostes/Bureau/TAG/corrections_patterns/etape6/matching.shp";
    //    STGraph stg2 = CorrectionMerge.correctMerge(stg, shpLiens);

    //    
    //    stg.updateGeometries();
    //
    //    TAGIoManager.serializeBinary(stg, outputStg2);
    //    TAGIoManager.exportTAG(stg, outputStg);
    //    TAGIoManager.exportSnapshots(stg, outputStg, TAGIoManager.NODE_AND_EDGE);
    //
    //
    //    IPopulation<IFeature> pop = new Population<IFeature>();
    //
    //
    //
    //    ReincarnationSTPattern pattern = new ReincarnationSTPattern();       
    //    for(STEntity edge : stg.getEdges()){
    //      STProperty<Boolean> ts = edge.getTimeSerie();
    //      if(pattern.find(ts)){
    //        // System.out.println("Réincarnation ! ");
    //        List<FuzzyTemporalInterval> times = pattern.findEvent(edge.getTimeSerie());
    //        DefaultFeature f = new DefaultFeature(edge.getGeometry().toGeoxGeometry());
    //        AttributeManager.addAttribute(f, "death", times.get(0).toString(), "String");
    //        AttributeManager.addAttribute(f, "re-live", times.get(1).toString(), "String");
    //        pop.add(f);
    //      }
    //    }
    //    System.out.println(pop.size());
    //    ShapefileWriter.write(pop,"/home/bcostes/Bureau/TAG/corrections_patterns/test/reincarnations.shp");
    //    pop.clear();
    //
    //
    //    AppearanceSTPattern pattern2 = new AppearanceSTPattern();       
    //    for(STEntity edge : stg.getEdges()){
    //      STProperty<Boolean> ts = edge.getTimeSerie();
    //
    //      if(pattern2.find(ts)){
    //        List<FuzzyTemporalInterval> times = pattern2.findEvent(ts);
    //        DefaultFeature f = new DefaultFeature(edge.getGeometry().toGeoxGeometry());
    //        AttributeManager.addAttribute(f, "live", times.get(0).toString(), "String");
    //        AttributeManager.addAttribute(f, "death", times.get(1).toString(), "String");
    //        pop.add(f);
    //      }        
    //    }      
    //    System.out.println(pop.size());
    //    ShapefileWriter.write(pop, "/home/bcostes/Bureau/TAG/corrections_patterns/test/appearances.shp");
    //




    //  } catch (XValuesOutOfOrderException e) {
    //    // TODO Auto-generated catch block
    //    e.printStackTrace();
    //  } catch (YValueOutOfRangeException e) {
    //    // TODO Auto-generated catch block
    //    e.printStackTrace();
    // }



    String inputStg = "/media/bcostes/Data/Benoit/these/analyses/TAG/TAG_v2/corrections/corrections_etapes/situation_initiale/tag_new.tag"; 

    STGraph stg = TAGIoManager.deserialize(inputStg);


    List<String> PASSAGES = new ArrayList<String>(
        Arrays.asList("passage","pas"
            , "ruelle","allee","chemin","champ","c.", "cloitre",
            "carreau","cour","port","preau",
            "all","cite","crs","gal","galerie","rle","sq","p","pont"));  

    List<String> IMPASSES = new ArrayList<String>(
        Arrays.asList("impasse","imp", "cul", "cul-de-sac",
            "i"));  

    List<String> PLACES = new ArrayList<String>(
        Arrays.asList( "place",
            "pl"));  


    List<String> RUES = new ArrayList<String>(
        Arrays.asList("rue", "r", "avenue","av", "boulevard","bd","petit","petite","grande","quai","quay","qu"));  

    int cptPassages =0, cptImpasses = 0, cptPlaces=0, cptRues =0, cptAutre =0, cptTotal =0;
    Map<String, Integer> series = new HashMap<String, Integer>();

    IPopulation<IFeature> out = new Population<IFeature>();

    StrokeBuilder strokeBuilder= new StrokeBuilder(stg);
    Map<ILineString, Set<STEntity>> strokesMap = strokeBuilder.buildHStructureMap();
    Map<STEntity, ILineString> strokesMapReverse = new HashMap<STEntity, ILineString>();
    for(STEntity e :stg.getEdges()){
      ILineString stroke = null;
      for(ILineString l : strokesMap.keySet()){
        if(strokesMap.get(l).contains(e)){
          stroke = l;
          break;
        }
      }
      if(stroke == null){
        System.out.println("WATTTT");
      }
      strokesMapReverse.put(e, stroke);
    }


    //on associe chaque stroke à son nombre de stroke qui l'intersecte
    Map<ILineString, Integer> strokeDegrees = new HashMap<ILineString, Integer>();
    for(ILineString stroke: strokesMap.keySet()){
      Set<STEntity> stentities = strokesMap.get(stroke);
      Set<STEntity> incidents = new HashSet<STEntity>();
      for(STEntity e: stentities){
        incidents.addAll(stg.getIncidentEdges(stg.getEndpoints(e).getFirst()));
        incidents.addAll(stg.getIncidentEdges(stg.getEndpoints(e).getSecond()));
      }
      //on supprime les arcs qui composent le stroke
      incidents.removeAll(stentities);
      //on regroupe par stroke
      Set<ILineString> strokesIncident = new HashSet<ILineString>();
      for(STEntity incident: incidents){
        strokesIncident.add(strokesMapReverse.get(incident));
      }
      strokesIncident.remove(stroke);
      strokeDegrees.put(stroke, strokesIncident.size());
    }

    Set<STEntity> passagesImpasses = new HashSet<STEntity>();

    for(STEntity edge : stg.getEdges()){
      boolean inconsitencie = false;
      ReincarnationSTPattern pattern = new ReincarnationSTPattern();    
      STProperty<Boolean> ts = edge.getTimeSerie();
      if(pattern.find(ts)){
        inconsitencie = true;
        String s = pattern.getSequence(ts);
        if(!series.containsKey(s)){
          series.put(s, 1);
        }
        else{
          series.put(s, series.get(s)+1);
        }
      }
      else{
        AppearanceSTPattern pattern2 = new AppearanceSTPattern();    
        if(pattern2.find(ts)){
          inconsitencie = true;
          String s = pattern.getSequence(ts);
          if(!series.containsKey(s)){
            series.put(s, 1);
          }
          else{
            series.put(s, series.get(s)+1);
          }
        }
      }

      if(!inconsitencie){
        continue;
      }
      cptTotal++;
      boolean found = false;
      for(FuzzyTemporalInterval t : stg.getTemporalDomain().asList()){
        if(!edge.existsAt(t)){
          continue;
        }

        if(stg.getIncidentEdgessAt(stg.getEndpoints(edge).getFirst(),t).size() == 1 
            || stg.getIncidentEdgessAt(stg.getEndpoints(edge).getSecond(),t).size() == 1){
          cptImpasses++;
          passagesImpasses.add(edge);
          found = true;
          break;
        }
        String name = edge.getTAttributeByName("name").getValueAt(t);
        if(name == null || name.equals(" ")){
          continue;
        }
        name = name.toLowerCase();
        name = name.trim();
        String norm1 = Normalizer.normalize(name, Normalizer.Form.NFD);
        Pattern pat = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        name = pat.matcher(norm1).replaceAll("");
        StringTokenizer tokenizer = new StringTokenizer(name, " ");
        if(tokenizer.hasMoreTokens()){
          String firstToken = tokenizer.nextToken();
          if(PASSAGES.contains(firstToken)){
            passagesImpasses.add(edge);

            found = true;
            cptPassages++;
            break;
          }
          else      if(IMPASSES.contains(firstToken)){
            passagesImpasses.add(edge);

            cptImpasses++;
            found = true;
            break;
          }
          else      if(PLACES.contains(firstToken)){
            cptPlaces++;
            //found = true;
            break;
          }
          else      if(RUES.contains(firstToken)){
            cptRues++;
            //found = true;
            break;
          }
          else{
            System.out.println(firstToken);
            //cptAutre++;
            //found = true;
            //break;
          }

        }

      }
      if(!found){
        cptAutre++;
        //on regarde si troke de degré 2
        //        if(strokeDegrees.get(strokesMapReverse.get(edge)) <=3){
        //          passagesImpasses.add(edge);
        //        }
      }
    }
    System.out.println(cptPassages);
    System.out.println(cptPlaces);
    System.out.println(cptImpasses);
    System.out.println(cptRues);
    System.out.println(cptAutre);
    System.out.println();
    System.out.println(cptPassages+cptPlaces+cptImpasses+cptRues+cptAutre+" / "+ cptTotal);



    for(STEntity e: passagesImpasses){
      out.add(new DefaultFeature(e.getGeometry().toGeoxGeometry()));
    }
    ShapefileWriter.write(out, "/home/bcostes/Bureau/tmp/passages_impasses.shp");

    //StrokeBuilder strokeBuilder= new StrokeBuilder(stg);
   // Map<ILineString, Set<STEntity>> strokesMap = strokeBuilder.buildHStructureMap();
   /* Map<STEntity, ILineString> strokesMapReverse = new HashMap<STEntity, ILineString>();
    for(STEntity e :stg.getEdges()){
      ILineString stroke = null;
      for(ILineString l : strokesMap.keySet()){
        if(strokesMap.get(l).contains(e)){
          stroke = l;
          break;
        }
      }
      if(stroke == null){
        System.out.println("WATTTT");
      }
      strokesMapReverse.put(e, stroke);
    }


    //on associe chaque stroke à son nombre de stroke qui l'intersecte
    Map<ILineString, Integer> strokeDegrees = new HashMap<ILineString, Integer>();
    for(ILineString stroke: strokesMap.keySet()){
      Set<STEntity> stentities = strokesMap.get(stroke);
      Set<STEntity> incidents = new HashSet<STEntity>();
      for(STEntity e: stentities){
        incidents.addAll(stg.getIncidentEdges(stg.getEndpoints(e).getFirst()));
        incidents.addAll(stg.getIncidentEdges(stg.getEndpoints(e).getSecond()));
      }
      //on supprime les arcs qui composent le stroke
      incidents.removeAll(stentities);
      //on regroupe par stroke
      Set<ILineString> strokesIncident = new HashSet<ILineString>();
      for(STEntity incident: incidents){
        strokesIncident.add(strokesMapReverse.get(incident));
      }
      strokesIncident.remove(stroke);
      strokeDegrees.put(stroke, strokesIncident.size());
    }*/





   out = new Population<IFeature>();
    for(ILineString l: strokeDegrees.keySet()){
      IFeature f= new DefaultFeature(l);
      AttributeManager.addAttribute(f, "degree", strokeDegrees.get(l), "Integer");
      out.add(f);
    }
    ShapefileWriter.write(out, "/home/bcostes/Bureau/tmp/stroke_degrees.shp");



    String s ="";
    for(STEntity e : stg.getEdges()){
      double d = strokeDegrees.get(strokesMapReverse.get(e));
      s+=d+"\n";
    }

    String s2 ="";
    ReincarnationSTPattern pattern = new ReincarnationSTPattern();       
    for(STEntity edge : stg.getEdges()){
      STProperty<Boolean> ts = edge.getTimeSerie();
      if(pattern.find(ts)){
        // System.out.println("Réincarnation ! ");
        double d = strokeDegrees.get(strokesMapReverse.get(edge));
        s2+=d+"\n";
      }
    }  
    String s3 ="";
    AppearanceSTPattern pattern2 = new AppearanceSTPattern();       
    for(STEntity edge : stg.getEdges()){
      STProperty<Boolean> ts = edge.getTimeSerie();
      if(pattern2.find(ts)){
        double d = strokeDegrees.get(strokesMapReverse.get(edge));

        s3+=d+"\n";          }        
    }      
    String s4 =s2+s3;

    try {
      FileWriter fr = new FileWriter(new File("/home/bcostes/Bureau/tmp/strokes_edges_degree.txt"));
      BufferedWriter br = new BufferedWriter(fr);
      br.write(s);
      br.flush();
      br.close();
      fr = new FileWriter(new File("/home/bcostes/Bureau/tmp/strokes_reincarnations_degree.txt"));
      br = new BufferedWriter(fr);
      br.write(s2);
      br.flush();
      br.close();
      fr = new FileWriter(new File("/home/bcostes/Bureau/tmp/strokes_apprerances_degree.txt"));
      br = new BufferedWriter(fr);
      br.write(s3);
      br.flush();
      br.close();
      fr = new FileWriter(new File("/home/bcostes/Bureau/tmp/strokes_inconsistencies_degree.txt"));
      br = new BufferedWriter(fr);
      br.write(s4);
      br.flush();
      br.close();
    } catch (IOException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }



    //        for(STEntity e: stg.getEdgesAt(
    //                new FuzzyTemporalInterval(new double[]{1870,1871,1871,1872},new double[]{0,1,1,0}, 4))){
    //            ILineString l = (ILineString)e.getGeometryAt(new FuzzyTemporalInterval(new double[]{1870,1871,1871,1872},new double[]{0,1,1,0}, 4)).toGeoxGeometry();
    //            IFeature f = new DefaultFeature(l);
    //            double s = f.getGeom().length() / (l.startPoint().distance(l.endPoint()));
    //            AttributeManager.addAttribute(f, "s", s, "Double");
    //            out.add(f);
    //        }
    //        ShapefileWriter.write(out, "/home/bcostes/Bureau/2.shp");
    //        out.clear();

    //        stg.edgeLocalIndicator(new BetweennessCentrality(), NORMALIZERS.CONVENTIONAL, false);
    //        stg.edgeLocalIndicator(new MeanDistance(), NORMALIZERS.CONVENTIONAL, false);
    //        stg.nodeLocalIndicator(new BetweennessCentrality(), NORMALIZERS.CONVENTIONAL, false);
    //        stg.nodeLocalIndicator(new MeanDistance(), NORMALIZERS.CONVENTIONAL, false);
    //
    //
    //        TAGIoManager.serializeBinary(stg, "/home/bcostes/Bureau/test/etape1/tag/ind/tag_ind.tag");
    //        TAGIoManager.exportTAG(stg, "/home/bcostes/Bureau/test/etape1/tag/ind/tagtag_ind.shp");
    //        TAGIoManager.exportSnapshots(stg, "/home/bcostes/Bureau/test/etape1/tag/ind/snap/tag.shp", TAGIoManager.NODE_AND_EDGE);
    //



    //String shp ="/home/bcostes/Bureau/bdtopo/ROUTE_grph_sansbois_simpli.shp";
    //        String shp = "/home/bcostes/Bureau/bdtopo/ROUTE_charles5.shp";
    //        
    //        
    //        
    //        JungSnapshot snap = SnapshotIOManager.shp2Snapshot
    //                (shp, 
    //                        new LengthEdgeWeighting(), null, false);
    //        Map<GraphEntity, Integer> ids = new HashMap<GraphEntity, Integer>();
    //        IPopulation<IFeature> pop = ShapefileReader.read(shp);
    //        pop.initSpatialIndex(Tiling.class,false);
    //        for(GraphEntity e : snap.getEdges()){
    //            Collection<IFeature> can = pop.select(e.getGeometry().toGeoxGeometry(),1);
    //            IFeature f = null;
    //            for(IFeature ff: can){
    //                if(ff.getGeom().equals(e.getGeometry().toGeoxGeometry())){
    //                    f =ff;
    //                    break;
    //                }
    //            }
    //            if(f == null){
    //                System.out.println("WAT");
    //                return;
    //            }
    //            ids.put(e, Integer.parseInt(
    //                    f.getAttribute(f.getFeatureType().getFeatureAttributeByName("ID")).toString()));
    //        }
    //        
    //        Map<GraphEntity, Double> bet = snap.calculateEdgeCentrality(new BetweennessCentrality(), NORMALIZERS.CONVENTIONAL);
    //      //  Map<GraphEntity, Double> meand = snap.calculateEdgeCentrality(new ClusteringCentrality(), NORMALIZERS.NONE);
    //       // Map<GraphEntity, Double> str = snap.calculateEdgeCentrality(new ControlCentrality(), NORMALIZERS.NONE);   
    //       // Map<GraphEntity, Double> local = snap.calculateNeighborhoodEdgeCentrality(new ClosenessCentrality(), 5, NORMALIZERS.CONVENTIONAL);
    //        //Map<GraphEntity, Double> local2 = snap.calculateNeighborhoodEdgeCentrality(new ClosenessCentrality(), 10, NORMALIZERS.CONVENTIONAL);
    //
    //        IPopulation<IFeature> out = new Population<IFeature>();
    //        for(GraphEntity e :bet.keySet()){
    //            IFeature f = new DefaultFeature(e.getGeometry().toGeoxGeometry());
    //            AttributeManager.addAttribute(f, "ID", ids.get(e), "Integer");
    //         //   AttributeManager.addAttribute(f, "degree", bet.get(e), "Double");
    //          //  AttributeManager.addAttribute(f, "clust", meand.get(e), "Double");
    //          //  AttributeManager.addAttribute(f, "ctrl", str.get(e), "Double");
    //          //  AttributeManager.addAttribute(f, "meanD5", local.get(e), "Double");
    //            AttributeManager.addAttribute(f, "betw", bet.get(e), "Double");
    //
    //
    //            out.add(f);
    //        }
    //        
    //        ShapefileWriter.write(out, "/home/bcostes/Bureau/testbetw.shp");

    //        
    //        
    //        
    //        // 222222222222222222222222222222222222222222
    //        bet.clear();
    //        meand.clear();
    //        str.clear();
    //        pop.clear();
    //        ids.clear();
    //        out.clear();
    //        snap = null;
    //        shp = "/home/bcostes/Bureau/bdtopo/ROUTE_ferme.shp";
    //        
    //        
    //        
    //        snap = SnapshotIOManager.shp2Snapshot
    //                (shp, 
    //                        new LengthEdgeWeighting(), null, false);
    //        ids = new HashMap<GraphEntity, Integer>();
    //        pop = ShapefileReader.read(shp);
    //        pop.initSpatialIndex(Tiling.class,false);
    //        for(GraphEntity e : snap.getEdges()){
    //            Collection<IFeature> can = pop.select(e.getGeometry().toGeoxGeometry(),1);
    //            IFeature f = null;
    //            for(IFeature ff: can){
    //                if(ff.getGeom().equals(e.getGeometry().toGeoxGeometry())){
    //                    f =ff;
    //                    break;
    //                }
    //            }
    //            if(f == null){
    //                System.out.println("WAT");
    //                return;
    //            }
    //            ids.put(e, Integer.parseInt(
    //                    f.getAttribute(f.getFeatureType().getFeatureAttributeByName("ID")).toString()));
    //        }
    //        
    //        bet = snap.calculateEdgeCentrality(new DegreeCentrality(), NORMALIZERS.NONE);
    //        meand = snap.calculateEdgeCentrality(new ClusteringCentrality(), NORMALIZERS.NONE);
    //        str = snap.calculateEdgeCentrality(new ControlCentrality(), NORMALIZERS.NONE);   
    //        local = snap.calculateNeighborhoodEdgeCentrality(new ClosenessCentrality(), 5, NORMALIZERS.CONVENTIONAL);
    //        local2 = snap.calculateNeighborhoodEdgeCentrality(new ClosenessCentrality(), 10, NORMALIZERS.CONVENTIONAL);
    //
    //         out = new Population<IFeature>();
    //        for(GraphEntity e :bet.keySet()){
    //            IFeature f = new DefaultFeature(e.getGeometry().toGeoxGeometry());
    //            AttributeManager.addAttribute(f, "ID", ids.get(e), "Integer");
    //            AttributeManager.addAttribute(f, "degree", bet.get(e), "Double");
    //            AttributeManager.addAttribute(f, "clust", meand.get(e), "Double");
    //            AttributeManager.addAttribute(f, "ctrl", str.get(e), "Double");
    //            AttributeManager.addAttribute(f, "meanD5", local.get(e), "Double");
    //            AttributeManager.addAttribute(f, "meanD10", local2.get(e), "Double");
    //
    //
    //            out.add(f);
    //        }
    //        
    //        
    //        ShapefileWriter.write(out, "/home/bcostes/Bureau/bdtopo/ind_local/ROUTE_ferme.shp");
    //        
    //        
    //        
    //        
    //        
    //        
    //        
    //     // 3333333333333333333333333333333333
    //        bet.clear();
    //        meand.clear();
    //        str.clear();
    //        pop.clear();
    //        ids.clear();
    //        out.clear();
    //        snap = null;
    //        shp = "/home/bcostes/Bureau/bdtopo/ROUTE_grph_sansbois_simpli.shp";
    //        
    //        
    //        
    //        snap = SnapshotIOManager.shp2Snapshot
    //                (shp, 
    //                        new LengthEdgeWeighting(), null, false);
    //        ids = new HashMap<GraphEntity, Integer>();
    //        pop = ShapefileReader.read(shp);
    //        pop.initSpatialIndex(Tiling.class,false);
    //        for(GraphEntity e : snap.getEdges()){
    //            Collection<IFeature> can = pop.select(e.getGeometry().toGeoxGeometry(),1);
    //            IFeature f = null;
    //            for(IFeature ff: can){
    //                if(ff.getGeom().equals(e.getGeometry().toGeoxGeometry())){
    //                    f =ff;
    //                    break;
    //                }
    //            }
    //            if(f == null){
    //                System.out.println("WAT");
    //                return;
    //            }
    //            ids.put(e, Integer.parseInt(
    //                    f.getAttribute(f.getFeatureType().getFeatureAttributeByName("ID")).toString()));
    //        }
    //        
    //        bet = snap.calculateEdgeCentrality(new DegreeCentrality(), NORMALIZERS.NONE);
    //        meand = snap.calculateEdgeCentrality(new ClusteringCentrality(), NORMALIZERS.NONE);
    //        str = snap.calculateEdgeCentrality(new ControlCentrality(), NORMALIZERS.NONE);   
    //        local = snap.calculateNeighborhoodEdgeCentrality(new ClosenessCentrality(), 5, NORMALIZERS.CONVENTIONAL);
    //        local2 = snap.calculateNeighborhoodEdgeCentrality(new ClosenessCentrality(), 10, NORMALIZERS.CONVENTIONAL);
    //
    //         out = new Population<IFeature>();
    //        for(GraphEntity e :bet.keySet()){
    //            IFeature f = new DefaultFeature(e.getGeometry().toGeoxGeometry());
    //            AttributeManager.addAttribute(f, "ID", ids.get(e), "Integer");
    //            AttributeManager.addAttribute(f, "degree", bet.get(e), "Double");
    //            AttributeManager.addAttribute(f, "clust", meand.get(e), "Double");
    //            AttributeManager.addAttribute(f, "ctrl", str.get(e), "Double");
    //            AttributeManager.addAttribute(f, "meanD5", local.get(e), "Double");
    //            AttributeManager.addAttribute(f, "meanD10", local2.get(e), "Double");
    //
    //
    //            out.add(f);
    //        }
    //        
    //        ShapefileWriter.write(out, "/home/bcostes/Bureau/bdtopo/ind_local/ROUTE_grph_sansbois_simpli.shp.shp");
    //        
    //        
    //        
    //        
    //        // 44444444444444444444444444444444444
    //        bet.clear();
    //        meand.clear();
    //        str.clear();
    //        pop.clear();
    //        ids.clear();
    //        out.clear();
    //        snap = null;
    //        shp = "/home/bcostes/Bureau/bdtopo/ROUTE_riveDroite.shp";
    //        
    //        
    //        
    //        snap = SnapshotIOManager.shp2Snapshot
    //                (shp, 
    //                        new LengthEdgeWeighting(), null, false);
    //        ids = new HashMap<GraphEntity, Integer>();
    //        pop = ShapefileReader.read(shp);
    //        pop.initSpatialIndex(Tiling.class,false);
    //        for(GraphEntity e : snap.getEdges()){
    //            Collection<IFeature> can = pop.select(e.getGeometry().toGeoxGeometry(),1);
    //            IFeature f = null;
    //            for(IFeature ff: can){
    //                if(ff.getGeom().equals(e.getGeometry().toGeoxGeometry())){
    //                    f =ff;
    //                    break;
    //                }
    //            }
    //            if(f == null){
    //                System.out.println("WAT");
    //                return;
    //            }
    //            ids.put(e, Integer.parseInt(
    //                    f.getAttribute(f.getFeatureType().getFeatureAttributeByName("ID")).toString()));
    //        }
    //        
    //        bet = snap.calculateEdgeCentrality(new DegreeCentrality(), NORMALIZERS.NONE);
    //        meand = snap.calculateEdgeCentrality(new ClusteringCentrality(), NORMALIZERS.NONE);
    //        str = snap.calculateEdgeCentrality(new ControlCentrality(), NORMALIZERS.NONE);   
    //        local = snap.calculateNeighborhoodEdgeCentrality(new ClosenessCentrality(), 5, NORMALIZERS.CONVENTIONAL);
    //        local2 = snap.calculateNeighborhoodEdgeCentrality(new ClosenessCentrality(), 10, NORMALIZERS.CONVENTIONAL);
    //
    //         out = new Population<IFeature>();
    //        for(GraphEntity e :bet.keySet()){
    //            IFeature f = new DefaultFeature(e.getGeometry().toGeoxGeometry());
    //            AttributeManager.addAttribute(f, "ID", ids.get(e), "Integer");
    //            AttributeManager.addAttribute(f, "degree", bet.get(e), "Double");
    //            AttributeManager.addAttribute(f, "clust", meand.get(e), "Double");
    //            AttributeManager.addAttribute(f, "ctrl", str.get(e), "Double");
    //            AttributeManager.addAttribute(f, "meanD5", local.get(e), "Double");
    //            AttributeManager.addAttribute(f, "meanD10", local2.get(e), "Double");
    //
    //
    //            out.add(f);
    //        }
    //        
    //        ShapefileWriter.write(out, "/home/bcostes/Bureau/bdtopo/ind_local/ROUTE_riveDroite.shp.shp");
    //        
    //        
    //        
    //        // 555555555555555555555555
    //        bet.clear();
    //        meand.clear();
    //        str.clear();
    //        pop.clear();
    //        ids.clear();
    //        out.clear();
    //        snap = null;
    //        shp = "/home/bcostes/Bureau/bdtopo/ROUTE_riveGauche.shp";
    //        
    //        
    //        
    //        snap = SnapshotIOManager.shp2Snapshot
    //                (shp, 
    //                        new LengthEdgeWeighting(), null, false);
    //        ids = new HashMap<GraphEntity, Integer>();
    //        pop = ShapefileReader.read(shp);
    //        pop.initSpatialIndex(Tiling.class,false);
    //        for(GraphEntity e : snap.getEdges()){
    //            Collection<IFeature> can = pop.select(e.getGeometry().toGeoxGeometry(),1);
    //            IFeature f = null;
    //            for(IFeature ff: can){
    //                if(ff.getGeom().equals(e.getGeometry().toGeoxGeometry())){
    //                    f =ff;
    //                    break;
    //                }
    //            }
    //            if(f == null){
    //                System.out.println("WAT");
    //                return;
    //            }
    //            ids.put(e, Integer.parseInt(
    //                    f.getAttribute(f.getFeatureType().getFeatureAttributeByName("ID")).toString()));
    //        }
    //        
    //        bet = snap.calculateEdgeCentrality(new DegreeCentrality(), NORMALIZERS.NONE);
    //        meand = snap.calculateEdgeCentrality(new ClusteringCentrality(), NORMALIZERS.NONE);
    //        str = snap.calculateEdgeCentrality(new ControlCentrality(), NORMALIZERS.NONE);   
    //        local = snap.calculateNeighborhoodEdgeCentrality(new ClosenessCentrality(), 5, NORMALIZERS.CONVENTIONAL);
    //        local2 = snap.calculateNeighborhoodEdgeCentrality(new ClosenessCentrality(), 10, NORMALIZERS.CONVENTIONAL);
    //
    //         out = new Population<IFeature>();
    //        for(GraphEntity e :bet.keySet()){
    //            IFeature f = new DefaultFeature(e.getGeometry().toGeoxGeometry());
    //            AttributeManager.addAttribute(f, "ID", ids.get(e), "Integer");
    //            AttributeManager.addAttribute(f, "degree", bet.get(e), "Double");
    //            AttributeManager.addAttribute(f, "clust", meand.get(e), "Double");
    //            AttributeManager.addAttribute(f, "ctrl", str.get(e), "Double");
    //            AttributeManager.addAttribute(f, "meanD5", local.get(e), "Double");
    //            AttributeManager.addAttribute(f, "meanD10", local2.get(e), "Double");
    //
    //
    //            out.add(f);
    //        }
    //        
    //        ShapefileWriter.write(out, "/home/bcostes/Bureau/bdtopo/ind_local/ROUTE_riveGauche.shp.shp");
    //        
    //        
    //        
    //        
    //        
    //        
    //        // 66666666666666666666666666666666666666
    //        bet.clear();
    //        meand.clear();
    //        str.clear();
    //        pop.clear();
    //        ids.clear();
    //        out.clear();
    //        snap = null;
    //        shp = "/home/bcostes/Bureau/bdtopo/ROUTE_grph_sansbois.shp";
    //        
    //        
    //        
    //        snap = SnapshotIOManager.shp2Snapshot
    //                (shp, 
    //                        new LengthEdgeWeighting(), null, false);
    //        ids = new HashMap<GraphEntity, Integer>();
    //        pop = ShapefileReader.read(shp);
    //        pop.initSpatialIndex(Tiling.class,false);
    //        for(GraphEntity e : snap.getEdges()){
    //            Collection<IFeature> can = pop.select(e.getGeometry().toGeoxGeometry(),1);
    //            IFeature f = null;
    //            for(IFeature ff: can){
    //                if(ff.getGeom().equals(e.getGeometry().toGeoxGeometry())){
    //                    f =ff;
    //                    break;
    //                }
    //            }
    //            if(f == null){
    //                System.out.println("WAT");
    //                return;
    //            }
    //            ids.put(e, Integer.parseInt(
    //                    f.getAttribute(f.getFeatureType().getFeatureAttributeByName("ID")).toString()));
    //        }
    //        
    //        bet = snap.calculateEdgeCentrality(new DegreeCentrality(), NORMALIZERS.NONE);
    //        meand = snap.calculateEdgeCentrality(new ClusteringCentrality(), NORMALIZERS.NONE);
    //        str = snap.calculateEdgeCentrality(new ControlCentrality(), NORMALIZERS.NONE);   
    //        local = snap.calculateNeighborhoodEdgeCentrality(new ClosenessCentrality(), 5, NORMALIZERS.CONVENTIONAL);
    //        local2 = snap.calculateNeighborhoodEdgeCentrality(new ClosenessCentrality(), 10, NORMALIZERS.CONVENTIONAL);
    //
    //         out = new Population<IFeature>();
    //        for(GraphEntity e :bet.keySet()){
    //            IFeature f = new DefaultFeature(e.getGeometry().toGeoxGeometry());
    //            AttributeManager.addAttribute(f, "ID", ids.get(e), "Integer");
    //            AttributeManager.addAttribute(f, "degree", bet.get(e), "Double");
    //            AttributeManager.addAttribute(f, "clust", meand.get(e), "Double");
    //            AttributeManager.addAttribute(f, "ctrl", str.get(e), "Double");
    //            AttributeManager.addAttribute(f, "meanD5", local.get(e), "Double");
    //            AttributeManager.addAttribute(f, "meanD10", local2.get(e), "Double");
    //
    //
    //            out.add(f);
    //        }
    //        
    //        ShapefileWriter.write(out, "/home/bcostes/Bureau/bdtopo/ind_local/ROUTE_grph_sansbois.shp.shp");
    //        
    //        
    //        
    //        
    //        
    //        
    //        // 777777777777777777777777777777
    //        bet.clear();
    //        meand.clear();
    //        str.clear();
    //        pop.clear();
    //        ids.clear();
    //        out.clear();
    //        snap = null;
    //        shp = "/home/bcostes/Bureau/bdtopo/ROUTE_grph.shp";
    //        
    //        
    //        
    //        snap = SnapshotIOManager.shp2Snapshot
    //                (shp, 
    //                        new LengthEdgeWeighting(), null, false);
    //        ids = new HashMap<GraphEntity, Integer>();
    //        pop = ShapefileReader.read(shp);
    //        pop.initSpatialIndex(Tiling.class,false);
    //        for(GraphEntity e : snap.getEdges()){
    //            Collection<IFeature> can = pop.select(e.getGeometry().toGeoxGeometry(),1);
    //            IFeature f = null;
    //            for(IFeature ff: can){
    //                if(ff.getGeom().equals(e.getGeometry().toGeoxGeometry())){
    //                    f =ff;
    //                    break;
    //                }
    //            }
    //            if(f == null){
    //                System.out.println("WAT");
    //                return;
    //            }
    //            ids.put(e, Integer.parseInt(
    //                    f.getAttribute(f.getFeatureType().getFeatureAttributeByName("ID")).toString()));
    //        }
    //        
    //        bet = snap.calculateEdgeCentrality(new DegreeCentrality(), NORMALIZERS.NONE);
    //        meand = snap.calculateEdgeCentrality(new ClusteringCentrality(), NORMALIZERS.NONE);
    //        str = snap.calculateEdgeCentrality(new ControlCentrality(), NORMALIZERS.NONE);   
    //        local = snap.calculateNeighborhoodEdgeCentrality(new ClosenessCentrality(), 5, NORMALIZERS.CONVENTIONAL);
    //        local2 = snap.calculateNeighborhoodEdgeCentrality(new ClosenessCentrality(), 10, NORMALIZERS.CONVENTIONAL);
    //
    //         out = new Population<IFeature>();
    //        for(GraphEntity e :bet.keySet()){
    //            IFeature f = new DefaultFeature(e.getGeometry().toGeoxGeometry());
    //            AttributeManager.addAttribute(f, "ID", ids.get(e), "Integer");
    //            AttributeManager.addAttribute(f, "degree", bet.get(e), "Double");
    //            AttributeManager.addAttribute(f, "clust", meand.get(e), "Double");
    //            AttributeManager.addAttribute(f, "ctrl", str.get(e), "Double");
    //            AttributeManager.addAttribute(f, "meanD5", local.get(e), "Double");
    //            AttributeManager.addAttribute(f, "meanD10", local2.get(e), "Double");
    //
    //
    //            out.add(f);
    //        }
    //        ShapefileWriter.write(out, "/home/bcostes/Bureau/bdtopo/ind_local/ROUTE_grph.shp.shp");
    //        
    //        


    //        IPopulation<IFeature> topo = ShapefileReader.read("/home/bcostes/Bureau/bdtopo/ROUTE_ini.SHP");
    //        topo.initSpatialIndex(Tiling.class,false);
    //        CarteTopo map = new CarteTopo("");
    //        IPopulation<Arc> arcs = map.getPopArcs();
    //        for(IFeature f: topo){
    //            Arc a =arcs.nouvelElement();
    //            a.setGeom(new GM_LineString(f.getGeom().coord()));
    //            a.addCorrespondant(f);
    //        }
    //        map.creeTopologieArcsNoeuds(0.5);
    //        map.rendPlanaire(0.5);
    //        map.filtreArcsDoublons();
    //        map.filtreDoublons(0.5);
    //        map.creeNoeudsManquants(0.5);
    //        map.filtreNoeudsSimples();
    //        Groupe g  = new Groupe();
    //        g.addAllArcs(map.getListeArcs());
    //        g.addAllNoeuds(map.getListeNoeuds());
    //        
    //        List<Groupe> cp = g.decomposeConnexes();
    //        Map<Integer, Integer> sizes = new HashMap<Integer, Integer>();
    //        int cpt=0;
    //        for(Groupe gg: cp){
    //            sizes.put(gg.getListeArcs().size(),cpt);
    //            cpt++;
    //        }
    //        List<Integer> l= new ArrayList<Integer>(sizes.keySet());
    //        Collections.sort(l);
    //        System.out.println(l.get(l.size()-1));
    //        Groupe cMax = cp.get(sizes.get(l.get(l.size()-1)));
    //        System.out.println(cMax.getListeArcs().size());
    //        
    //        IPopulation<IFeature> cmaxOut = new Population<IFeature>();
    //        for(Arc a: cMax.getListeArcs()){
    //            IFeature feat = new DefaultFeature(a.getGeom());
    //            cmaxOut.add(feat);
    //
    //        }
    //        ShapefileWriter.write(cmaxOut, "/home/bcostes/Bureau/bdtopo/test.shp");
    //        IPopulation<IFeature>error = new Population<IFeature>();
    //
    //        cmaxOut.clear();
    //        for(Arc a: cMax.getListeArcs()){
    //            IFeature feat = new DefaultFeature(a.getGeom());
    //            AttributeManager.addAttribute(feat, "ID", cmaxOut.size()+1, "String");
    //            List<IFeature> corr = a.getCorrespondants();
    //            if(corr.size()>1){
    //                Set<Integer> importances = new HashSet<Integer>();
    //               for(IFeature f: corr){
    //                   try{
    //                       Integer c= Integer.parseInt(f.getAttribute(f.getFeatureType().
    //                               getFeatureAttributeByName("IMPORTANCE")).toString());
    //                       importances.add(c);
    //                   }
    //                   catch(Exception e){
    //                       
    //                   }
    //               }
    //               if(importances.size()>1){
    //                   List<Integer> values = new ArrayList<Integer>(importances);
    //                   Collections.sort(values);
    //                   int d= values.get(0); // la plus importante
    //                   AttributeManager.addAttribute(feat, "IMPORTANCE", Integer.toString(d), "Integer");
    //                   cmaxOut.add(feat);
    //                   continue;
    //               }
    //               else if(importances.size()==1){
    //                   AttributeManager.addAttribute(feat, "IMPORTANCE", Integer.toString(importances.iterator().next())
    //                           , "String");
    //                   cmaxOut.add(feat);
    //                   continue;
    //               }
    //               else{
    //                   AttributeManager.addAttribute(feat, "IMPORTANCE", "NC"
    //                           , "String");
    //                   cmaxOut.add(feat);
    //                   continue; 
    //               }
    //            }
    //            else if(corr.size() == 1){
    //                IFeature  f  =corr.get(0);
    //                String i =f.getAttribute(f.getFeatureType().
    //                        getFeatureAttributeByName("IMPORTANCE")).toString();
    //                AttributeManager.addAttribute(feat, "IMPORTANCE", i
    //                        , "String");
    //                cmaxOut.add(feat);
    //            }
    //            else if(corr.isEmpty()){
    //                Collection<IFeature> candidats = topo.select(a.getGeom(),5);
    //                IFeature can = null;
    //                for(IFeature f:candidats){
    //                    if(f.getGeom().buffer(0.05).intersection(a.getGeom()).length() > 0.5* a.getGeom().length()){
    //                        can = f;
    //                        break;
    //                    }
    //                }
    //                if(can == null){
    //                    System.out.println(a.getGeom());
    //                }
    //                String i =can.getAttribute(can.getFeatureType().
    //                        getFeatureAttributeByName("IMPORTANCE")).toString();
    //                AttributeManager.addAttribute(feat, "IMPORTANCE", i
    //                        , "String");
    //                cmaxOut.add(feat);
    //            }
    //        }
    //        
    //        ShapefileWriter.write(cmaxOut, "/home/bcostes/Bureau/bdtopo/ROUTE_grph.shp");





    //        IPopulation<IFeature> bdtopo = ShapefileReader.read("/media/bcostes/Data/Benoit/these/donnees/vecteur/filaires/originaux/BDTOPO/BDTOPO-75/BDTOPO/1_DONNEES_LIVRAISON_2011-12-00477/BDT_2-1_SHP_LAMB93_D075-ED113/A_RESEAU_ROUTIER/ROUTE.shp");
    //        IPopulation<IFeature> passages = ShapefileReader.read("/media/bcostes/Data/Benoit/these/analyses/TAG/analyse_patterns/v2/impasses_passages.shp");
    //
    //        IPopulation<IFeature> out = new Population<IFeature>();
    //        bdtopo.initSpatialIndex(Tiling.class, false);
    //        passages.initSpatialIndex(Tiling.class, false);
    //        for(IFeature passage: passages){
    //            Collection<IFeature> candidats = bdtopo.select(passage.getGeom(), 25);
    //            for(IFeature candidat : candidats){
    //                if(candidat.getGeom().buffer(10).contains(passage.getGeom())){
    //                    out.add(passage);
    //                    break;
    //                }
    //            }
    //        }
    //        
    //        ShapefileWriter.write(out, "/home/bcostes/Bureau/decoup3.shp");



    //        STGraph stg = TAGIoManager.deserialize("/media/bcostes/Data/Benoit/these/analyses/TAG/TAG/v6/tag.tag");
    //        ManualIterativeBuilder.detect_doublons(stg, "/home/bcostes/Bureau/doublons.shp");
    //        System.exit(0);
    ////
    //        stg.updateGeometries();
    //        
    //        
    //        
    //        TAGIoManager.serializeBinary(stg, "/media/bcostes/Data/Benoit/these/analyses/TAG/TAG/v6/with_indicators/tag_ind.tag");
    //        TAGIoManager.serializeXml(stg, "/media/bcostes/Data/Benoit/these/analyses/TAG/TAG/v6/with_indicators/tag_ind.xml");
    //        TAGIoManager.exportTAG(stg, "/media/bcostes/Data/Benoit/these/analyses/TAG/TAG/v6/with_indicators/shp/tag_ind.shp");
    //        TAGIoManager.exportTAGSnapshots(stg, "/media/bcostes/Data/Benoit/these/analyses/TAG/TAG/v6/with_indicators/shp/tag_snapshots/tag_ind_snapshot.shp");
    //        TAGIoManager.exportSnapshots(stg, "/media/bcostes/Data/Benoit/these/analyses/TAG/TAG/v6/with_indicators/shp/snapshots/snapshot.shp", TAGIoManager.NODE_AND_EDGE);


    //        Set<STEntity> edges = new HashSet<STEntity>();
    //
    //        for(STEntity edge : stg.getEdges()){
    //            for(FuzzyTemporalInterval t :edge.getTimeSerie().asList().keySet()){
    ////                if(stg.getIncidentEdgessAt(stg.getEndpoints(edge).getFirst(),t).size() == 1 
    ////                        || stg.getIncidentEdgessAt(stg.getEndpoints(edge).getSecond(),t).size() == 1){
    ////                    edges.add(edge);
    ////                    break;
    ////                }
    //                String name = edge.getTAttributes().get("name").getValueAt(t);
    //                if(name == null || name.equals(" ")){
    //                    continue;
    //                }
    //                name = name.toLowerCase();
    //                name = name.trim();
    //                
    //                StringTokenizer tokenizer = new StringTokenizer(name, " ");
    //                if(tokenizer.hasMoreTokens()){
    //                    String firstToken = tokenizer.nextToken();
    //                    if(NAMES.contains(firstToken)){
    //                        edges.add(edge);
    //                        break;
    //                    }
    //                }
    //            }
    //        }
    //        
    //        IPopulation<IFeature> out= new Population<IFeature>();
    //        for(STEntity e : edges){
    //            IFeature f = new DefaultFeature(e.getGeometry().toGeoxGeometry());
    //            AttributeManager.addAttribute(f, "timeserie", e.getTimeSerie().toSequence(), "String");
    //            out.add(f);
    //        }
    //        ShapefileWriter.write(out, "/media/bcostes/Data/Benoit/these/analyses/TAG/analyse_patterns/v2/impasses_passages.shp");
    //       
    //        System.out.println(edges.size());



    //fr.ign.cogit.v1.tag.STGraph oldstg =  fr.ign.cogit.v1.io.TAGIoManager.deserializeXml("/media/bcostes/Data/Benoit/these/analyses/TAG/TAG/v3/tag_ind.xml");


    // STGraph stg = TAGIoManager.deserialize("/home/bcostes/Bureau/TAG/v3/etape4/tag_new.tag");






    //        FuzzyTemporalInterval t1 = new FuzzyTemporalInterval(1784,1785,1789,1791);
    //        FuzzyTemporalInterval t2 = new FuzzyTemporalInterval(1808,1810,1836,1853);
    //        FuzzyTemporalInterval t3 = new FuzzyTemporalInterval(1825,1827,1836,1839);
    //   FuzzyTemporalInterval t4 = new FuzzyTemporalInterval(1848,1849,1849,1850);   






    // STGraph stg = TAGIoManager.deserialize("/media/bcostes/Data/Benoit/these/analyses/TAG/TAG/v2_with_id/tag_with_id.tag");








    //STGraph stg =XStreamTest.test("/media/bcostes/Data/Benoit/these/analyses/TAG/TAG/v2_with_id/with_indicators/tag_id_indicators.xml");


    // System.out.println(stg.getEdges().iterator().next().getTweights().toString());

    //        stg.edgeLocalIndicator(new DegreeCentrality(), NORMALIZERS.CONVENTIONAL);
    //        stg.edgeLocalIndicator(new BetweennessCentrality(), NORMALIZERS.CONVENTIONAL);
    //        stg.edgeLocalIndicator(new MeanDistance(), NORMALIZERS.CONVENTIONAL);
    //        stg.edgeLocalIndicator(new StraightnessCentrality(), NORMALIZERS.CONVENTIONAL);
    //        stg.edgeLocalIndicator(new ClusteringCentrality(), NORMALIZERS.CONVENTIONAL);
    //        stg.edgeLocalIndicator(new ControlCentrality(), NORMALIZERS.CONVENTIONAL);
    //
    //
    //        stg.nodeLocalIndicator(new DegreeCentrality(), NORMALIZERS.CONVENTIONAL);
    //        stg.nodeLocalIndicator(new BetweennessCentrality(), NORMALIZERS.CONVENTIONAL);
    //        stg.nodeLocalIndicator(new MeanDistance(), NORMALIZERS.CONVENTIONAL);
    //        stg.nodeLocalIndicator(new StraightnessCentrality(), NORMALIZERS.CONVENTIONAL);
    //        stg.nodeLocalIndicator(new ClusteringCentrality(), NORMALIZERS.CONVENTIONAL);
    //        stg.nodeLocalIndicator(new ControlCentrality(), NORMALIZERS.CONVENTIONAL);
    //
    //        stg.graphGlobalIndicator(new Alpha());
    //        stg.graphGlobalIndicator(new AveragePathLength());
    //        stg.graphGlobalIndicator(new Beta());
    //        stg.graphGlobalIndicator(new ClusteringCoefficient());
    //        stg.graphGlobalIndicator(new Density());
    //        stg.graphGlobalIndicator(new DetourIndex());
    //        stg.graphGlobalIndicator(new Diameter());
    //        stg.graphGlobalIndicator(new Gamma());
    //        stg.graphGlobalIndicator(new MeanEdgeLength());
    //        stg.graphGlobalIndicator(new MeanEdgesOrientation());
    //        stg.graphGlobalIndicator(new MeanNodeDegree());
    //        stg.graphGlobalIndicator(new Mu());
    //        stg.graphGlobalIndicator(new Pi());
    //        stg.graphGlobalIndicator(new TotalEdgeLength());
    //
    //        TAGIoManager.serialize(stg,"/media/Data/Benoit/these/analyses/TAG/TAG/v2_with_id/with_indicators/tag_id_indicators.tag");
    //        TAGIoManager.exportTAG(stg, "/media/Data/Benoit/these/analyses/TAG/TAG/v2_with_id/with_indicators/shp/tag_id_indicators.shp");
    //        TAGIoManager.exportTAGSnapshots(stg, "/media/Data/Benoit/these/analyses/TAG/TAG/v2_with_id/with_indicators/shp/tag_snapshots/tag_id_indicators.shp", TAGIoManager.NODE_AND_EDGE);
    //        XStreamTest.test2(stg, "/media/Data/Benoit/these/analyses/TAG/TAG/v2_with_id/with_indicators/tag_id_indicators.xml");
    //



    //        STGraph stg = TAGIoManager.deserialize("/home/bcostes/Bureau/TAG/etape4/tag_new.tag");
    //
    //        for(int i=1; i< stg.getTemporalDomain().asList().size()-1; i++){
    //            FuzzyTemporalInterval ti = stg.getTemporalDomain().asList().get(i);
    //
    //            Map<STEntity, FuzzyTemporalInterval> edges = new HashMap<STEntity, FuzzyTemporalInterval>();
    //
    //            for(STEntity e : stg.getEdges()){
    //                if(e.existsAt(ti)){
    //                    for(int j=0; j< i; j++){
    //                        FuzzyTemporalInterval tj = stg.getTemporalDomain().asList().get(j);
    //                        if(e.existsAt(tj)){
    //                            edges.put(e, tj);
    //                            break;
    //                        }
    //                    }
    //                }
    //            }
    //            double mean = 0;
    //            double cpt = 0.;
    //            for(STEntity e : edges.keySet()){
    //                double d = Distances.hausdorff((ILineString)e.getGeometryAt(edges.get(e)).toGeoxGeometry(), (ILineString)e.getGeometryAt(ti).toGeoxGeometry());
    //                mean += d;
    //                cpt += 1.;
    //            }
    //            mean = mean / cpt;
    //            double sigma = 0.;
    //            for(STEntity e : edges.keySet()){
    //                double d = Distances.hausdorff((ILineString)e.getGeometryAt(edges.get(e)).toGeoxGeometry(), (ILineString)e.getGeometryAt(ti).toGeoxGeometry());
    //                sigma+= (d - mean) * (d - mean);
    //
    //            }
    //            sigma= Math.sqrt(sigma / cpt);
    //            IPopulation<IFeature> out = new Population<IFeature>();
    //            for(STEntity e : edges.keySet()){
    //                double d = Distances.hausdorff((ILineString)e.getGeometryAt(edges.get(e)).toGeoxGeometry(), (ILineString)e.getGeometryAt(ti).toGeoxGeometry());
    //                    if(d >mean ){
    //                        out.add(new DefaultFeature(e.getGeometry().toGeoxGeometry()));
    //                    }
    //                }
    //            ShapefileWriter.write(out,  "/home/bcostes/Bureau/tmp/distances/" + ti.toString() + ".shp");
    //        }



    //        Map<FuzzyTemporalInterval, List<Double>> angles = new HashMap<FuzzyTemporalInterval, List<Double>>();
    //
    //                for(FuzzyTemporalInterval t :stg.getTemporalDomain().asList()){
    //                    if(t.equals(new FuzzyTemporalInterval(1884, 1885, 1888, 1889))){
    //                        continue;
    //                    }
    //                    IPopulation<IFeature>out = new Population<IFeature>();
    //                    for(STEntity e : stg.getEdges()){
    //                        if(e.existsAt(t)){
    //                        double angle = EdgesOrientation.meanOrientation((ILineString)e.getGeometryAt(t).toGeoxGeometry());
    //                       
    //                        //angle =(angle >= Math.PI /2.) ? Math.abs(Math.PI /2. - angle) : angle;
    //                        angle = Math.abs(Math.PI - angle);
    //                        angle = angle * 180./ Math.PI;
    //                        
    //                        if(!angles.containsKey(t)){
    //                            List<Double> l = new ArrayList<Double>();
    //                            l.add(angle);
    //                            l.add(180. + angle);
    //                            angles.put(t, l);
    //                        }
    //                        else{
    //                            angles.get(t).add(angle);
    //                            angles.get(t).add(180. + angle);
    //
    //                        }
    //                        
    //                        IFeature f = new DefaultFeature(e.getGeometryAt(t).toGeoxGeometry());
    //                        AttributeManager.addAttribute(f, "orientation", angle, "Double");
    //                        out.add(f);
    //                        }
    //                    }
    //                    ShapefileWriter.write(out, "/home/bcostes/Bureau/tmp/orientation/" + t.toString() + ".shp");
    //                }
    //                for(FuzzyTemporalInterval t : angles.keySet()){
    //                        File f = new File("/home/bcostes/Bureau/tmp/orientation");
    //                        if(!f.exists()){
    //                            f.mkdir();
    //                        }
    //                        try {
    //                            FileWriter fr = new FileWriter(f.toString() + "/" + t.toString());
    //                            BufferedWriter br = new BufferedWriter(fr);
    //                            String str ="";
    //                            for(Double d : angles.get(t)){
    //                                str += d + "\n";
    //                            }
    //                            br.write(str);
    //                            br.flush();
    //                            br.close();
    //                        } catch (IOException e) {
    //                            // TODO Auto-generated catch block
    //                            e.printStackTrace();
    //                        }
    //        
    //                    
    //                }
    //    }


    //        LifeSTPattern patternLife = new LifeSTPattern();
    //        Map<FuzzyTemporalInterval, Set<STEntity>> selection = new HashMap<FuzzyTemporalInterval, Set<STEntity>>();
    //        for(STEntity edge : stg.getEdges()){
    //            TimeSerie ts = edge.getTimeSerie();
    //            ts.asList().remove(new FuzzyTemporalInterval(1884, 1885, 1888, 1889));
    //            if(patternLife.find(ts)){
    //                FuzzyTemporalInterval time = patternLife.findEvent(edge.getTimeSerie());
    //                if(!selection.containsKey(time)){
    //                    Set<STEntity> s = new HashSet<STEntity>();
    //                    s.add(edge);
    //                    selection.put(time,s);
    //                }
    //                else{
    //                    selection.get(time).add(edge);
    //                }
    //            }
    //        }

    // IPopulation<IFeature> features = new Population<IFeature>();






    //        Map<FuzzyTemporalInterval, List<Double>> angles = new HashMap<FuzzyTemporalInterval, List<Double>>();
    //        for(FuzzyTemporalInterval t : selection.keySet()){
    //            for(STEntity e : selection.get(t)){
    //                Double angle = EdgesOrientation.mainOrientation((ILineString)e.getGeometry().toGeoxGeometry());
    //                angle = angle * 180./Math.PI;
    //                //angle = Math.abs(90. - angle);
    //
    //                Set<STEntity> neighbors = new HashSet<STEntity>();
    //                for(STEntity nei : stg.getIncidentEdges(stg.getEndpoints(e).getFirst())){
    //                    if(nei.existsAt(t) && !(selection.get(t).contains(nei))){
    //                        neighbors.add(nei);
    //                    }
    //                }
    //                for(STEntity nei : stg.getIncidentEdges(stg.getEndpoints(e).getSecond())){
    //                    if(nei.existsAt(t) && !(selection.get(t).contains(nei))){
    //                        neighbors.add(nei);
    //                    }
    //                }
    //                neighbors.remove(e);
    //                if(neighbors.size()!=1 && !neighbors.isEmpty()){
    //                    for(STEntity nei : neighbors){
    //                        Double angleN = EdgesOrientation.mainOrientation((ILineString)nei.getGeometry().toGeoxGeometry());
    //                        angleN = angleN * 180./Math.PI;
    //                        angleN = Math.abs(90. - angleN);
    //                        double angleShift = Math.abs(angleN - angle);
    //                        
    //                        if(!angles.containsKey(t)){
    //                            List<Double> l = new ArrayList<Double>();
    //                            l.add(angleShift);
    //                            //l.add(90. + angleShift);
    //                            l.add(180. + angleShift);
    //                           // l.add(270. + angleShift);
    //                            angles.put(t, l);
    //                        }
    //                        else{
    //                            angles.get(t).add(angleShift);
    //                            //angles.get(t).add(90. + angleShift);
    //                            angles.get(t).add(180. + angleShift);
    //                            //angles.get(t).add(270. + angleShift);
    //
    //                        }
    //                    }
    //
    //                }
    //            }
    //        }
    //                System.out.println("-----");
    //                STEntity first = stg.getEndpoints(e).getFirst();
    //                IDirectPosition p2 = first.getGeometry().toGeoxGeometry().coord().get(0);
    //                IDirectPosition p1 = null;
    //                                
    //                if(e.getGeometry().toGeoxGeometry().coord().get(0).distance(p2)<e.getGeometry().toGeoxGeometry()
    //                        .coord().get(e.getGeometry().toGeoxGeometry().coord().size()-1).distance(p2)){
    //                    p1 = e.getGeometry().toGeoxGeometry().coord().get(1);
    //                }
    //                else{
    //                    p1 = e.getGeometry().toGeoxGeometry()
    //                            .coord().get(e.getGeometry().toGeoxGeometry().coord().size()-2);
    //                }
    //                Set<STEntity> neighbors = new HashSet<STEntity>();
    //                for(STEntity nei : stg.getIncidentEdges(first)){
    //                    if(nei.existsAt(t) && !(selection.get(t).contains(nei))){
    //                        neighbors.add(nei);
    //                    }
    //                }
    //                neighbors.remove(e);
    //                if(neighbors.size()!=1 && !neighbors.isEmpty()){
    //                    features.add(new DefaultFeature(new GM_Point(p2)));
    //                    features.add(new DefaultFeature(new GM_Point(p1)));
    //                    for(STEntity nei : neighbors){
    //                        IDirectPosition p3 = null;
    //                        if(nei.getGeometry().toGeoxGeometry().coord().get(0).distance(p2)<nei.getGeometry().toGeoxGeometry()
    //                                .coord().get(nei.getGeometry().toGeoxGeometry().coord().size()-1).distance(p2)){
    //                            p3 = nei.getGeometry().toGeoxGeometry().coord().get(1);
    //                        }
    //                        else{
    //                            p3 = nei.getGeometry().toGeoxGeometry()
    //                                    .coord().get(nei.getGeometry().toGeoxGeometry().coord().size()-2);
    //                        }
    //                        features.add(new DefaultFeature(new GM_Point(p3)));
    //
    //                        Double angle = Angle.angleTroisPoints(p1, p2, p3).getValeur();
    //                        angle = Math.abs(Math.PI - angle);
    //                        // angle = Math.abs(Math.PI/2. - angle);
    //                        angle = angle * 180. / Math.PI;
    //                        if(!angles.containsKey(t)){
    //                            List<Double> l = new ArrayList<Double>();
    //                            l.add(angle);
    //                            l.add(180. + angle);
    //                            angles.put(t, l);
    //                        }
    //                        else{
    //                            angles.get(t).add(angle);
    //                            angles.get(t).add(180. + angle);
    //                        }
    //                    }
    //                }
    //
    //                //***********
    //
    //                STEntity second = stg.getEndpoints(e).getSecond();
    //                p2 = second.getGeometry().toGeoxGeometry().coord().get(0);
    //                p1 = null;
    //                if(e.getGeometry().toGeoxGeometry().coord().get(0).distance(p2)<e.getGeometry().toGeoxGeometry()
    //                        .coord().get(e.getGeometry().toGeoxGeometry().coord().size()-1).distance(p2)){
    //                    p1 = e.getGeometry().toGeoxGeometry().coord().get(1);
    //                }
    //                else{
    //                    p1 = e.getGeometry().toGeoxGeometry()
    //                            .coord().get(e.getGeometry().toGeoxGeometry().coord().size()-2);
    //                }
    //                neighbors.clear();
    //                neighbors = new HashSet<STEntity>();
    //                for(STEntity nei : stg.getIncidentEdges(first)){
    //                    if(nei.existsAt(t) && !(selection.get(t).contains(nei))){
    //                        neighbors.add(nei);
    //                    }
    //                }
    //                neighbors.remove(e);
    //                if(neighbors.size()!=1 && !neighbors.isEmpty()){
    //                    features.add(new DefaultFeature(new GM_Point(p2)));
    //                    features.add(new DefaultFeature(new GM_Point(p1)));
    //
    //                    for(STEntity nei : neighbors){
    //                        IDirectPosition p3 = null;
    //                        if(nei.getGeometry().toGeoxGeometry().coord().get(0).distance(p2)<nei.getGeometry().toGeoxGeometry()
    //                                .coord().get(nei.getGeometry().toGeoxGeometry().coord().size()-1).distance(p2)){
    //                            p3 = nei.getGeometry().toGeoxGeometry().coord().get(1);
    //                        }
    //                        else{
    //                            p3 = nei.getGeometry().toGeoxGeometry()
    //                                    .coord().get(nei.getGeometry().toGeoxGeometry().coord().size()-2);
    //                        }
    //                        features.add(new DefaultFeature(new GM_Point(p3)));
    //
    //                        Double angle = Angle.angleTroisPoints(p1, p2, p3).getValeur();
    //                        angle = Math.abs(Math.PI - angle);
    //
    //                        // angle = Math.abs(Math.PI/2. - angle);
    //                        angle = angle * 180. / Math.PI;
    //
    //                        if(!angles.containsKey(t)){
    //                            List<Double> l = new ArrayList<Double>();
    //                            l.add(angle);
    //                            l.add(180. + angle);
    //                            angles.put(t, l);
    //                        }
    //                        else{
    //                            angles.get(t).add(angle);
    //                            angles.get(t).add(180.+ angle);
    //                        }
    //                    }
    //                }
    //            }
    //        }
    //
    //        for(FuzzyTemporalInterval  t: angles.keySet()){
    //            File f = new File("/home/bcostes/Bureau/tmp/orientation");
    //            if(!f.exists()){
    //                f.mkdir();
    //            }
    //            try {
    //                FileWriter fr = new FileWriter(f.toString() + "/shift_" + t.toString());
    //                BufferedWriter br = new BufferedWriter(fr);
    //                String str ="";
    //                for(Double d : angles.get(t)){
    //                    str += (int)d.doubleValue() + "\n";
    //                }
    //                br.write(str);
    //                br.flush();
    //                br.close();
    //            } catch (IOException e) {
    //                // TODO Auto-generated catch block
    //                e.printStackTrace();
    //            }

    //   }
  }
  //
  //        ShapefileWriter.write(features, "/home/bcostes/Bureau/tmp/orientation/points.shp");


  //        stg.getTemporalDomain().asList().remove(new FuzzyTemporalInterval(1884, 1885, 1888, 1889));
  //               JungSnapshot s =stg.getSnapshotAt(stg.getTemporalDomain().asList().get(0));
  //
  //                 Map<GraphEntity, Double> m= s.calculateEdgeCentrality(new MeanDistance(), NORMALIZERS.NONE);
  //                 IPopulation<IFeature> out = new Population<IFeature>();
  //                 for(GraphEntity e : s.getEdges()){
  //                     
  //                     IFeature df = new DefaultFeature(e.getGeometry().toGeoxGeometry());
  //                     AttributeManager.addAttribute(df, "mean_d", m.get(e),"Double");
  //                     out.add(df);
  //                 }
  //                 ShapefileWriter.write(out, "/home/bcostes/Bureau/tmp/mean_d.shp");
  //TAGIoManager.ex




  //  SnapshotGraph snap = SnapshotIOManager.shp2Snapshot("/media/Data/Benoit/these/analyses/TAG/TAG/etape0/snapshot_1785.0_1795.0_edges.shp", new LengthEdgeWeighting(), null, true);




  //                snap.nodeLocalIndicator(new AlphaCentrality(), NORMALIZERS.NONE);
  //                snap.nodeLocalIndicator(new BetweennessCentrality(), NORMALIZERS.NONE);
  //                snap.nodeLocalIndicator(new Circuity(), NORMALIZERS.NONE);
  //                snap.nodeLocalIndicator(new ClosenessCentrality(), NORMALIZERS.NONE);
  //                snap.nodeLocalIndicator(new ClusteringCentrality(), NORMALIZERS.NONE);
  //                snap.nodeLocalIndicator(new CombinedDegreeCentrality(), NORMALIZERS.NONE);
  //                snap.nodeLocalIndicator(new ControlCentrality(), NORMALIZERS.NONE);
  //                snap.nodeLocalIndicator(new DegreeCentrality(), NORMALIZERS.NONE);
  //                snap.nodeLocalIndicator(new Efficiency(), NORMALIZERS.NONE);
  //                snap.nodeLocalIndicator(new EigenvectorCentrality(), NORMALIZERS.NONE);
  //                snap.nodeLocalIndicator(new Empan(), NORMALIZERS.NONE);
  //                snap.nodeLocalIndicator(new MaxDistance(), NORMALIZERS.NONE);
  //                snap.nodeLocalIndicator(new MeanDistance(), NORMALIZERS.NONE);
  //                snap.nodeLocalIndicator(new MinDistance(), NORMALIZERS.NONE);
  //                snap.nodeLocalIndicator(new PageRankCentrality(), NORMALIZERS.NONE);
  //                snap.nodeLocalIndicator(new StraightnessCentrality(), NORMALIZERS.NONE);
  //                snap.nodeLocalIndicator(new WeightedDegreeCentrality(), NORMALIZERS.NONE);
  //                
  //                String s = "";
  //                for(String ind : snap.getNodesLocalIndicators()){
  //                   s+=ind + " ; "; 
  //                }
  //                s = s.substring(0, s.length()-2);
  //                s+="\n";
  //                
  //                for(GraphEntity e  : snap.getVertices()){
  //                    for(String ind : snap.getNodesLocalIndicators()){
  //                        s+=e.getLocalIndicators().get(ind)+" ; ";
  //                     }
  //                    s = s.substring(0, s.length()-2);
  //                    s+="\n";
  //                }
  //                FileWriter fr;
  //                try {
  //                    fr = new FileWriter("/media/Data/Benoit/these/analyses/indicateurs/correlations/nodes/indicateurs.csv");
  //                    BufferedWriter br = new BufferedWriter(fr);
  //                    br.write(s);
  //                    br.flush();
  //                    br.close();
  //                } catch (IOException e1) {
  //                    // TODO Auto-generated catch block
  //                    e1.printStackTrace();
  //                }




  //        Map<ILocalIndicator, NORMALIZERS> edgeLocal = new HashMap<ILocalIndicator, SnapshotGraph.NORMALIZERS>();
  //        edgeLocal.put(new BetweennessCentrality(), NORMALIZERS.CONVENTIONAL);
  //        edgeLocal.put(new Circuity(), NORMALIZERS.CONVENTIONAL);
  //        edgeLocal.put(new ClusteringCentrality(), NORMALIZERS.NONE);
  //        edgeLocal.put(new ControlCentrality(), NORMALIZERS.NONE);
  //        edgeLocal.put(new DegreeCentrality(), NORMALIZERS.CONVENTIONAL);
  //        edgeLocal.put(new MeanDistance(), NORMALIZERS.CONVENTIONAL);
  //        edgeLocal.put(new StraightnessCentrality(), NORMALIZERS.CONVENTIONAL);
  //        edgeLocal.put(new WeightedDegreeCentrality(), NORMALIZERS.CONVENTIONAL);
  //
  //        stg.edgeLocalIndicator(edgeLocal);
  //        System.out.println("OKKKKKKKK 1");
  //
  //        // noeuds
  //        Map<ILocalIndicator, NORMALIZERS> nodeLocal = new HashMap<ILocalIndicator, SnapshotGraph.NORMALIZERS>();
  //
  //        nodeLocal.put(new BetweennessCentrality(), NORMALIZERS.CONVENTIONAL);
  //        nodeLocal.put(new Circuity(), NORMALIZERS.CONVENTIONAL);
  //        nodeLocal.put(new ClusteringCentrality(), NORMALIZERS.NONE);
  //        nodeLocal.put(new ControlCentrality(), NORMALIZERS.NONE);
  //        nodeLocal.put(new DegreeCentrality(), NORMALIZERS.CONVENTIONAL);
  //        nodeLocal.put(new MeanDistance(), NORMALIZERS.CONVENTIONAL);
  //        nodeLocal.put(new StraightnessCentrality(), NORMALIZERS.CONVENTIONAL);
  //        nodeLocal.put(new WeightedDegreeCentrality(), NORMALIZERS.CONVENTIONAL);
  //
  //        stg.nodeLocalIndicator(nodeLocal);
  //        System.out.println("OKKKKKKKK 2");
  //
  //        //locale ...
  //        Map<ILocalIndicator, NORMALIZERS> edgeNei1 = new HashMap<ILocalIndicator, SnapshotGraph.NORMALIZERS>();
  //
  //        edgeNei1.put(new BetweennessCentrality(), NORMALIZERS.CONVENTIONAL);
  //        edgeNei1.put(new Circuity(), NORMALIZERS.CONVENTIONAL);
  //        edgeNei1.put(new DegreeCentrality(), NORMALIZERS.CONVENTIONAL);
  //        edgeNei1.put(new MeanDistance(),NORMALIZERS.CONVENTIONAL);
  //        edgeNei1.put(new StraightnessCentrality(), NORMALIZERS.CONVENTIONAL);
  //        edgeNei1.put(new WeightedDegreeCentrality(), NORMALIZERS.CONVENTIONAL);
  //
  //        stg.neighborhoodEdgeLocalIndicator(edgeNei1, 2);        
  //        stg.neighborhoodEdgeLocalIndicator(edgeNei1, 3);
  //        stg.neighborhoodEdgeLocalIndicator(edgeNei1, 5);
  //        stg.neighborhoodEdgeLocalIndicator(edgeNei1, 10);
  //
  //
  //
  //
  //        stg.graphGlobalIndicator(new Alpha());
  //        stg.graphGlobalIndicator(new AveragePathLength());
  //        stg.graphGlobalIndicator(new Beta());
  //        stg.graphGlobalIndicator(new ClusteringCoefficient());
  //        stg.graphGlobalIndicator(new Density());
  //        stg.graphGlobalIndicator(new DetourIndex());
  //        stg.graphGlobalIndicator(new Diameter());
  //        stg.graphGlobalIndicator(new Gamma());
  //        stg.graphGlobalIndicator(new MeanEdgeLength());
  //        stg.graphGlobalIndicator(new MeanEdgesOrientation());
  //        stg.graphGlobalIndicator(new MeanNodeDegree());
  //        stg.graphGlobalIndicator(new Mu());
  //        stg.graphGlobalIndicator(new Pi());
  //        stg.graphGlobalIndicator(new TotalEdgeLength());
  //
  //
  //        System.out.println();
  //        for(FuzzyTemporalInterval t : stg.getTemporalDomain().asList()){
  //            if(!t.equals(new FuzzyTemporalInterval(1884, 1885, 1888, 1889) )){
  //                JungSnapshot s = stg.getSnapshotAt(t);
  //                System.out.println(s.getVertexCount()+" " + s.getEdgeCount()+" "+ stg.getGlobalIndicators().get(t).get((new AveragePathLength()).getName())+
  //                        " "+  stg.getGlobalIndicators().get(t).get((new ClusteringCoefficient()).getName()));
  //            }
  //        }
  //        Map<FuzzyTemporalInterval, Map<String, Double>> values = stg.getGlobalIndicators();
  //        values.remove(new FuzzyTemporalInterval(1884, 1885, 1888, 1889));
  //        GlobalIndicatorChart chart = new GlobalIndicatorChart(values);
  ////        chart.build();



  //        STGraph stg = TAGIoManager.deserialize("/media/Data/Benoit/these/analyses/indicateurs/tag_centralities/tag_with_centralities.tag");
  //
  // TAGIoManager.exportTAG(stg, "/media/Data/Benoit/these/analyses/indicateurs/tag_centralities/centralities_tag.shp");
  //
  //  TAGIoManager.serialize(stg, "/media/Data/Benoit/these/analyses/indicateurs/tag_centralities/tag_with_centralities.tag");
  //        Map<FuzzyTemporalInterval, Map<String, List<Double>>> values = new HashMap<FuzzyTemporalInterval, Map<String,List<Double>>>();
  //
  //        for(FuzzyTemporalInterval t : stg.getTemporalDomain().asList()){
  //            if(!t.equals(new FuzzyTemporalInterval(1884, 1885, 1888, 1889) )){
  //                Map<String, List<Double>> ind = new HashMap<String, List<Double>>();
  //                for(String s : stg.getEdgesLocalIndicators().get(t)){
  //                    List<Double> l = new ArrayList<Double>();
  //                    for(STEntity e : stg.getEdges()){
  //                        if(e.existsAt(t)){
  //                            l.add(e.getLocalIndicatorAt(s, t));
  //                        }
  //                    }
  //                    ind.put(s, l);
  //                }
  //                values.put(t, ind);
  //            }
  //        }
  //        LocalIndicatorChart chart = new LocalIndicatorChart(values);
  //        chart.build();
  //}


  //    public static void export(){
  //        GraphDatabaseService graphDb = new GraphDatabaseFactory()
  //        .newEmbeddedDatabase("/home/bcostes/Bureau/neo4j/");
  //        registerShutdownHook(graphDb);
  //        
  //        Node n = graphDb.createNode();
  //        
  //        Relationship r = n.createRelationshipTo(n, null);
  //        
  //        try (Transaction tx = graphDb.beginTx())
  //        {
  //
  //            SpatialDatabaseService spatialService = new SpatialDatabaseService(graphDb);
  //            SimplePointLayer layerNodes = (SimplePointLayer)spatialService.getLayer("layer_nodes");
  //            EditableLayer layerEdges = (EditableLayer)spatialService.getLayer("layer_edges");
  //            
  //            
  //           /* int idToFind = 1;
  //           Label labelNodes = DynamicLabel.label( "Edge" );
  //            ResourceIterator<Node> users = graphDb
  //                    .findNodesByLabelAndProperty( labelNodes, "id", idToFind )
  //                    .iterator();
  //                    
  //            Node firstUserNode;
  //            if ( users.hasNext() )
  //            {
  //                firstUserNode = users.next();
  //                System.out.println(firstUserNode.getProperty("coord1"));
  //            }
  //            users.close();*/
  //            
  //
  //            /*SimpleFeatureType featureType = null;
  //            try {
  //                featureType = DataUtilities.createType("Test", //$NON-NLS-1$
  //                        "location:Point," + //$NON-NLS-1$
  //                        "ID:Integer" //$NON-NLS-1$
  //                        );
  //            } catch (SchemaException ex) {
  //                ex.printStackTrace();
  //            }
  //            SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(featureType);
  //            DefaultFeatureCollection collection = new DefaultFeatureCollection("test", featureType);
  //
  //            List<SpatialDatabaseRecord> l = GeoPipeline.start(layer).toSpatialDatabaseRecordList();
  //            LayerIndexReader index = layer.getIndex();
  //
  //            for(SpatialDatabaseRecord record : l){
  //                Point pt = (Point)record.getGeometry();
  //                System.out.println(pt.getX());
  //                featureBuilder.add(pt);
  //                featureBuilder.add(new Integer(5));
  //                SimpleFeature newFeature = featureBuilder.buildFeature(null);
  //                collection.add(newFeature);
  //            }
  //            ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();
  //            Map<String, Serializable> params = new HashMap<String, Serializable>();
  //            params.put("url", new URL("file:///" + "/home/bcostes/Bureau/shp/test.shp")); //$NON-NLS-1$  //$NON-NLS-2$
  //            params.put("create spatial index", Boolean.TRUE); //$NON-NLS-1$  
  //            ShapefileDataStore newDataStore;
  //            try {
  //                newDataStore = (ShapefileDataStore) dataStoreFactory.createNewDataStore(params);
  //                newDataStore.createSchema(featureType);
  //                org.geotools.data.Transaction transaction = new DefaultTransaction("create"); //$NON-NLS-1$
  //
  //                String typeName = newDataStore.getTypeNames()[0];
  //                SimpleFeatureSource featureSource = newDataStore.getFeatureSource(typeName);
  //                if (featureSource instanceof SimpleFeatureStore) {
  //                    SimpleFeatureStore featureStore = (SimpleFeatureStore) featureSource;
  //                    featureStore.setTransaction(transaction);
  //                    try {
  //                        featureStore.addFeatures(collection);
  //                        transaction.commit();
  //
  //                    } catch (Exception problem) {
  //                        problem.printStackTrace();
  //                        transaction.rollback();
  //
  //                    } finally {
  //                        transaction.close();
  //                    }
  //                } else {
  //                    System.out.println(typeName + " does not support read/write access");  //$NON-NLS-1$
  //                }
  //            } catch (IOException e) {
  //                // TODO Auto-generated catch block
  //                e.printStackTrace();
  //            }
  //            tx.success();
  //        } catch (MalformedURLException e1) {
  //            // TODO Auto-generated catch block
  //            e1.printStackTrace();
  //        }*/
  //        }
  //        graphDb.shutdown();
  //
  //    }

  //    private static enum RelTypes implements RelationshipType
  //    {
  //        SpatialLink
  //    }
  //
  //
  //    public static void createDb() {
  //        // Create the graph db
  //        GraphDatabaseService graphDb = new GraphDatabaseFactory()
  //        .newEmbeddedDatabase("/home/bcostes/Bureau/neo4j/");
  //        registerShutdownHook(graphDb);
  //        SpatialDatabaseService spatialService = new SpatialDatabaseService(graphDb);
  //        SimplePointLayer layer;
  //        IndexDefinition indexNodes;
  //        IndexDefinition indexEdges;
  //        Label labelNodes;
  //        Label labelEdges;
  //        try ( Transaction tx = graphDb.beginTx() )
  //        {
  //            Schema schema = graphDb.schema();
  //            indexNodes = schema.indexFor(DynamicLabel.label( "Node" ))
  //                    .on( "id" )
  //                    .create();
  //            tx.success();
  //        }
  //        try ( Transaction tx = graphDb.beginTx() )
  //        {
  //            Schema schema = graphDb.schema();
  //            indexEdges = schema.indexFor(DynamicLabel.label( "Edge" ))
  //                    .on( "id" )
  //                    .create();
  //            tx.success();
  //        }
  //
  //
  //        try (Transaction tx = graphDb.beginTx())
  //        {
  //            GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory(null);       
  //
  //            labelNodes = DynamicLabel.label( "Node" );
  //            labelEdges = DynamicLabel.label( "Edge" );
  //            layer = spatialService.createSimplePointLayer("layer_nodes");
  //            Point pt = geometryFactory.createPoint(new Coordinate(1000, 1000)); 
  //            SpatialDatabaseRecord record = layer.add(pt);
  //            record.getGeomNode().addLabel(labelNodes);
  //            record.getGeomNode().setProperty("id", 1);
  //            record.getGeomNode().setProperty("toto", "toto1");
  //
  //
  //
  //            Point pt2 = geometryFactory.createPoint(new Coordinate(500, 1500)); 
  //            SpatialDatabaseRecord record2 = layer.add(pt2);
  //            record2.getGeomNode().addLabel(labelNodes);
  //            record2.getGeomNode().setProperty("id", 2);
  //            record2.getGeomNode().setProperty("toto", "toto2");
  //
  //
  //
  //
  //
  //            Point pt3 = geometryFactory.createPoint(new Coordinate(1500, 1500)); 
  //            SpatialDatabaseRecord record3 = layer.add(pt3);
  //            record3.getGeomNode().addLabel(labelNodes);
  //            record3.getGeomNode().setProperty("id", 3);
  //            record3.getGeomNode().setProperty("toto", "toto3");
  //            
  //           /* int idToFind = 2;
  //                    ResourceIterator<Node> users = graphDb
  //                            .findNodesByLabelAndProperty( labelNodes, "id", idToFind )
  //                            .iterator();
  //                            
  //                Node firstUserNode;
  //                if ( users.hasNext() )
  //                {
  //                    firstUserNode = users.next();
  //                    System.out.println(firstUserNode.getProperty("toto"));
  //                }
  //                users.close();*/
  //                            
  //    
  //
  //
  //
  //
  //            List<SpatialDatabaseRecord> l = GeoPipeline.start(layer).toSpatialDatabaseRecordList();
  //            List<SpatialDatabaseRecord> l2 = new ArrayList<SpatialDatabaseRecord>();
  //            l2.addAll(l);
  //
  //
  //            EditableLayer layer2 = spatialService.getOrCreateEditableLayer("layer_edges");
  //
  //            for(SpatialDatabaseRecord r : l){
  //                l2.remove(r);
  //                for(SpatialDatabaseRecord r2: l2){
  //                    Point p1 = (Point)r.getGeometry();
  //                    Point p2 = (Point)r2.getGeometry();
  //
  //                    CoordinateSequence s = new CoordinateSequence2D(p1.getX(), p1.getY(), p2.getX(), p2.getY());
  //                    LineString line = geometryFactory.createLineString(s);
  //                    SpatialDatabaseRecord rline = layer2.add(line);
  //                    rline.getGeomNode().addLabel(labelEdges);
  //                    rline.getGeomNode().setProperty("id", 1);
  //                    rline.getGeomNode().setProperty("coord1", line.toText());
  //
  //
  //                    Relationship rel = r.getGeomNode().createRelationshipTo(r2.getGeomNode(), RelTypes.SpatialLink);
  //                    rel.setProperty("id_edge", 1);
  //                }
  //            }
  //
  //
  //
  //            tx.success();
  //        }
  //        graphDb.shutdown();
  //    }




  // Database operations go here
  /* Node firstNode = graphDb.createNode();
            firstNode.setProperty( "prenom", "Benoit" );
            firstNode.setProperty( "nom", "Costes" );

            Node secondNode = graphDb.createNode();
            secondNode.setProperty( "prenom", "Mélanie" );
            secondNode.setProperty( "nom", "Morineau" );

            Node thirdNode = graphDb.createNode();
            thirdNode.setProperty( "prenom", "Alexandre" );


            Relationship relationship = firstNode.createRelationshipTo(secondNode, RelTypes.KNOWS );
            relationship.setProperty( "depuis", "5 ans" );
            relationship.setProperty( "d'où", "FB" );
            relationship = firstNode.createRelationshipTo(secondNode, RelTypes.LOVE );
            relationship.setProperty( "depuis", "4 ans" );

            relationship = firstNode.createRelationshipTo(thirdNode, RelTypes.KNOWS );
            relationship.setProperty( "depuis", "12 ans" );
            relationship.setProperty( "d'où", "Lycée Maurice Ravel" );
            Iterator<Relationship> itr = firstNode.getRelationships().iterator();
           while(itr.hasNext()){
               Relationship r = itr.next();
               System.out.println(r.getType());
               System.out.println(r.getProperty("depuis"));
           }*/

  /* tx.success();
            tx.finish();

        }
    }*/



}
