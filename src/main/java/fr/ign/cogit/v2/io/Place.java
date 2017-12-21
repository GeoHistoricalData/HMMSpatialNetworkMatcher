package fr.ign.cogit.v2.io;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.type.GF_AttributeType;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPositionList;
import fr.ign.cogit.geoxygene.api.spatial.geomroot.IGeometry;
import fr.ign.cogit.geoxygene.contrib.cartetopo.Arc;
import fr.ign.cogit.geoxygene.contrib.cartetopo.CarteTopo;
import fr.ign.cogit.geoxygene.contrib.cartetopo.Face;
import fr.ign.cogit.geoxygene.contrib.cartetopo.Noeud;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPosition;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPositionList;
import fr.ign.cogit.geoxygene.spatial.coordgeom.GM_LineString;
import fr.ign.cogit.geoxygene.spatial.coordgeom.GM_Polygon;

public class Place {

  /**
   * Noeuds internes de la place
   */
  private Set<Noeud> nodes;
  /**
   * Arcs internes de la place
   */
  private Set<Arc> edges;
  /**
   * Arcs incidents de la place
   */
  private Set<Arc> edgesP;

  public Place(Set<Noeud> nodes, Set<Arc> edges, Set<Arc> edgesP) {
    this.nodes = nodes;
    this.setEdges(edges);
    this.edgesP = edgesP;
  }

  /**
   * Détections des places dans un graphe, utilisant l'indice de Miller pour
   * sortir les faces circulaires du réseau, et de la sémantique pour les autres
   * places si elle est renseignée
   * @param topo mapping entre le noeud fusionné représentant la place, et les
   *          entités la consituant initialement (arcs et noeuds)
   * @return
   */
  public static Map<Noeud, Place> placesDetection(CarteTopo topo) {
    Map<Noeud, Place> places = new HashMap<Noeud, Place>();
    topo.creeTopologieFaces();

    List<Face> faces = new ArrayList<Face>();

    for (Face f : topo.getListeFaces()) {
      // 1 - détection des faces circulaires
      IDirectPositionList list = new DirectPositionList();
      list.addAll(f.getGeom().coord());
      GM_LineString ls = new GM_LineString(list);
      GM_Polygon poly = new GM_Polygon(ls);
      // calcul de l'indice de Miller de la face topo
      double miller = 4. * Math.PI * f.surface()
          / (poly.length() * poly.length());
      if (miller > 0.9) {
        faces.add(f);
      }
      // 2 - tentative de détection par la sémantique
      else {
        Set<Arc> edges = new HashSet<Arc>();
        edges.addAll(f.getArcsDirects());
        edges.addAll(f.getArcsIndirects());
        boolean isPlace = false;
        for (Arc a : edges) {
          boolean isLocalPlace = false;
          // récupération du feature correspondant
          IFeature fC = a.getCorrespondant(0);
          if (fC != null) {
            for (GF_AttributeType att : fC.getFeatureType()
                .getFeatureAttributes()) {
              // est-ce une place
              String attN = fC.getAttribute(att).toString();
              if (attN.toLowerCase().startsWith("place")
                  || attN.toLowerCase().equals("pl")) {
                isLocalPlace = true;
                break;
              }

            }
            if (!isLocalPlace) {
              isPlace = false;
              break;
            } else {
              isPlace = true;
            }
          }
        }
        if (isPlace) {
          faces.add(f);
        }
      }
    }

    // regroupement des faces en contact
    List<List<Face>> facesConnected = new ArrayList<List<Face>>();
    for (Face f : faces) {
      List<Face> newlf = new ArrayList<Face>();
      newlf.add(f);
      for (List<Face> lf : new ArrayList<List<Face>>(facesConnected)) {
        boolean added = false;
        for (Face ff : lf) {
          if (f.getGeom().touches(ff.getGeom())) {
            newlf.addAll(lf);
            added = true;
            break;
          }
        }
        if (added) {
          facesConnected.remove(lf);
        }
      }

      facesConnected.add(newlf);

    }

    for (List<Face> lf : facesConnected) {
      if (lf.size() == 1) {
        Face f = lf.get(0);
        // une seule face
        // création de l'objet place
        Set<Arc> edges = new HashSet<Arc>();
        // les arcs internes
        edges.addAll(f.getArcsDirects());
        edges.addAll(f.getArcsIndirects());
        // les noeuds internes
        Set<Noeud> nodes = new HashSet<Noeud>();
        for (Arc a : edges) {
          nodes.add(a.getNoeudIni());
          nodes.add(a.getNoeudFin());
        }
        // les arcs incidents
        Set<Arc> edgesP = new HashSet<Arc>();
        for (Arc a : topo.getListeArcs()) {
          if (nodes.contains(a.getNoeudIni())
              && !nodes.contains(a.getNoeudFin())
              || !nodes.contains(a.getNoeudIni())
              && nodes.contains(a.getNoeudFin())) {
            edgesP.add(a);
          }
        }
        // instanciation de la nouvelle place
        Place p = new Place(nodes, edges, edgesP);
        // fusion des objets de la place en un noeud
        Noeud nP = Place.mergePlace(topo, p, f);
        places.put(nP, p);
      } else {
        // plusieurs faces
        Set<Arc> edges = new HashSet<Arc>();
        Set<Noeud> nodes = new HashSet<Noeud>();
        Set<Arc> edgesP = new HashSet<Arc>();
        for (Face f : lf) {
          edges.addAll(f.getArcsDirects());
          edges.addAll(f.getArcsIndirects());
        }
        for (Arc a : edges) {
          nodes.add(a.getNoeudIni());
          nodes.add(a.getNoeudFin());
        }
        for (Arc a : topo.getListeArcs()) {
          if (nodes.contains(a.getNoeudIni())
              && !nodes.contains(a.getNoeudFin())
              || !nodes.contains(a.getNoeudIni())
              && nodes.contains(a.getNoeudFin())) {
            edgesP.add(a);
          }
        }
        // instanciation de la nouvelle place
        Place p = new Place(nodes, edges, edgesP);
        // fusion des objets de la place en un noeud
        Noeud nP = Place.mergeMultiplePlace(topo, p, lf);
        places.put(nP, p);
      }
    }
    return places;
  }

  /**
   * Fusionne en un nouveau noeud (centroid) les entités consituants une place
   * multiple (arcs et noeuds)
   * @param topo
   * @param p
   * @return
   */
  private static Noeud mergeMultiplePlace(CarteTopo topo, Place p, List<Face> lf) {
    // récupération des noeuds des edgesP qui sont sur la place
    Set<Noeud> nodesP = new HashSet<Noeud>();
    for (Arc a : p.getEdgesP()) {
      if (p.getNodes().contains(a.getNoeudIni())) {
        nodesP.add(a.getNoeudIni());
      } else {
        nodesP.add(a.getNoeudFin());
      }
    }
    // le point de fusion
    double x = 0, y = 0;
    for (Noeud n : nodesP) {
      x += n.getCoord().getX();
      y += n.getCoord().getY();
    }
    x /= (double) p.getNodes().size();
    y /= (double) p.getNodes().size();
    // union des faces
    IGeometry union = new GM_Polygon();
    for (Face f : lf) {
      union = union.union(f.getGeom());
    }
    IDirectPosition pos = new DirectPosition(union.centroid().getX(), union
        .centroid().getY());
    Noeud n = new Noeud(pos);
    for (Noeud nn : p.getNodes()) {
      n.addAllCorrespondants(nn.getCorrespondants());
    }
    for (Arc a : p.getEdges()) {
      n.addAllCorrespondants(a.getCorrespondants());
    }
    topo.addNoeud(n);
    // modification des extrémités des arcs incidents de la face
    /*
     * for (Arc arc : p.getEdgesP()) { if
     * (p.getNodes().contains(arc.getNoeudIni())) { arc.setNoeudIni(n);
     * ILineString l = arc.getGeometrie(); l.removeControlPoint(0);
     * l.addControlPoint(0, n.getCoord()); // on rééchantillone l =
     * Operateurs.echantillone(l, Math.min(l.length() / 2., 1.));
     * arc.setGeom(l); } else { arc.setNoeudFin(n); ILineString l =
     * arc.getGeometrie(); l.removeControlPoint(l.coord().size() - 1);
     * l.addControlPoint(n.getCoord()); l = Operateurs.echantillone(l,
     * Math.min(l.length() / 2., 1.)); arc.setGeom(l); } }
     */
    for (Noeud node : nodesP) {
      Arc newArc = topo.getPopArcs().nouvelElement();
      newArc.setNoeudIni(node);
      newArc.setNoeudFin(n);
      // l'arc initial
      Arc a = null;
      for (Arc aa : p.getEdges()) {
        if (aa.getNoeudIni().equals(node) || aa.getNoeudFin().equals(node)) {
          a = aa;
          break;
        }
      }
      newArc.addAllCorrespondants(a.getCorrespondants());
      IDirectPositionList l = new DirectPositionList();
      l.add(node.getCoord());
      l.add(n.getCoord());
      newArc.setGeom(new GM_LineString(l));
    }

    Set<Noeud> nnn = new HashSet<Noeud>(p.getNodes());
    nnn.removeAll(nodesP);

    // suppresion des arcs et des noeuds internes de la place
    topo.enleveArcs(p.getEdges());
    topo.enleveNoeuds(nnn);
    return n;
  }

  /**
   * Fusionne en un nouveau noeud (centroid) les entités consituants une place
   * (arcs et noeuds)
   * @param topo
   * @param p
   * @return
   */
  private static Noeud mergePlace(CarteTopo topo, Place p, Face f) {
    // création d'un nouveau noeud, centroid des noeuds internes de la face
    double x = 0, y = 0;
    for (Noeud n : p.getNodes()) {
      x += n.getCoord().getX();
      y += n.getCoord().getY();
    }
    x /= (double) p.getNodes().size();
    y /= (double) p.getNodes().size();
    IDirectPosition pos = new DirectPosition(f.getGeom().centroid().getX(), f
        .getGeom().centroid().getY());
    Noeud n = new Noeud(pos);
    for (Noeud nn : p.getNodes()) {
      n.addAllCorrespondants(nn.getCorrespondants());
    }
    for (Arc a : p.getEdges()) {
      n.addAllCorrespondants(a.getCorrespondants());
    }
    topo.addNoeud(n);
    // modification des extrémités des arcs incidents de la face
    /*
     * for (Arc arc : p.getEdgesP()) { Arc newArc =
     * topo.getPopArcs().nouvelElement(); if
     * (p.getNodes().contains(arc.getNoeudIni())) {
     * newArc.setNoeudIni(arc.getNoeudIni()); newArc.setNoeudFin(n);
     * IDirectPositionList l = new DirectPositionList();
     * l.add(arc.getNoeudIni().getCoord()); l.add(n.getCoord());
     * newArc.setGeom(new GM_LineString(l)); } else {
     * newArc.setNoeudIni(arc.getNoeudFin()); newArc.setNoeudFin(n);
     * IDirectPositionList l = new DirectPositionList();
     * l.add(arc.getNoeudFin().getCoord()); l.add(n.getCoord());
     * newArc.setGeom(new GM_LineString(l)); } p.getEdges().add(newArc); }
     */

    for (Noeud node : p.getNodes()) {
      Arc newArc = topo.getPopArcs().nouvelElement();
      newArc.setNoeudIni(node);
      newArc.setNoeudFin(n);
      // l'arc initial
      Arc a = null;
      for (Arc aa : p.getEdges()) {
        if (aa.getNoeudIni().equals(node) || aa.getNoeudFin().equals(node)) {
          a = aa;
          break;
        }
      }
      newArc.addAllCorrespondants(a.getCorrespondants());
      IDirectPositionList l = new DirectPositionList();
      l.add(node.getCoord());
      l.add(n.getCoord());
      newArc.setGeom(new GM_LineString(l));
    }

    // suppresion des arcs et des noeuds internes de la place
    topo.enleveArcs(p.getEdges());
    // topo.enleveNoeuds(p.getNodes());
    return n;
  }

  // *****************************************************************************
  // ***************************** Accesseurs
  // ************************************
  // *****************************************************************************

  public Set<Noeud> getNodes() {
    return nodes;
  }

  public void setNodes(Set<Noeud> nodes) {
    this.nodes = nodes;
  }

  public void setEdges(Set<Arc> edges) {
    this.edges = edges;
  }

  public Set<Arc> getEdges() {
    return edges;
  }

  public Set<Arc> getEdgesP() {
    return edgesP;
  }

  public void setEdgesP(Set<Arc> edgesP) {
    this.edgesP = edgesP;
  }

  /*
   * public static void main(String args[]) { IPopulation<IFeature> pop =
   * ShapefileReader .read(
   * "/media/Data/Benoit/these/donnees/vecteur/filaires/filaires_corriges/verniquet_l93_utf8_corr.shp"
   * ); CarteTopo topo = new CarteTopo("void"); IPopulation<Arc> popA =
   * topo.getPopArcs(); for (IFeature f : pop) { Arc a = popA.nouvelElement();
   * a.addCorrespondant(f); a.setGeometrie(new
   * GM_LineString(f.getGeom().coord())); } topo.creeTopologieArcsNoeuds(0.1);
   * topo.rendPlanaire(0.1); topo.fusionNoeuds(0.1); topo.filtreArcsDoublons();
   * topo.filtreNoeudsIsoles(); topo.filtreDoublons(0.1);
   * topo.creeTopologieArcsNoeuds(0.1);
   * 
   * List<String> att = new ArrayList<String>(); att.add("TYPE_VOIE");
   * Map<Noeud, Place> places = Place.placesDetection(topo, att);
   * System.out.println(places.size()); ShapefileWriter.write(topo.getPopArcs(),
   * "/home/bcostes/Bureau/tmp/places/test.shp");
   * 
   * 
   * }
   */
}
