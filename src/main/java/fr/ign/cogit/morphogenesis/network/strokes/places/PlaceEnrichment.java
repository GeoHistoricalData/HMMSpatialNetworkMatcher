package fr.ign.cogit.morphogenesis.network.strokes.places;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IFeatureCollection;
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.api.spatial.geomaggr.IMultiCurve;
import fr.ign.cogit.geoxygene.api.spatial.geomroot.IGeometry;
import fr.ign.cogit.geoxygene.contrib.cartetopo.Arc;
import fr.ign.cogit.geoxygene.contrib.cartetopo.CarteTopo;
import fr.ign.cogit.geoxygene.contrib.cartetopo.CarteTopoFactory;
import fr.ign.cogit.geoxygene.contrib.cartetopo.Noeud;
import fr.ign.cogit.geoxygene.feature.DefaultFeature;
import fr.ign.cogit.geoxygene.feature.Population;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileWriter;

public class PlaceEnrichment {

  private List<Arc> edges;
  private List<Arc> connectedEdges;
  private List<Noeud> nodes;
  private Logger logger = Logger.getLogger(PlaceEnrichment.class);

  public PlaceEnrichment(List<Arc> edges, List<Noeud> nodes,
      List<Arc> connectedEdges) {
    this.edges = edges;
    this.nodes = nodes;
    this.connectedEdges = connectedEdges;
  }

  public static List<PlaceEnrichment> detect(IFeatureCollection<IFeature> pop,
      CarteTopo map, String nameAttribute, String nameSuffix) {
    List<PlaceEnrichment> result = new ArrayList<PlaceEnrichment>();

    Map<String, IPopulation<IFeature>> mapping = new HashMap<String, IPopulation<IFeature>>();
    for (IFeature f : pop) {
      /*
       * double c = 4. * Math.PI * f.getGeom().area() / (f.getGeom().length() *
       * f.getGeom().length()); if (c > 0.9) { System.out.println(c);
       * out.add(new DefaultFeature(f.getGeom())); }
       */
      if (f.getAttribute(nameAttribute).toString() != null
          && !f.getAttribute(nameAttribute).toString().equals("")) {
        String name = f.getAttribute(nameAttribute).toString().toLowerCase();
        String type = name.substring(0, f.getAttribute(nameAttribute)
            .toString().toLowerCase().indexOf(" "));
        ;
        if (type.equals("pl")) {
          if (mapping.containsKey(name)) {
            mapping.get(name).add(f);
          } else {
            IPopulation<IFeature> l = new Population<IFeature>();
            l.add(f);
            mapping.put(name, l);
          }
        }
      }
    }

    for (String rue : mapping.keySet()) {
      List<Noeud> node = new ArrayList<Noeud>();
      List<Arc> edge = new ArrayList<Arc>();
      IGeometry g = mapping.get(rue).getGeomAggregate().convexHull();
      IFeatureCollection<IFeature> inputFeatureCollectionCorrected = new Population<IFeature>();
      for (IFeature feat : mapping.get(rue)) {
        if (feat.getGeom() instanceof IMultiCurve) {
          for (int i = 0; i < ((IMultiCurve<?>) feat.getGeom()).size(); i++) {
            inputFeatureCollectionCorrected.add(new DefaultFeature(
                ((IMultiCurve<?>) feat.getGeom()).get(i)));
          }
        } else {
          inputFeatureCollectionCorrected
              .add(new DefaultFeature(feat.getGeom()));
        }
      }
      CarteTopo tmp = CarteTopoFactory
          .newCarteTopo(inputFeatureCollectionCorrected);
      tmp.creeTopologieFaces();
      if (tmp.getListeFaces().size() == 0) {
        continue;
      }
      if (g.area() == 0) {
        continue;
      }

      for (Arc a : map.getListeArcs()) {
        if (g.buffer(0.5).contains(a.getGeom())) {
          edge.add(a);
          node.add(a.getNoeudIni());
          node.add(a.getNoeudFin());
        }
      }
      List<Arc> connectedEdges = new ArrayList<Arc>();

      for (Arc a : map.getListeArcs()) {
        if (edge.contains(a)) {
          for (Arc aa : a.getNoeudIni().getEntrants()) {
            if (!connectedEdges.contains(aa) && !edge.contains(aa)) {
              connectedEdges.add(aa);
            }
          }
          for (Arc aa : a.getNoeudIni().getSortants()) {
            if (!connectedEdges.contains(aa) && !edge.contains(aa)) {
              connectedEdges.add(aa);
            }
          }
          for (Arc aa : a.getNoeudFin().getEntrants()) {
            if (!connectedEdges.contains(aa) && !edge.contains(aa)) {
              connectedEdges.add(aa);
            }
          }
          for (Arc aa : a.getNoeudFin().getSortants()) {
            if (!connectedEdges.contains(aa) && !edge.contains(aa)) {
              connectedEdges.add(aa);
            }
          }
        }
      }
      PlaceEnrichment e = new PlaceEnrichment(edge, node, connectedEdges);
      result.add(e);
    }

    return result;
  }

  public List<Arc> getConnectedEdges() {
    return this.connectedEdges;
  }

  public List<Arc> getOtherConnectedEdges(Arc a) {
    /*
     * for (Arc aa : connectedEdges) { System.out.println(aa.toString()); } if
     * (!this.connectedEdges.contains(a)) { logger.error("Edge " + a.toString()
     * + " not connected with this place."); return null; }
     */
    List<Arc> l = new ArrayList<Arc>(this.connectedEdges);
    l.remove(a);

    return l;
  }

  public void setEdges(List<Arc> edges) {
    this.edges = edges;
  }

  public List<Arc> getEdges() {
    return edges;
  }

  public void setNodes(List<Noeud> nodes) {
    this.nodes = nodes;
  }

  public List<Noeud> getNodes() {
    return nodes;
  }

  public static void export(List<PlaceEnrichment> places, String file) {
    IFeatureCollection<IFeature> col = new Population<IFeature>();
    for (PlaceEnrichment p : places) {
      for (Arc a : p.getEdges()) {
        col.add(new DefaultFeature(a.getGeom()));
      }
    }
    ShapefileWriter.write(col, file);
  }

}
