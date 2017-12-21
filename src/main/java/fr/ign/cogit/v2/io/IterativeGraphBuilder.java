package fr.ign.cogit.v2.io;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.uci.ics.jung.graph.UndirectedSparseMultigraph;
import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IFeatureCollection;
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPositionList;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ILineString;
import fr.ign.cogit.geoxygene.api.spatial.geomprim.IPoint;
import fr.ign.cogit.geoxygene.api.spatial.geomroot.IGeometry;
import fr.ign.cogit.geoxygene.contrib.I18N;
import fr.ign.cogit.geoxygene.contrib.appariement.EnsembleDeLiens;
import fr.ign.cogit.geoxygene.contrib.appariement.Lien;
import fr.ign.cogit.geoxygene.contrib.appariement.reseaux.Appariement;
import fr.ign.cogit.geoxygene.contrib.appariement.reseaux.LienReseaux;
import fr.ign.cogit.geoxygene.contrib.appariement.reseaux.ParametresApp;
import fr.ign.cogit.geoxygene.contrib.appariement.reseaux.topologie.ArcApp;
import fr.ign.cogit.geoxygene.contrib.appariement.reseaux.topologie.NoeudApp;
import fr.ign.cogit.geoxygene.contrib.appariement.reseaux.topologie.ReseauApp;
import fr.ign.cogit.geoxygene.contrib.cartetopo.Arc;
import fr.ign.cogit.geoxygene.contrib.cartetopo.CarteTopo;
import fr.ign.cogit.geoxygene.contrib.cartetopo.Groupe;
import fr.ign.cogit.geoxygene.contrib.cartetopo.Noeud;
import fr.ign.cogit.geoxygene.feature.DefaultFeature;
import fr.ign.cogit.geoxygene.feature.Population;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPositionList;
import fr.ign.cogit.geoxygene.spatial.coordgeom.GM_LineString;
import fr.ign.cogit.geoxygene.spatial.geomprim.GM_Point;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileReader;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileWriter;
import fr.ign.cogit.geoxygene.util.index.Tiling;
import fr.ign.cogit.v2.geometry.LightDirectPosition;
import fr.ign.cogit.v2.geometry.LightLineString;
import fr.ign.cogit.v2.geometry.LightMultipleGeometry;
import fr.ign.cogit.v2.lineage.IterativeFiliationGraph;
import fr.ign.cogit.v2.lineage.MatchingLink;
import fr.ign.cogit.v2.lineage.STGroupe;
import fr.ign.cogit.v2.tag.FuzzyTemporalInterval;
import fr.ign.cogit.v2.tag.STEntity;
import fr.ign.cogit.v2.tag.STGraph;
import fr.ign.cogit.v2.tag.STProperty;
import fr.ign.cogit.v2.tag.TemporalDomain;


public class IterativeGraphBuilder {
  private Map<FuzzyTemporalInterval, String> shapefiles;
  private ParametresApp params;
  private TemporalDomain temporalDomain;

  public IterativeGraphBuilder(Map<FuzzyTemporalInterval, String> shapefiles) {
    this.shapefiles = shapefiles;
    this.params = new ParametresApp();
    this.params.debugBilanSurObjetsGeo = true;
    // fait buguer l'algo car rond points déja traités en amont
    this.params.varianteChercheRondsPoints = false;
    this.params.debugTirets = false;
    this.params.distanceArcsMax = 15;
    this.params.distanceArcsMin = 5;
    this.params.distanceNoeudsMax = 15;
    this.params.distanceNoeudsImpassesMax = 15;
    this.params.varianteRedecoupageNoeudsNonApparies = true;
    // this.params.varianteForceAppariementSimple = true;
    this.params.varianteRedecoupageNoeudsNonApparies_DistanceNoeudArc = 15;
    this.params.varianteRedecoupageNoeudsNonApparies_DistanceProjectionNoeud = 5;

    List<FuzzyTemporalInterval> ti = new ArrayList<FuzzyTemporalInterval>();
    ti.addAll(shapefiles.keySet());
    Collections.sort(ti);
    this.temporalDomain = new TemporalDomain(ti);
  }

  public STGraph buildSTGraph(boolean mergePlaces,  Map<FuzzyTemporalInterval, Double> accuracies ) {

    // le premier
    Iterator<FuzzyTemporalInterval> it = this.temporalDomain.asList().iterator();
    // initialisation
    FuzzyTemporalInterval t = it.next();
    String shp = this.shapefiles.get(t);
    IPopulation<IFeature> pop1 = ShapefileReader.read(shp);
    params.populationsArcs1.add(pop1);
    Map<IGeometry, List<IGeometry>> places = null;
    if (mergePlaces) {
      places = new HashMap<IGeometry, List<IGeometry>>();
    }
    ReseauApp reseauRef = this.importData(this.params, true, places);
    STGraph snapshot1 = this.createJungGraph(t, params.populationsArcs1.get(0),
        params.populationsNoeuds1.get(0), places);
    
    if(accuracies != null){
        STProperty<Double> staccuracies = new STProperty<Double>(STProperty.PROPERTIE_FINAL_TYPES.Attribute, "Accuracies");
        staccuracies.setValues(accuracies);
        snapshot1.setAccuracies(staccuracies);
    }

    IterativeFiliationGraph filiationGraph = new IterativeFiliationGraph(
        this.temporalDomain, snapshot1);

    // début de la boucle
    while (it.hasNext()) {
      FuzzyTemporalInterval t2 = it.next();
      String shp2 = this.shapefiles.get(t2);
      IPopulation<IFeature> pop2 = ShapefileReader.read(shp2);
      params.populationsArcs2.clear();
      params.populationsNoeuds2.clear();
      params.populationsArcs2.add(pop2);

      if (mergePlaces) {
        places = new HashMap<IGeometry, List<IGeometry>>();
      }
      ReseauApp reseauComp = this.importData(this.params, false, places);
      STGraph snapshot2 = this.createJungGraph(t2,
          params.populationsArcs2.get(0), params.populationsNoeuds2.get(0),
          places);

      /*
       * ShapefileWriter.write(params.populationsArcs1.get(0),
       * "/home/bcostes/Bureau/tmp/arcs1.shp");
       * ShapefileWriter.write(params.populationsArcs2.get(0),
       * "/home/bcostes/Bureau/tmp/arcs2.shp");
       * ShapefileWriter.write(params.populationsNoeuds1.get(0),
       * "/home/bcostes/Bureau/tmp/noeuds1.shp");
       * ShapefileWriter.write(params.populationsNoeuds2.get(0),
       * "/home/bcostes/Bureau/tmp/noeuds2.shp");
       */

      EnsembleDeLiens liens = this.match(reseauRef, reseauComp);

      // les stlinks
      Set<MatchingLink> stlinks = this.createSTLinks(filiationGraph.getStGraph(), t2,
          snapshot2, liens);
      filiationGraph.buildNextIteration(t2, snapshot2,stlinks);

      IFeatureCollection<IFeature> popN = new Population<IFeature>();
      IFeatureCollection<IFeature> popE = new Population<IFeature>();
      for (STEntity edge : filiationGraph.getStGraph().getEdges()) {
        popE.add(new DefaultFeature(((LightLineString) edge.getGeometry())
            .toGeoxGeometry()));
      }
      for (STEntity node : filiationGraph.getStGraph().getVertices()) {
        popN.add(new DefaultFeature((node.getGeometry()).toGeoxGeometry()));
      }

      params.populationsArcs1.clear();
      params.populationsNoeuds1.clear();
      params.populationsArcs1.add(popE);
      params.populationsNoeuds1.add(popN);

      reseauRef = this.importData(this.params);

    }
    this.shapefiles.clear();
    this.shapefiles = null;

    return filiationGraph.getStGraph();
  }

  /**
   * Crée les stlink entre deux snapshots
   * @param t1
   * @param t2
   * @param g1
   * @param g2
   * @param liens
   * @return
   */
  private Set<MatchingLink> createSTLinks(STGraph stgraph, FuzzyTemporalInterval t2,
      UndirectedSparseMultigraph<STEntity, STEntity> g2, EnsembleDeLiens liens) {

    /*
     * IFeatureCollection<IFeature> arcs1 = new Population<IFeature>();
     * IFeatureCollection<IFeature> arcs2 = new Population<IFeature>();
     * IFeatureCollection<IFeature> n1 = new Population<IFeature>();
     * IFeatureCollection<IFeature> n2 = new Population<IFeature>();
     * 
     * for (STEntity e : stgraph.getEdges()) { arcs1.add(new
     * DefaultFeature(e.getGeometry().toGeoxGeometry())); } for (STEntity node :
     * stgraph.getVertices()) { n1.add(new
     * DefaultFeature(node.getGeometry().toGeoxGeometry())); }
     * 
     * for (STEntity e : g2.getEdges()) { arcs2.add(new
     * DefaultFeature(e.getGeometryAt(t2).toGeoxGeometry())); } for (STEntity
     * node : g2.getVertices()) { n2.add(new
     * DefaultFeature(node.getGeometryAt(t2).toGeoxGeometry())); }
     * 
     * ShapefileWriter.write(arcs1, "/home/bcostes/Bureau/tmp/g1_e.shp");
     * ShapefileWriter.write(arcs2, "/home/bcostes/Bureau/tmp/g2_e.shp");
     * ShapefileWriter.write(n1, "/home/bcostes/Bureau/tmp/g1_n.shp");
     * ShapefileWriter.write(n2, "/home/bcostes/Bureau/tmp/g2_n.shp");
     */

    Set<MatchingLink> stlinks = new HashSet<MatchingLink>();
    for (Lien lien : liens) {
       // MatchingLink stlink = new MatchingLink(t2);
      MatchingLink stlink =null;
      STGroupe sources = new STGroupe();
      STGroupe targets = new STGroupe();
      for (IFeature source : lien.getObjetsRef()) {
        // noeud ou arc ?
        if (source.getGeom() instanceof GM_Point) {
          // sommet
          STEntity stnode = null;
          for (STEntity stnode2 : stgraph.getVertices()) {
            if (((GM_Point) stnode2.getGeometry().toGeoxGeometry())
                .equalsExact(source.getGeom(), 0.005)) {
              stnode = stnode2;
              break;
            }
          }
          if (stnode != null) {
            targets.getNodes().add(stnode);
          } else {
            // problème : noeud non trouvé
            System.out
                .println("Sommet 1 non trouvé : DefaultFiliationGraphBuilder.createSTLinks");
            System.out.println(source.getGeom().toString());
            System.exit(-16);
          }
        } else {
          // arc
          STEntity stedge = null;
          for (STEntity stedge2 : stgraph.getEdges()) {
            if (stedge2.getGeometry().toGeoxGeometry()
                .equalsExact(source.getGeom(), 0.005)) {
              stedge = stedge2;
              break;
            }
          }
          if (stedge != null) {
            targets.getEdges().add(stedge);
          } else {
            // problème : arc non trouvé
            System.out
                .println("Arc 1 non trouvé : DefaultFiliationGraphBuilder.createSTLinks");
            System.exit(-17);
          }
        }
      }
      // ************************************************************
      // objets comp
      for (IFeature target : lien.getObjetsComp()) {
        // noeud ou arc ?
        if (target.getGeom() instanceof GM_Point) {
          // sommet
          STEntity stnode = null;
          for (STEntity stnode2 : g2.getVertices()) {
            if (((GM_Point) stnode2.getGeometryAt(t2).toGeoxGeometry())
                .equals(target.getGeom())) {
              stnode = stnode2;
              break;
            }
          }
          if (stnode != null) {
            sources.getNodes().add(stnode);
          } else {
            // problème : noeud non trouvé
            System.out
                .println("Sommet 2 non trouvé : DefaultFiliationGraphBuilder.createSTLinks");
            System.exit(-18);
          }
        } else {
          // arc
          STEntity stedge = null;
          for (STEntity stedge2 : g2.getEdges()) {
            if (stedge2.getGeometryAt(t2).toGeoxGeometry()
                .equals(target.getGeom())) {
              stedge = stedge2;
              break;
            }
          }
          if (stedge != null) {
            sources.getEdges().add(stedge);
          } else {
            // problème : arc non trouvé
            System.out
                .println("Arc 2 non trouvé : DefaultFiliationGraphBuilder.createSTLinks");
            System.exit(-19);
          }
        }
      }
      stlink.setSources(sources);
      stlink.setTargets(targets);
      stlinks.add(stlink);
    }
    if (stlinks.size() != liens.size()) {
      System.out
          .println("Taille de la liste de STLink en sortie non conforme à la taille de l'EnsembleDeLiens en entrée");
      System.exit(-20);
    }
    return stlinks;
  }

  /**
   * Crée une graphe jung à partir des populations d'arcs et de sommets
   * @param t
   * @param popEdges
   * @param popNodes
   * @return
   */
  private STGraph createJungGraph(FuzzyTemporalInterval t,
      IFeatureCollection<? extends IFeature> popEdges,
      IFeatureCollection<? extends IFeature> popNodes,
      Map<IGeometry, List<IGeometry>> places) {
    STGraph graph = new STGraph(this.temporalDomain);
    // les noeuds
    Map<IFeature, STEntity> nodes = new HashMap<IFeature, STEntity>();
    STProperty<Boolean> s = new STProperty<Boolean>(STProperty.PROPERTIE_FINAL_TYPES.TimeSerie, null);
    for(FuzzyTemporalInterval tt: this.temporalDomain.asList()){
        s.setValueAt(t, false);
    }
    s.setValueAt(t, true);
    STEntity.setCurrentType(STEntity.NODE);
    for (IFeature node : popNodes) {
      STEntity stnode = new STEntity(s.copy());
      // s'agit t'il d'une place ?
      IGeometry gN = null;
      if (places != null) {
        for (IGeometry g : places.keySet()) {
          if (g.distance(node.getGeom()) < 0.0005) {
            gN = g;
            break;
          }
        }
      }
      if (gN != null) {
        // il s'agit d'un noeud issu de la fusion d'une place
        List<LightDirectPosition> nodesG = new ArrayList<LightDirectPosition>();
        List<LightLineString> edgesG = new ArrayList<LightLineString>();
        for (IGeometry gg : places.get(gN)) {
          if (gg instanceof IPoint) {
            nodesG.add(new LightDirectPosition(gg.coord().get(0)));
          } else {
            edgesG.add(new LightLineString(gg.coord()));
          }
        }
        LightMultipleGeometry gM = new LightMultipleGeometry(edgesG, nodesG);
        stnode.setGeometryAt(t, gM);
        gM.setGeoxGeom(new LightDirectPosition(gN.coord().get(0)));
        places.remove(gN);
      } else {
        stnode.setGeometryAt(t, new LightDirectPosition(node.getGeom().coord()
            .get(0)));
      }
      nodes.put(node, stnode);
    }
    // les arcs
    STEntity.setCurrentType(STEntity.EDGE);
    for (IFeature edge : popEdges) {
      STEntity stegde = new STEntity(s.copy());
      stegde.setGeometryAt(t, new LightLineString(edge.getGeom().coord()));
      stegde.setWeightAt(t, edge.getGeom().length());
      // identification des extrémités
      IFeature node1 = null, node2 = null;
      for (IFeature node : popNodes) {
        if (node.getGeom().coord().get(0).equals(edge.getGeom().coord().get(0))) {
          node1 = node;
          break;
        }
      }
      for (IFeature node : popNodes) {
        if (node
            .getGeom()
            .coord()
            .get(0)
            .equals(
                edge.getGeom().coord().get(edge.getGeom().coord().size() - 1))) {
          node2 = node;
          break;
        }
      }

      if (node1 == null || node2 == null) {
        // problème ! on a pas trouvé les extrémités
        System.out
            .println("Extrémités non trouvées : DefaultFiliationGraphBuilder.createJungGraph");
        System.exit(-15);
      }
      // sinon on crée un arc jung
      graph.addEdge(stegde, nodes.get(node1), nodes.get(node2));
    }

    for (STEntity n : graph.getVertices()) {
      n.setGeometry(n.getGeometryAt(t));
    }
    for (STEntity e : graph.getEdges()) {
      e.setGeometry(e.getGeometryAt(t));
    }
    return graph;
  }

  /**
   * Réalise l'appariement
   * @param paramApp
   * @return
   */
  private EnsembleDeLiens match(ReseauApp reseauRef, ReseauApp reseauComp) {
    if (this.params.projeteNoeuds2SurReseau1) {
      reseauRef.projete(reseauComp,
          this.params.projeteNoeuds2SurReseau1DistanceNoeudArc,
          this.params.projeteNoeuds2SurReseau1DistanceProjectionNoeud,
          this.params.projeteNoeuds2SurReseau1ImpassesSeulement);
    }
    if (this.params.projeteNoeuds1SurReseau2) {
      reseauComp.projete(reseauRef,
          this.params.projeteNoeuds1SurReseau2DistanceNoeudArc,
          this.params.projeteNoeuds1SurReseau2DistanceProjectionNoeud,
          this.params.projeteNoeuds1SurReseau2ImpassesSeulement);
    }

    reseauRef.instancieAttributsNuls(this.params.distanceNoeudsMax);
    reseauComp.initialisePoids();

    // matching
    EnsembleDeLiens liens = Appariement.appariementReseaux(reseauRef,
        reseauComp, this.params);

    // EXPORT
    EnsembleDeLiens liensGeneriques = LienReseaux.exportLiensAppariement(liens,
        reseauRef, this.params);

    Appariement.nettoyageLiens(reseauRef, reseauComp);

    // regroupement en liens n:m
    IPopulation<IFeature> p1 = new Population<IFeature>();
    p1.addAll(this.params.populationsArcs1.get(0));
    p1.addAll(this.params.populationsNoeuds1.get(0));
    IPopulation<IFeature> p2 = new Population<IFeature>();
    p2.addAll(this.params.populationsArcs2.get(0));
    p2.addAll(this.params.populationsNoeuds2.get(0));

    List<Lien> toRemove = new ArrayList<Lien>();
    for (Lien lien : liensGeneriques.getElements()) {
      if (lien.getEvaluation() < 0.5) {
        toRemove.add(lien);
      }
    }
    for (Lien l : toRemove) {
      liensGeneriques.enleveElement(l);
    }
    System.out.println(liensGeneriques.size());
    ShapefileWriter.write(liensGeneriques, "/home/bcostes/Bureau/tmp/lien.shp");

    return liensGeneriques.regroupeLiens(p1, p2);
  }

  /**
   * Importe les données à partir d'une population de feature (les arcs). Crée
   * les noeuds
   * @param paramApp
   * @param ref
   * @return
   */
  private ReseauApp importData(final ParametresApp paramApp, final boolean ref,
      Map<IGeometry, List<IGeometry>> places) {

    CarteTopo topo = null;
    if (ref) {
      topo = new CarteTopo("void");
      IPopulation<Arc> popArcs = topo.getPopArcs();
      for (IFeature feature : paramApp.populationsArcs1.get(0)) {
        Arc arc = popArcs.nouvelElement();
        try {
          IDirectPositionList ll = new DirectPositionList();
          ll.add(feature.getGeom().coord().get(0));
          for (int i = 1; i < feature.getGeom().coord().size(); i++) {
            if (!feature.getGeom().coord().get(i)
                .equals(feature.getGeom().coord().get(i - 1), 0.05)) {
              ll.add(feature.getGeom().coord().get(i));
            }
          }
          GM_LineString line = new GM_LineString(ll);
          arc.setGeometrie(line);
          arc.addCorrespondant(feature);
        } catch (ClassCastException e) {
          e.printStackTrace();
        }
      }
    } else {
      topo = new CarteTopo("void");
      IPopulation<Arc> popArcs = topo.getPopArcs();
      for (IFeature feature : paramApp.populationsArcs2.get(0)) {
        Arc arc = popArcs.nouvelElement();
        try {
          IDirectPositionList ll = new DirectPositionList();
          ll.add(feature.getGeom().coord().get(0));
          for (int i = 1; i < feature.getGeom().coord().size(); i++) {
            if (!feature.getGeom().coord().get(i)
                .equals(feature.getGeom().coord().get(i - 1), 0.05)) {
              ll.add(feature.getGeom().coord().get(i));
            }
          }
          GM_LineString line = new GM_LineString(ll);
          arc.setGeometrie(line);
          arc.addCorrespondant(feature);
        } catch (ClassCastException e) {
          e.printStackTrace();
        }
      }
    }

    /*
     * for (Arc a : topo.getListeArcs()) { for (int i = 1; i <
     * a.getGeometrie().coord().size(); i++) { if
     * (a.getGeometrie().getControlPoint(i)
     * .equals(a.getGeometrie().getControlPoint(i - 1), 0.05)) {
     * System.out.println(a.getGeometrie().getControlPoint(i)); } } }
     */

    // on crée la topologie pour avoir les noeuds
    topo.creeTopologieArcsNoeuds(0.1);
    topo.creeNoeudsManquants(0.1);
    topo.filtreDoublons(0.1);
    topo.filtreArcsDoublons();
    topo.rendPlanaire(0.1);
    topo.filtreNoeudsSimples();
    topo.filtreDoublons(0.1);
    topo.filtreNoeudsIsoles();

    // 2- On fusionne les noeuds proches
    if (ref) {
      if (paramApp.topologieSeuilFusionNoeuds1 >= 0) {
        topo.fusionNoeuds(paramApp.topologieSeuilFusionNoeuds1);
      }
      if (paramApp.topologieSurfacesFusionNoeuds1 != null) {
        topo.fusionNoeuds(paramApp.topologieSurfacesFusionNoeuds1);
      }
    } else {
      if (paramApp.topologieSeuilFusionNoeuds2 >= 0) {
        topo.fusionNoeuds(paramApp.topologieSeuilFusionNoeuds2);
      }
      if (paramApp.topologieSurfacesFusionNoeuds2 != null) {
        topo.fusionNoeuds(paramApp.topologieSurfacesFusionNoeuds2);
      }
    }
    // 4- On filtre les noeuds simples (avec 2 arcs incidents)
    if ((ref && paramApp.topologieElimineNoeudsAvecDeuxArcs1)
        || (!ref && paramApp.topologieElimineNoeudsAvecDeuxArcs2)) {
      topo.filtreNoeudsSimples();
    }

    // 5- On fusionne des arcs en double
    if (ref && paramApp.topologieFusionArcsDoubles1) {
      topo.filtreArcsDoublons();
    }
    if (!ref && paramApp.topologieFusionArcsDoubles2) {
      topo.filtreArcsDoublons();
    }

    // détection des places
    if (places != null) {
      Map<Noeud, Place> placesDetected = Place.placesDetection(topo);
      for (Noeud n : placesDetected.keySet()) {
        List<IGeometry> l = new ArrayList<IGeometry>();
        for (Noeud nn : placesDetected.get(n).getNodes()) {
          l.add(new GM_Point(nn.getCoord()));
        }
        for (Arc a : placesDetected.get(n).getEdges()) {
          l.add(new GM_LineString(a.getCoord()));
        }
        places.put(new GM_Point(n.getCoord()), l);
      }
    }

    // TODO: traitement des multiples composantes connexes
    // si le traitement a induit plusieurs composantes connexes, ou si il y en a
    // plusieurs en entrée,
    // on prend la plus grosse
    Groupe g = new Groupe();
    g.addAllArcs(topo.getListeArcs());
    g.addAllNoeuds(topo.getListeNoeuds());
    List<Groupe> gs = g.decomposeConnexes();
    Groupe gmax = null;
    double smax = -1;
    for (Groupe gg : gs) {
      if (gg.getListeArcs().size() > smax) {
        smax = gg.getListeArcs().size();
        gmax = gg;
      }
    }

    if (ref) {
      paramApp.populationsArcs1.clear();
      IFeatureCollection<IFeature> pE = new Population<IFeature>();
      pE.addAll(gmax.getListeArcs());
      paramApp.populationsArcs1.add(pE);
      IFeatureCollection<IFeature> pN = new Population<IFeature>();
      pN.addAll(gmax.getListeNoeuds());
      paramApp.populationsNoeuds1.add(pN);
    } else {
      paramApp.populationsArcs2.clear();
      IFeatureCollection<IFeature> pE = new Population<IFeature>();
      pE.addAll(gmax.getListeArcs());
      paramApp.populationsArcs2.add(pE);
      IFeatureCollection<IFeature> pN = new Population<IFeature>();
      pN.addAll(gmax.getListeNoeuds());
      paramApp.populationsNoeuds2.add(pN);
    }

    topo.nettoyer();
    gs.clear();

    ReseauApp reseau = null;
    if (ref) {
      reseau = new ReseauApp(I18N.getString("AppariementIO.ReferenceNetwork")); //$NON-NLS-1$
    } else {
      reseau = new ReseauApp(I18N.getString("AppariementIO.ComparisonNetwork")); //$NON-NLS-1$
    }
    IPopulation<? extends IFeature> popArcApp = reseau.getPopArcs();
    IPopulation<? extends IFeature> popNoeudApp = reseau.getPopNoeuds();
    // /////////////////////////
    // import des arcs
    Iterator<IFeatureCollection<? extends IFeature>> itPopArcs = null;
    boolean populationsArcsAvecOrientationDouble = true;
    if (ref) {
      itPopArcs = paramApp.populationsArcs1.iterator();
      populationsArcsAvecOrientationDouble = paramApp.populationsArcsAvecOrientationDouble1;
    } else {
      populationsArcsAvecOrientationDouble = paramApp.populationsArcsAvecOrientationDouble2;
      itPopArcs = paramApp.populationsArcs2.iterator();
    }
    while (itPopArcs.hasNext()) {
      IFeatureCollection<? extends IFeature> popGeo = itPopArcs.next();
      // import d'une population d'arcs
      for (IFeature element : popGeo) {
        ArcApp arc = (ArcApp) popArcApp.nouvelElement();
        ILineString ligne = new GM_LineString((IDirectPositionList) element
            .getGeom().coord().clone());
        arc.setGeometrie(ligne);
        if (populationsArcsAvecOrientationDouble) {
          arc.setOrientation(2);
        } else {
          String attribute = (ref) ? paramApp.attributOrientation1
              : paramApp.attributOrientation2;
          Map<Object, Integer> orientationMap = (ref) ? paramApp.orientationMap1
              : paramApp.orientationMap2;
          if (attribute.isEmpty()) {
            arc.setOrientation(1);
          } else {
            Object value = element.getAttribute(attribute);
            // System.out.println(attribute + " = " + value);
            if (orientationMap != null) {
              Integer orientation = orientationMap.get(value);
              if (orientation != null) {
                arc.setOrientation(orientation.intValue());
              }
            } else {
              if (value instanceof Number) {
                Number v = (Number) value;
                arc.setOrientation(v.intValue());
              } else {
                if (value instanceof String) {
                  String v = (String) value;
                  try {
                    arc.setOrientation(Integer.parseInt(v));
                  } catch (Exception e) {
                    // FIXME Pretty specfific to BDTOPO Schema... no time to
                    // make it better
                    if (v.equalsIgnoreCase("direct")) { //$NON-NLS-1$
                      arc.setOrientation(1);
                    } else {
                      if (v.equalsIgnoreCase("inverse")) { //$NON-NLS-1$
                        arc.setOrientation(-1);
                      } else {
                        arc.setOrientation(2);
                      }
                    }
                  }
                }
              }
            }
          }
        }
        arc.addCorrespondant(element);
      }
    }

    // import des noeuds
    Iterator<?> itPopNoeuds = null;
    // on attribut à paramsApp.populationNoeudsX les sommets de la carte topo
    if (ref) {
      itPopNoeuds = paramApp.populationsNoeuds1.iterator();
    } else {
      itPopNoeuds = paramApp.populationsNoeuds2.iterator();
    }

    while (itPopNoeuds.hasNext()) {
      IFeatureCollection<?> popGeo = (IFeatureCollection<?>) itPopNoeuds.next();
      // import d'une population de noeuds
      for (IFeature element : popGeo.getElements()) {
        NoeudApp noeud = (NoeudApp) popNoeudApp.nouvelElement();
        // noeud.setGeometrie((GM_Point)element.getGeom());
        noeud.setGeometrie(new GM_Point((IDirectPosition) ((GM_Point) element
            .getGeom()).getPosition().clone()));
        noeud.addCorrespondant(element);
        noeud.setTaille(paramApp.distanceNoeudsMax);
      }
    }

    // Indexation spatiale des arcs et noeuds
    // On crée un dallage régulier avec en moyenne 20 objets par case
    int nb = (int) Math.sqrt(reseau.getPopArcs().size() / 20);
    if (nb == 0) {
      nb = 1;
    }
    reseau.getPopArcs().initSpatialIndex(Tiling.class, true, nb);
    reseau.getPopNoeuds().initSpatialIndex(
        reseau.getPopArcs().getSpatialIndex());
    // Instanciation de la topologie
    // 6 - On crée la topologie de faces
    reseau.creeTopologieArcsNoeuds(0.1);
    if (!ref && paramApp.varianteChercheRondsPoints) {
      reseau.creeTopologieFaces();
    }
    // 7 - On double la taille de recherche pour les impasses
    if (paramApp.distanceNoeudsImpassesMax >= 0) {
      if (ref) {
        Iterator<?> itNoeuds = reseau.getPopNoeuds().getElements().iterator();
        while (itNoeuds.hasNext()) {
          NoeudApp noeud2 = (NoeudApp) itNoeuds.next();
          if (noeud2.arcs().size() == 1) {
            noeud2.setTaille(paramApp.distanceNoeudsImpassesMax);
          }
        }
      }
    }
    return reseau;
  }

  /**
   * Importe les données à partir d'une population de feature (les arcs). Crée
   * les noeuds
   * @param paramApp
   * @param ref
   * @return
   */
  private ReseauApp importData(final ParametresApp paramApp) {

    /*
     * CarteTopo topo = null;
     * 
     * topo = new CarteTopo("void"); IPopulation<Arc> popArcs =
     * topo.getPopArcs(); for (IFeature feature :
     * paramApp.populationsArcs1.get(0)) { Arc arc = popArcs.nouvelElement();
     * try { GM_LineString line = new GM_LineString(feature.getGeom().coord());
     * arc.setGeometrie(line); arc.addCorrespondant(feature); } catch
     * (ClassCastException e) { e.printStackTrace(); } }
     * 
     * // on crée la topologie pour avoir les noeuds
     * topo.creeTopologieArcsNoeuds(0.); topo.rendPlanaire(0.);
     * topo.creeNoeudsManquants(0.); topo.filtreDoublons(0.);
     * topo.filtreArcsDoublons(); topo.filtreDoublons(0.);
     * topo.filtreNoeudsIsoles();
     * 
     * // TODO: traitement des multiples composantes connexes // si le
     * traitement a induit plusieurs composantes connexes, ou si il y en a //
     * plusieurs en entrée, // on prend la plus grosse Groupe g = new Groupe();
     * g.addAllArcs(topo.getListeArcs()); g.addAllNoeuds(topo.getListeNoeuds());
     * List<Groupe> gs = g.decomposeConnexes(); Groupe gmax = null; double smax
     * = -1; for (Groupe gg : gs) { if (gg.getListeArcs().size() > smax) { smax
     * = gg.getListeArcs().size(); gmax = gg; } }
     * 
     * paramApp.populationsArcs1.clear(); IFeatureCollection<IFeature> pE = new
     * Population<IFeature>(); pE.addAll(gmax.getListeArcs());
     * paramApp.populationsArcs1.add(pE); IFeatureCollection<IFeature> pN = new
     * Population<IFeature>(); pN.addAll(gmax.getListeNoeuds());
     * paramApp.populationsNoeuds1.clear(); paramApp.populationsNoeuds1.add(pN);
     * 
     * topo.nettoyer(); gs.clear();
     */
    ReseauApp reseau = null;

    reseau = new ReseauApp(I18N.getString("AppariementIO.ReferenceNetwork")); //$NON-NLS-1$

    IPopulation<? extends IFeature> popArcApp = reseau.getPopArcs();
    IPopulation<? extends IFeature> popNoeudApp = reseau.getPopNoeuds();
    // /////////////////////////
    // import des arcs
    Iterator<IFeatureCollection<? extends IFeature>> itPopArcs = null;
    boolean populationsArcsAvecOrientationDouble = true;

    itPopArcs = paramApp.populationsArcs1.iterator();
    populationsArcsAvecOrientationDouble = paramApp.populationsArcsAvecOrientationDouble1;

    while (itPopArcs.hasNext()) {
      IFeatureCollection<? extends IFeature> popGeo = itPopArcs.next();
      // import d'une population d'arcs
      for (IFeature element : popGeo) {
        ArcApp arc = (ArcApp) popArcApp.nouvelElement();
        ILineString ligne = new GM_LineString((IDirectPositionList) element
            .getGeom().coord().clone());
        arc.setGeometrie(ligne);
        if (populationsArcsAvecOrientationDouble) {
          arc.setOrientation(2);
        } else {
          String attribute = paramApp.attributOrientation1;
          Map<Object, Integer> orientationMap = paramApp.orientationMap1;
          if (attribute.isEmpty()) {
            arc.setOrientation(1);
          } else {
            Object value = element.getAttribute(attribute);
            // System.out.println(attribute + " = " + value);
            if (orientationMap != null) {
              Integer orientation = orientationMap.get(value);
              if (orientation != null) {
                arc.setOrientation(orientation.intValue());
              }
            } else {
              if (value instanceof Number) {
                Number v = (Number) value;
                arc.setOrientation(v.intValue());
              } else {
                if (value instanceof String) {
                  String v = (String) value;
                  try {
                    arc.setOrientation(Integer.parseInt(v));
                  } catch (Exception e) {
                    // FIXME Pretty specfific to BDTOPO Schema... no time to
                    // make it better
                    if (v.equalsIgnoreCase("direct")) { //$NON-NLS-1$
                      arc.setOrientation(1);
                    } else {
                      if (v.equalsIgnoreCase("inverse")) { //$NON-NLS-1$
                        arc.setOrientation(-1);
                      } else {
                        arc.setOrientation(2);
                      }
                    }
                  }
                }
              }
            }
          }
        }
        arc.addCorrespondant(element);
      }
    }

    // import des noeuds
    Iterator<?> itPopNoeuds = null;
    // on attribut à paramsApp.populationNoeudsX les sommets de la carte topo

    itPopNoeuds = paramApp.populationsNoeuds1.iterator();

    while (itPopNoeuds.hasNext()) {
      IFeatureCollection<?> popGeo = (IFeatureCollection<?>) itPopNoeuds.next();
      // import d'une population de noeuds
      for (IFeature element : popGeo.getElements()) {
        NoeudApp noeud = (NoeudApp) popNoeudApp.nouvelElement();
        // noeud.setGeometrie((GM_Point)element.getGeom());
        noeud.setGeometrie(new GM_Point((IDirectPosition) ((GM_Point) element
            .getGeom()).getPosition().clone()));
        noeud.addCorrespondant(element);
        noeud.setTaille(paramApp.distanceNoeudsMax);
      }
    }

    // Indexation spatiale des arcs et noeuds
    // On crée un dallage régulier avec en moyenne 20 objets par case
    int nb = (int) Math.sqrt(reseau.getPopArcs().size() / 20);
    if (nb == 0) {
      nb = 1;
    }
    reseau.getPopArcs().initSpatialIndex(Tiling.class, true, nb);
    reseau.getPopNoeuds().initSpatialIndex(
        reseau.getPopArcs().getSpatialIndex());
    // Instanciation de la topologie
    // 6 - On crée la topologie de faces
    reseau.creeTopologieArcsNoeuds(0.1);
    // 7 - On double la taille de recherche pour les impasses
    if (paramApp.distanceNoeudsImpassesMax >= 0) {

      Iterator<?> itNoeuds = reseau.getPopNoeuds().getElements().iterator();
      while (itNoeuds.hasNext()) {
        NoeudApp noeud2 = (NoeudApp) itNoeuds.next();
        if (noeud2.arcs().size() == 1) {
          noeud2.setTaille(paramApp.distanceNoeudsImpassesMax);
        }
      }

    }
    return reseau;
  }

  public static void main(String args[]) {

    // tests de matching
    Map<FuzzyTemporalInterval, String> files = new HashMap<FuzzyTemporalInterval, String>();

//    files
//        .put(
//            new FuzzyTemporalInterval(1785, 1790, 1793, 1795),
//            "/media/Data/Benoit/these/donnees/vecteur/filaires/filaires_corriges/verniquet_l93_utf8_corr.shp");
//
//    files
//        .put(
//            new FuzzyTemporalInterval(1808, 1810, 1836, 1839),
//            "/media/Data/Benoit/these/donnees/vecteur/filaires/filaires_corriges/vasserot_jacoubet_l93_utf8_corr.shp");

    /*
     * files .put( new FuzzyTemporalInterval(1826, 1827, 1838, 1839),
     * "/media/Data/Benoit/these/donnees/vecteur/filaires/filaires_corriges/jacoubet_l93_utf8_corr.shp"
     * );
     * 
     * files .put( new FuzzyTemporalInterval(1849, 1850, 1851, 1852),
     * "/media/Data/Benoit/these/donnees/vecteur/filaires/filaires_corriges/andriveau_l93_utf8_corr.shp"
     * );
     * 
     * files .put( new FuzzyTemporalInterval(1870, 1871, 1871, 1872),
     * "/media/Data/Benoit/these/donnees/vecteur/filaires/filaires_corriges/1871_L93_TEMPORAIRE_emprise.shp"
     * );
     */
    /*
     * files .put( new FuzzyTemporalInterval(1884, 1885, 1888, 1889),
     * "/media/Data/Benoit/these/donnees/vecteur/filaires/filaires_corriges/poubelle_TEMPORAIRE_emprise.shp"
     * );
     */
    /*
     * files .put( new FuzzyTemporalInterval(1999, 1999, 1999, 1999),
     * "/media/Data/Benoit/these/donnees/vecteur/filaires/filaires_corriges/georoute_1999_emprise_L93.shp"
     * );
     */
    
    //précision
//    Map<FuzzyTemporalInterval, Double> accuracies = new HashMap<FuzzyTemporalInterval, Double>();
//    accuracies.put( new FuzzyTemporalInterval(1785, 1790, 1793, 1795),1.);
//    accuracies.put( new FuzzyTemporalInterval(1808, 1810, 1836, 1839),0.1);

    IterativeGraphBuilder gb = new IterativeGraphBuilder(files);
    STGraph stgraph = gb.buildSTGraph(true, null);

    System.out.println("AFTER");
    System.out.println(stgraph.getVertexCount() + " " + stgraph.getEdgeCount());

    // XStreamTest.test2(stgraph, "/home/bcostes/Bureau/tmp/testxtream.tag");

    TAGIoManager.exportTAG(stgraph, "/home/bcostes/Bureau/tmp/tag.shp");
    // /// TAGIoManager.exportTAGSnapshots(stgraph,
    // "/home/bcostes/Bureau/tmp/tag.shp", TAGIoManager.NODE_AND_EDGE);

    TAGIoManager.serializeBinary(stgraph, "/home/bcostes/Bureau/tmp/tag");

  }
}
