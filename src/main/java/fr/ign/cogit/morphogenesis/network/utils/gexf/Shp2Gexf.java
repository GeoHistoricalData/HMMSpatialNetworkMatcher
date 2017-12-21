package fr.ign.cogit.morphogenesis.network.utils.gexf;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IFeatureCollection;
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.contrib.cartetopo.Arc;
import fr.ign.cogit.geoxygene.contrib.cartetopo.CarteTopo;
import fr.ign.cogit.geoxygene.contrib.cartetopo.Noeud;
import fr.ign.cogit.geoxygene.spatial.coordgeom.GM_LineString;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileReader;

public class Shp2Gexf {

  public class NoeudGexf {
    private int id;

    public int getId() {
      return id;
    }

    public void setId(int id) {
      this.id = id;
    }

    public double getX() {
      return x;
    }

    public void setX(double x) {
      this.x = x;
    }

    public double getY() {
      return y;
    }

    public void setY(double y) {
      this.y = y;
    }

    private double x;
    private double y;

    public NoeudGexf(int id, double x, double y) {
      this.id = id;
      this.x = x;
      this.y = y;
    }
  }

  public class ArcGexf {
    private int id;
    private int source;
    private int target;
    private double weight;

    public ArcGexf(int id, int source, int target, double weight) {
      this.id = id;
      this.source = source;
      this.target = target;
      this.weight = weight;
    }

    public int getId() {
      return id;
    }

    public void setId(int id) {
      this.id = id;
    }

    public int getSource() {
      return source;
    }

    public void setSource(int source) {
      this.source = source;
    }

    public int getTarget() {
      return target;
    }

    public void setTarget(int target) {
      this.target = target;
    }

    public double getWeight() {
      return weight;
    }

    public void setWeight(double weight) {
      this.weight = weight;
    }
  }

  public void convert(String shp, String gexf) {
    IFeatureCollection<IFeature> input = ShapefileReader.read(shp);
    // création du graph géométrique
    CarteTopo topo = new CarteTopo("void");
    IPopulation<Arc> arcs = topo.getPopArcs();
    for (IFeature feature : input) {
      Arc arc = arcs.nouvelElement();
      try {
        GM_LineString line = new GM_LineString(feature.getGeom().coord());
        arc.setGeometrie(line);
        arc.addCorrespondant((IFeature) feature);
      } catch (ClassCastException e) {
        e.printStackTrace();
      }
    }
    topo.creeNoeudsManquants(0);
    topo.fusionNoeuds(0);
    topo.filtreArcsDoublons();
    topo.filtreNoeudsIsoles();
    topo.rendPlanaire(0);
    topo.fusionNoeuds(0);
    topo.filtreArcsDoublons();

    // mapping noeud topo / noeud gexf
    Map<Noeud, NoeudGexf> mapNodes = new HashMap<Noeud, Shp2Gexf.NoeudGexf>();
    int cpt = 0;
    for (Noeud n : topo.getListeNoeuds()) {
      NoeudGexf ngexf = this.new NoeudGexf(cpt, n.getCoord().getX(), n
          .getCoord().getY());
      mapNodes.put(n, ngexf);
      cpt++;
    }
    // mapping arc topo / arc gexf
    Map<Arc, ArcGexf> mapEdges = new HashMap<Arc, Shp2Gexf.ArcGexf>();
    cpt = 0;
    for (Arc a : topo.getListeArcs()) {
      ArcGexf agexf = this.new ArcGexf(cpt, mapNodes.get(a.getNoeudIni())
          .getId(), mapNodes.get(a.getNoeudFin()).getId(), a.longueur());
      mapEdges.put(a, agexf);
      cpt++;
    }

    File xmlFile = new File(gexf);
    FileOutputStream out;
    try {
      out = new FileOutputStream(xmlFile);
      // metadata & co
      String metadata = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
      out.write(metadata.getBytes());
      metadata = "<gexf xmlns:viz=\"http://www.gexf.net/1.1draft/viz\""
          + " xmlns=\"http://www.gexf.net/1.2draft\" version=\"1.2\">\n";
      out.write(metadata.getBytes());

      // création du graph
      metadata = "\t<graph defaultedgetype=\"undirected\">\n";
      out.write(metadata.getBytes());
      // les noeuds
      metadata = "\t\t<nodes>\n";
      out.write(metadata.getBytes());
      for (Noeud n : mapNodes.keySet()) {
        NoeudGexf ngexf = mapNodes.get(n);
        String nodeS = "\t\t\t<node id =\"" + ngexf.getId() + "\">\n"
            + "\t\t\t\t<viz:position x = \"" + ngexf.getX() + "\" y = \""
            + ngexf.getY() + "\"/>\n" + "\t\t\t</node>\n";
        out.write(nodeS.getBytes());
      }
      metadata = "\t\t</nodes>\n";
      out.write(metadata.getBytes());
      // les arcs
      metadata = "\t\t<edges>\n";
      out.write(metadata.getBytes());
      for (Arc a : mapEdges.keySet()) {
        ArcGexf agexf = mapEdges.get(a);
        String edgeS = "\t\t\t<edge id = \"" + agexf.getId() + "\" source = \""
            + agexf.getSource() + "\" target = \"" + agexf.getTarget()
            + "\" weight = \"" + agexf.getWeight() + "\"/>\n";
        out.write(edgeS.getBytes());
      }
      metadata = "\t\t</edges>\n";
      out.write(metadata.getBytes());
      metadata = "\t</graph>\n";
      out.write(metadata.getBytes());
      metadata = "</gexf>\n";
      out.write(metadata.getBytes());
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  public static void main(String[] args) {

    String inputShp = "/media/Data/Benoit/these/donnees/vecteur/filaires/FILAIRES_L93_OK/2010_BDTOPO_emprise_topologieOk.shp";
    String ouputGexf = "/home/bcostes/Bureau/gml/bdtopo_emprise.gexf";

    Shp2Gexf convertor = new Shp2Gexf();
    convertor.convert(inputShp, ouputGexf);
  }

}
