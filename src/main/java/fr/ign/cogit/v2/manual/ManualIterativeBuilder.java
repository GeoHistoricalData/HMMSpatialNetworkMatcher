package fr.ign.cogit.v2.manual;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nrc.fuzzy.XValuesOutOfOrderException;
import nrc.fuzzy.YValueOutOfRangeException;
import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IFeatureCollection;
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPositionList;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ILineString;
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
import fr.ign.cogit.geoxygene.feature.DefaultFeature;
import fr.ign.cogit.geoxygene.feature.Population;
import fr.ign.cogit.geoxygene.spatial.coordgeom.GM_LineString;
import fr.ign.cogit.geoxygene.spatial.geomprim.GM_Point;
import fr.ign.cogit.geoxygene.util.attribute.AttributeManager;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileReader;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileWriter;
import fr.ign.cogit.geoxygene.util.index.Tiling;
import fr.ign.cogit.v2.geometry.LightDirectPosition;
import fr.ign.cogit.v2.geometry.LightLineString;
import fr.ign.cogit.v2.io.TAGIoManager;
import fr.ign.cogit.v2.lineage.IterativeFiliationGraph;
import fr.ign.cogit.v2.lineage.MatchingLink;
import fr.ign.cogit.v2.lineage.STGroupe;
import fr.ign.cogit.v2.mergeProcess.MatchingUtils;
import fr.ign.cogit.v2.tag.FuzzyTemporalInterval;
import fr.ign.cogit.v2.tag.STEntity;
import fr.ign.cogit.v2.tag.STGraph;
import fr.ign.cogit.v2.tag.STProperty;
import fr.ign.cogit.v2.tag.TemporalDomain;


public class ManualIterativeBuilder {

    private ParametresApp params;
    private TemporalDomain temporalDomain;

    public ManualIterativeBuilder(TemporalDomain dt) {
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

        this.temporalDomain = dt;
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
    public Set<MatchingLink> createSTLinks(STGraph stgraph, FuzzyTemporalInterval t2,
            STGraph g2, EnsembleDeLiens liens) {


        Set<MatchingLink> stlinks = new HashSet<MatchingLink>();
        for (Lien lien : liens) {
            MatchingLink stlink = new MatchingLink();
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
                    // arc  Map<String, Map<FuzzyTemporalInterval, String>> attributesNamesByDates = new HashMap<String, Map<FuzzyTemporalInterval, String>>();
//                  Map<FuzzyTemporalInterval, String> attributesId = new HashMap<FuzzyTemporalInterval, String>();
//                  attributesId.put(new FuzzyTemporalInterval(1785, 1790, 1793, 1795),
//                          "ID");
//                  attributesId.put(new FuzzyTemporalInterval(1808, 1810, 1836, 1839),
//                          "ID");
//                  attributesId.put(new FuzzyTemporalInterval(1826, 1827, 1838, 1839),
//                          "ID");
//                  attributesId.put(new FuzzyTemporalInterval(1849, 1850, 1851, 1852),
//                          "ID");
//                  attributesId.put(new FuzzyTemporalInterval(1870, 1871, 1871, 1872),
//                          "ID");
//                  attributesId.put(new FuzzyTemporalInterval(1884, 1885, 1888, 1889),
//                           "ID");
//                  attributesNamesByDates.put("ID", attributesId);
                    STEntity stedge = null;
                    for (STEntity stedge2 : stgraph.getEdges()) {
                        if (stedge2.getGeometry().toGeoxGeometry().equals(source.getGeom())) {
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
                        System.out.println(source.getGeom().toString());
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
                        if (((GM_Point) stnode2.getGeometry().toGeoxGeometry())
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
                        if (stedge2.getGeometry().toGeoxGeometry().equals(target.getGeom())) {
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
    public STGraph createJungGraph(FuzzyTemporalInterval t, String shp,
            boolean detectPlaces,
            Map<String, Map<FuzzyTemporalInterval, String>> attributesByDates) {
        IPopulation<IFeature> popIn = ShapefileReader.read(shp);
        ReseauApp res = TopologyBuilder.buildTopology(popIn, params, detectPlaces);
        STGraph graph = new STGraph(this.temporalDomain);
        graph.setAttributes(new ArrayList<String>(attributesByDates.keySet()));
        // les noeuds
        Map<IFeature, STEntity> nodes = new HashMap<IFeature, STEntity>();
        STProperty<Boolean> s = new STProperty<Boolean>(STProperty.PROPERTIE_FINAL_TYPES.TimeSerie, null);
        Map<FuzzyTemporalInterval, Boolean> values= new HashMap<FuzzyTemporalInterval, Boolean>();
        for(FuzzyTemporalInterval tt: this.temporalDomain.asList()){
            values.put(tt, false);
        }
        s.setValues(values);
        s.setValueAt(t, true);
        STEntity.setCurrentType(STEntity.NODE);
        for (IFeature node : res.getPopNoeuds()) {
            STEntity stnode = new STEntity(s.copy());
            stnode.setGeometryAt(t, new LightDirectPosition(node.getGeom().coord()
                    .get(0)));
            nodes.put(node, stnode);
        }
        // les arcs
        STEntity.setCurrentType(STEntity.EDGE);
        for (IFeature edge : res.getPopArcs()) {
            STEntity stegde = new STEntity(s.copy());
            stegde.setGeometryAt(t, new LightLineString(edge.getGeom().coord()));
            stegde.setWeightAt(t, edge.getGeom().length());
            for (String attName : attributesByDates.keySet()) {
                            
                STProperty<String> statt = new STProperty<String>(STProperty.PROPERTIE_FINAL_TYPES.Attribute, attName, this.temporalDomain.asList());
                String attNameAtThisDate = attributesByDates.get(attName).get(t);
                                
                if(edge.getCorrespondants().size() == 1){
                    IFeature cor = edge.getCorrespondant(0);
                    if(cor.getAttribute(attNameAtThisDate) == null){
                        statt.setValueAt(t, "");
                        stegde.getTAttributes().add(statt);
                    }
                    else{
                        statt.setValueAt(t, cor.getAttribute(attNameAtThisDate).toString());
                        stegde.getTAttributes().add(statt);
                    }
                }
                else{
                    String ss="";
                    for(IFeature cor : edge.getCorrespondants()){
                        if(cor.getAttribute(attNameAtThisDate) != null && !cor.getAttribute(attNameAtThisDate).toString().equals("")){
                            ss +=cor.getAttribute(attNameAtThisDate).toString() + ";";
                        }
                    }
                    if(ss.endsWith(";")){
                        ss = ss.substring(0,ss.length()-1);
                    }
                    statt.setValueAt(t, ss);
                    stegde.getTAttributes().add(statt);
                }
            }
            // identification des extrémités
            IFeature node1 = null, node2 = null;
            for (IFeature node : res.getPopNoeuds()) {
                if (node.getGeom().coord().get(0).equals(edge.getGeom().coord().get(0))) {
                    node1 = node;
                    break;
                }
            }
            for (IFeature node : res.getPopNoeuds()) {
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

    public EnsembleDeLiens match(STGraph g1, STGraph g2,
            String shpLiensOutput) {
        ReseauApp reseauRef = this.createMatchingNetwork(g1, true);
        ReseauApp reseauComp = this.createMatchingNetwork(g2, false);

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
        
        
        
        
//        IPopulation<IFeature> p1 = new Population<IFeature>();
//        p1.addAll(this.params.populationsArcs1.get(0));
//        p1.addAll(this.params.populationsNoeuds1.get(0));
//        IPopulation<IFeature> p2 = new Population<IFeature>();
//        p2.addAll(this.params.populationsArcs2.get(0));
//        p2.addAll(this.params.populationsNoeuds2.get(0));
        List<Lien> toRemove = new ArrayList<Lien>();
        for (Lien lien : liensGeneriques.getElements()) {
            if (lien.getEvaluation() < 0.5) {
                toRemove.add(lien);
            }
        }
        for (Lien l : toRemove) {
            liensGeneriques.enleveElement(l);
        }
       // EnsembleDeLiens liensFinaux  =liensGeneriques.regroupeLiens(p1, p2);


        EnsembleDeLiens liensFinaux = MatchingUtils.getConnectedMatchs(liensGeneriques);



        if (shpLiensOutput != null) {
            MatchingLinkIOManager.exportEnsembleDeLiens(liensGeneriques, shpLiensOutput);
        }

        return liensFinaux;

    }

    private ReseauApp createMatchingNetwork(STGraph g, boolean ref) {
        IPopulation<IFeature> popArc = new Population<IFeature>();
        IPopulation<IFeature> popNoeud = new Population<IFeature>();
        for (STEntity edge : g.getEdges()) {
            popArc.add(new DefaultFeature(((LightLineString) edge.getGeometry())
                    .toGeoxGeometry()));
        }
        for (STEntity node : g.getVertices()) {
            popNoeud.add(new DefaultFeature((node.getGeometry()).toGeoxGeometry()));
        }
        if (ref) {
            this.params.populationsArcs1.clear();
            this.params.populationsNoeuds1.clear();
            this.params.populationsArcs1.add(popArc);
            this.params.populationsNoeuds1.add(popNoeud);
        } else {
            this.params.populationsArcs2.clear();
            this.params.populationsNoeuds2.clear();
            this.params.populationsArcs2.add(popArc);
            this.params.populationsNoeuds2.add(popNoeud);
        }

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
            itPopArcs = this.params.populationsArcs1.iterator();
            populationsArcsAvecOrientationDouble = this.params.populationsArcsAvecOrientationDouble1;
        } else {
            populationsArcsAvecOrientationDouble = this.params.populationsArcsAvecOrientationDouble2;
            itPopArcs = this.params.populationsArcs2.iterator();
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
                    String attribute = (ref) ? this.params.attributOrientation1
                            : this.params.attributOrientation2;
                    Map<Object, Integer> orientationMap = (ref) ? this.params.orientationMap1
                            : this.params.orientationMap2;
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
            itPopNoeuds = this.params.populationsNoeuds1.iterator();
        } else {
            itPopNoeuds = this.params.populationsNoeuds2.iterator();
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
                noeud.setTaille(this.params.distanceNoeudsMax);
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
        if (!ref && this.params.varianteChercheRondsPoints) {
            reseau.creeTopologieFaces();
        }
        // 7 - On double la taille de recherche pour les impasses
        if (this.params.distanceNoeudsImpassesMax >= 0) {
            if (ref) {
                Iterator<?> itNoeuds = reseau.getPopNoeuds().getElements().iterator();
                while (itNoeuds.hasNext()) {
                    NoeudApp noeud2 = (NoeudApp) itNoeuds.next();
                    if (noeud2.arcs().size() == 1) {
                        noeud2.setTaille(this.params.distanceNoeudsImpassesMax);
                    }
                }
            }
        }
        return reseau;
    }

    public STGraph buildNewStgraph(STGraph stg, STGraph snapshot,
            FuzzyTemporalInterval tsnapshot, Set<MatchingLink> links) {
        IterativeFiliationGraph filiationGraph = new IterativeFiliationGraph(
                this.temporalDomain, stg);
        filiationGraph.buildNextIteration(tsnapshot, snapshot,links);
        return filiationGraph.getStGraph();
    }
    public static void detect_doublons(STGraph stgNew, String output) {
        Set<STEntity>nodes = new HashSet<STEntity>();
        nodes.addAll(stgNew.getVertices());
        for(STEntity node : stgNew.getVertices()){
            nodes.remove(node);
            for(STEntity node2 : nodes){
                if(node.equals(node2)){
                    continue;
                }
                if(node.getGeometry().toGeoxGeometry().coord().get(0).equals(node2.getGeometry().toGeoxGeometry().coord().get(0),0.05)){
                    System.out.println("Noeuds doublons : "+ node.getGeometry().toGeoxGeometry().coord().get(0)+" ; " + node2.getGeometry().toGeoxGeometry().coord().get(0));
                }
            }
        }
        IFeatureCollection<IFeature> out = new Population<IFeature>();
        Set<STEntity>edges = new HashSet<STEntity>();
        edges.addAll(stgNew.getEdges());
        for(STEntity edge : stgNew.getEdges()){
            edges.remove(edge);
            for(STEntity edge2 : edges){
                if(edge.equals(edge2)){
                    continue;
                }
                if(edge.getGeometry().toGeoxGeometry().distance(edge2.getGeometry().toGeoxGeometry())>2){
                    continue;
                }
                IGeometry buf1 = edge.getGeometry().toGeoxGeometry().buffer(3);
                IGeometry buf2 = edge2.getGeometry().toGeoxGeometry().buffer(3);
                if(buf1.intersects(buf2)){
                    if(buf1.contains(edge2.getGeometry().toGeoxGeometry()) || buf2.contains(edge.getGeometry().toGeoxGeometry())){
                        IFeature f = new DefaultFeature(buf1.union(buf2));
                        AttributeManager.addAttribute(f, "id", Integer.toString(out.size()+1), "String");
                        out.add(f);
                        break;
                    }
                }

            }
        }
        ShapefileWriter.write(out, output);
    }
    public static void main(String args[]) throws XValuesOutOfOrderException, YValueOutOfRangeException {
        
        
        
        
        
        
        
        TemporalDomain dt = new TemporalDomain();
        dt.asList().add(new FuzzyTemporalInterval(new double[]{1784,1785,1789,1791},new double[]{0,1,1,0}, 4));
        dt.asList().add(new FuzzyTemporalInterval(new double[]{1808,1810,1836,1853},new double[]{0,1,1,0}, 4));
        dt.asList().add(new FuzzyTemporalInterval(new double[]{1825,1827,1836,1839},new double[]{0,1,1,0}, 4));
        dt.asList().add(new FuzzyTemporalInterval(new double[]{1848,1849,1849,1850},new double[]{0,1,1,0}, 4));
       dt.asList().add(new FuzzyTemporalInterval(new double[]{1870,1871,1871,1872},new double[]{0,1,1,0}, 4));
        dt.asList().add(new FuzzyTemporalInterval(new double[]{1887,1888,1888,1889},new double[]{0,1,1,0}, 4));

        // les attributs qu'on veut
        Map<String, Map<FuzzyTemporalInterval, String>> attributesNamesByDates = new HashMap<String, Map<FuzzyTemporalInterval, String>>();

        //identifiants
        Map<FuzzyTemporalInterval, String> attributesId = new HashMap<FuzzyTemporalInterval, String>();
        attributesId.put(new FuzzyTemporalInterval(new double[]{1784,1785,1789,1791},new double[]{0,1,1,0}, 4),
                "ID");
        attributesId.put(new FuzzyTemporalInterval(new double[]{1808,1810,1836,1853},new double[]{0,1,1,0}, 4),
                "ID");
        attributesId.put(new FuzzyTemporalInterval(new double[]{1825,1827,1836,1839},new double[]{0,1,1,0}, 4),
                "ID");
        attributesId.put(new FuzzyTemporalInterval(new double[]{1848,1849,1849,1850},new double[]{0,1,1,0}, 4),
                "ID");
        attributesId.put(new FuzzyTemporalInterval(new double[]{1870,1871,1871,1872},new double[]{0,1,1,0}, 4),
                "ID");
        attributesId.put(new FuzzyTemporalInterval(new double[]{1887,1888,1888,1889},new double[]{0,1,1,0}, 4),
                 "ID");
        attributesNamesByDates.put("ID", attributesId);

        // le nom des rues
        Map<FuzzyTemporalInterval, String> attributesNames = new HashMap<FuzzyTemporalInterval, String>();
        attributesNames.put(new FuzzyTemporalInterval(new double[]{1784,1785,1789,1791},new double[]{0,1,1,0}, 4),
                "NOM_ENTIER");
        attributesNames.put(new FuzzyTemporalInterval(new double[]{1808,1810,1836,1853},new double[]{0,1,1,0}, 4),
                "nom_entier");
        attributesNames.put(new FuzzyTemporalInterval(new double[]{1825,1827,1836,1839},new double[]{0,1,1,0}, 4),
                "NOM_ENTIER");
        attributesNames.put(new FuzzyTemporalInterval(new double[]{1848,1849,1849,1850},new double[]{0,1,1,0}, 4),
                "nom_entier");
        attributesNames.put(new FuzzyTemporalInterval(new double[]{1870,1871,1871,1872},new double[]{0,1,1,0}, 4),
                "NOM_1888");
         attributesNames.put(new FuzzyTemporalInterval(new double[]{1887,1888,1888,1889},new double[]{0,1,1,0}, 4),
                 "NOM_1888");
        attributesNamesByDates.put("name", attributesNames);

        //précisions

        Map<FuzzyTemporalInterval, Double> accuraciesMap = new HashMap<FuzzyTemporalInterval, Double>();
        accuraciesMap.put(new FuzzyTemporalInterval(new double[]{1784,1785,1789,1791},new double[]{0,1,1,0}, 4),
                1.);
        accuraciesMap.put(new FuzzyTemporalInterval(new double[]{1808,1810,1836,1853},new double[]{0,1,1,0}, 4),
                1.); //0.7
        accuraciesMap.put(new FuzzyTemporalInterval(new double[]{1825,1827,1836,1839},new double[]{0,1,1,0}, 4),
            1.);
        accuraciesMap.put(new FuzzyTemporalInterval(new double[]{1848,1849,1849,1850},new double[]{0,1,1,0}, 4),
            1.);
        accuraciesMap.put(new FuzzyTemporalInterval(new double[]{1870,1871,1871,1872},new double[]{0,1,1,0}, 4),
            1.);
        accuraciesMap.put(new FuzzyTemporalInterval(new double[]{1887,1888,1888,1889},new double[]{0,1,1,0}, 4),
                1.);
        STProperty<Double> accuracies = new STProperty<Double>(STProperty.PROPERTIE_FINAL_TYPES.Attribute, "Accuracies");
        accuracies.setValues(accuraciesMap);
        ManualIterativeBuilder builder = new ManualIterativeBuilder(dt);

        // **********************************************************************
        // ************************** INITIALISATION ****************************
        // **********************************************************************
        // date du nouveau snapshot
//                FuzzyTemporalInterval tsnapshot = new FuzzyTemporalInterval(new double[]{1784,1785,1789,1791},new double[]{0,1,1,0}, 4);
//                // création du graph initial
//                STGraph stgIni = builder
//                        .createJungGraph(
//                                tsnapshot,
//                                "/media/bcostes/Data/Benoit/these/donnees/vecteur/filaires/filaires_corriges/verniquet_l93_utf8_corr.shp",
//                                false, attributesNamesByDates);
//                stgIni.setAccuracies(accuracies);
//                stgIni.updateGeometries();
//                // on l'exporte
//                TAGIoManager.serializeBinary(stgIni,
//                        "/home/bcostes/Bureau/test/tag_new.tag");
//                TAGIoManager.serializeXml(stgIni, "/home/bcostes/Bureau/test/" + "xstream_backup.tag");
//                TAGIoManager.exportTAG(stgIni, "/home/bcostes/Bureau/test"
//                        + "/snapshot_" + tsnapshot.getX(0) + "_" + tsnapshot.getX(tsnapshot.size()-1) + ".shp");

        // **********************************************************************
        // **************************** BOUCLE **********************************
        // **********************************************************************
        // date du nouveau snapshot
        FuzzyTemporalInterval tsnapshot = new FuzzyTemporalInterval(new double[]{1808,1810,1836,1853},new double[]{0,1,1,0}, 4);
        // dossier ou on va chercher les anciens fichiers
        String repOld = "/home/bcostes/Bureau/stag_vasserot";
        // dossier ou on va stocker les nouveaux fichie
        String repNew = "/home/bcostes/Bureau/stag_vasserot/vasserot";
        // chargement du TAG
        STGraph stg = TAGIoManager.deserialize(repOld + "/tag_new.tag");
        
        stg.addFuzzyTemporalInterval(tsnapshot, 1.);

//        // création du second TAG correspondant au snapshot suivant
        STGraph snapshot = builder
                .createJungGraph(
                        tsnapshot,
                        "/home/bcostes/Bureau/stag_vasserot/snapshot_vasserot.shp",
                        false, attributesNamesByDates);
        
        
        snapshot.setAccuracies(accuracies);
        // export de ce snapshot en shapefile
        snapshot.updateGeometries();
        TAGIoManager.serializeBinary(snapshot, repNew + "/snapshot_" + tsnapshot.getX(0)
                + "_" + tsnapshot.getX(2) + ".tag");
        TAGIoManager.exportTAG(snapshot, repNew + "/snapshot_" + tsnapshot.getX(0)
                + "_" + tsnapshot.getX(2) + ".shp");
        // matching
        EnsembleDeLiens liens = builder.match(stg, snapshot, repNew
                + "/matching.shp");
        // création des STLinks
        Set<MatchingLink> stlinks = builder
                .createSTLinks(stg, tsnapshot, snapshot, liens);
        // procédure de modification du tag + snapshot
        STGraph stgNew = builder.buildNewStgraph(stg, snapshot, tsnapshot, stlinks);
        stgNew.updateGeometries();
        
        System.out.println(stgNew.getEdges().size() + " "
                + stgNew.getVertices().size());
        
 //export du nouveau tag
     // STGraph stg = TAGIoManager.deserialize(repNew + "/tag_new.tag");
       // stg.setAccuracies(accuracies);
        //stg.updateGeometries();
        TAGIoManager.serializeBinary(stgNew, repNew + "/tag_new.tag");
        TAGIoManager.serializeXml(stgNew,  repNew + "/xstream_backup.xml");
        TAGIoManager.exportTAG(stgNew, repNew + "/tag/tagnew.shp");
        TAGIoManager.exportSnapshots(stgNew, repNew + "/tag/tagnew.shp",
                TAGIoManager.NODE_AND_EDGE);


        // **********************************************************************
        // ************** CORRECTIONS MANUELLES APPARIEMENT *********************
        //        // **********************************************************************
                // FuzzyTemporalInterval tsnapshot = null;
               //  FuzzyTemporalInterval told = new FuzzyTemporalInterval(1785, 1790, 1793, 1795);
              // FuzzyTemporalInterval tsnapshot =  new FuzzyTemporalInterval(1826, 1827, 1838, 1839);
               // FuzzyTemporalInterval tsnapshot =  new FuzzyTemporalInterval(1849, 1850, 1851, 1852);
                //FuzzyTemporalInterval tsnapshot = new FuzzyTemporalInterval(1870, 1871, 1871, 1872);
                //FuzzyTemporalInterval tsnapshot = new FuzzyTemporalInterval(1808, 1810, 1836,
                //       1839);
        

//        FuzzyTemporalInterval tsnapshot = new FuzzyTemporalInterval(new double[]{1887,1888,1888,1889},new double[]{0,1,1,0}, 4);
//
//        String repOld = "/home/bcostes/Bureau/TAG/v3/etape4";
//        String repNew = "/home/bcostes/Bureau/TAG/v3/etape5";
//        // chargement des TAG
//        STGraph stg = TAGIoManager.deserialize(repOld + "/tag_new.tag");
//      stg.addFuzzyTemporalInterval(tsnapshot, 1.);
//
//
//
//        STGraph snapshot = TAGIoManager.deserialize(repNew
//                + "/snapshot_"+tsnapshot.getX(0)+"_"+tsnapshot.getX(2)+".tag");
//
//
//        // les liens d'appariement remaniés
//        IPopulation<IFeature> popArcs1 = new Population<IFeature>();
//        for(STEntity e : stg.getEdges()){
//            popArcs1.add(new DefaultFeature(e.getGeometry().toGeoxGeometry()));
//        }//                }
//
//        IPopulation<IFeature> popNoeuds1 = new Population<IFeature>();
//        for(STEntity e : stg.getVertices()){
//            popArcs1.add(new DefaultFeature(e.getGeometry().toGeoxGeometry()));
//        }
//        IPopulation<IFeature> popArcs2 = new Population<IFeature>();
//        for(STEntity e : snapshot.getEdges()){
//            popArcs2.add(new DefaultFeature(e.getGeometry().toGeoxGeometry()));
//        }
//        IPopulation<IFeature> popNoeuds2 = new Population<IFeature>();
//        for(STEntity e :snapshot.getVertices()){
//            popArcs2.add(new DefaultFeature(e.getGeometry().toGeoxGeometry()));
//        }

//        ShapefileWriter.write(popArcs1, "/home/bcostes/Bureau/tmp/popArcs1.shp");
//        ShapefileWriter.write(popArcs2, "/home/bcostes/Bureau/tmp/popArcs2.shp");

//        EnsembleDeLiens liens = STLinkIOManager.importEnsembleDeLiens(popArcs1, popNoeuds1, popArcs2, popNoeuds2,
//                repNew + "/matching.shp");

        //  EnsembleDeLiens liens = STLinkIOManager.importEnsembleDeLiens(repOld + "/snapshot_1785.0_1795.0_edges.shp",repOld + "/snapshot_1785.0_1795.0.shp", repNew
        //      + "/snapshot_" + tsnapshot.a() + "_" + tsnapshot.d() + "_edges.shp",
        //     repNew + "/snapshot_" + tsnapshot.a() + "_" + tsnapshot.d() + ".shp",
        //    repNew + "/matching.shp");

//        Set<STLink> stlinks = builder
//                .createSTLinks(stg, tsnapshot, snapshot, liens);
//        snapshot.setStLinks(stlinks);
//        // procédure de modification du tag + snapshot
//        STGraph stgNew = builder.buildNewStgraph(stg, snapshot, tsnapshot);
//        System.out.println(stgNew.getEdges().size() + " "
//                + stgNew.getVertices().size());
//        // export du nouveau tag
//        ManualIterativeBuilder.detect_doublons(stgNew, repNew + "/doublons.shp");
//
//        TAGIoManager.serializeBinary(stgNew, repNew + "/tag_new.tag");
//        TAGIoManager.serializeXml(stgNew,  repNew + "/xstream_backup.tag");
//        TAGIoManager.exportTAG(stgNew, repNew + "/tag/tagnew.shp");
//        TAGIoManager.exportSnapshots(stgNew, repNew + "/tag/tagnew.shp",
//                TAGIoManager.NODE_AND_EDGE);


    }


}
