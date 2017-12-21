package fr.ign.cogit.v2;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.contrib.cartetopo.Arc;
import fr.ign.cogit.geoxygene.contrib.cartetopo.CarteTopo;
import fr.ign.cogit.geoxygene.spatial.coordgeom.GM_LineString;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileReader;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileWriter;
import nrc.fuzzy.XValuesOutOfOrderException;
import nrc.fuzzy.YValueOutOfRangeException;



public class Test2 {

  public static void main(String[] args) throws XValuesOutOfOrderException, YValueOutOfRangeException {
    


    //    class MapMatching extends HMMMapMatcher{
    //
    //      public MapMatching(IFeatureCollection<? extends IFeature> gpsPop,
    //          IFeatureCollection<? extends IFeature> network, double sigmaZ,
    //          double selection, double beta, double distanceLimit) {
    //        super(gpsPop, network, sigmaZ, selection, beta, distanceLimit);
    //      }
    //
    //      @Override
    //      protected void importNetwork(IFeatureCollection<? extends IFeature> network) {
    //        // TODO Auto-generated method stub
    //        Chargeur.importAsEdges(network, this.getNetworkMap(), "",
    //            null, "", null, null, 0.0);
    //      }
    //
    //    }
    //
    //    final double sigmaZ = 5.;
    //    final  double selection = 15.;
    //    final  double beta =5;
    //    final  double distanceLimit = 30.;
    //
    //   // IFeatureCollection<IFeature> points = ShapefileReader.read("/home/bcostes/Bureau/pointsq.shp");
    //    IPopulation<IFeature> out = new Population<IFeature>();
    //    SnapshotGraph snap = SnapshotIOManager.shp2Snapshot("/home/bcostes/Bureau/t3.shp", new LengthEdgeWeighting(), null, false);
    //    IFeatureCollection<IFeature> network = ShapefileReader.read("/home/bcostes/Bureau/t2.shp");
    //     MapMatching matcher = new MapMatching(new Population<IFeature>(), network, sigmaZ, selection, beta, distanceLimit);
    //    //MapMatching matcher = new MapMatching(points, network, sigmaZ, selection, beta, distanceLimit);
    //
    ////    Node result = matcher.computeTransitions();
    ////
    ////    for(int i=0; i< matcher.getPoints().size(); i++){
    ////      IDirectPositionList list = new DirectPositionList();
    ////      list.add(matcher.getPoints().get(i).getGeom().coord().get(0));
    ////      list.add(Operateurs.milieu(result.getStates().get(i).getGeometrie()));
    ////
    ////      out.add(new DefaultFeature(new GM_LineString(list)));
    ////    }
    //
    //
    //        EnsembleDeLiens liens = new EnsembleDeLiens();
    //    
    //        Map<GraphEntity, IFeature> map = new HashMap<GraphEntity, IFeature>();
    //        for(GraphEntity g: snap.getEdges()){
    //          map.put(g, new DefaultFeature(g.getGeometry().toGeoxGeometry()));
    //        }
    //    
    //    
    //        Map<GraphEntity, Boolean> mapb= new HashMap<GraphEntity, Boolean>();
    //        for(GraphEntity g: map.keySet()){
    //          mapb.put(g, false);
    //        }
    //    
    //        Stack<GraphEntity> stack = new Stack<GraphEntity>();
    //        stack.addAll(snap.getEdges());
    //        IPopulation<IFeature> out2 = new Population<IFeature>();
    //        IPopulation<IFeature> out3 = new Population<IFeature>();
    //    
    //        while(!stack.isEmpty()){
    //          matcher.getPoints().clear();
    //          Set<GraphEntity> path = new HashSet<GraphEntity>();
    //          List<ILineString> lines = new ArrayList<ILineString>();
    //          GraphEntity e1 = stack.pop();
    //          path.add(e1);
    //          List<GraphEntity> vv = new ArrayList<GraphEntity>(snap.getEndpoints(e1));
    //          Collections.shuffle(vv);
    //          GraphEntity v1 = vv.get(0);
    //          ILineString line = (ILineString) map.get(e1).getGeom();
    //    
    //          if(v1.getGeometry().toGeoxGeometry().coord().get(0).distance(line.startPoint())> 
    //          v1.getGeometry().toGeoxGeometry().coord().get(0).distance(line.endPoint())){
    //            line = line.reverse();
    //          }
    //          lines.add(line);
    //          //on fais un chemin aléatoire
    //          int pathSize = 3 + (int)(Math.random() * 17);
    //          for(int i=0; i<pathSize; i++){
    //            List<GraphEntity> set = new ArrayList<GraphEntity>(snap.getIncidentEdges(v1));
    //            set.removeAll(path);
    //            if(set.isEmpty()){
    //              break;
    //            }
    //            Collections.shuffle(set);
    //            GraphEntity newtEdge = set.get(0);
    //            mapb.put(newtEdge, true);
    //            line = (ILineString) map.get(newtEdge).getGeom();
    //            path.add(newtEdge);
    //            stack.remove(newtEdge);
    //            if(v1.getGeometry().toGeoxGeometry().coord().get(0).distance(line.startPoint())> 
    //            v1.getGeometry().toGeoxGeometry().coord().get(0).distance(line.endPoint())){
    //              line = line.reverse();
    //            }
    //            lines.add(line);
    //            if(snap.getEndpoints(newtEdge).getFirst().equals(v1)){
    //              v1 = snap.getEndpoints(newtEdge).getSecond();
    //            }
    //            else{
    //              v1 = snap.getEndpoints(newtEdge).getFirst();
    //            }
    //    
    //          }
    //          if(path.size() <3){
    //            for(GraphEntity g: path){
    //              mapb.put(g,  false);
    //              stack.push(g);
    //            }
    //            Collections.shuffle(stack);
    //            continue;
    //          }
    //    
    //    
    //          // Merge
    //          Map<IFeature, ILineString> mapF = new HashMap<IFeature, ILineString>();
    //          IFeatureCollection<IFeature> gps = new Population<IFeature>();
    //          for(ILineString line2: lines){
    //            double length = line2.length();
    //    
    //    
    //            IDirectPosition p1 = Operateurs.pointEnAbscisseCurviligne(line2, (30.*length/100.));
    //    //        IDirectPosition p2 = Operateurs.pointEnAbscisseCurviligne(line2, (30.*length/100.));
    //    //        IDirectPosition p3 = Operateurs.pointEnAbscisseCurviligne(line2, (40.*length/100.));
    //            IDirectPosition p4 = Operateurs.pointEnAbscisseCurviligne(line2, (50.*length/100.));
    //    //        IDirectPosition p5 = Operateurs.pointEnAbscisseCurviligne(line2, (60.*length/100.));
    //            IDirectPosition p6 = Operateurs.pointEnAbscisseCurviligne(line2, (70.*length/100.));
    //    
    //    
    //    
    //            IFeature f1 = new DefaultFeature(new GM_Point(p1));
    //    //        IFeature f2= new DefaultFeature(new GM_Point(p2));
    //    //        IFeature f3 = new DefaultFeature(new GM_Point(p3));
    //            IFeature f4 = new DefaultFeature(new GM_Point(p4));
    //           // IFeature f5 = new DefaultFeature(new GM_Point(p5));
    //            IFeature f6 = new DefaultFeature(new GM_Point(p6));
    //            mapF.put(f1,line2);
    //            gps.add(f1);
    //    //        mapF.put(f2,line2);
    //    //        gps.add(f2);
    //    //        mapF.put(f3,line2);
    //    //        gps.add(f3);
    //            mapF.put(f4,line2);
    //            gps.add(f4);
    //    //        mapF.put(f5,line2);
    //    //        gps.add(f4);
    //            mapF.put(f6,line2);
    //            gps.add(f6);
    //          }
    //    
    //    
    //    
    //          IFeatureCollection<IFeature> gpsPop2 = new Population<IFeature>();
    //          gpsPop2.addAll(gps);
    //          IFeatureCollection<? extends IFeature> popPts = matcher.getPoints();
    //          @SuppressWarnings("unchecked")
    //          Population<IFeature> p = (Population<IFeature>)popPts;
    //          p.addAll(gpsPop2);
    //    
    //    
    //          out2.addAll(gpsPop2);
    //          while(gpsPop2.size() >2 ){
    //            Node result = matcher.computeTransitions();
    //            if(result == null || result.getStates() == null || result.getStates().isEmpty() ||
    //                matcher.getPoints().size() < 2){
    //              //on supprime le premier points
    //              ILineString ll = mapF.get(gpsPop2.get(0));
    //              Set<IFeature> pts = new HashSet<IFeature>();
    //              for(IFeature pp: mapF.keySet()){
    //                if(mapF.get(pp).equals(ll)){
    //                  pts.add(pp);
    //                }
    //              }
    //              gpsPop2.removeAll(pts);
    //              popPts = matcher.getPoints();
    //              popPts.clear();
    //              @SuppressWarnings("unchecked")
    //              Population<IFeature>p1 = (Population<IFeature>)popPts;
    //              p1.addAll(gpsPop2);
    //              continue;
    //            }
    //            for(int i=0; i< matcher.getPoints().size(); i++){
    //              Arc matchedArc = result.getStates().get(i);
    //              //récupération de la lineString
    //              ILineString line2 = mapF.get(matcher.getPoints().get(i));
    //              IFeature f = null;
    //              for(IFeature ff : map.values()){
    //                if(line2.equals((ILineString)ff.getGeom())){
    //                  f =ff;
    //                  break;
    //                }
    //              }
    //              Lien l = liens.nouvelElement();
    //              l.addObjetRef(f);
    //              l.addObjetComp(matchedArc);
    //    
    //              IDirectPosition pp1 = Operateurs.milieu((ILineString)matchedArc.getGeometrie());
    //              IDirectPositionList list = new DirectPositionList();
    //              list.add(pp1);
    //              list.add(matcher.getPoints().get(i).getGeom().coord().get(0));
    //              out3.add(new DefaultFeature(new GM_LineString(list)));
    //    
    //            }
    //            gpsPop2.removeAll(matcher.getPoints());
    //            popPts = matcher.getPoints();
    //            popPts.clear();
    //            @SuppressWarnings("unchecked")
    //            Population<IFeature> p1 = (Population<IFeature>)popPts;
    //            p1.addAll(gpsPop2);
    //          }
    //    
    //    
    //          //arret ? 
    //          boolean stop = true;
    //          int cpt=0;
    //          for(GraphEntity g: mapb.keySet()){
    //            if(!mapb.get(g)){
    //              stop = false;
    //              cpt++;
    //              // break;
    //            }
    //          }
    //          if(stop){
    //            break;
    //          }
    //        }
    //        IFeatureCollection<IFeature> col = new Population<IFeature>();
    //        col.addAll(map.values());
    //        //liens = liens.regroupeLiens(col, matcher.getNetworkMap().getPopArcs());
    //        liens.creeGeometrieDesLiens();
    //        for(Lien l: liens){
    //          for(IFeature f1: l.getObjetsRef()){
    //            IDirectPosition p1 = Operateurs.milieu((ILineString)f1.getGeom());
    //            for(IFeature f2: l.getObjetsComp()){
    //              IDirectPosition p2 = Operateurs.milieu((ILineString)f2.getGeom());
    //              IDirectPositionList list = new DirectPositionList();
    //              list.add(p1);
    //              list.add(p2);
    //              out.add(new DefaultFeature(new GM_LineString(list)));
    //            }
    //          }
    //        }
    //    
    //    
    //    
    //
    //
    //    //
    //    //
    //    //    snap = SnapshotIOManager.shp2Snapshot("/home/bcostes/Bureau/t2.shp", new LengthEdgeWeighting(), null, false);
    //    //    network = ShapefileReader.read("/home/bcostes/Bureau/t3.shp");
    //    //    matcher = new MapMatching(new Population<IFeature>(), network, sigmaZ, selection, beta, distanceLimit);
    //    //
    //    //
    //    //    EnsembleDeLiens liens2 = new EnsembleDeLiens();
    //    //
    //    //    map = new HashMap<GraphEntity, IFeature>();
    //    //    for(GraphEntity g: snap.getEdges()){
    //    //      map.put(g, new DefaultFeature(g.getGeometry().toGeoxGeometry()));
    //    //    }
    //    //
    //    //
    //    //    mapb= new HashMap<GraphEntity, Boolean>();
    //    //    for(GraphEntity g: map.keySet()){
    //    //      mapb.put(g, false);
    //    //    }
    //    //
    //    //    stack = new Stack<GraphEntity>();
    //    //    stack.addAll(snap.getEdges());
    //    //
    //    //
    //    //    while(!stack.isEmpty()){
    //    //      matcher.getPoints().clear();
    //    //      Set<GraphEntity> path = new HashSet<GraphEntity>();
    //    //      List<ILineString> lines = new ArrayList<ILineString>();
    //    //      GraphEntity e1 = stack.pop();
    //    //      path.add(e1);
    //    //      List<GraphEntity> vv = new ArrayList<GraphEntity>(snap.getEndpoints(e1));
    //    //      Collections.shuffle(vv);
    //    //      GraphEntity v1 = vv.get(0);
    //    //      ILineString line = (ILineString) map.get(e1).getGeom();
    //    //
    //    //      if(v1.getGeometry().toGeoxGeometry().coord().get(0).distance(line.startPoint())> 
    //    //      v1.getGeometry().toGeoxGeometry().coord().get(0).distance(line.endPoint())){
    //    //        line = line.reverse();
    //    //      }
    //    //      lines.add(line);
    //    //      //on fais un chemin aléatoire
    //    //      int pathSize = 3 + (int)(Math.random() * 17);
    //    //      for(int i=0; i<pathSize; i++){
    //    //        List<GraphEntity> set = new ArrayList<GraphEntity>(snap.getIncidentEdges(v1));
    //    //        set.removeAll(path);
    //    //        if(set.isEmpty()){
    //    //          break;
    //    //        }
    //    //        Collections.shuffle(set);
    //    //        GraphEntity newtEdge = set.get(0);
    //    //        mapb.put(newtEdge, true);
    //    //        line = (ILineString) map.get(newtEdge).getGeom();
    //    //        path.add(newtEdge);
    //    //        stack.remove(newtEdge);
    //    //        if(v1.getGeometry().toGeoxGeometry().coord().get(0).distance(line.startPoint())> 
    //    //        v1.getGeometry().toGeoxGeometry().coord().get(0).distance(line.endPoint())){
    //    //          line = line.reverse();
    //    //        }
    //    //        lines.add(line);
    //    //        if(snap.getEndpoints(newtEdge).getFirst().equals(v1)){
    //    //          v1 = snap.getEndpoints(newtEdge).getSecond();
    //    //        }
    //    //        else{
    //    //          v1 = snap.getEndpoints(newtEdge).getFirst();
    //    //        }
    //    //
    //    //      }
    //    //      if(path.size() <3){
    //    //        for(GraphEntity g: path){
    //    //          mapb.put(g,  false);
    //    //          stack.push(g);
    //    //        }
    //    //        Collections.shuffle(stack);
    //    //        continue;
    //    //      }
    //    //
    //    //
    //    //      // Merge
    //    //      Map<IFeature, ILineString> mapF = new HashMap<IFeature, ILineString>();
    //    //      IFeatureCollection<IFeature> gps = new Population<IFeature>();
    //    //      for(ILineString line2: lines){
    //    //        double length = line2.length();
    //    ////
    //    ////
    //    //        IDirectPosition p1 = Operateurs.pointEnAbscisseCurviligne(line2, (10.*length/100.));
    //    ////        IDirectPosition p2 = Operateurs.pointEnAbscisseCurviligne(line2, (30.*length/100.));
    //    ////        IDirectPosition p3 = Operateurs.pointEnAbscisseCurviligne(line2, (40.*length/100.));
    //    //        IDirectPosition p4 = Operateurs.pointEnAbscisseCurviligne(line2, (50.*length/100.));
    //    ////        IDirectPosition p5 = Operateurs.pointEnAbscisseCurviligne(line2, (60.*length/100.));
    //    //        IDirectPosition p6 = Operateurs.pointEnAbscisseCurviligne(line2, (90.*length/100.));
    //    //
    //    //
    //    //
    //    //        IFeature f1 = new DefaultFeature(new GM_Point(p1));
    //    ////        IFeature f2= new DefaultFeature(new GM_Point(p2));
    //    ////        IFeature f3 = new DefaultFeature(new GM_Point(p3));
    //    //        IFeature f4 = new DefaultFeature(new GM_Point(p4));
    //    ////        IFeature f5 = new DefaultFeature(new GM_Point(p5));
    //    //        IFeature f6 = new DefaultFeature(new GM_Point(p6));
    //    //        mapF.put(f1,line2);
    //    //        gps.add(f1);
    //    ////        mapF.put(f2,line2);
    //    ////        gps.add(f2);
    //    ////        mapF.put(f3,line2);
    //    ////        gps.add(f3);
    //    //        mapF.put(f4,line2);
    //    //        gps.add(f4);
    //    ////        mapF.put(f5,line2);
    //    ////        gps.add(f4);
    //    //        mapF.put(f6,line2);
    //    //        gps.add(f6);
    //    //      }
    //    //
    //    //
    //    //
    //    //      IFeatureCollection<IFeature> gpsPop2 = new Population<IFeature>();
    //    //      gpsPop2.addAll(gps);
    //    //      IFeatureCollection<? extends IFeature> popPts = matcher.getPoints();
    //    //      @SuppressWarnings("unchecked")
    //    //      Population<IFeature> p = (Population<IFeature>)popPts;
    //    //      p.addAll(gpsPop2);
    //    //
    //    //
    //    //      out2.addAll(gpsPop2);
    //    //      while(gpsPop2.size() >2 ){
    //    //        Node result = matcher.computeTransitions();
    //    //        if(result == null || result.getStates() == null || result.getStates().isEmpty() ||
    //    //            matcher.getPoints().size() < 2){
    //    //          //on supprime le premier points
    //    //          ILineString ll = mapF.get(gpsPop2.get(0));
    //    //          Set<IFeature> pts = new HashSet<IFeature>();
    //    //          for(IFeature pp: mapF.keySet()){
    //    //            if(mapF.get(pp).equals(ll)){
    //    //              pts.add(pp);
    //    //            }
    //    //          }
    //    //          gpsPop2.removeAll(pts);
    //    //          popPts = matcher.getPoints();
    //    //          popPts.clear();
    //    //          @SuppressWarnings("unchecked")
    //    //          Population<IFeature>p1 = (Population<IFeature>)popPts;
    //    //          p1.addAll(gpsPop2);
    //    //          continue;
    //    //        }
    //    //        for(int i=0; i< matcher.getPoints().size(); i++){
    //    //          Arc matchedArc = result.getStates().get(i);
    //    //          //récupération de la lineString
    //    //          ILineString line2 = mapF.get(matcher.getPoints().get(i));
    //    //          IFeature f = null;
    //    //          for(IFeature ff : map.values()){
    //    //            if(line2.equals((ILineString)ff.getGeom())){
    //    //              f =ff;
    //    //              break;
    //    //            }
    //    //          }
    //    //          Lien l = liens2.nouvelElement();
    //    //          l.addObjetRef(f);
    //    //          l.addObjetComp(matchedArc);
    //    //
    //    //          IDirectPosition pp1 = Operateurs.milieu((ILineString)matchedArc.getGeometrie());
    //    //          IDirectPositionList list = new DirectPositionList();
    //    //          list.add(pp1);
    //    //          list.add(matcher.getPoints().get(i).getGeom().coord().get(0));
    //    //          out3.add(new DefaultFeature(new GM_LineString(list)));
    //    //
    //    //        }
    //    //        gpsPop2.removeAll(matcher.getPoints());
    //    //        popPts = matcher.getPoints();
    //    //        popPts.clear();
    //    //        @SuppressWarnings("unchecked")
    //    //        Population<IFeature> p1 = (Population<IFeature>)popPts;
    //    //        p1.addAll(gpsPop2);
    //    //      }
    //    //
    //    //
    //    //      //arret ? 
    //    //      boolean stop = true;
    //    //      int cpt=0;
    //    //      for(GraphEntity g: mapb.keySet()){
    //    //        if(!mapb.get(g)){
    //    //          stop = false;
    //    //          cpt++;
    //    //          // break;
    //    //        }
    //    //      }
    //    //      if(stop){
    //    //        break;
    //    //      }
    //    //    }
    //    //    col = new Population<IFeature>();
    //    //    col.addAll(map.values());
    //    //    //liens2 = liens2.regroupeLiens(col, matcher.getNetworkMap().getPopArcs());
    //    //    liens2.creeGeometrieDesLiens();
    //    ////    for(Lien l: liens2){
    //    ////      for(IFeature f1: l.getObjetsRef()){
    //    ////        IDirectPosition p1 = Operateurs.milieu((ILineString)f1.getGeom());
    //    ////        for(IFeature f2: l.getObjetsComp()){
    //    ////          IDirectPosition p2 = Operateurs.milieu((ILineString)f2.getGeom());
    //    ////          IDirectPositionList list = new DirectPositionList();
    //    ////          list.add(p1);
    //    ////          list.add(p2);
    //    ////          out.add(new DefaultFeature(new GM_LineString(list)));
    //    ////        }
    //    ////      }
    //    ////    }
    //    //
    //    //
    //    //
    //    //
    //    //    EnsembleDeLiens liensFinaux = new EnsembleDeLiens();
    //    //    liensFinaux = EnsembleDeLiens.compile(liens, liens2);
    //    ////    for(Lien lien: liens){
    //    ////      IFeature fref = lien.getObjetsRef().get(0);
    //    ////      IFeature fcomp = lien.getObjetsComp().get(0);
    //    ////
    //    ////      for(Lien lien2: liens2){
    //    ////        IFeature fref2 = lien2.getObjetsRef().get(0);
    //    ////        IFeature fcomp2 = lien2.getObjetsComp().get(0);
    //    ////        if(fref.getGeom().equals(fcomp2.getGeom()) && fcomp.getGeom().equals(fref2.getGeom())){
    //    ////            liensFinaux.add(lien);
    //    ////            break;
    //    ////        }
    //    ////      }
    //    ////    }
    //    //
    //    //
    //    //     System.out.println(liens.size()+" "+ liens2.size());
    //    //
    //    //for(Lien l: liensFinaux){
    //    //      for(IFeature f1: l.getObjetsRef()){
    //    //        IDirectPosition p1 = Operateurs.milieu((ILineString)f1.getGeom());
    //    //        for(IFeature f2: l.getObjetsComp()){
    //    //          IDirectPosition p2 = Operateurs.milieu((ILineString)f2.getGeom());
    //    //          IDirectPositionList list = new DirectPositionList();
    //    //          list.add(p1);
    //    //          list.add(p2);
    //    //          out.add(new DefaultFeature(new GM_LineString(list)));
    //    //        }
    //    //      }
    //    //    }
    //    //
    //    //
    //    //
    //    //    System.out.println(liensFinaux.size());
    //
    //    //sens inverse
    //
    //    ShapefileWriter.write(out, "/home/bcostes/Bureau/tttttt2.shp");
    //    ShapefileWriter.write(out2, "/home/bcostes/Bureau/point32.shp");
    //    ShapefileWriter.write(out3, "/home/bcostes/Bureau/match2.shp");
    // System.out.println(liens.size()+" "+ liens2.size());



    //    
    //    MapMatching matcher = new MapMatching(gpsPop, network, sigmaZ, selection, beta, distanceLimit);
    //    IPopulation<IFeature> out = new Population<IFeature>();
    //    IFeatureCollection<IFeature> gpsPop2 = new Population<IFeature>();
    //    gpsPop2.addAll(gpsPop);
    //    while(gpsPop2.size() >2 ){
    //      System.out.println("it : " + matcher.getPoints().size());
    //      
    //      Node result = matcher.computeTransitions();
    //      
    //
    //      if(result == null || result.getStates() == null || result.getStates().isEmpty() ||
    //          matcher.getPoints().size() < 2){
    //        //on supprime le premier points
    //        gpsPop2.remove(0);
    //        IFeatureCollection<? extends IFeature> popPts = matcher.getPoints();
    //        popPts.clear();
    //        @SuppressWarnings("unchecked")
    //        Population<IFeature> p = (Population<IFeature>)popPts;
    //        p.addAll(gpsPop2);
    //        continue;
    //      }
    //      
    //
    //      //            for(ILineString l :result.getGeometry()){
    //      //            out.add(new DefaultFeature(l));
    //      //            }
    //      //            ShapefileWriter.write(out, "/home/bcostes/Bureau/test.shp");
    //
    //      System.out.println("yeah");
    //      // récup de l'arc apparié
    //      for(int i=0; i< matcher.getPoints().size(); i++){
    //        Arc matchedArc = result.getStates().get(i);
    //        out.add(matchedArc);
    //      }
    //      gpsPop2.removeAll(matcher.getPoints());
    //      IFeatureCollection<? extends IFeature> popPts = matcher.getPoints();
    //      popPts.clear();
    //      @SuppressWarnings("unchecked")
    //      Population<IFeature> p = (Population<IFeature>)popPts;
    //      p.addAll(gpsPop2);
    //    }
    //    
    //   
    //    System.out.println(out.size());
    //    ShapefileWriter.write(out, "/home/bcostes/Bureau/match.shp");
    //    



    //    
    //        String inputStg ="/media/bcostes/Data/Benoit/these/analyses/TAG/TAG_v2/construction/etape2/tag_new.tag";
    //        STGraph stg = TAGIoManager.deserialize(inputStg);
    //        System.out.println(stg.getEdgeCount());
    //        System.exit(0);
    //        Map<FuzzyTemporalInterval, Double> accuraciesMap = new HashMap<FuzzyTemporalInterval, Double>();
    //        accuraciesMap.put(new FuzzyTemporalInterval(new double[]{1784,1785,1789,1791},new double[]{0,1,1,0}, 4),
    //            1.);
    //        /* accuraciesMap.put(new FuzzyTemporalInterval(new double[]{1808,1810,1836,1853},new double[]{0,1,1,0}, 4),
    //                    1.);*/ //0.7
    //        accuraciesMap.put(new FuzzyTemporalInterval(new double[]{1825,1827,1836,1839},new double[]{0,1,1,0}, 4),
    //            1.);
    //        accuraciesMap.put(new FuzzyTemporalInterval(new double[]{1848,1849,1849,1850},new double[]{0,1,1,0}, 4),
    //            1.);
    //        accuraciesMap.put(new FuzzyTemporalInterval(new double[]{1870,1871,1871,1872},new double[]{0,1,1,0}, 4),
    //            1.);
    //        accuraciesMap.put(new FuzzyTemporalInterval(new double[]{1887,1888,1888,1889},new double[]{0,1,1,0}, 4),
    //            1.);
    //        STProperty<Double> accuracies = new STProperty<Double>(STProperty.PROPERTIE_FINAL_TYPES.Attribute, "Accuracies");
    //        accuracies.setValues(accuraciesMap);
    //        stg.setAccuracies(accuracies);
    //        stg.updateGeometries();
    //        TAGIoManager.exportTAG(stg,"/home/bcostes/Bureau/tst.shp" );
    //    

    // b.buildLinesClusters(roads, threshold, principle)
    //    IPopulation<IFeature> poly = ShapefileReader.read("/home/bcostes/Bureau/ilots_simpli.shp");
    //    IPopulation<IFeature> lines = ShapefileReader.read("/home/bcostes/Bureau/pts-Voronoï Segments extraits.shp");
    //
    //
    //    poly.initSpatialIndex(Tiling.class, false);
    //    lines.initSpatialIndex(Tiling.class, false);
    //    IPopulation<IFeature> out = new Population<IFeature>();
    //
    //    for(IFeature line: lines){
    //      System.out.println(line);
    //      Collection<IFeature> polyClose = poly.select(line.getGeom(), 0);
    //      if(polyClose.isEmpty()){
    //        out.add(line);
    //        continue;
    //      }
    //      boolean selected = false;
    //      for(IFeature pol : polyClose){
    //        if(pol.getGeom().intersects(line.getGeom())){
    //          selected = true;
    //          break;
    //
    //        }
    //        if(!selected){ 
    //          out.add(line);
    //        }
    //      }
    //    }
    //    ShapefileWriter.write(out, "/home/bcostes/Bureau/test2.shp");


    // IPopulation<IFeature> poly = ShapefileReader.read("/home/bcostes/Bureau/pol.shp");
    //  poly.initSpatialIndex(Tiling.class,false);
    //  
    //  IPopulation<IFeature> out = new Population<IFeature>();
    //  for(IFeature f : poly){
    //    IDirectPositionList coords = f.getGeom().coord();
    //    ILineString lSampled = Operateurs.resampling(new GM_LineString(coords), 1);
    //    for(IDirectPosition pt: lSampled.getControlPoint()){
    //      out.add(new DefaultFeature(new GM_Point(pt)));
    //    }
    //  }
    //  ShapefileWriter.write(out, "/home/bcostes/Bureau/pts05.shp");
    //  System.exit(0);
    // System.exit(0);

    //  VoronoiDiagramBuilder vbuilder = new VoronoiDiagramBuilder();
    //
    //  List<Geometry> coordinates = new ArrayList<Geometry>();
    //  GeometryFactory factory = new GeometryFactory();
    //  for(IFeature f  : poly){
    //    IDirectPositionList coords = f.getGeom().coord();
    //    ILineString lSampled = Operateurs.resampling(new GM_LineString(coords), 2.5);
    //    for(IDirectPosition pt: lSampled.getControlPoint()){
    //      Coordinate c = new Coordinate(pt.getX(), pt.getY());
    //      coordinates.add(factory.createPoint(c));
    //    }
    //  }
    //  Geometry[] coordinatesTab = new Geometry[coordinates.size()];
    //  coordinatesTab = coordinates.toArray(coordinatesTab);
    //  GeometryCollection points = new GeometryCollection(coordinatesTab, factory);
    //
    //  vbuilder.setSites(points);
    //  vbuilder.setClipEnvelope(points.getEnvelopeInternal());
    //  vbuilder.setTolerance(1.);
    //  GeometryCollection  diagram = (GeometryCollection)vbuilder.getDiagram(factory);
    //  System.out.println("ok");
    //  IPopulation<IFeature> lines = new Population<IFeature>();
    //  lines.initSpatialIndex(Tiling.class, false);
    //  for(int i=0; i< diagram.getNumGeometries(); i++){
    //    Polygon p = (Polygon) diagram.getGeometryN(i);
    //    a: for(int j=0; j< p.getNumPoints()-1; j++){
    //      Coordinate p1 = p.getCoordinates()[j];
    //      Coordinate p2 = p.getCoordinates()[j+1];
    //      IDirectPositionList list= new DirectPositionList();
    //      list.add(new DirectPosition(p1.x, p1.y));
    //      list.add(new DirectPosition(p2.x, p2.y));
    //      ILineString l = new GM_LineString(list);
    //      Collection<IFeature> polyClose = poly.select(l, 1);
    //      for(IFeature pol : polyClose){
    //        if(pol.getGeom().intersects(l)){
    //          continue a;
    //        }
    //      }
    //      //            Collection<IFeature> linesIn = lines.select(l,1);
    //      //            for(IFeature f: linesIn){
    //        //              ILineString ll = (ILineString)f.getGeom();
    //      //              if(ll.startPoint().equals(l.startPoint(),0.5) && ll.endPoint().equals(l.endPoint(),0.5) ||
    //      //                  ll.startPoint().equals(l.endPoint(),0.5) && ll.endPoint().equals(l.startPoint(),0.5)){
    //      //                continue a;
    //      //              }
    //      //            }
    //      lines.add(new DefaultFeature(l));
    //    }
    //    // lines.initSpatialIndex(Tiling.class, false);
    //  }

    //
    //  TriangulationJTS jts = new TriangulationJTS("");
    //  jts.importAsNodes(points);
    //  try {
    //    jts.triangule();
    //  } catch (Exception e) {
    //    e.printStackTrace();
    //  }
    //ShapefileWriter.write(lines, "/home/bcostes/Bureau/voronoi_lines.shp");


    //      IPopulation<IFeature> lines = ShapefileReader.read("/home/bcostes/Bureau/voronoi_lines.shp");
    //      lines.initSpatialIndex(Tiling.class, false);
    //      IPopulation<IFeature> newLines = new Population<IFeature>();
    //      int cpt=0;
    //      a: for(IFeature f : lines){
    //        cpt++;
    //        if(cpt%100 == 0){
    //          System.out.println(cpt +" / " + lines.size());
    //        }
    //        if(newLines.isEmpty()){
    //          newLines.add(f);
    //          continue;
    //        }
    //        ILineString l = new GM_LineString(f.getGeom().coord());
    //        Collection<IFeature> c = newLines.select(f.getGeom(), 0.005);
    //        for(IFeature ff: c){
    //          if(f.equals(ff)){
    //            continue;
    //          }
    //          ILineString ll = new GM_LineString(ff.getGeom().coord());
    //          if(ll.startPoint().equals(l.startPoint(),0.5) && ll.endPoint().equals(l.endPoint(),0.5) ||
    //              ll.startPoint().equals(l.endPoint(),0.5) && ll.endPoint().equals(l.startPoint(),0.5)){
    //            continue a;
    //          }
    //        }
    //        newLines.add(f);
    //        newLines.initSpatialIndex(Tiling.class, false);
    //      }
    //  
    //      CarteTopo map = new CarteTopo("");
    //      IPopulation<Arc> arcs = map.getPopArcs();
    //      for(IFeature f: newLines){
    //        Arc a = arcs.nouvelElement();
    //        a.setOrientation(2);
    //        a.setGeom(new GM_LineString(f.getGeom().coord()));
    //        a.addCorrespondant(f);
    //      }
    //  
    //      map.creeTopologieArcsNoeuds(0);
    //      map.creeNoeudsManquants(0);
    //      map.filtreArcsDoublons();
    //      map.filtreNoeudsIsoles();
    //      map.filtreNoeudsSimples();
    //  
    //      ShapefileWriter.write(map.getPopArcs(), "/home/bcostes/Bureau/test_lines_topo.shp");

    IPopulation<IFeature> poly = ShapefileReader.read("/home/bcostes/Bureau/tt_verniquet.shp");
    CarteTopo map = new CarteTopo("");
    IPopulation<Arc> arcs = map.getPopArcs();
    for(IFeature f: poly){
      Arc a = arcs.nouvelElement();
      a.setOrientation(2);
      a.setGeom(new GM_LineString(f.getGeom().coord()));
      a.addCorrespondant(f);
    }
    //      
    map.creeTopologieArcsNoeuds(0);
    map.creeNoeudsManquants(0);

    map.rendPlanaire(0);
    map.creeTopologieArcsNoeuds(0);
    map.filtreNoeudsSimples();

    map.fusionNoeuds(2.);
    map.fusionNoeuds(2.);
    map.fusionNoeuds(2.);
    map.fusionNoeuds(2.);
    map.fusionNoeuds(2.);
    map.fusionNoeuds(2.);
    map.fusionNoeuds(2.);
    map.fusionNoeuds(2.);
    map.fusionNoeuds(2.);
    map.fusionNoeuds(2.);
    map.fusionNoeuds(2.);
    map.fusionNoeuds(2.);
    map.fusionNoeuds(2.);
    map.fusionNoeuds(2.);
    map.fusionNoeuds(2.);
    map.fusionNoeuds(2.);
    map.fusionNoeuds(2.);
    map.fusionNoeuds(2.);
    map.fusionNoeuds(2.);
    map.fusionNoeuds(2.);
    map.fusionNoeuds(2.);
    map.fusionNoeuds(2.);
    map.fusionNoeuds(2.);
    map.fusionNoeuds(2.);
    map.fusionNoeuds(2.);
    

    map.filtreNoeudsSimples();
    map.filtreArcsDoublons(0);
    map.filtreNoeudsSimples();
    map.filtreNoeudsIsoles();


//    Set<Arc> aaa  = new HashSet<Arc>();
//
//    for(Arc a : new ArrayList<Arc>(map.getPopArcs())){
//      if(a.getNoeudIni().getEntrants().size()+a.getNoeudIni().getSortants().size() == 1 ||
//          a.getNoeudFin().getEntrants().size()+a.getNoeudFin().getSortants().size() == 1  ){
//        if(a.longueur() <40){
//          aaa.add(a);
//        }
//      }
//    }
//    while(!aaa.isEmpty()){
//      map.enleveArcs(aaa);
//      aaa.clear();
//      for(Arc a : new ArrayList<Arc>(map.getPopArcs())){
//        if(a.getNoeudIni().getEntrants().size()+a.getNoeudIni().getSortants().size() == 1 ||
//            a.getNoeudFin().getEntrants().size()+a.getNoeudFin().getSortants().size() == 1  ){
//          if(a.longueur() <40){
//            aaa.add(a);
//          }
//        }
//      }
//    }
//    aaa.clear();
//    for(Arc a : new ArrayList<Arc>(map.getPopArcs())){
//      if(a.getNoeudIni().getEntrants().size()+a.getNoeudIni().getSortants().size() == 1 ||
//          a.getNoeudFin().getEntrants().size()+a.getNoeudFin().getSortants().size() == 1  ){
//        if(a.longueur() <40){
//          aaa.add(a);
//        }
//      }
//    }
//    map.enleveArcs(aaa);
//
//
//
//
//    for(Arc a: map.getPopArcs()){
//      a.setGeometrie(GaussianFilter.gaussianFilter(a.getGeometrie(),10, 10));
//      IDirectPositionList c = a.getGeometrie().coord();
//
//      GeometryUtils.filterLowAngles(c, 10.);
//      GeometryUtils.filterLargeAngles(c, 170.);
//      a.setGeometrie(GaussianFilter.gaussianFilter(a.getGeometrie(),10, 10));
//
//      a.setGeometrie(new GM_LineString(c));
//
//    }       
//    map.fusionNoeuds(1);
//    map.rendPlanaire(0);
//    map.filtreNoeudsSimples();

    //                map.fusionNoeuds(2);
    //                
    //
    //                for(Arc a: map.getPopArcs()){
      //                 a.setGeometrie(GaussianFilter.gaussianFilter(a.getGeometrie(),10, 10));
      //                 IDirectPositionList c = a.getGeometrie().coord();
      //
      //                 GeometryUtils.filterLowAngles(c, 10.);
      //                 GeometryUtils.filterLargeAngles(c, 170.);
      //                 a.setGeometrie(GaussianFilter.gaussianFilter(a.getGeometrie(),10, 10));
      //
      //                 a.setGeometrie(new GM_LineString(c));
      //                 
    //                }
    ShapefileWriter.write(map.getPopArcs(), "/home/bcostes/Bureau/test.shp");


    //  }
  }
}
