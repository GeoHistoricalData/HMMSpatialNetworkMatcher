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
public class StreetWidthDichotomie {

  /**
   * Distance maximale à l'îlot le plus proche pour affecter la largeur
   */
  private static final int DIST_MAX = 50;
  private static final double ACCURACY = 0.1;
  private static final Logger logger = Logger
      .getLogger(StreetWidthDichotomie.class);

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
      double max = DIST_MAX;
      double min = ACCURACY;
      double dh = 0;
      IDirectPositionList list = new DirectPositionList();
      list.add(middle);
      // le vecteur de recherche dans le sens direct
      list.add(new DirectPosition(middle.getX() + max * normalDirect[0], middle
          .getY() + max * normalDirect[1]));
      ILineString vectorDirectMax = new GM_LineString(list);
      list.clear();
      list.add(middle);
      list.add(new DirectPosition(middle.getX() + min * normalDirect[0], middle
          .getY() + min * normalDirect[1]));
      ILineString vectorDirect = new GM_LineString(list);

      if (!vectorDirectMax.intersects(blocksUnion)) {
        vectorDirect.setControlPoint(1, new DirectPosition(middle.getX(),
            middle.getY())); // on est surment sorti de la zone d'étude
      } else {

        while (true) {
          dh = (max + min) / 2.;
          vectorDirect.setControlPoint(1, new DirectPosition(middle.getX() + dh
              * normalDirect[0], middle.getY() + dh * normalDirect[1]));
          if (vectorDirect.intersects(blocksUnion)) {
            // casde sortie
            if (vectorDirect.intersection(blocksUnion).length() < ACCURACY) {
              break;
            }
            // sinon, on échange max et dh
            max = dh;
          } else {
            // sinon on échange min et dh
            min = dh;
          }
        }
      }
      max = DIST_MAX;
      min = ACCURACY;
      dh = 0;
      list = new DirectPositionList();
      list.add(middle);
      // le vecteur de recherche dans le sens direct
      list.add(new DirectPosition(middle.getX() + max * normalUndirect[0],
          middle.getY() + max * normalUndirect[1]));
      ILineString vectorUndirectMax = new GM_LineString(list);
      list.clear();
      list.add(middle);
      list.add(new DirectPosition(middle.getX() + min * normalUndirect[0],
          middle.getY() + min * normalUndirect[1]));
      ILineString vectorUndirect = new GM_LineString(list);
      if (!vectorUndirectMax.intersects(blocksUnion)) {
        vectorDirect.setControlPoint(1, new DirectPosition(middle.getX(),
            middle.getY())); // on est surment sorti de la zone d'étude
      } else {
        while (true) {
          dh = (max + min) / 2.;
          vectorDirect.setControlPoint(1, new DirectPosition(middle.getX() + dh
              * normalUndirect[0], middle.getY() + dh * normalUndirect[1]));
          if (vectorDirect.intersects(blocksUnion)) {
            // cas de sortie
            if (vectorDirect.intersection(blocksUnion).length() < ACCURACY) {
              break;
            }
            // sinon, on échange max et dh
            max = dh;
          } else {
            // sinon on échange min et dh
            min = dh;
          }
        }
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

    StreetWidthDichotomie.approximateWidth(inputShpStreets, inputShpBlocks,
        outputShpStreet);

    /*
     * IFeatureCollection<IFeature> col = ShapefileReader.read(inputShpStreets);
     * for (IFeature f : col) { AttributeManager.addAttribute(f, "test", 1,
     * "Integer"); System.out.println("ok"); }
     */
  }
}
