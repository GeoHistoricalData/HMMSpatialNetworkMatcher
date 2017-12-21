package fr.ign.cogit.morphogenesis.generation.barthelemy;

public class Parameters {

  public static int NEW_CENTERS_COUNT = 6; // nombre de nouveaux centres
  public static int NEW_CENTERS_DELAY = 100;
  public static int CENTERS_MAX = 50000;
  public static double XMIN = 300000;
  public static double YMIN = 7500000;
  public static double XMAX = 1000000;
  public static double YMAX = 7500000;
  public static double XWINDOWLEFTDOWN = 647550;
  public static double YWINDOWLEFTDOWN = 6858500;
  public static double WINDOWSIZE = 8000;
  public static double PROBA_WINDOW_GROWTH = 0.005; // proba d'expension de la
  // fenetre
  public static double WINDOW_GROWTH = WINDOWSIZE / 4; // taille d'expension de
  // la fenetre

  public static String OUTPUT_IMAGES = "/home/bcostes/Bureau/test_rasterize_gdal/";
  public static double XC = (XWINDOWLEFTDOWN) + WINDOWSIZE / 2.;
  public static double YC = (YWINDOWLEFTDOWN) + WINDOWSIZE / 2.;
  public static double RC = 0.1; // population distribution gradient

  public static double DIST_MIN = 50;

}
