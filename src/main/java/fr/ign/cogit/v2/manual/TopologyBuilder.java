package fr.ign.cogit.v2.manual;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IFeatureCollection;
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.api.feature.type.GF_AttributeType;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPositionList;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ILineString;
import fr.ign.cogit.geoxygene.api.spatial.geomroot.IGeometry;
import fr.ign.cogit.geoxygene.contrib.I18N;
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
import fr.ign.cogit.geoxygene.util.attribute.AttributeManager;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileReader;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileWriter;
import fr.ign.cogit.geoxygene.util.index.Tiling;
import fr.ign.cogit.v2.io.Place;

public class TopologyBuilder {

  public static ReseauApp buildTopology(IFeatureCollection<IFeature> arcs,
      ParametresApp params, boolean detectPlaces) {
    CarteTopo topo = null;

    topo = new CarteTopo("void");
    IPopulation<Arc> popArcs = topo.getPopArcs();
    for (IFeature feature : arcs) {
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
    // on crée la topologie pour avoir les noeuds
    topo.creeTopologieArcsNoeuds(0.1);
    topo.creeNoeudsManquants(0.1);
    topo.filtreDoublons(0.1);
    topo.filtreArcsDoublons();
    topo.rendPlanaire(0.1);
    topo.filtreNoeudsSimples();
    topo.filtreDoublons(0.1);
    topo.filtreArcsDoublons();
    topo.filtreNoeudsIsoles();

    // détection des places
    if (detectPlaces) {
      Map<Noeud, Place> placesDetected = Place.placesDetection(topo);
      for (Noeud n : placesDetected.keySet()) {
        List<IGeometry> l = new ArrayList<IGeometry>();
        for (Noeud nn : placesDetected.get(n).getNodes()) {
          l.add(new GM_Point(nn.getCoord()));
        }
        for (Arc a : placesDetected.get(n).getEdges()) {
          l.add(new GM_LineString(a.getCoord()));
        }
        // places.put(new GM_Point(n.getCoord()), l);
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

    IFeatureCollection<IFeature> pE = new Population<IFeature>();
    pE.addAll(gmax.getListeArcs());
    IFeatureCollection<IFeature> pN = new Population<IFeature>();
    pN.addAll(gmax.getListeNoeuds());

    ReseauApp reseau = null;
    reseau = new ReseauApp(I18N.getString("AppariementIO.ReferenceNetwork")); //$NON-NLS-1$

    IPopulation<? extends IFeature> popArcApp = reseau.getPopArcs();
    IPopulation<? extends IFeature> popNoeudApp = reseau.getPopNoeuds();
    // /////////////////////////
    // import des arcs
    // import d'une population d'arcs
    for (IFeature element : pE) {
      ArcApp arc = (ArcApp) popArcApp.nouvelElement();
      ILineString ligne = new GM_LineString((IDirectPositionList) element
          .getGeom().coord().clone());
      arc.setGeometrie(ligne);
      arc.setOrientation(2);
      arc.addAllCorrespondants(element.getCorrespondants());
    }

    // import des noeuds
    // import d'une population de noeuds
    for (IFeature element : pN) {
      NoeudApp noeud = (NoeudApp) popNoeudApp.nouvelElement();
      // noeud.setGeometrie((GM_Point)element.getGeom());
      noeud.setGeometrie(new GM_Point((IDirectPosition) ((GM_Point) element
          .getGeom()).getPosition().clone()));
      noeud.addAllCorrespondants(element.getCorrespondants());
      noeud.setTaille(params.distanceNoeudsMax);
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
    reseau.creeTopologieFaces();

    // 7 - On double la taille de recherche pour les impasses
    if (params.distanceNoeudsImpassesMax >= 0) {
      Iterator<?> itNoeuds = reseau.getPopNoeuds().getElements().iterator();
      while (itNoeuds.hasNext()) {
        NoeudApp noeud2 = (NoeudApp) itNoeuds.next();
        if (noeud2.arcs().size() == 1) {
          noeud2.setTaille(params.distanceNoeudsImpassesMax);
        }
      }
    }
    topo.nettoyer();
    gs.clear();
    return reseau;
  }

  public static void exportTopology(ReseauApp res, String shpArcs,
      String shpNoeuds) {
    IFeatureCollection<IFeature> popArc = new Population<IFeature>();
    for (Arc arc : res.getListeArcs()) {
      if (arc.getCorrespondants().size() > 1) {
        boolean sameAtt = true;
        for (IFeature ff : arc.getCorrespondants()) {
          for (IFeature fff : arc.getCorrespondants()) {
            if (ff.equals(fff)) {
              continue;
            }
            for (GF_AttributeType att : ff.getFeatureType()
                .getFeatureAttributes()) {
              if (!ff.getAttribute(att).toString()
                  .equals(fff.getAttribute(att).toString())
                  && !ff.getAttribute(att).toString().equals("")
                  && !fff.getAttribute(att).toString().equals("")) {
                sameAtt = false;
                break;
              }
            }
            if (!sameAtt) {
              break;
            }
          }
          if (!sameAtt) {
            break;
          }
        }
        if (sameAtt) {
          IFeature f = new DefaultFeature(arc.getGeom());
          int indexF = 0;
          int nbAttNull = 0;
          for (GF_AttributeType att : arc.getCorrespondant(0).getFeatureType()
              .getFeatureAttributes()) {
            if (arc.getCorrespondant(0).getAttribute(att).toString().equals("")) {
              nbAttNull++;
            }
          }
          for (int i = 1; i < arc.getCorrespondants().size(); i++) {
            int nbAttNull2 = 0;
            for (GF_AttributeType att : arc.getCorrespondant(i)
                .getFeatureType().getFeatureAttributes()) {
              if (arc.getCorrespondant(i).getAttribute(att).toString()
                  .equals("")) {
                nbAttNull2++;
              }
            }
            if (nbAttNull2 < nbAttNull) {
              nbAttNull = nbAttNull2;
              indexF = i;
            }
          }
          IFeature cor = arc.getCorrespondant(indexF);
          for (GF_AttributeType att : cor.getFeatureType()
              .getFeatureAttributes()) {
            AttributeManager.addAttribute(f, att.getMemberName(), cor
                .getAttribute(att).toString(), att.getValueType());
          }
          popArc.add(f);
        } else {
          popArc.addAll(arc.getCorrespondants());
        }
      } else {
        IFeature f = new DefaultFeature(arc.getGeom());
        IFeature cor = arc.getCorrespondant(0);
        for (GF_AttributeType att : cor.getFeatureType().getFeatureAttributes()) {
          AttributeManager.addAttribute(f, att.getMemberName(), cor
              .getAttribute(att).toString(), att.getValueType());
        }
        popArc.add(f);
      }

    }
    ShapefileWriter.write(popArc, shpArcs);
    IFeatureCollection<IFeature> popNoeuds = new Population<IFeature>();
    for (Noeud n : res.getListeNoeuds()) {
      IFeature f = new DefaultFeature(n.getGeom());
      popNoeuds.add(f);
    }
    ShapefileWriter.write(popNoeuds, shpNoeuds);
  }

  public static void main(String args[]) {
    ParametresApp params = new ParametresApp();
    params.debugBilanSurObjetsGeo = true;
    // fait buguer l'algo car rond points déja traités en amont
    params.varianteChercheRondsPoints = false;
    params.debugTirets = false;
    params.distanceArcsMax = 15;
    params.distanceArcsMin = 5;
    params.distanceNoeudsMax = 15;
    params.distanceNoeudsImpassesMax = 15;
    params.varianteRedecoupageNoeudsNonApparies = true;
    // this.params.varianteForceAppariementSimple = true;
    params.varianteRedecoupageNoeudsNonApparies_DistanceNoeudArc = 15;
    params.varianteRedecoupageNoeudsNonApparies_DistanceProjectionNoeud = 5;

    Map<IGeometry, List<IGeometry>> places = new HashMap<IGeometry, List<IGeometry>>();

    IPopulation<IFeature> popIn = ShapefileReader
        .read("/media/Data/Benoit/these/donnees/vecteur/filaires/filaires_corriges/simplifications/vasserot_jacoubet_l93_utf8.shp");
    ReseauApp res = TopologyBuilder.buildTopology(popIn, params, true);

    TopologyBuilder.exportTopology(res,
        "/home/bcostes/Bureau/tmp/test/vasserot_jacoubet_arcs.shp",
        "/home/bcostes/Bureau/tmp/test/vasserot_jacoubet_noeuds.shp");
  }

}
