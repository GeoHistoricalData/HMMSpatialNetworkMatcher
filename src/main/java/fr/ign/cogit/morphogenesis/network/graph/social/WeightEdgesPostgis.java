package fr.ign.cogit.morphogenesis.network.graph.social;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ILineString;
import fr.ign.cogit.geoxygene.api.spatial.geomaggr.IMultiCurve;
import fr.ign.cogit.geoxygene.contrib.cartetopo.Arc;
import fr.ign.cogit.geoxygene.contrib.cartetopo.CarteTopo;
import fr.ign.cogit.geoxygene.contrib.cartetopo.CarteTopoFactory;
import fr.ign.cogit.geoxygene.contrib.cartetopo.Noeud;
import fr.ign.cogit.geoxygene.contrib.geometrie.Operateurs;
import fr.ign.cogit.geoxygene.feature.DefaultFeature;
import fr.ign.cogit.geoxygene.feature.Population;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPosition;
import fr.ign.cogit.geoxygene.spatial.geomprim.GM_Point;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileReader;
import fr.ign.cogit.geoxygene.util.index.Tiling;
import fr.ign.cogit.morphogenesis.network.graph.io.GraphReader;

public class WeightEdgesPostgis {

  public static void main(String[] args) throws SQLException {

    String tablenameNoeuds = "geocode_minutier1851_individus_paris";
    String tablenameRelation = "geocode_minutier1851_liens_paris";

    String host = "127.0.0.1";
    String port = "5432";
    String sourceName = "these";
    String login = "postgres";
    String password = " ";
    Connection destDbConnection = null;

    SocialGraph g = GraphReader.createSocialGraphGeocoded(host, login,
        password, port, sourceName, tablenameNoeuds, tablenameRelation,
        "id_composante_connexe");

    IPopulation<IFeature> popVasserot = ShapefileReader
        .read("/media/Data/Benoit/these/donnees/vecteur/FILAIRES_L93_OK/1810_1836_VASSEROT_accentsOK_topologieOk.shp");
    IPopulation<IFeature> popPoubelle = ShapefileReader
        .read("/media/Data/Benoit/these/donnees/vecteur/FILAIRES_L93_OK/1885_ALPHAND_POUBELLE_emprise_topologieOk.shp");

    Map<Relation, Double> weightsVasserot = new HashMap<Relation, Double>();
    Map<Relation, Double> weightsPoubelle = new HashMap<Relation, Double>();

    IPopulation<IFeature> inputFeatureCollectionCorrected1 = new Population<IFeature>();
    // On simplifie les multi line string et on passe par des localFeature
    // pour conserver les attributs
    for (IFeature feat : popVasserot) {
      if (feat.getGeom() instanceof IMultiCurve) {
        for (int i = 0; i < ((IMultiCurve<?>) feat.getGeom()).size(); i++) {
          inputFeatureCollectionCorrected1.add(new DefaultFeature(
              ((IMultiCurve<?>) feat.getGeom()).get(i)));
        }
      } else {
        inputFeatureCollectionCorrected1
            .add(new DefaultFeature(feat.getGeom()));
      }
    }

    Map<Individu, IDirectPosition> mapNoeudVasserot = new HashMap<Individu, IDirectPosition>();

    inputFeatureCollectionCorrected1.initSpatialIndex(Tiling.class, true);

    System.out.println(g.getVertices().size());
    Map<IDirectPosition, IFeature> toProjete = new HashMap<IDirectPosition, IFeature>();
    for (Individu ind : g.getVertices()) {
      IDirectPosition pt = new DirectPosition(ind.getPosition().getX(), ind
          .getPosition().getY());

      IFeature minF = null;
      double distMin = Double.MAX_VALUE;

      for (IFeature f : inputFeatureCollectionCorrected1) {
        double d = f.getGeom().distance(new GM_Point(pt));
        if (d < distMin) {
          distMin = d;
          minF = f;
        }
      }

      if (minF == null) {
        System.err.println("Feature Projection null");
        System.exit(-1);
      }

      IDirectPosition ptInsert = Operateurs.projection(pt,
          (ILineString) minF.getGeom());
      if (ptInsert == null) {
        System.out.println("AAAAAAAA");
        System.exit(-1);
      }
      mapNoeudVasserot.put(ind, ptInsert);
      toProjete.put(pt, minF);

    }

    for (IDirectPosition p : toProjete.keySet()) {
      for (IFeature f : inputFeatureCollectionCorrected1) {
        if (f.getGeom().equals(toProjete.get(p).getGeom())) {
          f.setGeom(Operateurs.projectionEtInsertion(p, (ILineString) toProjete
              .get(p).getGeom()));

          break;
        }
      }

    }

    CarteTopo mapVasserot = CarteTopoFactory.newCarteTopo("",
        inputFeatureCollectionCorrected1);//$NON-NLS-1$
    for (Arc a : mapVasserot.getListeArcs()) {
      a.setOrientation(2);
      a.setPoids(a.longueur());
    }

    for (Relation r : g.getEdges()) {
      Individu i1 = r.getFirst();
      Individu i2 = r.getSecond();
      IDirectPosition pt1 = mapNoeudVasserot.get(i1);
      IDirectPosition pt2 = mapNoeudVasserot.get(i2);

      Noeud n1 = null, n2 = null;

      for (Noeud n : mapVasserot.getListeNoeuds()) {
        if (n.getCoord().distance(pt1) < 0.005) {
          n1 = n;
        }
        if (n.getCoord().distance(pt2) < 0.005) {
          n2 = n;
        }
        if (n1 != null && n2 != null) {
          break;
        }
      }

      if (n1 == null || n2 == null) {
        System.out.println("aie");
      } else {

        weightsVasserot.put(r, n1.plusCourtChemin(n2, 0).getLength());
      }

    }

    for (Relation r : g.getEdges()) {
      if (weightsVasserot.containsKey(r)) {
        System.out.println(weightsVasserot.get(r));
      }
    }
  }
}
