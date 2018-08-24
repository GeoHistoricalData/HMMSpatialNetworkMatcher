package fr.ign.cogit.HMMSpatialNetworkMatcher.spatial_impl.io;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fr.ign.cogit.HMMSpatialNetworkMatcher.api.IHiddenState;
import fr.ign.cogit.HMMSpatialNetworkMatcher.api.IObservation;
import fr.ign.cogit.HMMSpatialNetworkMatcher.spatial_impl.spatial_hmm.CompositeHiddenState;
import fr.ign.cogit.HMMSpatialNetworkMatcher.spatial_impl.spatial_hmm.FeatHiddenState;
import fr.ign.cogit.HMMSpatialNetworkMatcher.spatial_impl.spatial_hmm.FeatObservation;
import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ILineString;
import fr.ign.cogit.geoxygene.api.spatial.geomprim.IPoint;
import fr.ign.cogit.geoxygene.contrib.geometrie.Operateurs;
import fr.ign.cogit.geoxygene.feature.DefaultFeature;
import fr.ign.cogit.geoxygene.feature.Population;
import fr.ign.cogit.geoxygene.spatial.coordgeom.GM_LineString;
import fr.ign.cogit.geoxygene.spatial.geomprim.GM_Point;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileWriter;
import fr.ign.cogit.geoxygene.util.index.Tiling;

public class HMMExporter {

  public void export(Map<IObservation, Set<IHiddenState>> matching, Map<IObservation, Set<IHiddenState>> simplifiedMatching, String output) {

    this.exportMatching(matching, output);
    this.exportMatching(simplifiedMatching, output + "_simplified");

  }

  public IPopulation<IFeature> exportMatching(Map<IObservation, Set<IHiddenState>> matching) {
    Map<IFeature, Set<IFeature>> controlFinalLinks = new HashMap<>();
    IPopulation<IFeature> out = new Population<>();

    for (IObservation o : matching.keySet()) {
      FeatObservation a = (FeatObservation) o;
      for (IFeature f1 : a.getCorrespondants()) {
        ILineString g1 = new GM_LineString(f1.getGeom().coord());
        g1 = Operateurs.resampling(g1, g1.length() / 4);
        IDirectPosition p11 = g1.getControlPoint(1);
        IDirectPosition p12 = Operateurs.milieu(g1);
        IDirectPosition p13 = g1.getControlPoint(g1.getControlPoint().size() - 2);

        for (IHiddenState s : matching.get(o)) {

          if (s instanceof CompositeHiddenState) {
            for (IHiddenState hd : ((CompositeHiddenState) s).getStates()) {
              FeatHiddenState a2 = (FeatHiddenState) hd;
              for (IFeature f2 : a2.getCorrespondants()) {
                if (controlFinalLinks.containsKey(f1)) {
                  if (controlFinalLinks.get(f1).contains(f2)) {
                    continue;
                  }
                  controlFinalLinks.get(f1).add(f2);
                } else {
                  controlFinalLinks.put(f1, new HashSet<>(Collections.singletonList(f2)));
                }
                ILineString g2 = new GM_LineString(f2.getGeom().coord());
                g2 = Operateurs.resampling(g2, g2.length() / 4);
                IDirectPosition p21 = g2.getControlPoint(1);
                IDirectPosition p22 = Operateurs.milieu(g2);
                IDirectPosition p23 = g2.getControlPoint(g2.getControlPoint().size() - 2);

                IDirectPosition pproj11 = Operateurs.projection(p11, g2);
                if (pproj11.equals(g2.startPoint(), 1) || pproj11.equals(g2.endPoint(), 1)) {
                  pproj11 = p11;
                }
                IDirectPosition pproj12 = Operateurs.projection(p12, g2);
                if (pproj12.equals(g2.startPoint(), 0.005) || pproj12.equals(g2.endPoint(), 0.005)) {
                  pproj12 = p12;
                }
                IDirectPosition pproj13 = Operateurs.projection(p13, g2);
                if (pproj13.equals(g2.startPoint(), 1) || pproj13.equals(g2.endPoint(), 1)) {
                  pproj13 = p13;
                }

                IDirectPosition pproj21 = Operateurs.projection(p21, g1);
                if (pproj21.equals(g1.startPoint(), 1) || pproj21.equals(g1.endPoint(), 1)) {
                  pproj21 = p21;
                }
                IDirectPosition pproj22 = Operateurs.projection(p22, g1);
                if (pproj22.equals(g1.startPoint(), 0.005) || pproj22.equals(g1.endPoint(), 0.005)) {
                  pproj22 = p22;
                }
                IDirectPosition pproj23 = Operateurs.projection(p23, g1);
                if (pproj23.equals(g1.startPoint(), 1) || pproj23.equals(g1.endPoint(), 1)) {
                  pproj23 = p23;
                }

                ILineString line11 = new GM_LineString(Arrays.asList(p11, pproj11));
                ILineString line12 = new GM_LineString(Arrays.asList(p12, pproj12));
                ILineString line13 = new GM_LineString(Arrays.asList(p13, pproj13));
                ILineString line14 = new GM_LineString(Arrays.asList(p12, p22));

                ILineString line21 = new GM_LineString(Arrays.asList(p21, pproj21));
                ILineString line22 = new GM_LineString(Arrays.asList(p22, pproj22));
                ILineString line23 = new GM_LineString(Arrays.asList(p23, pproj23));

                ILineString line31 = new GM_LineString(Arrays.asList(p12, p21));
                ILineString line32 = new GM_LineString(Arrays.asList(p12, p22));
                ILineString line33 = new GM_LineString(Arrays.asList(p12, p23));
                ILineString line34 = new GM_LineString(Arrays.asList(p11, p22));
                ILineString line35 = new GM_LineString(Arrays.asList(p12, p22));
                ILineString line36 = new GM_LineString(Arrays.asList(p13, p22));

                List<ILineString> lines = new ArrayList<>(Arrays.asList(line11, line12, line13, line14, line21, line22, line23, line31, line32, line33, line34, line35, line36));

                ILineString lmin = Collections.min(lines, (l1, l2) -> {
                  if (l1.startPoint().equals(l1.endPoint(), 0.005)) {
                    return 1;
                  } else if (l2.startPoint().equals(l2.endPoint(), 0.005)) {
                    return -1;
                  } else {
                    double length1 = l1.length();
                    double length2 = l2.length();
                    if (length1 > length2) {
                      return 1;
                    } else if (length1 < length2) {
                      return -1;
                    }
                    return 0;
                  }
                });

                IFeature f = new DefaultFeature(lmin);
                // IFeature f = new DefaultFeature(new GM_LineString(new DirectPositionList(Arrays.asList(p12,p22))));
                out.add(f);
              }
            }
          } else {
            FeatHiddenState a2 = (FeatHiddenState) s;
            for (IFeature f2 : a2.getCorrespondants()) {
              if (controlFinalLinks.containsKey(f1)) {
                if (controlFinalLinks.get(f1).contains(f2)) {
                  continue;
                }
                controlFinalLinks.get(f1).add(f2);
              } else {
                controlFinalLinks.put(f1, new HashSet<>(Collections.singletonList(f2)));
              }
              ILineString g2 = new GM_LineString(f2.getGeom().coord());
              g2 = Operateurs.resampling(g2, g2.length() / 4);
              IDirectPosition p21 = g2.getControlPoint(1);
              IDirectPosition p22 = Operateurs.milieu(g2);
              IDirectPosition p23 = g2.getControlPoint(g2.getControlPoint().size() - 2);

              IDirectPosition pproj11 = Operateurs.projection(p11, g2);
              if (pproj11.equals(g2.startPoint(), 1) || pproj11.equals(g2.endPoint(), 1)) {
                pproj11 = p11;
              }
              IDirectPosition pproj12 = Operateurs.projection(p12, g2);
              if (pproj12.equals(g2.startPoint(), 0.005) || pproj12.equals(g2.endPoint(), 0.005)) {
                pproj12 = p12;
              }
              IDirectPosition pproj13 = Operateurs.projection(p13, g2);
              if (pproj13.equals(g2.startPoint(), 1) || pproj13.equals(g2.endPoint(), 1)) {
                pproj13 = p13;
              }

              IDirectPosition pproj21 = Operateurs.projection(p21, g1);
              if (pproj21.equals(g1.startPoint(), 1) || pproj21.equals(g1.endPoint(), 1)) {
                pproj21 = p21;
              }
              IDirectPosition pproj22 = Operateurs.projection(p22, g1);
              if (pproj22.equals(g1.startPoint(), 0.005) || pproj22.equals(g1.endPoint(), 0.005)) {
                pproj22 = p22;
              }
              IDirectPosition pproj23 = Operateurs.projection(p23, g1);
              if (pproj23.equals(g1.startPoint(), 1) || pproj23.equals(g1.endPoint(), 1)) {
                pproj23 = p23;
              }

              ILineString line11 = new GM_LineString(Arrays.asList(p11, pproj11));
              ILineString line12 = new GM_LineString(Arrays.asList(p12, pproj12));
              ILineString line13 = new GM_LineString(Arrays.asList(p13, pproj13));
              ILineString line14 = new GM_LineString(Arrays.asList(p12, p22));

              ILineString line21 = new GM_LineString(Arrays.asList(p21, pproj21));
              ILineString line22 = new GM_LineString(Arrays.asList(p22, pproj22));
              ILineString line23 = new GM_LineString(Arrays.asList(p23, pproj23));

              ILineString line31 = new GM_LineString(Arrays.asList(p12, p21));
              ILineString line32 = new GM_LineString(Arrays.asList(p12, p22));
              ILineString line33 = new GM_LineString(Arrays.asList(p12, p23));
              ILineString line34 = new GM_LineString(Arrays.asList(p11, p22));
              ILineString line35 = new GM_LineString(Arrays.asList(p12, p22));
              ILineString line36 = new GM_LineString(Arrays.asList(p13, p22));

              List<ILineString> lines = new ArrayList<>(Arrays.asList(line11, line12, line13, line14, line21, line22, line23, line31, line32, line33, line34, line35, line36));

              ILineString lmin = Collections.min(lines, (l1, l2) -> {
                if (l1.startPoint().equals(l1.endPoint(), 0.005)) {
                  return 1;
                } else if (l2.startPoint().equals(l2.endPoint(), 0.005)) {
                  return -1;
                } else {
                  double length1 = l1.length();
                  double length2 = l2.length();
                  if (length1 > length2) {
                    return 1;
                  } else if (length1 < length2) {
                    return -1;
                  }
                  return 0;
                }
              });
              IFeature f = new DefaultFeature(lmin);
              // IFeature f = new DefaultFeature(new GM_LineString(new DirectPositionList(Arrays.asList(p12,p22))));
              out.add(f);
            }
          }
        }
      }
    }
    return out;
  }

  private void exportMatching(Map<IObservation, Set<IHiddenState>> matching, String output) {
    IPopulation<IFeature> out = exportMatching(matching);
    ShapefileWriter.write(out, output + ".shp");
  }

  @SuppressWarnings("unused")
  private void exportSimplifiedMatching(Map<IObservation, IHiddenState> matching, String output) {
    Map<IFeature, Set<IFeature>> controlFinalLinks = new HashMap<>();
    IPopulation<IFeature> out = new Population<>();

    for (IObservation o : matching.keySet()) {
      FeatObservation a = (FeatObservation) o;
      for (IFeature f1 : a.getCorrespondants()) {
        ILineString g1 = new GM_LineString(f1.getGeom().coord());
        g1 = Operateurs.resampling(g1, g1.length() / 4);
        IDirectPosition p11 = g1.getControlPoint(1);
        IDirectPosition p12 = Operateurs.milieu(g1);
        IDirectPosition p13 = g1.getControlPoint(g1.getControlPoint().size() - 2);

        if (matching.get(o) instanceof CompositeHiddenState) {
          for (IHiddenState hd : ((CompositeHiddenState) matching.get(o)).getStates()) {
            FeatHiddenState a2 = (FeatHiddenState) hd;
            for (IFeature f2 : a2.getCorrespondants()) {
              if (controlFinalLinks.containsKey(f1)) {
                if (controlFinalLinks.get(f1).contains(f2)) {
                  continue;
                }
                controlFinalLinks.get(f1).add(f2);
              } else {
                controlFinalLinks.put(f1, new HashSet<>(Collections.singletonList(f2)));
              }
              ILineString g2 = new GM_LineString(f2.getGeom().coord());
              g2 = Operateurs.resampling(g2, g2.length() / 4);
              IDirectPosition p21 = g2.getControlPoint(1);
              IDirectPosition p22 = Operateurs.milieu(g2);
              IDirectPosition p23 = g2.getControlPoint(g2.getControlPoint().size() - 2);

              IDirectPosition pproj11 = Operateurs.projection(p11, g2);
              if (pproj11.equals(g2.startPoint(), 1) || pproj11.equals(g2.endPoint(), 1)) {
                pproj11 = p11;
              }
              IDirectPosition pproj12 = Operateurs.projection(p12, g2);
              if (pproj12.equals(g2.startPoint(), 0.005) || pproj12.equals(g2.endPoint(), 0.005)) {
                pproj12 = p12;
              }
              IDirectPosition pproj13 = Operateurs.projection(p13, g2);
              if (pproj13.equals(g2.startPoint(), 1) || pproj13.equals(g2.endPoint(), 1)) {
                pproj13 = p13;
              }

              IDirectPosition pproj21 = Operateurs.projection(p21, g1);
              if (pproj21.equals(g1.startPoint(), 1) || pproj21.equals(g1.endPoint(), 1)) {
                pproj21 = p21;
              }
              IDirectPosition pproj22 = Operateurs.projection(p22, g1);
              if (pproj22.equals(g1.startPoint(), 0.005) || pproj22.equals(g1.endPoint(), 0.005)) {
                pproj22 = p22;
              }
              IDirectPosition pproj23 = Operateurs.projection(p23, g1);
              if (pproj23.equals(g1.startPoint(), 1) || pproj23.equals(g1.endPoint(), 1)) {
                pproj23 = p23;
              }

              ILineString line11 = new GM_LineString(Arrays.asList(p11, pproj11));
              ILineString line12 = new GM_LineString(Arrays.asList(p12, pproj12));
              ILineString line13 = new GM_LineString(Arrays.asList(p13, pproj13));
              ILineString line14 = new GM_LineString(Arrays.asList(p12, p22));

              ILineString line21 = new GM_LineString(Arrays.asList(p21, pproj21));
              ILineString line22 = new GM_LineString(Arrays.asList(p22, pproj22));
              ILineString line23 = new GM_LineString(Arrays.asList(p23, pproj23));

              ILineString line31 = new GM_LineString(Arrays.asList(p12, p21));
              ILineString line32 = new GM_LineString(Arrays.asList(p12, p22));
              ILineString line33 = new GM_LineString(Arrays.asList(p12, p23));
              ILineString line34 = new GM_LineString(Arrays.asList(p11, p22));
              ILineString line35 = new GM_LineString(Arrays.asList(p12, p22));
              ILineString line36 = new GM_LineString(Arrays.asList(p13, p22));

              List<ILineString> lines = new ArrayList<>(Arrays.asList(line11, line12, line13, line14, line21, line22, line23, line31, line32, line33, line34, line35, line36));

              ILineString lmin = Collections.min(lines, (l1, l2) -> {
                if (l1.startPoint().equals(l1.endPoint(), 0.005)) {
                  return 1;
                } else if (l2.startPoint().equals(l2.endPoint(), 0.005)) {
                  return -1;
                } else {
                  double length1 = l1.length();
                  double length2 = l2.length();
                  if (length1 > length2) {
                    return 1;
                  } else if (length1 < length2) {
                    return -1;
                  }
                  return 0;
                }
              });

              IFeature f = new DefaultFeature(lmin);
              // IFeature f = new DefaultFeature(new GM_LineString(new DirectPositionList(Arrays.asList(p12,p22))));
              out.add(f);
            }
          }
        } else {
          IHiddenState hd = matching.get(o);
          FeatHiddenState a2 = (FeatHiddenState) hd;
          for (IFeature f2 : a2.getCorrespondants()) {
            if (controlFinalLinks.containsKey(f1)) {
              if (controlFinalLinks.get(f1).contains(f2)) {
                continue;
              }
              controlFinalLinks.get(f1).add(f2);
            } else {
              controlFinalLinks.put(f1, new HashSet<>(Collections.singletonList(f2)));
            }
            ILineString g2 = new GM_LineString(f2.getGeom().coord());
            g2 = Operateurs.resampling(g2, g2.length() / 4);
            IDirectPosition p21 = g2.getControlPoint(1);
            IDirectPosition p22 = Operateurs.milieu(g2);
            IDirectPosition p23 = g2.getControlPoint(g2.getControlPoint().size() - 2);

            IDirectPosition pproj11 = Operateurs.projection(p11, g2);
            if (pproj11.equals(g2.startPoint(), 1) || pproj11.equals(g2.endPoint(), 1)) {
              pproj11 = p11;
            }
            IDirectPosition pproj12 = Operateurs.projection(p12, g2);
            if (pproj12.equals(g2.startPoint(), 0.005) || pproj12.equals(g2.endPoint(), 0.005)) {
              pproj12 = p12;
            }
            IDirectPosition pproj13 = Operateurs.projection(p13, g2);
            if (pproj13.equals(g2.startPoint(), 1) || pproj13.equals(g2.endPoint(), 1)) {
              pproj13 = p13;
            }

            IDirectPosition pproj21 = Operateurs.projection(p21, g1);
            if (pproj21.equals(g1.startPoint(), 1) || pproj21.equals(g1.endPoint(), 1)) {
              pproj21 = p21;
            }
            IDirectPosition pproj22 = Operateurs.projection(p22, g1);
            if (pproj22.equals(g1.startPoint(), 0.005) || pproj22.equals(g1.endPoint(), 0.005)) {
              pproj22 = p22;
            }
            IDirectPosition pproj23 = Operateurs.projection(p23, g1);
            if (pproj23.equals(g1.startPoint(), 1) || pproj23.equals(g1.endPoint(), 1)) {
              pproj23 = p23;
            }

            ILineString line11 = new GM_LineString(Arrays.asList(p11, pproj11));
            ILineString line12 = new GM_LineString(Arrays.asList(p12, pproj12));
            ILineString line13 = new GM_LineString(Arrays.asList(p13, pproj13));
            ILineString line14 = new GM_LineString(Arrays.asList(p12, p22));

            ILineString line21 = new GM_LineString(Arrays.asList(p21, pproj21));
            ILineString line22 = new GM_LineString(Arrays.asList(p22, pproj22));
            ILineString line23 = new GM_LineString(Arrays.asList(p23, pproj23));

            ILineString line31 = new GM_LineString(Arrays.asList(p12, p21));
            ILineString line32 = new GM_LineString(Arrays.asList(p12, p22));
            ILineString line33 = new GM_LineString(Arrays.asList(p12, p23));
            ILineString line34 = new GM_LineString(Arrays.asList(p11, p22));
            ILineString line35 = new GM_LineString(Arrays.asList(p12, p22));
            ILineString line36 = new GM_LineString(Arrays.asList(p13, p22));

            List<ILineString> lines = new ArrayList<>(Arrays.asList(line11, line12, line13, line14, line21, line22, line23, line31, line32, line33, line34, line35, line36));

            ILineString lmin = Collections.min(lines, (l1, l2) -> {
              if (l1.startPoint().equals(l1.endPoint(), 0.005)) {
                return 1;
              } else if (l2.startPoint().equals(l2.endPoint(), 0.005)) {
                return -1;
              } else {
                double length1 = l1.length();
                double length2 = l2.length();
                if (length1 > length2) {
                  return 1;
                } else if (length1 < length2) {
                  return -1;
                }
                return 0;
              }
            });

            IFeature f = new DefaultFeature(lmin);
            // IFeature f = new DefaultFeature(new GM_LineString(new DirectPositionList(Arrays.asList(p12,p22))));
            out.add(f);
          }
        }

      }
    }
    ShapefileWriter.write(out, output + "_simplified.shp");
  }

  public List<List<String>> exportWithID(Map<IObservation, Set<IHiddenState>> matching, IPopulation<IFeature> popRef, IPopulation<IFeature> popComp, String idRef, String idComp) {
    IPopulation<IFeature> matches = exportMatching(matching);
    popRef.initSpatialIndex(Tiling.class, false);
    popComp.initSpatialIndex(Tiling.class, false);
    matches.initSpatialIndex(Tiling.class, false);
    List<List<String>> result = new ArrayList<>();
    for (IFeature fref : popRef) {
      for (IFeature m : matches) {
        IPoint p1 = new GM_Point(m.getGeom().coord().get(0));
        IPoint p2 = new GM_Point(m.getGeom().coord().get(1));
        if (p1.distance(fref.getGeom()) < 0.01 || p2.distance(fref.getGeom()) < 0.01) {
          for (IFeature fcomp : popComp) {
            if (p1.distance(fcomp.getGeom()) < 0.01 || p2.distance(fcomp.getGeom()) < 0.01) {
              result.add(Arrays.asList(fref.getAttribute(idRef).toString(), fcomp.getAttribute(idComp).toString()));
              break;
            }
          }
        }
      }
    }
    return result;
  }
}
