package fr.ign.cogit.morphogenesis.network.analysis.streetWidth;

import org.apache.log4j.Logger;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IFeatureCollection;
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPositionList;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ILineString;
import fr.ign.cogit.geoxygene.api.spatial.geomroot.IGeometry;
import fr.ign.cogit.geoxygene.contrib.geometrie.Operateurs;
import fr.ign.cogit.geoxygene.feature.Population;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPosition;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPositionList;
import fr.ign.cogit.geoxygene.spatial.coordgeom.GM_LineString;
import fr.ign.cogit.geoxygene.spatial.coordgeom.GM_Polygon;
import fr.ign.cogit.geoxygene.util.attribute.AttributeManager;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileReader;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileWriter;

/**
 * Méthode approximative pour calculer la largeur d'une rue étant donné le
 * parcellaire (ou les ilots) La largeur est calculée au milieu du tronçon : on
 * détermine le vecteur nutinaire orthogonal au segment en son milieu. On
 * construit alors successivement des vecteur dirigé par ce vecteur orthogonal
 * et de longueur variable jusqu'a ce que l'extrémité du vecteur construit
 * touche un îlot.
 * @author bcostes
 * 
 */
public class StreetWidth {

  /**
   * Distance maximale à l'îlot le plus proche pour affecter la largeur
   */
  private static final int DIST_MAX = 50;
  private static final double ACCURACY = 0.1;
  private static final Logger logger = Logger.getLogger(StreetWidth.class);

  public static void approximateWidth(String inputShpStreets,
      String inputShpBlocks, String outputShpStreet) {

    long t0 = System.currentTimeMillis();
    if (logger.isInfoEnabled()) {
      logger
          .info("--------------------------------------------------------------------------");
      logger.info("Starting street width approximation ... ");
      logger.info("Loading input street network ... ");
    }
    IPopulation<IFeature> inputStreets = ShapefileReader.read(inputShpStreets);

    if (logger.isInfoEnabled()) {
      logger.info("Loading input block network ... ");
    }
    IPopulation<IFeature> inputBlocks = ShapefileReader.read(inputShpBlocks);

    // union des polygones en un super polygone
    if (logger.isInfoEnabled()) {
      logger.info("Calculating blocks union ... ");
    }
    IGeometry blocksUnion = new GM_Polygon();
    for (IFeature block : inputBlocks) {
      blocksUnion = blocksUnion.union(block.getGeom());
    }
    int nb_streets = inputStreets.size();
    int cpt = 0;
    IFeatureCollection<IFeature> ouputstreets = new Population<IFeature>();

    for (IFeature street : inputStreets) {
      cpt++;
      if (logger.isInfoEnabled()) {
        logger.info("Calculating streets width ... ( " + cpt + " / "
            + nb_streets + " )");
      }
      if (street.getGeom().intersects(blocksUnion)) {
        if (logger.isInfoEnabled()) {
          logger
              .info("Street intersects blocks union ... not taken into account...");
          AttributeManager.addAttribute(street, "width", 0, "Double");
          ouputstreets.add(street);
          continue;
        }
      }
      // Détermination du milieu
      ILineString streetRegular = Operateurs.echantillone(new GM_LineString(
          street.getGeom().coord()), 1);

      if (streetRegular.coord().size() == 2) {
        if (logger.isInfoEnabled()) {
          logger.info("Very short street ... not taken into account...");
          AttributeManager.addAttribute(street, "width", 0, "Double");
          ouputstreets.add(street);
          continue;
        }
      }

      int index_middle = (int) streetRegular.coord().size() / 2;
      IDirectPosition middle = streetRegular.coord().get(index_middle);
      IDirectPosition beforeMiddle = streetRegular.coord()
          .get(index_middle - 1);
      // Détermination du v
      // si y = ax + b, alors u (a, -1) est normal à la droite
      double[] normalDirect = {
          (middle.getY() - beforeMiddle.getY())
              / (middle.getX() - beforeMiddle.getX()), -1. };
      // on le norme
      double norme = Math.sqrt(normalDirect[0] * normalDirect[0] + 1);
      normalDirect[0] /= norme;
      normalDirect[1] /= norme;
      double[] normalUndirect = { -normalDirect[0], -normalDirect[1] };
      // TODO : implémenter ici une dichotomie pour aller plus vite !
      // on cherche dans les deux sens et on prendra le max
      double valDirect = ACCURACY;
      IDirectPositionList list = new DirectPositionList();
      list.add(middle);
      // le vecteur de recherche dans le sens direct
      list.add(new DirectPosition(middle.getX() + valDirect * normalDirect[0],
          middle.getY() + valDirect * normalDirect[1]));
      ILineString vectorDirect = new GM_LineString(list);
      while (!blocksUnion.intersects(vectorDirect)
          && vectorDirect.length() < DIST_MAX) {
        valDirect += ACCURACY;
        vectorDirect.setControlPoint(1, new DirectPosition(middle.getX()
            + valDirect * normalDirect[0], middle.getY() + valDirect
            * normalDirect[1]));
      }
      if (vectorDirect.length() > DIST_MAX) {
        vectorDirect.setControlPoint(1, new DirectPosition(middle.getX(),
            middle.getY())); // on est surment sorti de la zone d'étude
      }
      // on cherche dans l'autre sens
      double valUndirect = ACCURACY;
      list = new DirectPositionList();
      list.add(middle);
      // le vecteur de recherche dans le sens indirect
      list.add(new DirectPosition(middle.getX() + valUndirect
          * normalUndirect[0], middle.getY() + valUndirect * normalUndirect[1]));
      ILineString vectorUndirect = new GM_LineString(list);
      while (!blocksUnion.intersects(vectorUndirect)
          && vectorUndirect.length() < DIST_MAX) {
        valUndirect += ACCURACY;
        vectorUndirect.setControlPoint(1, new DirectPosition(middle.getX()
            + valUndirect * normalUndirect[0], middle.getY() + valUndirect
            * normalUndirect[1]));
      }
      if (vectorUndirect.length() > DIST_MAX) {
        vectorUndirect.setControlPoint(1, new DirectPosition(middle.getX(),
            middle.getY())); // on est surment sorti de la zone d'étude
      }
      double width = Math.max(
          vectorDirect.length(),
          Math.max(vectorUndirect.length(), vectorDirect.length()
              + vectorUndirect.length()));

      AttributeManager.addAttribute(street, "width", width, "Double");
      ouputstreets.add(street);
    }
    if (logger.isInfoEnabled()) {
      logger.info("All streets width calculated");
    }

    if (logger.isInfoEnabled()) {
      logger.info("Exporting results ...");
    }

    ShapefileWriter.write(ouputstreets, outputShpStreet);

    long t = System.currentTimeMillis() - t0;
    long secondes = t / 1000;
    long minutes = 0;
    long heures = 0;
    if (secondes > 60) {
      minutes = secondes / 60;
      secondes = secondes - 60 * minutes;
    }
    if (minutes > 60) {
      heures = minutes / 60;
      minutes = heures - 60 * minutes;
    }
    if (logger.isInfoEnabled()) {
      logger.info("Terminated in " + heures + " hours " + minutes + " minutes "
          + secondes + " seconds");
      logger
          .info("--------------------------------------------------------------------------");
    }
  }

  public static void main(String args[]) {
    String inputShpStreets = "/home/bcostes/Bureau/tmp_largeur_rues/voies_reduct.2.shp";

    String inputShpBlocks = "/home/bcostes/Bureau/tmp_largeur_rues/VASSEROTS_ILOTS.shp";
    String outputShpStreet = "/home/bcostes/Bureau/tmp_largeur_rues/streets_with_width.shp";

    StreetWidth.approximateWidth(inputShpStreets, inputShpBlocks,
        outputShpStreet);

    /*
     * IFeatureCollection<IFeature> col = ShapefileReader.read(inputShpStreets);
     * for (IFeature f : col) { AttributeManager.addAttribute(f, "test", 1,
     * "Integer"); System.out.println("ok"); }
     */
  }
}
