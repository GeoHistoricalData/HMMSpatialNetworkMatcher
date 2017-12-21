package fr.ign.cogit.v2.mergeProcess.mergeUtility;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ILineString;
import fr.ign.cogit.geoxygene.contrib.geometrie.Operateurs;
import fr.ign.cogit.v2.geometry.LightDirectPosition;
import fr.ign.cogit.v2.geometry.LightLineString;
import fr.ign.cogit.v2.geometry.LightMultipleGeometry;
import fr.ign.cogit.v2.tag.FuzzyTemporalInterval;
import fr.ign.cogit.v2.tag.STEntity;
import fr.ign.cogit.v2.tag.STGraph;
import fr.ign.cogit.v2.tag.STProperty;
import fr.ign.cogit.v2.utils.JungUtils;

public class MergeUtils {

  /**
   * Modification de node1 par node2
   * @param node1
   * @param node2
   */
  public static void mergeNodes(STGraph stgraph, STEntity node1, STEntity node2){

    for (FuzzyTemporalInterval t2 : node2.getTimeSerie().getValues().keySet()) {
      // existence
      if (node2.existsAt(t2)) {
        node1.existsAt(t2, true);
      }
      // géométries
      if (node2.getGeometryAt(t2) != null) {
        node1.setGeometryAt(t2, node2.getGeometryAt(t2));
      } 
      // poids
      /*if (node2.getWeightAt(t2) != null) {
                node1.setWeightAt(t2, node2.getWeightAt(t2));
            }*/
      // attributs
      /*for(STProperty<String> att: node2.getTAttributes()){
                if(att.getValueAt(t2) != null){
                    node1.getTAttributeByName(att.getName()).setValueAt(t2, att.getValueAt(t2));
                }
            }*/
      // indicateurs
      for(STProperty<Double> ind: node2.getTIndicators()){
        if(node1.getTIndicatorByName(ind.getName()) == null){
          node1.getTIndicators().add(new STProperty<Double>(STProperty.PROPERTIE_FINAL_TYPES.Indicator, ind.getName()));
        }
        if(ind.getValueAt(t2) != null){
          node1.getTIndicatorByName(ind.getName()).setValueAt(t2, ind.getValueAt(t2));
        }
      }
    }
    // mise à jour du graphe jung
    JungUtils<STEntity, STEntity> JU = new JungUtils<STEntity, STEntity>();

    JU.replaceNode(stgraph, node2, node1);

    //mise à jour de la géométrie
    node1.updateGeometry(stgraph);
  }

  /**
   * Modification de node et suppression du groupe
   * @param stGraph
   * @param node
   * @param group
   */
  public static void mergeNodes(STGraph stGraph, STEntity node,
      Set<STEntity> group) {
    Set<FuzzyTemporalInterval> times = new HashSet<FuzzyTemporalInterval>();
    for (STEntity e : group) {
      for (FuzzyTemporalInterval t : e.getTimeSerie().getValues().keySet()) {
        if (e.existsAt(t)) {
          times.add(t);
        }
      }
    }

    
    // si un des noeud fusionnés est fictif à une date t, ils ne l'est plus
    Set<FuzzyTemporalInterval> fictivesDates = new HashSet<FuzzyTemporalInterval>();
     for(FuzzyTemporalInterval t : group.iterator().next().getTimeSerie().getValues().keySet()){
       for(STEntity e : group){
         if(e.getType() == STEntity.NODE){
           if(!e.existsAt(t) && e.getGeometryAt(t) != null){
             fictivesDates.add(t);
             break;
           }
         }
       }
     }
    // times.addAll(fictivesDates);

    for (FuzzyTemporalInterval t2 : times) {

      
      // existence
      node.existsAt(t2, true);

      // géométries
      List<LightLineString> l1 = new ArrayList<LightLineString>();
      List<LightDirectPosition> l2 = new ArrayList<LightDirectPosition>();
      for (STEntity nodej : group) {
        if (nodej.getGeometryAt(t2) != null) {
          if (nodej.getType() == STEntity.NODE) {
            if (nodej.getGeometryAt(t2) instanceof LightDirectPosition) {
              l2.add((LightDirectPosition) nodej.getGeometryAt(t2));
            } else if (nodej.getGeometryAt(t2) instanceof LightMultipleGeometry) {
              l2.addAll(((LightMultipleGeometry) nodej.getGeometryAt(t2))
                  .getLightDirectPosition());
              l1.addAll(((LightMultipleGeometry) nodej.getGeometryAt(t2))
                  .getLightLineString());
            }
          } else {
            l1.add((LightLineString) nodej.getGeometryAt(t2));
          }
        }
      }
      if (l1.size() == 0) {
        if (l2.size() == 1) {
          LightDirectPosition geom = new LightDirectPosition(l2.get(0)
              .toGeoxDirectPosition());
          node.setGeometryAt(t2, geom);
        } else {
          LightMultipleGeometry geom = new LightMultipleGeometry(l1, l2);
          node.setGeometryAt(t2, geom);
        }
      } else if (l2.size() == 0) {
        if (l1.size() == 1) {
          LightMultipleGeometry geom = new LightMultipleGeometry(l1, l2);
          node.setGeometryAt(t2, geom);
        } else {
        }

      } else {
        LightMultipleGeometry geom = new LightMultipleGeometry(l1, l2);
        node.setGeometryAt(t2, geom);
      }


      // indicateurs
      // c'est la loose a priori quand on fusionne un sommet et un groupe ...?
      // (comment condenser les indicateurs du groupe ... (il y a un arc parmis eux!)
      for (STEntity nodej : group) {
        if (nodej.getType() == STEntity.NODE) {
          for(STProperty<Double> ind: nodej.getTIndicators()){
            if(node.getTIndicatorByName(ind.getName()) == null){
              node.getTIndicators().add(new STProperty<Double>(STProperty.PROPERTIE_FINAL_TYPES.Indicator, ind.getName()));
            }
            node.getTIndicatorByName(ind.getName()).setValueAt(t2, null);
          }
          break;
        }
      }

    }

    // on va mettre à jour le graphe jung
    List<STEntity> nodes = new ArrayList<STEntity>();
    List<STEntity> edges = new ArrayList<STEntity>();
    for (STEntity entity : group) {
      if (entity.getType() == STEntity.NODE) {
        nodes.add(entity);
      } else {
        edges.add(entity);
      }
    }
    // mis à jour des noeuds
    JungUtils<STEntity, STEntity> JU = new JungUtils<STEntity, STEntity>();
    for (STEntity n : nodes) {
      // mise à jour du graphe jung
      JU.replaceNode(stGraph, n, node);
    }

    times = new HashSet<FuzzyTemporalInterval>();
    for (STEntity e : edges) {
      for (FuzzyTemporalInterval t : e.getTimeSerie().getValues().keySet()) {
        if (e.existsAt(t)) {
          times.add(t);
        }
      }
    }
    
    
   // times.addAll(fictivesDates);
    
    for (FuzzyTemporalInterval tj : times) { // il faut modifier les coordonnées
      // des extrémités des lines string // connectées à newNode à la date tj
      IDirectPosition p = node.getGeometryAt(tj).toGeoxGeometry().coord()
          .get(0);

      // les arcs connextés à newnode à t2
      List<IDirectPosition> pnodes = new ArrayList<IDirectPosition>();

      for (STEntity n : nodes) {
        if(n.getGeometryAt(tj) == null){
          System.out.println(n.getGeometry().toGeoxGeometry());
        }
        pnodes.add(n.getGeometryAt(tj).toGeoxGeometry().coord().get(0));
      }
      for (STEntity e : stGraph.getIncidentEdges(node)) {
        if (e.existsAt(tj)) { // quelle extrémité modifier ?
          ILineString l = ((LightLineString) e.getGeometryAt(tj))
              .toGeoxGeometry();
          // si deux points seulement on rééchantillone
          if (l.coord().size() == 2) {
            l = Operateurs.echantillone(l, l.length() / 3.);
          }

          if (pnodes.contains(l.getControlPoint(0))) {
            l.setControlPoint(0, p);
          } else {
            l.setControlPoint(l.coord().size() - 1, p);
          }
          e.getTGeometry().setValueAt(tj, new LightLineString(l.coord()));
        }
      }
    }
    


    // suppression des arcs
    for (STEntity edge : edges) {
      stGraph.removeEdge(edge);
    }

  }

  /**
   * Modification de node et suppression du groupe
   * @param stGraph
   * @param node
   * @param group
   */
  public static void mergeNodes(STGraph stGraph, Set<STEntity> group1,
      Set<STEntity> group2) {
    STEntity.setCurrentType(STEntity.NODE);
    Set<FuzzyTemporalInterval> times1 = new HashSet<FuzzyTemporalInterval>();
    Set<FuzzyTemporalInterval> times2 = new HashSet<FuzzyTemporalInterval>();
    for (STEntity e : group1) {
      for (FuzzyTemporalInterval t : e.getTimeSerie().getValues().keySet()) {
        if (e.existsAt(t)) {
          times1.add(t);
        }
      }
    }
    for (STEntity e : group2) {
      for (FuzzyTemporalInterval t : e.getTimeSerie().getValues().keySet()) {
        if (e.existsAt(t)) {
          times2.add(t);
        }
      }
    }
    STEntity newNode = new STEntity(group1.iterator().next().getTimeSerie());
    for (FuzzyTemporalInterval t2 : times1) {
      // existence
      newNode.existsAt(t2, true);

      // géométries
      List<LightLineString> l1 = new ArrayList<LightLineString>();
      List<LightDirectPosition> l2 = new ArrayList<LightDirectPosition>();
      for (STEntity nodej : group1) {
        if (nodej.getGeometryAt(t2) != null) {
          if (nodej.getType() == STEntity.NODE) {
            if (nodej.getGeometryAt(t2) instanceof LightDirectPosition) {
              l2.add((LightDirectPosition) nodej.getGeometryAt(t2));
            } else if (nodej.getGeometryAt(t2) instanceof LightMultipleGeometry) {
              l2.addAll(((LightMultipleGeometry) nodej.getGeometryAt(t2))
                  .getLightDirectPosition());
              l1.addAll(((LightMultipleGeometry) nodej.getGeometryAt(t2))
                  .getLightLineString());
            }
          } else {
            l1.add((LightLineString) nodej.getGeometryAt(t2));
          }
        }
      }
      if (l1.size() == 0) {
        if (l2.size() == 1) {
          LightDirectPosition geom = new LightDirectPosition(l2.get(0)
              .toGeoxDirectPosition());
          newNode.setGeometryAt(t2, geom);
        } else {
          LightMultipleGeometry geom = new LightMultipleGeometry(l1, l2);
          newNode.setGeometryAt(t2, geom);
        }
      } else if (l2.size() == 0) {
        if (l1.size() == 1) {
          LightMultipleGeometry geom = new LightMultipleGeometry(l1, l2);
          newNode.setGeometryAt(t2, geom);
        } else {
        }

      } else {
        LightMultipleGeometry geom = new LightMultipleGeometry(l1, l2);
        newNode.setGeometryAt(t2, geom);
      }

      // indicateurs
      // c'est la loose a priori quand on fusionne un sommet et un groupe ...?
      // (comment condenser les indicateurs du groupe ... (il y a un arc parmis eux!)
      for (STEntity nodej : group1) {
        if (nodej.getType() == STEntity.NODE) {
          for(STProperty<Double> ind: nodej.getTIndicators()){
            if(newNode.getTIndicatorByName(ind.getName()) == null){
              newNode.getTIndicators().add(new STProperty<Double>(STProperty.PROPERTIE_FINAL_TYPES.Indicator, ind.getName()));
            }
            newNode.getTIndicatorByName(ind.getName()).setValueAt(t2, null);
          }
          break;
        }
      }
     
    }
    // on va mettre à jour le graphe jung
    List<STEntity> nodes = new ArrayList<STEntity>();
    List<STEntity> edges = new ArrayList<STEntity>();
    for (STEntity entity : group1) {
      if (entity.getType() == STEntity.NODE) {
        nodes.add(entity);
      } else {
        edges.add(entity);
      }
    }
    // mis à jour des noeuds
    JungUtils<STEntity, STEntity> JU = new JungUtils<STEntity, STEntity>();
    for (STEntity n : nodes) {
      // mise à jour du graphe jung
      JU.replaceNode(stGraph, n, newNode);
    }

    Set<FuzzyTemporalInterval> times = new HashSet<FuzzyTemporalInterval>();
    for (STEntity e : edges) {
      for (FuzzyTemporalInterval t : e.getTimeSerie().getValues().keySet()) {
        if (e.existsAt(t)) {
          times.add(t);
        }
      }
    }

    for (FuzzyTemporalInterval tj : times) { // il faut modifier les coordonnées
      // des extrémités des lines string // connectées à newNode à la date tj
      IDirectPosition p = newNode.getGeometryAt(tj).toGeoxGeometry().coord()
          .get(0);

      // les arcs connextés à newnode à t2
      List<IDirectPosition> pnodes = new ArrayList<IDirectPosition>();

      for (STEntity n : nodes) {
        pnodes.add(n.getGeometryAt(tj).toGeoxGeometry().coord().get(0));
      }
      for (STEntity e : stGraph.getIncidentEdges(newNode)) {
        if (e.existsAt(tj)) { // quelle extrémité modifier ?
            ILineString l = ((LightLineString) e.getGeometryAt(tj))
                .toGeoxGeometry();
            // si deux points seulement on rééchantillone
            if (l.coord().size() == 2) {
              l = Operateurs.echantillone(l, l.length() / 3.);
            }

            if (pnodes.contains(l.getControlPoint(0))) {
              l.setControlPoint(0, p);
            } else {
              l.setControlPoint(l.coord().size() - 1, p);
            }
            e.getTGeometry().setValueAt(tj, new LightLineString(l.coord()));
        }
      }
    }

    // suppression des arcs
    for (STEntity edge : edges) {
      stGraph.removeEdge(edge);
    }
    for (FuzzyTemporalInterval t2 : times2) {
      // existence
      newNode.existsAt(t2, true);

      // géométries
      List<LightLineString> l1 = new ArrayList<LightLineString>();
      List<LightDirectPosition> l2 = new ArrayList<LightDirectPosition>();
      for (STEntity nodej : group2) {
        if (nodej.getGeometryAt(t2) != null) {
          if (nodej.getType() == STEntity.NODE) {
            if (nodej.getGeometryAt(t2) instanceof LightDirectPosition) {
              l2.add((LightDirectPosition) nodej.getGeometryAt(t2));
            } else if (nodej.getGeometryAt(t2) instanceof LightMultipleGeometry) {
              l2.addAll(((LightMultipleGeometry) nodej.getGeometryAt(t2))
                  .getLightDirectPosition());
              l1.addAll(((LightMultipleGeometry) nodej.getGeometryAt(t2))
                  .getLightLineString());
            }
          } else {
            l1.add((LightLineString) nodej.getGeometryAt(t2));
          }
        }
      }
      if (l1.size() == 0) {
        if (l2.size() == 1) {
          LightDirectPosition geom = new LightDirectPosition(l2.get(0)
              .toGeoxDirectPosition());
          newNode.setGeometryAt(t2, geom);
        } else {
          LightMultipleGeometry geom = new LightMultipleGeometry(l1, l2);
          newNode.setGeometryAt(t2, geom);
        }
      } else if (l2.size() == 0) {
        if (l1.size() == 1) {
          LightMultipleGeometry geom = new LightMultipleGeometry(l1, l2);
          newNode.setGeometryAt(t2, geom);
        } else {
        }

      } else {
        LightMultipleGeometry geom = new LightMultipleGeometry(l1, l2);
        newNode.setGeometryAt(t2, geom);
      }

      // indicateurs
      // c'est la loose a priori quand on fusionne un sommet et un groupe ...?
      // (comment condenser les indicateurs du groupe ... (il y a un arc parmis eux!)
      for (STEntity nodej : group2) {
        if (nodej.getType() == STEntity.NODE) {
          for(STProperty<Double> ind: nodej.getTIndicators()){
            if(newNode.getTIndicatorByName(ind.getName()) == null){
              newNode.getTIndicators().add(new STProperty<Double>(STProperty.PROPERTIE_FINAL_TYPES.Indicator, ind.getName()));
            }
            newNode.getTIndicatorByName(ind.getName()).setValueAt(t2, null);
          }
          break;
        }
      }
      
    }
 // on va mettre à jour le graphe jung
     nodes = new ArrayList<STEntity>();
     edges = new ArrayList<STEntity>();
    for (STEntity entity : group2) {
      if (entity.getType() == STEntity.NODE) {
        nodes.add(entity);
      } else {
        edges.add(entity);
      }
    }
    // mis à jour des noeuds
     JU = new JungUtils<STEntity, STEntity>();
    for (STEntity n : nodes) {
      // mise à jour du graphe jung
      JU.replaceNode(stGraph, n, newNode);
    }

    times = new HashSet<FuzzyTemporalInterval>();
    for (STEntity e : edges) {
      for (FuzzyTemporalInterval t : e.getTimeSerie().getValues().keySet()) {
        if (e.existsAt(t)) {
          times.add(t);
        }
      }
    }

    for (FuzzyTemporalInterval tj : times) { // il faut modifier les coordonnées
      // des extrémités des lines string // connectées à newNode à la date tj
      IDirectPosition p = newNode.getGeometryAt(tj).toGeoxGeometry().coord()
          .get(0);

      // les arcs connextés à newnode à t2
      List<IDirectPosition> pnodes = new ArrayList<IDirectPosition>();

      for (STEntity n : nodes) {
        pnodes.add(n.getGeometryAt(tj).toGeoxGeometry().coord().get(0));
      }
      for (STEntity e : stGraph.getIncidentEdges(newNode)) {
        if (e.existsAt(tj)) { // quelle extrémité modifier ?
            ILineString l = ((LightLineString) e.getGeometryAt(tj))
                .toGeoxGeometry();
            // si deux points seulement on rééchantillone
            if (l.coord().size() == 2) {
              l = Operateurs.echantillone(l, l.length() / 3.);
            }

            if (pnodes.contains(l.getControlPoint(0))) {
              l.setControlPoint(0, p);
            } else {
              l.setControlPoint(l.coord().size() - 1, p);
            }
            e.getTGeometry().setValueAt(tj, new LightLineString(l.coord()));
        }
      }
    }

    // suppression des arcs
    for (STEntity edge : edges) {
      stGraph.removeEdge(edge);
    }
    newNode.updateGeometry(stGraph);
  }

  public static void mergeEdges(STGraph stgraph, STEntity edge1,
      STEntity edge2) {
    for (FuzzyTemporalInterval t2 : edge2.getTimeSerie().getValues().keySet()) {
      // existence
      if (edge2.existsAt(t2)) {
        edge1.existsAt(t2, true);
      }
      // géométries
      if (edge2.getGeometryAt(t2) != null) {
        edge1.setGeometryAt(t2, edge2.getGeometryAt(t2));
      } 
      // poids
      if (edge2.getWeightAt(t2) != null) {
        edge1.setWeightAt(t2, edge2.getWeightAt(t2));
      }
      // attributs
      for(STProperty<String> att: edge2.getTAttributes()){
        if(att.getValueAt(t2) != null){
          edge1.getTAttributeByName(att.getName()).setValueAt(t2, att.getValueAt(t2));
        }
      }
      // Transformations
      if (edge2.getTransformationAt(t2) != null) {
        edge1.setTransformationAt(t2, edge2.getTransformationAt(t2));
      }

      // indicateurs
      for(STProperty<Double> ind: edge2.getTIndicators()){
        if(edge1.getTIndicatorByName(ind.getName()) == null){
          edge1.getTIndicators().add(new STProperty<Double>(STProperty.PROPERTIE_FINAL_TYPES.Indicator, ind.getName()));
        }
        if(ind.getValueAt(t2) != null){
          edge1.getTIndicatorByName(ind.getName()).setValueAt(t2, ind.getValueAt(t2));
        }
      }
    }
    // mise à jour du graphe jung
    JungUtils<STEntity, STEntity> JU = new JungUtils<STEntity, STEntity>();

    JU.replaceEdge(stgraph, edge2, edge1);

    //mise à jour de la géométrie
    edge1.updateGeometry(stgraph);
  }

}
