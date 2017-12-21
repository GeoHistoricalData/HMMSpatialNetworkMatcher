package fr.ign.cogit.v2.mergeProcess.mergeUtility;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.uci.ics.jung.graph.UndirectedSparseMultigraph;
import edu.uci.ics.jung.graph.util.Pair;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPositionList;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ILineString;
import fr.ign.cogit.geoxygene.contrib.geometrie.Distances;
import fr.ign.cogit.geoxygene.contrib.geometrie.Operateurs;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPositionList;
import fr.ign.cogit.geoxygene.spatial.coordgeom.GM_LineString;
import fr.ign.cogit.geoxygene.spatial.geomaggr.GM_MultiPoint;
import fr.ign.cogit.v2.geometry.LightDirectPosition;
import fr.ign.cogit.v2.geometry.LightLineString;
import fr.ign.cogit.v2.geometry.LightMultipleGeometry;
import fr.ign.cogit.v2.lineage.MatchingLink;
import fr.ign.cogit.v2.tag.FuzzyTemporalInterval;
import fr.ign.cogit.v2.tag.STEntity;
import fr.ign.cogit.v2.tag.STGraph;
import fr.ign.cogit.v2.tag.STProperty;

public class SplitUtils {

  private static double DIST_MAX_PROJECTION = 50;

  public static Set<MatchingLink> splitLinks(Set<STEntity> edgesSource,
      Set<STEntity> edgesTarget,
      UndirectedSparseMultigraph<STEntity, STEntity> stGraph/*,
            FuzzyTemporalInterval ttarget*/) {

    if (edgesSource.isEmpty() || edgesTarget.isEmpty()) {
      return null;
    }
    Set<MatchingLink> newLinks = new HashSet<MatchingLink>();
    for (STEntity esource : edgesSource) {
      // on cherche son homologue dans edgesTarget
      STEntity eIdentical = null;
      Set<STEntity> identicals =new HashSet<STEntity>();
      for (STEntity etarget : edgesTarget) {
        if (stGraph.getEndpoints(esource).getFirst()
            .equals(stGraph.getEndpoints(etarget).getFirst())
            && stGraph.getEndpoints(esource).getSecond()
            .equals(stGraph.getEndpoints(etarget).getSecond())) {
          identicals.add(etarget);
        } else if (stGraph.getEndpoints(esource).getFirst()
            .equals(stGraph.getEndpoints(etarget).getSecond())
            && stGraph.getEndpoints(esource).getSecond()
            .equals(stGraph.getEndpoints(etarget).getFirst())) {
          identicals.add(etarget);
        }
      }

      if (identicals.isEmpty()) {
        // WTF ?
            continue;
      }
      if(identicals.size() == 1){
        eIdentical = identicals.iterator().next();
      }
      else{
        // si plusieurs candidats on prend celui donc l'écart est le plus petit
        double dmin = Double.MAX_VALUE;
        for(STEntity eee: identicals){
          double d =Distances.ecartSurface((ILineString)eee.getGeometry()
              .toGeoxGeometry(), (ILineString)esource.getGeometry().toGeoxGeometry());
          if(d<dmin){
            dmin = d;
            eIdentical = eee;
          }
        }
      }


      // on creé un nouoveau lien
      MatchingLink l = new MatchingLink(/*ttarget*/);
      l.getSources().getEdges().add(esource);
      l.getTargets().getEdges().add(eIdentical);
      newLinks.add(l);
    }
    return newLinks;
  }

  /**
   * Marche si les arcs sont tous adjacents
   * @param edgesSource
   * @param edgesTarget
   * @param gSources
   * @param gTargets
   * @param ttarget
   * @return
   */
  public static boolean splitCurviligne(Set<STEntity> edgesSource,
      Set<STEntity> edgesTarget,
      STGraph stgraph){

    // on cherche les noeuds ST à projeter
    Set<STEntity> nodesSource = new HashSet<STEntity>();
    Set<STEntity> nodesTarget = new HashSet<STEntity>();
    Set<STEntity> nodesIniSource = new HashSet<STEntity>();
    Set<STEntity> nodesIniTarget = new HashSet<STEntity>();
    for (STEntity e : edgesSource) {
      nodesIniSource.add(stgraph.getEndpoints(e).getFirst());
      nodesIniSource.add(stgraph.getEndpoints(e).getSecond());
    }
    for (STEntity e : edgesTarget) {
      nodesIniTarget.add(stgraph.getEndpoints(e).getFirst());
      nodesIniTarget.add(stgraph.getEndpoints(e).getSecond());
    }
    for (STEntity e : edgesSource) {
      if (!nodesIniTarget.contains(stgraph.getEndpoints(e).getFirst())) {
        nodesSource.add(stgraph.getEndpoints(e).getFirst());
      }
      if (!nodesIniTarget.contains(stgraph.getEndpoints(e).getSecond())) {
        nodesSource.add(stgraph.getEndpoints(e).getSecond());
      }
    }
    for (STEntity e : edgesTarget) {
      if (!nodesIniSource.contains(stgraph.getEndpoints(e).getFirst())) {
        nodesTarget.add(stgraph.getEndpoints(e).getFirst());
      }
      if (!nodesIniSource.contains(stgraph.getEndpoints(e).getSecond())) {
        nodesTarget.add(stgraph.getEndpoints(e).getSecond());
      }
    }
    if (!isPlanar(edgesSource, edgesTarget, nodesSource, nodesTarget)) {
      return false;
    }


    // on commence par projeter les noeuds ST source
    for (STEntity nodeSource : nodesSource) {
      // récupération de l'arc ST le plus proche
      STEntity edgeClose = null;
      double dmin = Double.MAX_VALUE;
      for (STEntity e : edgesTarget) {
        double d = e.getGeometry().toGeoxGeometry()
            .distance(nodeSource.getGeometry().toGeoxGeometry());
        if (d < dmin) {
          dmin = d;
          edgeClose = e;
        }
      }

      STEntity[] edgesSplit = SplitUtils.splitEdgeCurviligne(edgeClose, nodeSource,stgraph);


      if (edgesSplit == null) {
        continue;
      }

      edgesTarget.remove(edgeClose);
      edgesTarget.add(edgesSplit[0]);
      edgesTarget.add(edgesSplit[1]);

    }
    // puis on projete les noeuds ST targets
    for (STEntity nodeTarget : nodesTarget) {
      // récupération de l'arc ST le plus proche
      STEntity edgeClose = null;
      double dmin = Double.MAX_VALUE;
      for (STEntity e : edgesSource) {
        double d = e.getGeometry().toGeoxGeometry()
            .distance(nodeTarget.getGeometry().toGeoxGeometry());
        if (d < dmin) {
          dmin = d;
          edgeClose = e;
        }
      }
      STEntity[] edgesSplit = SplitUtils.splitEdgeCurviligne(edgeClose, nodeTarget,stgraph);
      if (edgesSplit == null) {
        continue;
      }
      edgesSource.remove(edgeClose);
      edgesSource.add(edgesSplit[0]);
      edgesSource.add(edgesSplit[1]);
    }
    return true;

  }

  public static STEntity[] splitEdgeCurviligne(STEntity edgeToSplit,
      STEntity nodeSpliter, STGraph stgraph) {
    //DirectPosition du point à projeter
    IDirectPosition nodeP = ((LightDirectPosition)nodeSpliter.getGeometry()).toGeoxDirectPosition();
    // lineString de l'arc sur lequel on projete
    ILineString eProjection = ((LightLineString)edgeToSplit.getGeometry()).toGeoxGeometry();
    //projection
    IDirectPosition pProj = Operateurs.projection(nodeP, eProjection);
    if(nodeP.distance(pProj)>DIST_MAX_PROJECTION){
      return null;
    }
    if (pProj.equals(eProjection.getControlPoint(0), 0.5)
        || pProj.equals(eProjection.getControlPoint(eProjection
            .getControlPoint().size() - 1), 0.5)) {
      // la projection ne peut pas se faire
      return null;
    }
    //insertion du point projeté
    eProjection = Operateurs.projectionEtInsertion(nodeP, eProjection);
    //abscisse curviligne de la projectio n
    double param = eProjection.paramForPoint(pProj)[0];
    if(param == -1){
      return null;
    }
    // en relatif
    param = param / eProjection.length();
    // pour chaque date d'existence de edgeToSplit 
    for (FuzzyTemporalInterval t : edgeToSplit.getTimeSerie().getValues().keySet()) {
      if (edgeToSplit.existsAt(t)) {
        // abscisse curv pour le point à insérer
        //géométrie
        ILineString eProjectionT = ((LightLineString)edgeToSplit.getGeometryAt(t)).toGeoxGeometry();
        if(eProjection.startPoint().distance(eProjectionT.startPoint())>eProjection.startPoint().distance(eProjectionT.endPoint())){
          eProjectionT = eProjectionT.reverse();
        }
        double paramT = param * eProjectionT.length();
        // coordonnées du point à insérer
        IDirectPosition pInsertT = eProjectionT.param(paramT);
        // insertion
        IDirectPosition pInsertTModif = Operateurs.projection(pInsertT, eProjectionT);
        eProjectionT = Operateurs.projectionEtInsertion(pInsertT, eProjectionT);
        //mise à jour de la géométrie
        edgeToSplit.setGeometryAt(t, new LightLineString(eProjectionT.coord()));
        if(nodeSpliter.getGeometryAt(t) != null){
          nodeSpliter.existsAt(t, true);
          //le noeud a déja une géométrie, on fait une lightMultipleGeometrie
          List<LightLineString> l1 = new ArrayList<LightLineString>();
          List<LightDirectPosition> l2 = new ArrayList<LightDirectPosition>();

          if(nodeSpliter.getGeometryAt(t) instanceof LightMultipleGeometry){
            l1.addAll(((LightMultipleGeometry)nodeSpliter.getGeometryAt(t)).getLightLineString());
            l2.addAll(((LightMultipleGeometry)nodeSpliter.getGeometryAt(t)).getLightDirectPosition());
          }
          else{
            l2.add((LightDirectPosition)nodeSpliter.getGeometryAt(t));
          }
          l2.add(new LightDirectPosition(pInsertTModif));
          LightMultipleGeometry geom = new LightMultipleGeometry(l1, l2);
          nodeSpliter.setGeometryAt(t, geom);
          //modifier le control point de la line string
          ILineString line = (ILineString)edgeToSplit.getGeometryAt(t).toGeoxGeometry();
          int pos = -1;
          for(IDirectPosition p : line.getControlPoint()){
            pos++;
            if(p.equals(pInsertTModif)){
              break;
            }
          }
          if(pos != line.getControlPoint().size()-1){
            line.addControlPoint(pos, nodeSpliter.getGeometryAt(t).toGeoxGeometry().coord().get(0));
            line.removeControlPoint(pInsertTModif);
            edgeToSplit.setGeometryAt(t, new LightLineString(line.coord()));
          }
          else{
            //sinon tant pis on prend la derniere coordonnée
            nodeSpliter.setGeometryAt(t,
                new LightDirectPosition(pInsertTModif));
          }
        }
        else{
          nodeSpliter.setGeometryAt(t,
              new LightDirectPosition(pInsertTModif));
        }
      }
    }

    nodeSpliter.updateGeometry(stgraph);
    // on a calculer les coordonnées des projections de nodeSpliter sur les
    // différentes géométries de edgeToSplit, et on les a insérées dans
    // nodeSpliter.getGeometryAt(). Les géométries de edgeToplit ont été
    // modifiées en insérants les coordonnées du projeté dans la linestring
    // on va créeer deux nouveaux arcs et supprimer l'initial
    STEntity.setCurrentType(STEntity.EDGE);
    STEntity e1 = new STEntity(edgeToSplit.getTimeSerie());
    STEntity e2 = new STEntity(edgeToSplit.getTimeSerie());
    for (FuzzyTemporalInterval t : edgeToSplit.getTimeSerie().getValues().keySet()) {
      if (edgeToSplit.existsAt(t)) {
        // la géométrie
        ILineString egeom = ((LightLineString) edgeToSplit.getGeometryAt(t))
            .toGeoxGeometry();
        if (stgraph.getEndpoints(edgeToSplit).getSecond().getGeometryAt(t) != null) {
          IDirectPosition pnode = stgraph.getEndpoints(edgeToSplit).getSecond()
              .getGeometryAt(t).toGeoxGeometry().coord().get(0);
          if (egeom.getControlPoint(0).equals(pnode, 0.05)) {
            egeom = egeom.reverse();
          }
        } else if (stgraph.getEndpoints(edgeToSplit).getFirst().getGeometryAt(t) != null) {
          IDirectPosition pnode = stgraph.getEndpoints(edgeToSplit).getFirst()
              .getGeometryAt(t).toGeoxGeometry().coord().get(0);
          if (!egeom.getControlPoint(0).equals(pnode, 0.05)) {
            egeom = egeom.reverse();
          }
        } else {
          System.out.println("WTFFFFFFFFFFFFFFFFFFFFFFFFFFFFF");
        }

        IDirectPositionList l1 = new DirectPositionList();
        IDirectPositionList l2 = new DirectPositionList();
        int indexP = -1;
        for (int i = 0; i < egeom.getControlPoint().size(); i++) {
          if (!egeom.getControlPoint(i).equals(
              nodeSpliter.getGeometryAt(t).toGeoxGeometry().coord().get(0),
              0.005)) {
            l1.add(egeom.getControlPoint(i));
          } else {
            l1.add(egeom.getControlPoint(i));
            indexP = i;
            break;
          }
        }
        for (int i = indexP; i < egeom.getControlPoint().size(); i++) {
          l2.add(egeom.getControlPoint(i));
        }
        /*
         * if (l1.size() == 2) { l1 = Operateurs.echantillone(new
         * GM_LineString(l1), (new GM_LineString(l1)).length() / 3.).coord(); }
         * if (l2.size() == 2) { l2 = Operateurs.echantillone(new
         * GM_LineString(l2), (new GM_LineString(l2)).length() / 3.).coord(); }
         */
        e1.setGeometryAt(t, new LightLineString(l1));
        e2.setGeometryAt(t, new LightLineString(l2));
        // TODO : les poids ...
        e1.setWeightAt(t, ((new GM_LineString(l1)).length()));
        e2.setWeightAt(t, ((new GM_LineString(l2)).length()));
        // transformations
        e1.setTransformationAt(t, edgeToSplit.getTransformationAt(t));
        e2.setTransformationAt(t, edgeToSplit.getTransformationAt(t));
        //attributs
        for( STProperty<String> att : edgeToSplit.getTAttributes()){
          e1.getTAttributes().add(att.copy());
          e2.getTAttributes().add(att.copy());
        }
      }
    }
    // modification du graphe
    Pair<STEntity> nodes1 = new Pair<STEntity>(stgraph.getEndpoints(edgeToSplit)
        .getFirst(), nodeSpliter);
    Pair<STEntity> nodes2 = new Pair<STEntity>(nodeSpliter, stgraph.getEndpoints(
        edgeToSplit).getSecond());
    stgraph.removeEdge(edgeToSplit);
    stgraph.addEdge(e1, nodes1);
    stgraph.addEdge(e2, nodes2);

    e1.updateGeometry(stgraph);
    e2.updateGeometry(stgraph);


    STEntity[] newEdges = new STEntity[2];
    newEdges[0] = e1;
    newEdges[1] = e2;
    // les liens st
    return newEdges;
  }


  /**
   * Méthode employée pour rendre la visu du STAG planaire. Ne donne pas de géométries temporelle
   * mais uniquement une géométrie fusionée
   * Il ne faut plus appeler updateGeometri après (c'est juste pour de la visu)
   * @param edgeToSplit
   * @param nodeSpliter
   * @param stgraph
   * @return
   */
  public static STEntity[] fictiveSplitEdgeCurviligne(STEntity edgeToSplit,
      STEntity nodeSpliter, STGraph stgraph) {
    //DirectPosition du point à projeter
    IDirectPosition nodeP = ((LightDirectPosition)nodeSpliter.getGeometry()).toGeoxDirectPosition();
    // lineString de l'arc sur lequel on projete
    ILineString eProjection = ((LightLineString)edgeToSplit.getGeometry()).toGeoxGeometry();
    //projection
    IDirectPosition pProj = Operateurs.projection(nodeP, eProjection);
    if(nodeP.distance(pProj)>DIST_MAX_PROJECTION){
      return null;
    }
    if (pProj.equals(eProjection.getControlPoint(0), 0.5)
        || pProj.equals(eProjection.getControlPoint(eProjection
            .getControlPoint().size() - 1), 0.5)) {
      // la projection ne peut pas se faire
      return null;
    }
    //insertion du point projeté
    eProjection = Operateurs.projectionEtInsertion(nodeP, eProjection);


    //nodeSpliter.updateGeometry(stgraph);
    // on a calculer les coordonnées des projections de nodeSpliter sur les
    // différentes géométries de edgeToSplit, et on les a insérées dans
    // nodeSpliter.getGeometryAt(). Les géométries de edgeToplit ont été
    // modifiées en insérants les coordonnées du projeté dans la linestring
    // on va créeer deux nouveaux arcs et supprimer l'initial
    STEntity.setCurrentType(STEntity.EDGE);
    STEntity e1 = new STEntity(edgeToSplit.getTimeSerie());
    STEntity e2 = new STEntity(edgeToSplit.getTimeSerie());

    if (stgraph.getEndpoints(edgeToSplit).getSecond().getGeometry() != null) {
      IDirectPosition pnode = stgraph.getEndpoints(edgeToSplit).getSecond()
          .getGeometry().toGeoxGeometry().coord().get(0);
      if (eProjection.getControlPoint(0).equals(pnode, 0.05)) {
        eProjection = eProjection.reverse();
      }
    } else if (stgraph.getEndpoints(edgeToSplit).getFirst().getGeometry() != null) {
      IDirectPosition pnode = stgraph.getEndpoints(edgeToSplit).getFirst()
          .getGeometry().toGeoxGeometry().coord().get(0);
      if (!eProjection.getControlPoint(0).equals(pnode, 0.05)) {
        eProjection = eProjection.reverse();
      }
    } else {
      System.out.println("WTFFFFFFFFFFFFFFFFFFFFFFFFFFFFF");
    }

    IDirectPositionList l1 = new DirectPositionList();
    IDirectPositionList l2 = new DirectPositionList();
    int indexP = -1;
    for (int i = 0; i < eProjection.getControlPoint().size(); i++) {
      if (!eProjection.getControlPoint(i).equals(
          nodeSpliter.getGeometry().toGeoxGeometry().coord().get(0),
          0.005)) {
        l1.add(eProjection.getControlPoint(i));
      } else {
        l1.add(eProjection.getControlPoint(i));
        indexP = i;
        break;
      }
    }
    for (int i = indexP; i < eProjection.getControlPoint().size(); i++) {
      l2.add(eProjection.getControlPoint(i));
    }
    /*
     * if (l1.size() == 2) { l1 = Operateurs.echantillone(new
     * GM_LineString(l1), (new GM_LineString(l1)).length() / 3.).coord(); }
     * if (l2.size() == 2) { l2 = Operateurs.echantillone(new
     * GM_LineString(l2), (new GM_LineString(l2)).length() / 3.).coord(); }
     */
    e1.setGeometry(new LightLineString(l1));
    e2.setGeometry(new LightLineString(l2));
    for(FuzzyTemporalInterval t: stgraph.getTemporalDomain().asList()){
      if(e1.existsAt(t)){
        // TODO : les poids ...
        e1.setWeightAt(t, ((new GM_LineString(l1)).length()));
        e2.setWeightAt(t, ((new GM_LineString(l2)).length()));
        // transformations
        e1.setTransformationAt(t, edgeToSplit.getTransformationAt(t));
        e2.setTransformationAt(t, edgeToSplit.getTransformationAt(t));
        //attributs
        for( STProperty<String> att : edgeToSplit.getTAttributes()){
          e1.getTAttributes().add(att.copy());
          e2.getTAttributes().add(att.copy());
        }
      }

    }
    // modification du graphe
    Pair<STEntity> nodes1 = new Pair<STEntity>(stgraph.getEndpoints(edgeToSplit)
        .getFirst(), nodeSpliter);
    Pair<STEntity> nodes2 = new Pair<STEntity>(nodeSpliter, stgraph.getEndpoints(
        edgeToSplit).getSecond());
    stgraph.removeEdge(edgeToSplit);
    stgraph.addEdge(e1, nodes1);
    stgraph.addEdge(e2, nodes2);

   // e1.updateGeometry(stgraph);
    //e2.updateGeometry(stgraph);


    STEntity[] newEdges = new STEntity[2];
    newEdges[0] = e1;
    newEdges[1] = e2;
    // les liens st
    return newEdges;
  }

  private static boolean isPlanar(Set<STEntity> edgesSource,
      Set<STEntity> edgesTarget, Set<STEntity> nodesSource,
      Set<STEntity> nodesTarget) {
    List<ILineString> lines = new ArrayList<ILineString>();
    for (STEntity nodeSource : nodesSource) {
      // récupération de l'arc ST le plus proche
      STEntity edgeClose = null;
      double dmin = Double.MAX_VALUE;
      for (STEntity e : edgesTarget) {
        double d = e.getGeometry().toGeoxGeometry()
            .distance(nodeSource.getGeometry().toGeoxGeometry());
        if (d < dmin) {
          dmin = d;
          edgeClose = e;
        }
      }
      ILineString egeom = (ILineString) edgeClose.getGeometry()
          .toGeoxGeometry();
      IDirectPosition pProj = Operateurs.projection(nodeSource.getGeometry()
          .toGeoxGeometry().coord().get(0), egeom);
      if (pProj.equals(egeom.getControlPoint(0), 0.5)
          || pProj.equals(
              egeom.getControlPoint(egeom.getControlPoint().size() - 1), 0.5)) {
        // la projection ne peut pas se faire
        continue;
      }
      IDirectPositionList l = new DirectPositionList();
      l.add(nodeSource.getGeometry().toGeoxGeometry().coord().get(0));
      l.add(pProj);
      lines.add(new GM_LineString(l));
      // on regarde si le segment [point a projeter, point projeté] intersecte
      // une des lineString (pour interdire les projections des "coudes"
      for (STEntity e : edgesSource) {
        if (lines.get(lines.size() - 1).intersects(
            (ILineString) e.getGeometry().toGeoxGeometry())) {
          if (lines.get(lines.size() - 1).intersection(
              (ILineString) e.getGeometry().toGeoxGeometry()) instanceof GM_MultiPoint) {
            return false;
          }
        }
      }
    }
    // puis on projete les noeuds ST targets
    for (STEntity nodeTarget : nodesTarget) {
      // récupération de l'arc ST le plus proche
      STEntity edgeClose = null;
      double dmin = Double.MAX_VALUE;
      for (STEntity e : edgesSource) {
        double d = e.getGeometry().toGeoxGeometry()
            .distance(nodeTarget.getGeometry().toGeoxGeometry());
        if (d < dmin) {
          dmin = d;
          edgeClose = e;
        }
      }
      ILineString egeom = (ILineString) edgeClose.getGeometry()
          .toGeoxGeometry();
      IDirectPosition pProj = Operateurs.projection(nodeTarget.getGeometry()
          .toGeoxGeometry().coord().get(0), egeom);
      if (pProj.equals(egeom.getControlPoint(0), 0.5)
          || pProj.equals(
              egeom.getControlPoint(egeom.getControlPoint().size() - 1), 0.5)) {
        // la projection ne peut pas se faire
        continue;
      }
      IDirectPositionList l = new DirectPositionList();
      l.add(nodeTarget.getGeometry().toGeoxGeometry().coord().get(0));
      l.add(pProj);
      lines.add(new GM_LineString(l));
      for (STEntity e : edgesTarget) {
        if (lines.get(lines.size() - 1).intersects(
            (ILineString) e.getGeometry().toGeoxGeometry())) {
          if (lines.get(lines.size() - 1).intersection(
              (ILineString) e.getGeometry().toGeoxGeometry()) instanceof GM_MultiPoint) {
            return false;
          }
        }
      }
    }
    if (lines.isEmpty()) {
      return false;
    }
    if (lines.size() == 1) {
      return true;
    }
    // on regarde si les segment [point à projeter, point projeté] ne
    // s'intersectent pas entre eux
    return isPlanar(lines);
  }

  private static boolean isPlanar(List<ILineString> lines) {
    for (int i = 0; i < lines.size() - 1; i++) {
      if (lines.get(i).intersects(lines.get(i + 1))) {
        return false;
      }
    }
    return true;
  }
}
