package fr.ign.cogit.v2.utils.vectorization;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.triangulate.VoronoiDiagramBuilder;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPositionList;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ILineString;
import fr.ign.cogit.geoxygene.contrib.cartetopo.Arc;
import fr.ign.cogit.geoxygene.contrib.cartetopo.CarteTopo;
import fr.ign.cogit.geoxygene.contrib.geometrie.Operateurs;
import fr.ign.cogit.geoxygene.feature.DefaultFeature;
import fr.ign.cogit.geoxygene.feature.Population;
import fr.ign.cogit.geoxygene.generalisation.GaussianFilter;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPosition;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPositionList;
import fr.ign.cogit.geoxygene.spatial.coordgeom.GM_LineString;
import fr.ign.cogit.geoxygene.util.conversion.JtsGeOxygene;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileReader;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileWriter;
import fr.ign.cogit.geoxygene.util.index.Tiling;
import fr.ign.cogit.v2.utils.GeometryUtils;

/**
 * Construction d'un filaire à partir des ilots
 * Pour faire le graphe planaire et simple mieux vaut utilise OpenJump que Geoxygene => plus rapide
 * @author bcostes
 *
 */
public class Blocks2Streets {

 // static String polygoneShp = "/home/bcostes/Bureau/ilots_vasserot.shp";
  static String polygoneShp = "/media/bcostes/Data/Benoit/these/donnees/vecteur/parcelaire/verniquet/verniquet_ilots.shp";

  static String voronoiSegmentsShp = "/home/bcostes/Bureau/voronoi_lines.shp";

  static double resampling_threshold = 2.5;
  static double deadend_thresold = 40.;

  static final Logger logger = Logger.getLogger(Blocks2Streets.class);

  /**
   * Récupère les segments du diagrame de voronoi qui n'itersecent pas les polygones
   */
  public static Set<ILineString> getVoronoiSegments(){
    IPopulation<IFeature> poly = ShapefileReader.read(polygoneShp);
    poly.initSpatialIndex(Tiling.class, false);


    /*
     * Voronoi
     */
    VoronoiDiagramBuilder vbuilder = new VoronoiDiagramBuilder();

    List<Geometry> coordinates = new ArrayList<Geometry>();
    GeometryFactory factory = new GeometryFactory();
    logger.info("Resampling ... ");
    for(IFeature f  : poly){
      IDirectPositionList coords = f.getGeom().coord();
      // Resampling
      ILineString lSampled = Operateurs.resampling(new GM_LineString(coords), resampling_threshold);
      //ILineString lSampled = new GM_LineString(coords);
      for(IDirectPosition pt: lSampled.getControlPoint()){
        Coordinate c = new Coordinate(pt.getX(), pt.getY());
        coordinates.add(factory.createPoint(c));
      }
    }
    
    IPopulation<IFeature> out = new Population<IFeature>();
    for(Geometry c :coordinates){
      try {
        out.add(new DefaultFeature(JtsGeOxygene.makeGeOxygeneGeom(c)));
      } catch (Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    System.out.println(out.size());
    ShapefileWriter.write(out,"/home/bcostes/Bureau/pts.shp");
    System.out.println("pl");
    
    logger.info("Resampling done.");
    Geometry[] coordinatesTab = new Geometry[coordinates.size()];
    coordinatesTab = coordinates.toArray(coordinatesTab);
    GeometryCollection points = new GeometryCollection(coordinatesTab, factory);

    vbuilder.setSites(points);
    vbuilder.setClipEnvelope(points.getEnvelopeInternal());
    vbuilder.setTolerance(1.);
    logger.info("Voronoi diagram ... ");
    GeometryCollection  diagram = (GeometryCollection)vbuilder.getDiagram(factory);
    logger.info("Voronoi diagram done.");

    Set<ILineString> lines = new HashSet<ILineString>();
    logger.info("Selecting segments ... ");
    int cpt = 0;
    int cpt2 = 0;
    int size = diagram.getNumGeometries();
    int pas = size / 100;
    for(int i=0; i< diagram.getNumGeometries(); i++){
      cpt ++;
      if(cpt % pas == 0){
        cpt2 ++;
        cpt = 0;
        logger.info(cpt2 + "%");
      }
      Polygon p = (Polygon) diagram.getGeometryN(i);
      //Transformation des cellules en segments
      for(int j=0; j< p.getNumPoints()-1; j++){
        Coordinate p1 = p.getCoordinates()[j];
        Coordinate p2 = p.getCoordinates()[j+1];
        IDirectPositionList list= new DirectPositionList();
        list.add(new DirectPosition(p1.x, p1.y));
        list.add(new DirectPosition(p2.x, p2.y));
        ILineString l = new GM_LineString(list);
        Collection<IFeature> polyClose = poly.select(l, 0);
        boolean selected = false;
        for(IFeature pol : polyClose){
          if(pol.getGeom().intersects(l)){
            selected = true;
            break;
          }
        }
        if(!selected){
          lines.add(l);
        }
      }

    }
    logger.info("Selecting done.");
    return lines;
  }

  public static  Set<ILineString> deleteSmallDeadEnds(Set<ILineString> lines){
    CarteTopo map = new CarteTopo("");
    IPopulation<Arc> arcs = map.getPopArcs();
    for(ILineString f: lines){
      Arc a = arcs.nouvelElement();
      a.setOrientation(2);
      a.setGeom(f);
    }
    //      
    map.creeTopologieArcsNoeuds(0);
    map.creeNoeudsManquants(0);
    map.rendPlanaire(0);
    map.creeTopologieArcsNoeuds(0);
    map.filtreArcsDoublons(0);
    map.filtreNoeudsSimples();
    map.filtreNoeudsIsoles();


    Set<Arc> aaa  = new HashSet<Arc>();

    logger.info("Filtering dead ends ...");


    // Filtrage récursif des impasses
    for(Arc a : new ArrayList<Arc>(map.getPopArcs())){
      if(a.getNoeudIni().getEntrants().size()+a.getNoeudIni().getSortants().size() == 1 ||
          a.getNoeudFin().getEntrants().size()+a.getNoeudFin().getSortants().size() == 1  ){
        if(a.longueur() <deadend_thresold){
          aaa.add(a);
        }
      }
    }
    while(!aaa.isEmpty()){
      map.enleveArcs(aaa);
      aaa.clear();
      for(Arc a : new ArrayList<Arc>(map.getPopArcs())){
        if(a.getNoeudIni().getEntrants().size()+a.getNoeudIni().getSortants().size() == 1 ||
            a.getNoeudFin().getEntrants().size()+a.getNoeudFin().getSortants().size() == 1  ){
          if(a.longueur() <deadend_thresold){
            aaa.add(a);
          }
        }
      }
    }
    aaa.clear();
    for(Arc a : new ArrayList<Arc>(map.getPopArcs())){
      if(a.getNoeudIni().getEntrants().size()+a.getNoeudIni().getSortants().size() == 1 ||
          a.getNoeudFin().getEntrants().size()+a.getNoeudFin().getSortants().size() == 1  ){
        if(a.longueur() <deadend_thresold){
          aaa.add(a);
        }
      }
    }
    map.enleveArcs(aaa);
    Set<ILineString> newLines = new HashSet<ILineString>();
    for(Arc a : map.getListeArcs()){
      newLines.add(a.getGeometrie());
    }
    return newLines;
  }

  public static Set<ILineString> filter( Set<ILineString> lines){
    logger.info("Gaussian filtering...");
    Set<ILineString> newLines = new HashSet<ILineString>();
    // Filtrage et lissage
    for(ILineString a : lines){
      ILineString l = GaussianFilter.gaussianFilter(a,10, 10);
      IDirectPositionList c = l .coord();
    // GeometryUtils.filterLowAngles(c, 5.);
      //GeometryUtils.filterLargeAngles(c, 170.);
      l = new GM_LineString(c);
      if(l.sizeControlPoint() == 1){
        continue;
      }
      //l = GaussianFilter.gaussianFilter(l,10, 10);
      newLines.add(l);
    }       

    return newLines;
  }

  public static Set<ILineString> finalize(Set<ILineString> lines){
    Set<ILineString> newLines = new HashSet<ILineString>();
    CarteTopo map = new CarteTopo("");
    IPopulation<Arc> arcs = map.getPopArcs();
    for(ILineString f: lines){
      Arc a = arcs.nouvelElement();
      a.setOrientation(2);
      a.setGeom(f);
    }
    //      
    map.creeTopologieArcsNoeuds(0);
    map.creeNoeudsManquants(0);
    map.rendPlanaire(0);
    map.fusionNoeuds(1.);
    map.filtreNoeudsSimples();
    
    return newLines;
  }
  public static void main(String[] args) {
    IPopulation<IFeature> out = new Population<IFeature>();
//

//        Set<ILineString> lines = Blocks2Streets.getVoronoiSegments();
//        for(ILineString l : lines){
//          out.add(new DefaultFeature(l));
//        }
//        ShapefileWriter.write(out, "/home/bcostes/Bureau/voronoi_lines.shp");

    IPopulation<IFeature> vlines = ShapefileReader.read(voronoiSegmentsShp);
    Set<ILineString> lines = new HashSet<ILineString>();
    for(IFeature f :vlines){
      lines.add(new GM_LineString(f.getGeom().coord()));
    }
    lines  = Blocks2Streets.deleteSmallDeadEnds(lines);
    lines  = Blocks2Streets.filter(lines);
    for(ILineString l : lines){
      out.add(new DefaultFeature(l));
    }
    //lines = Blocks2Streets.finalize(lines);
    ShapefileWriter.write(out, "/home/bcostes/Bureau/tt_verniquet.shp");
  }


}
