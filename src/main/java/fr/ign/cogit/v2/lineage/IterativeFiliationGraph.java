package fr.ign.cogit.v2.lineage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ILineString;
import fr.ign.cogit.v2.geometry.LightLineString;
import fr.ign.cogit.v2.merging.OldMergeUtils;
import fr.ign.cogit.v2.merging.Split11Links;
import fr.ign.cogit.v2.merging.Split1NLinks;
import fr.ign.cogit.v2.merging.SplitNNLinks;
import fr.ign.cogit.v2.tag.FuzzyTemporalInterval;
import fr.ign.cogit.v2.tag.STEntity;
import fr.ign.cogit.v2.tag.STGraph;
import fr.ign.cogit.v2.tag.TemporalDomain;

public class IterativeFiliationGraph {
    public static boolean debug = false;

    /**
     * Constructeurs
     * @param temporalDomain
     * @param snapshot
     * @param tsnapshot
     * @param links
     */
    public IterativeFiliationGraph(TemporalDomain temporalDomain,
            STGraph snapshot) {
        this.stgraph = new STGraph(temporalDomain);
        this.stgraph.setAccuracies(snapshot.getAccuracies());
        for (STEntity edge : snapshot.getEdges()) {
            if (!stgraph.containsEdge(edge)) {
                stgraph.addEdge(edge, snapshot.getEndpoints(edge));
            }
        }
        this.stgraph.setAttributes(snapshot.getAttributes());
    }

    private STGraph stgraph;

    public STGraph getStGraph() {
        return this.stgraph;
    }

    public void setStgraph(STGraph stgraph) {
        this.stgraph = stgraph;
    }

    /**
     * 
     * @param tsnapshot1
     * @param snapshot1
     * @param tsnapshot2
     * @param snapshot2
     * @param links liens entre snapshot1 et snapshot2
     */
    public void buildNextIteration(FuzzyTemporalInterval tsnapshot2,
            STGraph snapshot2, Set<MatchingLink> matchingLinks) {
        this.stgraph.updateGeometries();
        snapshot2.setAccuracies(this.stgraph.getAccuracies());
        snapshot2.updateGeometries();
        System.out.println("DATES : " + tsnapshot2.toString());
        System.out.println("SNAPSHOT2 : " + snapshot2.getEdgeCount() + " edges, "
                + snapshot2.getVertexCount() + " nodes");

        

        
        // Appariement des noeuds de snapshot1
        for (STEntity node : new ArrayList<STEntity>(snapshot2.getVertices())) {
            // y a t-il une relation ST entre ce sommet et un autre sommet ?
            // (relation 1:1)
            MatchingLink stlink = this.findSTLink(node, new MatchingPattern(STEntity.NODE,
                    STEntity.NODE), matchingLinks, tsnapshot2, true);
            if (stlink != null) {
                OldMergeUtils.mergeSimpleNodes(stlink, snapshot2, this.stgraph, matchingLinks);
                
            } else { // pas de relation 1:1 sommet -> sommet // on va cherche une
                // relation node -> {node ,edge, node}
                List<Integer> targetsPattern = new ArrayList<Integer>();
                targetsPattern.add(STEntity.NODE);
                targetsPattern.add(STEntity.EDGE);
                targetsPattern.add(STEntity.NODE);
                stlink = this.findSTLink(node, new MatchingPattern(STEntity.NODE,
                        targetsPattern), matchingLinks, tsnapshot2, true);
                if (stlink != null) {
                    OldMergeUtils.mergeMultipleNodes(stlink, snapshot2, this.stgraph, matchingLinks, true);
                } else { // pas de relation 1:1 sommet -> {node ,edge, node} on
                    // cherche {sommet ,edge, node} -> node
                    targetsPattern = new ArrayList<Integer>();
                    targetsPattern.add(STEntity.NODE);
                    targetsPattern.add(STEntity.EDGE);
                    targetsPattern.add(STEntity.NODE);
                    stlink = this.findSTLink(node, new MatchingPattern(targetsPattern,
                            STEntity.NODE), matchingLinks, tsnapshot2, true);
                    if (stlink != null) {
                        if (debug) {
                            System.out.println(stlink.toString());
                        }
                        OldMergeUtils.mergeMultipleNodes(stlink, snapshot2, this.stgraph,
                                matchingLinks, false);

                    } else {
                        for (MatchingLink link : matchingLinks) {
                            if (link.getSources().getNodes().size() == 1 && link.getSources().getEdges().isEmpty()) {
                                if (link.getTargets().getEdges().size()  == link.getTargets().getNodes().size()-1 ) {
                                    if (link.getSources().getNodes().contains(node)) {
                                        stlink = link;
                                        break;
                                    }
                                }
                            }
                        }
                        if (stlink != null) {
                            Set<STEntity> edgesAdj = new HashSet<STEntity>();
                            Set<STEntity>allNodes = new HashSet<STEntity>();
                            for(STEntity e : stlink.getTargets().getEdges()){
                                edgesAdj.addAll(this.stgraph.getIncidentEdges(this.stgraph.getEndpoints(e).getFirst()));
                                edgesAdj.addAll(this.stgraph.getIncidentEdges(this.stgraph.getEndpoints(e).getSecond()));
                                allNodes.add(this.stgraph.getEndpoints(e).getFirst());
                                allNodes.add(this.stgraph.getEndpoints(e).getSecond());
                            }
                            edgesAdj.removeAll(stlink.getTargets().getEdges());
                            Set<STEntity>nodesExt = new HashSet<STEntity>();
                            for(STEntity e : edgesAdj){
                                if(allNodes.contains(this.stgraph.getEndpoints(e).getFirst())){
                                    nodesExt.add(this.stgraph.getEndpoints(e).getFirst());
                                }
                                else{
                                    nodesExt.add(this.stgraph.getEndpoints(e).getSecond());
                                }
                            }
                            if(nodesExt.size() != allNodes.size()){
                                continue;
                            }
                            else{
                                OldMergeUtils.mergeMultipleNodes(stlink, snapshot2, this.stgraph, matchingLinks, true);
                            }
                        }
                        else{
                            for (MatchingLink link : matchingLinks) {
                                if (link.getSources().getEdges().size()  == link.getSources().getNodes().size()-1 ) {
                                    if (link.getTargets().getNodes().size() == 1 && link.getTargets().getEdges().isEmpty()) {
                                        if (link.getSources().getNodes().contains(node)) {
                                            stlink = link;
                                            break;
                                        }
                                    }
                                }
                            }
                            if (stlink != null) {
                                Set<STEntity> edgesAdj = new HashSet<STEntity>();
                                Set<STEntity>allNodes = new HashSet<STEntity>();
                                for(STEntity e : stlink.getSources().getEdges()){
                                    edgesAdj.addAll(snapshot2.getIncidentEdges(snapshot2.getEndpoints(e).getFirst()));
                                    edgesAdj.addAll(snapshot2.getIncidentEdges(snapshot2.getEndpoints(e).getSecond()));
                                    allNodes.add(snapshot2.getEndpoints(e).getFirst());
                                    allNodes.add(snapshot2.getEndpoints(e).getSecond());
                                }
                                edgesAdj.removeAll(stlink.getSources().getEdges());
                                Set<STEntity>nodesExt = new HashSet<STEntity>();
                                for(STEntity e : edgesAdj){
                                    if(allNodes.contains(snapshot2.getEndpoints(e).getFirst())){
                                        nodesExt.add(snapshot2.getEndpoints(e).getFirst());
                                    }
                                    else{
                                        nodesExt.add(snapshot2.getEndpoints(e).getSecond());
                                    }
                                }
                                if(nodesExt.size() != allNodes.size()){
                                    continue;
                                }
                                else{
                                    OldMergeUtils.mergeMultipleNodes(stlink,  snapshot2, this.stgraph, matchingLinks, false);
                                }
                            }
                            else{
                                // / pas de pattern
                                // interessant
                                continue;
                            }
                        }
                    }
                }
            }

        }
        for (STEntity edge : new ArrayList<STEntity>(snapshot2.getEdges())) {
            // ya a t'il une relation 1 :1 entre cet arc et un autre arc ?
            MatchingLink stlink = this.findSTLink(edge, new MatchingPattern(STEntity.EDGE,
                    STEntity.EDGE), matchingLinks, tsnapshot2, true);
            if (stlink != null) {
                for (MatchingLink l : matchingLinks) {
                    for (STEntity e : l.getSources().getEdges()) {
                        if (!snapshot2.containsEdge(e)) {
                            System.out.println("1");
                            break;
                        }
                    }
                    for (STEntity e : l.getTargets().getEdges()) {
                        if (!this.stgraph.containsEdge(e)) {
                            System.out.println("1");
                            break;
                        }
                    }
                }

                Split11Links.split(stlink, matchingLinks, snapshot2,
                        this.stgraph);

                for (MatchingLink l : matchingLinks) {
                    for (STEntity e : l.getSources().getEdges()) {
                        if (!snapshot2.containsEdge(e)) {
                            System.out.println("2");
                            break;
                        }
                    }
                    for (STEntity e : l.getTargets().getEdges()) {
                        if (!this.stgraph.containsEdge(e)) {
                            System.out.println("2");
                            break;
                        }
                    }
                }

            } else { // relation 1 -> N ? ARC -> {ARC, ARC, NOEUD, etc.}
                for (MatchingLink link : matchingLinks) {
                    if (link.getSources().getEdges().size() == 1) {
                        if (link.getTargets().getEdges().size() > 1) {
                            if (link.getSources().getEdges().contains(edge)) {
                                stlink = link;
                                break;
                            }
                        }
                    }
                }
                if (stlink != null) {
                    // c'est le cas ! // on effectue la projection du paquet sur l'arc
                    // apparié


                    Split1NLinks.split(stlink, matchingLinks, snapshot2,
                            this.stgraph);


                } else { // on cherche un lien N -> 1
                    for (MatchingLink link : matchingLinks) {
                        if (link.getSources().getEdges().size() > 1) {
                            if (link.getTargets().getEdges().size() == 1) {
                                if (link.getSources().getEdges().contains(edge)) {
                                    stlink = link;
                                    break;
                                }
                            }
                        }
                    }
                    if (stlink != null) { // c'est le cas ! // on // effectue la
                        // projection du paquet sur l'arc appari

                        Split1NLinks.split(stlink, matchingLinks, snapshot2,
                                this.stgraph);


                    } else {
                        for (MatchingLink link : matchingLinks) {
                            if (link.getSources().getEdges().size() > 1) {
                                if (link.getTargets().getEdges().size() > 1) {
                                    if (link.getSources().getEdges().contains(edge)) {
                                        stlink = link;
                                        break;
                                    }
                                }
                            }
                        }
                        if (stlink != null) { // c'est le cas ! // on // effectue la
                            // projection du paquet sur l'arc appari


                            SplitNNLinks.split(stlink, matchingLinks, snapshot2,
                                    this.stgraph);


                        }
                    }
                }
            }

        }

        // on ajoute les objets
        for (STEntity edge : snapshot2.getEdges()) {
            if (!this.stgraph.containsEdge(edge)) {
                this.stgraph.addEdge(edge, snapshot2.getEndpoints(edge));
            }
        }
        
        


        // ********************************************************************
        // ************************** POST TRAITEMENT *************************
        // ********************************************************************
        // dernière passe de sécurité
//        for (STEntity e : new ArrayList<STEntity>(this.stgraph.getEdges())) {
//            if (!this.stgraph.containsEdge(e)) {
//                continue;
//            }
//            // on cherche un arc qui aurait les meme extrémitées
//            STEntity eee = null;
//            for (STEntity ee : new ArrayList<STEntity>(this.stgraph.getEdges())) {
//                if (ee.equals(e)) {
//                    continue;
//                }
//                if (this.stgraph.getEndpoints(e).getFirst()
//                        .equals(stgraph.getEndpoints(ee).getFirst())
//                        && this.stgraph.getEndpoints(e).getSecond()
//                        .equals(stgraph.getEndpoints(ee).getSecond())
//                        || this.stgraph.getEndpoints(e).getFirst()
//                        .equals(stgraph.getEndpoints(ee).getSecond())
//                        && this.stgraph.getEndpoints(e).getSecond()
//                        .equals(stgraph.getEndpoints(ee).getFirst())) {
//                    // on fusionne si les arcs sont proches
//                    if (Distances.distanceMoyenne((ILineString) e.getGeometry()
//                            .toGeoxGeometry(), (ILineString) ee.getGeometry()
//                            .toGeoxGeometry()) < 5) {
//                        if (!ee.existsAt(tsnapshot2)) {
//                            eee = ee;
//                            break;
//                        }
//                    }
//                }
//            }
//
//            if (eee != null) {
//                boolean bug = false;
//                for (FuzzyTemporalInterval t : e.getTimeSerie().getValues().keySet()) {
//                    if (eee.existsAt(t) && e.existsAt(t)) {
//                        bug = true;
//                        break;
//                    }
//                }
//                for (FuzzyTemporalInterval t : eee.getTimeSerie().getValues().keySet()) {
//                    if (eee.existsAt(t) && e.existsAt(t)) {
//                        bug = true;
//                        break;
//                    }
//                }
//                if (bug) {
//                    System.out.println("BUG 1 !!!!!!!!");
//                    // cas bizarre, on supprime un des deux
//                    Set<FuzzyTemporalInterval> times = new HashSet<FuzzyTemporalInterval>();
//                    for (FuzzyTemporalInterval tt : e.getTimeSerie().getValues().keySet()) {
//                        if (e.existsAt(tt)) {
//                            times.add(tt);
//                        }
//                    }
//                    Set<FuzzyTemporalInterval> times2 = new HashSet<FuzzyTemporalInterval>();
//                    for (FuzzyTemporalInterval tt : eee.getTimeSerie().getValues().keySet()) {
//                        if (eee.existsAt(tt)) {
//                            times2.add(tt);
//                        }
//                    }
//                    if (times.size() > times2.size() || times.size() == times2.size()) {
//                        // on supprime eee
//                        this.stgraph.removeEdge(eee);
//                        for (STLink l : new ArrayList<STLink>(this.stgraph.getStLinks())) {
//                            if (l.getSources().getEdges().contains(eee)) {
//                                l.getSources().getEdges().remove(eee);
//                                if (l.getSources().getEdges().isEmpty()
//                                        && l.getSources().getNodes().isEmpty()) {
//                                    this.stgraph.getStLinks().remove(l);
//                                }
//                            }
//                            if (l.getTargets().getEdges().contains(eee)) {
//                                l.getTargets().getEdges().remove(eee);
//                                if (l.getTargets().getEdges().isEmpty()
//                                        && l.getTargets().getNodes().isEmpty()) {
//                                    this.stgraph.getStLinks().remove(l);
//                                }
//                            }
//                        }
//                    } else {
//                        // on supprime e
//                        this.stgraph.removeEdge(e);
//                        for (STLink l : new ArrayList<STLink>(this.stgraph.getStLinks())) {
//                            if (l.getSources().getEdges().contains(e)) {
//                                l.getSources().getEdges().remove(e);
//                                if (l.getSources().getEdges().isEmpty()
//                                        && l.getSources().getNodes().isEmpty()) {
//                                    this.stgraph.getStLinks().remove(l);
//                                }
//                            }
//                            if (l.getTargets().getEdges().contains(e)) {
//                                l.getTargets().getEdges().remove(e);
//                                if (l.getTargets().getEdges().isEmpty()
//                                        && l.getTargets().getNodes().isEmpty()) {
//                                    this.stgraph.getStLinks().remove(l);
//                                }
//                            }
//                        }
//                    }
//                    continue;
//                }
//                STEntity newEdge = MergeUtils.createMergedEdge(eee, e); //
//                MergeUtils.updateEdge(e, newEdge, this.stgraph);
//                this.stgraph.removeEdge(eee);
//                newEdge.updateGeometry(this.stgraph);
//                for (STLink l : this.getStGraph().getStLinks()) {
//                    if (l.getSources().getEdges().contains(e)) {
//                        l.getSources().update(e, newEdge);
//                    }
//                    if (l.getSources().getEdges().contains(eee)) {
//                        l.getSources().update(eee, newEdge);
//                    }
//                    if (l.getTargets().getEdges().contains(e)) {
//                        l.getSources().update(e, newEdge);
//                    }
//                    if (l.getTargets().getEdges().contains(eee)) {
//                        l.getSources().update(eee, newEdge);
//                    }
//                }
//
//            }
//        }
//
//        for (STEntity e : new ArrayList<STEntity>(this.stgraph.getEdges())) {
//            Set<FuzzyTemporalInterval> times = new HashSet<FuzzyTemporalInterval>();
//            for (FuzzyTemporalInterval t : e.getTimeSerie().getValues().keySet()) {
//                if (e.existsAt(t)) {
//                    times.add(t);
//                }
//            }
//            for (FuzzyTemporalInterval t : new HashSet<FuzzyTemporalInterval>(times)) {
//                if (!this.stgraph.getEndpoints(e).getFirst().existsAt(t)) {
//                    Set<STEntity> edges = new HashSet<STEntity>();
//                    edges.addAll(this.stgraph.getIncidentEdges(this.stgraph.getEndpoints(
//                            e).getFirst()));
//                    for (STEntity ee : new HashSet<STEntity>(edges)) {
//                        if (!ee.existsAt(t)) {
//                            edges.remove(ee);
//                        }
//                    }
//                    if (edges.size() == 1) {
//                        // probleme....
//                        if (times.size() == 1) {
//                            this.stgraph.removeEdge(e);
//                            for (STLink l : new ArrayList<STLink>(this.stgraph.getStLinks())) {
//                                if (l.getSources().getEdges().contains(e)) {
//                                    l.getSources().getEdges().remove(e);
//                                    if (l.getSources().getEdges().isEmpty()
//                                            && l.getSources().getNodes().isEmpty()) {
//                                        this.stgraph.getStLinks().remove(l);
//                                    }
//                                }
//                                if (l.getTargets().getEdges().contains(e)) {
//                                    l.getTargets().getEdges().remove(e);
//                                    if (l.getTargets().getEdges().isEmpty()
//                                            && l.getTargets().getNodes().isEmpty()) {
//                                        this.stgraph.getStLinks().remove(l);
//                                    }
//                                }
//                            }
//                            break;
//                        } else {
//                            e.existsAt(t, false);
//                            e.setGeometryAt(t, null);
//                            e.setWeightAt(t, -1);
//                            //e.getTIndicators().put(t, new HashMap<String, Double>());
//                            e.updateGeometry(this.stgraph);
//                            times.remove(t);
//                            continue;
//                        }
//                    }
//                }
//                if (!this.stgraph.getEndpoints(e).getSecond().existsAt(t)) {
//                    Set<STEntity> edges = new HashSet<STEntity>();
//                    edges.addAll(this.stgraph.getIncidentEdges(this.stgraph.getEndpoints(
//                            e).getSecond()));
//                    for (STEntity ee : new HashSet<STEntity>(edges)) {
//                        if (!ee.existsAt(t)) {
//                            edges.remove(ee);
//                        }
//                    }
//                    if (edges.size() == 1) {
//                        // probleme....
//                        if (times.size() == 1) {
//                            this.stgraph.removeEdge(e);
//                            for (STLink l : new ArrayList<STLink>(this.stgraph.getStLinks())) {
//                                if (l.getSources().getEdges().contains(e)) {
//                                    l.getSources().getEdges().remove(e);
//                                    if (l.getSources().getEdges().isEmpty()
//                                            && l.getSources().getNodes().isEmpty()) {
//                                        this.stgraph.getStLinks().remove(l);
//                                    }
//                                }
//                                if (l.getTargets().getEdges().contains(e)) {
//                                    l.getTargets().getEdges().remove(e);
//                                    if (l.getTargets().getEdges().isEmpty()
//                                            && l.getTargets().getNodes().isEmpty()) {
//                                        this.stgraph.getStLinks().remove(l);
//                                    }
//                                }
//                            }
//                            break;
//                        } else {
//                            e.existsAt(t, false);
//                            e.setGeometryAt(t, null);
//                            e.setWeightAt(t, -1);
//                           // e.getTIndicators().put(t, new HashMap<String, Double>());
//                            e.updateGeometry(this.stgraph);
//                            times.remove(t);
//                            continue;
//                        }
//                    }
//                }
//            }
//        }
//        for (STEntity n : new ArrayList<STEntity>(this.stgraph.getVertices())) {
//            Set<FuzzyTemporalInterval> times = new HashSet<FuzzyTemporalInterval>();
//            for (FuzzyTemporalInterval t : n.getTimeSerie().getValues().keySet()) {
//                if (n.existsAt(t)) {
//                    times.add(t);
//                }
//            }
//            for (FuzzyTemporalInterval t : new HashSet<FuzzyTemporalInterval>(times)) {
//                Set<STEntity> edges = new HashSet<STEntity>();
//                edges.addAll(this.stgraph.getIncidentEdges(n));
//                for (STEntity e : new HashSet<STEntity>(edges)) {
//                    if (!e.existsAt(t)) {
//                        edges.remove(e);
//                    }
//                }
//                if (edges.size() == 0) {
//                    if (times.size() == 1) {
//                        this.stgraph.removeVertex(n);
//                        for (STLink l : new ArrayList<STLink>(this.stgraph.getStLinks())) {
//                            if (l.getSources().getEdges().contains(n)) {
//                                l.getSources().getEdges().remove(n);
//                                if (l.getSources().getEdges().isEmpty()
//                                        && l.getSources().getNodes().isEmpty()) {
//                                    this.stgraph.getStLinks().remove(l);
//                                }
//                            }
//                            if (l.getTargets().getEdges().contains(n)) {
//                                l.getTargets().getEdges().remove(n);
//                                if (l.getTargets().getEdges().isEmpty()
//                                        && l.getTargets().getNodes().isEmpty()) {
//                                    this.stgraph.getStLinks().remove(l);
//                                }
//                            }
//                        }
//                    } else {
//                        n.existsAt(t, false);
//                        n.setGeometryAt(t, null);
//                        n.setWeightAt(t, -1);
//                        //n.getTIndicators().put(t, new HashMap<String, Double>());
//                        n.updateGeometry(this.stgraph);
//                        times.remove(t);
//                    }
//                }
//            }
//
//        }
//
//        for (STLink l : new ArrayList<STLink>(this.stgraph.getStLinks())) {
//            for (STEntity n : new ArrayList<STEntity>(l.getSources().getNodes())) {
//                if (!this.stgraph.containsVertex(n)) {
//                    l.getSources().getNodes().remove(n);
//                }
//            }
//            for (STEntity n : new ArrayList<STEntity>(l.getTargets().getNodes())) {
//                if (!this.stgraph.containsVertex(n)) {
//                    l.getTargets().getNodes().remove(n);
//                }
//            }
//            for (STEntity e : new ArrayList<STEntity>(l.getSources().getEdges())) {
//                if (!this.stgraph.containsEdge(e)) {
//                    l.getSources().getEdges().remove(e);
//                }
//
//            }
//            for (STEntity e : new ArrayList<STEntity>(l.getTargets().getEdges())) {
//                if (!this.stgraph.containsEdge(e)) {
//                    l.getTargets().getEdges().remove(e);
//                }
//            }
//            if (l.getSources().getNodes().isEmpty()
//                    && l.getSources().getEdges().isEmpty()) {
//                this.stgraph.getStLinks().remove(l);
//            } else if (l.getTargets().getNodes().isEmpty()
//                    && l.getTargets().getEdges().isEmpty()) {
//                this.stgraph.getStLinks().remove(l);
//            }
//        }

        for (STEntity e : stgraph.getEdges()) {
            for (FuzzyTemporalInterval t : e.getTimeSerie().getValues().keySet()) {
                if (e.getGeometryAt(t) != null) {
                    ILineString ll = ((LightLineString) e.getGeometryAt(t))
                            .toGeoxGeometry();
                    if (stgraph.getEndpoints(e).getFirst().getGeometryAt(t) != null
                            && stgraph.getEndpoints(e).getSecond().getGeometryAt(t) != null) {
                        // if (ll.coord().size() == 2) {
                        // ll = Operateurs.echantillone(ll, ll.length() / 3.);
                        // }
                        IDirectPosition pp1 = stgraph.getEndpoints(e).getFirst()
                                .getGeometryAt(t).toGeoxGeometry().coord().get(0);
                        IDirectPosition pp2 = stgraph.getEndpoints(e).getSecond()
                                .getGeometryAt(t).toGeoxGeometry().coord().get(0);
                        if (pp1.equals(ll.getControlPoint(0))
                                && !pp2.equals(ll
                                        .getControlPoint(ll.getControlPoint().size() - 1))) {
                            ll.setControlPoint(ll.getControlPoint().size() - 1, pp2);
                            e.setGeometryAt(t, new LightLineString(ll.coord()));
                        } else if (pp1.equals(ll.getControlPoint(ll.getControlPoint()
                                .size() - 1)) && !pp2.equals(ll.getControlPoint(0))) {
                            ll.setControlPoint(0, pp2);
                            e.setGeometryAt(t, new LightLineString(ll.coord()));
                        } else if (pp2.equals(ll.getControlPoint(0))
                                && !pp1.equals(ll
                                        .getControlPoint(ll.getControlPoint().size() - 1))) {
                            ll.setControlPoint(ll.getControlPoint().size() - 1, pp1);
                            e.setGeometryAt(t, new LightLineString(ll.coord()));
                        } else if (pp2.equals(ll.getControlPoint(ll.getControlPoint()
                                .size() - 1)) && !pp1.equals(ll.getControlPoint(0))) {
                            ll.setControlPoint(0, pp1);
                            e.setGeometryAt(t, new LightLineString(ll.coord()));
                        }
                    }
                }
            }
        }

        this.stgraph.updateGeometries();


        // ********************************************************************
        // ************************ FIN POST TRAITEMENT ***********************
        // ********************************************************************
        // on supprime les liens parasites qui peuvent demeurer apres le traitement
        // (les liens qui concernent des entitées qui ne sont plus dans le graphe)

        // TODO : delete newt two lines
        // this.stgraph.getStLinks().clear();
        // this.stgraph.getStLinks().addAll(linksDebug);
        //System.out.println("links : " + this.getStGraph().getStLinks().size());
    }

    /**
     * Cherche si un lien d'appariement concerne entity, en regard du pattern
     * d'appariement pattern
     * @param entity
     * @param pattern
     * @direct sens de l'appariement
     * @return
     */
    private MatchingLink findSTLink(STEntity entity, MatchingPattern pattern,
            Set<MatchingLink> stlinks, FuzzyTemporalInterval t2, boolean direct) {
        if (direct) {
            if (entity.getType() == STEntity.NODE
                    && !pattern.sources.contains(STEntity.NODE)) {
                // pattern incohérent en regard du type de entity
                return null;
            }
            if (entity.getType() == STEntity.EDGE
                    && !pattern.sources.contains(STEntity.EDGE)) {
                // pattern incohérent en regard du type de entity
                return null;
            }
            List<MatchingLink> links = MatchingPattern.findMatchingPattern(pattern,
                    stlinks, t2);
            if (links.isEmpty()) {
                return null;
            }
            // il y a des liens d'appariement qui respectent le pattern
            for (MatchingLink link : links) {
                // entity est-il bien dans les sources ?
                if (entity.getType() == STEntity.NODE
                        && link.getSources().getNodes().contains(entity)) {
                    return link;
                }
                if (entity.getType() == STEntity.EDGE
                        && link.getSources().getEdges().contains(entity)) {
                    return link;
                }
            }
            return null;
        } else {
            if (entity.getType() == STEntity.NODE
                    && !pattern.targets.contains(STEntity.NODE)) {
                // pattern incohérent en regard du type de entity
                return null;
            }
            if (entity.getType() == STEntity.EDGE
                    && !pattern.targets.contains(STEntity.EDGE)) {
                // pattern incohérent en regard du type de entity
                return null;
            }
            List<MatchingLink> links = MatchingPattern.findMatchingPattern(pattern,
                    stlinks, t2);
            if (links.isEmpty()) {
                return null;
            }
            // il y a des liens d'appariement qui respectent le pattern
            for (MatchingLink link : links) {
                // entity est-il bien dans les cibles ?
                if (entity.getType() == STEntity.NODE
                        && link.getTargets().getNodes().contains(entity)) {
                    return link;
                }
                if (entity.getType() == STEntity.EDGE
                        && link.getTargets().getEdges().contains(entity)) {
                    return link;
                }
            }
            return null;
        }

    }
}
