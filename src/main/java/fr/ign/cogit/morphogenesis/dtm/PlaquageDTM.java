package fr.ign.cogit.morphogenesis.dtm;

import fr.ign.cogit.geoxygene.sig3d.gui.MainWindow;
import fr.ign.cogit.geoxygene.sig3d.semantic.DTM;
import fr.ign.cogit.geoxygene.sig3d.util.ColorShade;

public class PlaquageDTM {

  public static void main(String[] args) {

    String fileDtm = "/home/bcostes/Bureau/t.asc";
    // String fileDtm =
    // "/media/Data/Benoit/these/donnees/mnt/rge/Dpt_75_asc.asc";

    String fileShp = "/media/Data/Benoit/these/donnees/vecteur/FILAIRES_L93/1885_ALPHAND_POUBELLE.shp";

    String fileExport = "/home/bcostes/Bureau/test_mnt.shp";

    DTM dtm = new DTM(fileDtm, "", true, 3, ColorShade.BROWN_MONOCHROMATIC);
    /*
     * DirectPosition pMinPhoto = new DirectPosition(645172.1110576118808240,
     * 6857753.4813667591661215); DirectPosition pMaxPhoto = new
     * DirectPosition(657156.4233236195286736, 6867225.4378072898834944);
     */

    /*
     * DirectPosition pMinPhoto = new DirectPosition(596234.6129357293248177,
     * 126195.2360369058151264); DirectPosition pMaxPhoto = new
     * DirectPosition(603032.6129357293248177, 130471.7360369058151264);
     */

    /*
     * GM_Envelope env = new GM_Envelope(pMaxPhoto, pMinPhoto);
     * 
     * DTM dtm = new DTM(fileDtm, "", true, 4, //
     * "file:///home/bcostes/Bureau/test_poubelle_l93/1888_PL02_georef_res1.5.tif"
     * , "file:///home/bcostes/Bureau/test_poubelle_l93/plan_global.tif", env);
     */

    DTM.CONSTANT_OFFSET = 10;
    // IFeatureCollection<IFeature> col = dtm.mapShapeFile(fileShp, true);

    // ShapefileWriter.write(col, fileExport);

    MainWindow mw = new MainWindow();

    mw.getInterfaceMap3D().getCurrent3DMap().addLayer(dtm);

    // VectorLayer vl = new VectorLayer(col, "Couche", Color.BLACK);
    // mw.getInterfaceMap3D().getCurrent3DMap().addLayer(vl);

  }
}
