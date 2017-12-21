package fr.ign.cogit.v2.mergeProcess;

import java.util.Set;

import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ILineString;
import fr.ign.cogit.v2.geometry.LightLineString;
import fr.ign.cogit.v2.lineage.MatchingLink;
import fr.ign.cogit.v2.mergeProcess.mergeUtility.GroupGroupMU;
import fr.ign.cogit.v2.mergeProcess.mergeUtility.MergeUtility;
import fr.ign.cogit.v2.mergeProcess.mergeUtility.MultipleEdgeMultipleEdgeMU;
import fr.ign.cogit.v2.mergeProcess.mergeUtility.SingleEdgeMultipleEdgeMU;
import fr.ign.cogit.v2.mergeProcess.mergeUtility.SingleEdgeSingleEdgeMU;
import fr.ign.cogit.v2.mergeProcess.mergeUtility.SingleNodeGroupMU;
import fr.ign.cogit.v2.mergeProcess.mergeUtility.SingleNodeSingleNodeMU;
import fr.ign.cogit.v2.tag.FuzzyTemporalInterval;
import fr.ign.cogit.v2.tag.STEntity;
import fr.ign.cogit.v2.tag.STGraph;
import fr.ign.cogit.v2.tag.TemporalDomain;

/**
 * Classe mère réalisant la fusion d'un STGraph  avec un snapshot (sous forme d'un autre STgraph
 * à une seule date).
 * @author bcostes
 *
 */
public class MergingProcess {

    /**
     * Le nouveau stgraph fusionné
     */
    private STGraph stgraph;
    private Set<MatchingLink> matchingLinks;

    public MergingProcess(TemporalDomain temporalDomain,
            STGraph stgraph) {
        // on fait uNe copie du stgraph en entrée
        this.stgraph = stgraph;
    }


    public STGraph getStGraph() {
        return this.stgraph;
    }


    public Set<MatchingLink> getMatchingLinks() {
        return matchingLinks;
    }

    public void callMergeUtility(MergeUtility m){
        m.merge(this);
    }


    /**
     * Réalise la fusion du stgraph avec le snapshot à la datet snapshot
     * @param snapshot
     * @param tsnapshot
     * @param matchingLinks
     */
    public void merge(STGraph snapshot, FuzzyTemporalInterval tsnapshot, Set<MatchingLink> matchingLinks){
        this.matchingLinks = matchingLinks;
        // on ajoute les objets du snapshot dans stgraph
        for (STEntity edge : snapshot.getEdges()) {
            this.stgraph.addEdge(edge, snapshot.getEndpoints(edge));
        }
        
        System.out.println("before : " + stgraph.getEdges().size() + " "
                + stgraph.getVertices().size());
                
        // ************************* Fusion des sommets ***************************************
        
        // -------------------------------- lien 1 : 1 -----------------------------
        Set<MatchingLink> links = MatchingUtils.findAcceptableMatchingPattern(
                AcceptableMatchingPattern.SINGLENODE_SINGLENODE, this.matchingLinks);
        if(!links.isEmpty()){
            // on a trouvé des liens d'appariement 1:1 pour les sommets
            for(MatchingLink link: links){
                // On fusionne les deux sommets concernés par le lien d'appariement
                MergeUtility mU = new SingleNodeSingleNodeMU(link);
                mU.merge(this);
            }
        }
        
        // -------------------------------- lien 1 : {n,a,n} -----------------------------
       links = MatchingUtils.findAcceptableMatchingPattern(
                AcceptableMatchingPattern.SINGLENODE_GROUP, this.matchingLinks);
       links.addAll(MatchingUtils.findAcceptableMatchingPattern(
               AcceptableMatchingPattern.GROUP_SINGLENODE, this.matchingLinks));
        if(!links.isEmpty()){
            // on a trouvé des liens d'appariement 1: {n,a,n} pour les sommets
            for(MatchingLink link: links){
                // On fusionne les deux sommets concernés par le lien d'appariement
                MergeUtility mU = new SingleNodeGroupMU(link);
                mU.merge(this);
            }
        }
        
        
     // -------------------------------- lien {n,a,n} : {n,a,n} -----------------------------
        links = MatchingUtils.findAcceptableMatchingPattern(
                 AcceptableMatchingPattern.GROUP_GROUP, this.matchingLinks);
         if(!links.isEmpty()){
             // on a trouvé des liens d'appariement {n,a,n}: {n,a,n} pour les sommets
             for(MatchingLink link: links){
                 // On fusionne les deux sommets concernés par le lien d'appariement
                 MergeUtility mU = new GroupGroupMU(link);
                 mU.merge(this);
             }
         }

        
        // ************************* Fusion des arcs ***************************************
        
        // -------------------------------- lien 1 : 1 -----------------------------
        links = MatchingUtils.findAcceptableMatchingPattern(
                AcceptableMatchingPattern.SINGLEEDGE_SINGLEEDGE, this.matchingLinks);
        if(!links.isEmpty()){
            // on a trouvé des liens d'appariement 1:1 pour les arcs
            for(MatchingLink link: links){
                // On fusionne les deux arcs concernés par le lien d'appariement
                MergeUtility mU = new SingleEdgeSingleEdgeMU(link);
                mU.merge(this);
            }
        }
        
     // -------------------------------- lien 1 : n -----------------------------
        links = MatchingUtils.findAcceptableMatchingPattern(
                AcceptableMatchingPattern.SINGLEEDGE_MULTIPLEEDGE, this.matchingLinks);
        links.addAll(MatchingUtils.findAcceptableMatchingPattern(
                AcceptableMatchingPattern.MULTIPLEEDGE_SINGLEEDGE, this.matchingLinks));
        if(!links.isEmpty()){
            // on a trouvé des liens d'appariement 1:n pour les arcs
            for(MatchingLink link: links){
                // On fusionne les n+1 arcs concernés par le lien d'appariement
                MergeUtility mU = new SingleEdgeMultipleEdgeMU(link);
                mU.merge(this);
            }
        }
        
        // -------------------------------- lien n : m -----------------------------
        links = MatchingUtils.findAcceptableMatchingPattern(
                AcceptableMatchingPattern.MULTIPLEEDGE_MULTIPLEEDGE, this.matchingLinks);
        if(!links.isEmpty()){
            // on a trouvé des liens d'appariement n:m pour les arcs
            for(MatchingLink link: links){
                // On fusionne les deux n+m concernés par le lien d'appariement
                MergeUtility mU = new MultipleEdgeMultipleEdgeMU(link);
                mU.merge(this);
            }
        }
        
        // ************************* Post Traitement*************************************
        
        for (STEntity e : this.stgraph.getEdges()) {
            for (FuzzyTemporalInterval t : e.getTimeSerie().getValues().keySet()) {
                if (e.getGeometryAt(t) != null) {
                    ILineString ll = ((LightLineString) e.getGeometryAt(t))
                            .toGeoxGeometry();
                    if (this.stgraph.getEndpoints(e).getFirst().getGeometryAt(t) != null
                            && this.stgraph.getEndpoints(e).getSecond().getGeometryAt(t) != null) {
                        // if (ll.coord().size() == 2) {
                        // ll = Operateurs.echantillone(ll, ll.length() / 3.);
                        // }
                        IDirectPosition pp1 = this.stgraph.getEndpoints(e).getFirst()
                                .getGeometryAt(t).toGeoxGeometry().coord().get(0);
                        IDirectPosition pp2 = this.stgraph.getEndpoints(e).getSecond()
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
        
        System.out.println("after : " + stgraph.getEdges().size() + " "
                + stgraph.getVertices().size());
                
    }


    public void setMatchingLinks(Set<MatchingLink> matchingLinks) {
      this.matchingLinks = matchingLinks;
    }



}
