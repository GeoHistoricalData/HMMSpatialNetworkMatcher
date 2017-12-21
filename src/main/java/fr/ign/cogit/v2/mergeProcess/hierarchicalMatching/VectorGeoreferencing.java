package fr.ign.cogit.v2.mergeProcess.hierarchicalMatching;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import Jama.Matrix;
import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPositionList;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ILineString;
import fr.ign.cogit.geoxygene.contrib.cartetopo.Arc;
import fr.ign.cogit.geoxygene.contrib.cartetopo.CarteTopo;
import fr.ign.cogit.geoxygene.contrib.cartetopo.Noeud;
import fr.ign.cogit.geoxygene.contrib.geometrie.Angle;
import fr.ign.cogit.geoxygene.contrib.geometrie.Distances;
import fr.ign.cogit.geoxygene.contrib.geometrie.Operateurs;
import fr.ign.cogit.geoxygene.contrib.geometrie.Vecteur;
import fr.ign.cogit.geoxygene.distance.Frechet;
import fr.ign.cogit.geoxygene.feature.DefaultFeature;
import fr.ign.cogit.geoxygene.feature.Population;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPosition;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPositionList;
import fr.ign.cogit.geoxygene.spatial.coordgeom.GM_LineString;
import fr.ign.cogit.geoxygene.util.attribute.AttributeManager;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileReader;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileWriter;
import fr.ign.cogit.geoxygene.util.index.Tiling;
import fr.ign.cogit.morphogenesis.exploring_tool.view.MainFrame.newGraphProjectActionListener;

public class VectorGeoreferencing{


  /**
   * Les deux réseaux utilisés pour le recalage: le réseaux de référence (le plus précis), et le réseau
   * de comparaison qu'on va recaler au mieux sur le réseau de référence.
   * Le recalage n,'est pas un recalage au sens stricte du terme mais plutot un géoréférencement
   * vecteur sur vecteur (on ne cherche pas ici à recaler les arcs sur les arcs, mais uniquement le plus de  sommets sur
   * des sommets, en indentifiant automatiquement des sommets homologues)
   */
  private CarteTopo networkRef, networkComp;

  /**
   * Si on veut utiliser ogr2ogr
   * Si vrai, il faut que le programme soit installé sur la machine. Va créer un fichier temporaire
   */
  private boolean useOgr2Ogr;


  /**
   * Seuils utilisés pour le matching approximatif des arcs
   * Orientation (degrés) et distance de fréchet discrète partielle
   */
  private double orientation_threshold, distance_threshold;
  
  /**
   * Pour donner une idée de l'alteration moyenne engendrée par le recalage
   */
  private double meanLengthAlteration=0., totalLengthAlteration = 0., orientationAlteration = 0.;
  


  public VectorGeoreferencing(String shpRef, String shpComp, boolean useOgr2Ogr, double max_dist_auto_threshold){
    IPopulation<IFeature> in = ShapefileReader.read(shpRef);
    this.networkRef = new CarteTopo("network ref");
    IPopulation<Arc> arcsRef = networkRef.getPopArcs();
    for(IFeature f: in){
      Arc a = arcsRef.nouvelElement();
      a.setGeom(new GM_LineString(f.getGeom().coord()));
      a.addCorrespondant(f);
    }

    this.networkRef.creeTopologieArcsNoeuds(0.);
    this.networkRef.rendPlanaire(0.);
    this.networkRef.creeNoeudsManquants(0.);
    this.networkRef.filtreNoeudsSimples();

    in= ShapefileReader.read(shpComp);
    this.networkComp = new CarteTopo("network comp");
    IPopulation<Arc> arcsComp = networkComp.getPopArcs();
    for(IFeature f: in){
      Arc a = arcsComp.nouvelElement();
      a.setGeom(new GM_LineString(f.getGeom().coord()));
      a.addCorrespondant(f);
    }
    this.networkComp.creeTopologieArcsNoeuds(0.);
    this.networkComp.rendPlanaire(0.);
    this.networkComp.creeNoeudsManquants(0.);
    this.networkComp.filtreNoeudsSimples();
    this.networkRef.getPopArcs().initSpatialIndex(Tiling.class, false);
    this.networkComp.getPopArcs().initSpatialIndex(Tiling.class, false);

    this.useOgr2Ogr = useOgr2Ogr;
    if(!System.getProperty ( "os.name" ).equals("Linux")){
      //si t'es pas sous linux, pour le moment c'ets mort
      this.useOgr2Ogr = false;
    }
    
    this.distance_threshold = max_dist_auto_threshold;
    this.orientation_threshold = 30.;
  }

  public VectorGeoreferencing(CarteTopo networkRef, CarteTopo networkComp, boolean useOgr2Ogr, double max_dist_auto_threshold){
    this.networkComp = networkComp;
    this.networkRef = networkRef;
    if(!this.networkRef.getPopArcs().hasSpatialIndex()){
      this.networkRef.getPopArcs().initSpatialIndex(Tiling.class, false);
    }
    if(!this.networkComp.getPopArcs().hasSpatialIndex()){
      this.networkComp.getPopArcs().initSpatialIndex(Tiling.class, false);
    }
    if(!this.networkRef.getPopNoeuds().hasSpatialIndex()){
      this.networkRef.getPopNoeuds().initSpatialIndex(Tiling.class, false);
    }
    if(!this.networkComp.getPopNoeuds().hasSpatialIndex()){
      this.networkComp.getPopNoeuds().initSpatialIndex(Tiling.class, false);
    }

    this.useOgr2Ogr = useOgr2Ogr;
    if(!System.getProperty ( "os.name" ).equals("Linux")){
      //si t'es pas sous linux, pour le moment c'ets mort
      this.useOgr2Ogr = false;
    }
    this.distance_threshold = max_dist_auto_threshold;
    this.orientation_threshold = 30.;
  }


  /**
   * Appariement brutal des arcs: critères de distance géométrique (fréchet partiel) + orientation
   * @return
   */
  private Map<Arc, Set<Arc>> approximativeEdgesMatching(){
    Map<Arc, Set<Arc>> result = new HashMap<Arc, Set<Arc>>();
    for(Arc aref: this.networkComp.getPopArcs()){
      result.put(aref, new HashSet<Arc>());
      Collection<Arc> candidates = this.networkRef.getPopArcs().select(aref.getGeom(), this.distance_threshold);
           
      
      for(Arc acomp: candidates){
//        if(Frechet.partialFrechet(aref.getGeometrie(), acomp.getGeometrie()) > this.distance_threshold){
//          continue;
//        }
        if(Distances.hausdorff(aref.getGeometrie(), acomp.getGeometrie()) > this.distance_threshold){
          continue;
        }
        ILineString lineRef = aref.getGeometrie();
        IDirectPosition p11 = lineRef.startPoint();
        IDirectPosition p12 = lineRef.endPoint();
        ILineString lineComp = acomp.getGeometrie();
        IDirectPosition p21 = lineComp.startPoint();
        IDirectPosition p22 = lineComp.endPoint();
        IDirectPosition pmin1 = null, pmin2 = null;
        if(p11.distance(p21) < Math.min(p11.distance(p22),
            Math.min(p12.distance(p21), p12.distance(p22)))){
          pmin1 = p11;
          pmin2 = p21;
        }
        else if(p11.distance(p22) < Math.min(p11.distance(p21),
            Math.min(p12.distance(p21), p12.distance(p22)))){
          pmin1 = p11;
          pmin2 = p22;
        }
        else if(p12.distance(p21) < Math.min(p11.distance(p22),
            Math.min(p11.distance(p21), p12.distance(p22)))){
          pmin1 = p12;
          pmin2 = p21;
        }
        else{
          pmin1 = p12;
          pmin2 = p22;
        }
        if(pmin1.equals(p12)){
          lineRef = lineRef.reverse();
        }
        if(pmin2.equals(p22)){
          lineComp = lineComp.reverse();
        }

        Angle angleRef = Operateurs.directionPrincipaleOrientee(lineRef.coord());
        Angle angleComp = Operateurs.directionPrincipaleOrientee(lineComp.coord());
        double value =Angle.ecart(angleRef, angleComp).getValeur();
        if (Math.abs(value)*180./Math.PI > this.orientation_threshold){
          continue;
        }
        result.get(aref).add(acomp);
      }
    }
    return result;
  }



  /**
   * Effecteur le géoréférencement vecteur
   */
  public void vector_georeferencing(){

       
    // Matching approximatif des arcs
    Map<Arc, Set<Arc>> matching = this.approximativeEdgesMatching();
    // Chaque sommets est associés à un vecteur (soit déformation par rapport
    // au sommet apparié, soit inteprolation)
    Map<Noeud, Vecteur> transformations = new HashMap<Noeud, Vecteur>();

    String command ="";

       
    
    // Pour chaque sommet du réseau de comparaison
    for(Noeud ncomp: this.networkComp.getPopNoeuds()) {
      //on va essayer d'apparier ce sommets
      //sélection
      Collection<Noeud> candidates = this.networkRef.getPopNoeuds().select(ncomp.getGeometrie(), this.distance_threshold);
      if(candidates.isEmpty()){
        // pas de sommets candidats
        continue;
      }
      for(Noeud nref: new ArrayList<Noeud>(candidates)){
        // on ne tolère pas plus d'un d'écart entre le degré des sommets candidats
        if(ncomp.getEntrants().size() + ncomp.getSortants().size() !=nref.getEntrants().size()+nref.getSortants().size()){
          candidates.remove(nref);
          continue;
        }
        // Il faut que les arcs incidents aux sommets candidats soient appariés
        Set<Arc> incidents = new HashSet<Arc>();
        incidents.addAll(ncomp.getEntrants());
        incidents.addAll(ncomp.getSortants());
        if(!matching.keySet().containsAll(incidents)){
          // des arcs incidents à nref ne sont pas appariés : on passe
          candidates.clear();
          break;
        }

        boolean ok = true;
        Set<Arc> incidentsMin = new HashSet<Arc>();
        incidentsMin.addAll(nref.getEntrants());
        incidentsMin.addAll(nref.getSortants());
        for(Arc incident: incidents){
          if(incidentsMin.isEmpty()){
            ok = false;
            break;
          }
          Set<Arc> matched = new HashSet<Arc>();
          for(Arc a: matching.get(incident)){
            if(incidentsMin.contains(a)){
              matched.add(a);
            }
          }
          if(matched.size() != 1){
            ok = false;
            break;
          }
          incidentsMin.remove(matched.iterator().next());
        }
        if(!ok){
          candidates.remove(nref);
          continue;
        }
      }
      if(candidates.isEmpty()){
        continue;
      }
      //on va garder uniquement le plus proche
      Noeud nmin = null;
      if(candidates.size() == 1){
        nmin = candidates.iterator().next();
      }
      else{
        double dmin = Double.MAX_VALUE;
        for(Noeud n : candidates){
          double d =n.distance(ncomp);
          if(d< dmin){
            dmin = d;
            nmin = n;
          }
        }
      }
      //on calcul le vecteur entre les deux sommets appariés
      double xs1 = ncomp.getCoord().getX();
      double ys1 = ncomp.getCoord().getY();
      double xt1 = nmin.getCoord().getX();
      double yt1 = nmin.getCoord().getY();

      if(this.useOgr2Ogr){
        command+= " -gcp " + xs1 +" "+ ys1 +" "+xt1 + " " + yt1;
      }
      else{
        transformations.put(ncomp, new Vecteur(xt1-xs1, yt1 - ys1));
      }
    }
    
        
    //pour l'altération...
    double totalLength = 0.;
    double meanorientation = 0.;
    for(Arc a : this.networkComp.getPopArcs()){
      totalLength += a.longueur();
      for(int i=0; i< a.getGeometrie().getControlPoint().size() -1; i++){
        IDirectPositionList listP = new DirectPositionList();
        listP.add(a.getGeometrie().getControlPoint(i));
        listP.add(a.getGeometrie().getControlPoint(i+1));
        meanorientation += Operateurs.directionPrincipale(listP).getValeur()*180./Math.PI;
      }
    }
    meanorientation /= ((double)this.networkComp.getPopArcs().size());
    double meanLength = totalLength / ((double)this.networkComp.getPopArcs().size());

    // on utilise pas ogr2ogr => transo. de helmert locales
    if(!useOgr2Ogr){
            
      for(Noeud n : this.networkComp.getPopNoeuds()){
        if(transformations.containsKey(n)){
          //on avait trouvé un vecteur de déformation pour ce sommet
          IDirectPosition newP = new DirectPosition(n.getCoord().getX() + transformations.get(n).getX(),
              n.getCoord().getY() + transformations.get(n).getY());
          n.setCoord(newP);
        }
        else{
          //sinon interpolation par idw
          double threshold = 50.;
          Collection<Noeud> nodes = this.networkComp.getPopNoeuds().select(n.getCoord(), threshold);
          for(Noeud nn: new ArrayList<Noeud>(nodes)){
            if(!transformations.containsKey(nn)){
              nodes.remove(nn);
            }
          }
          while(nodes.isEmpty()){
            threshold+=50;
            nodes = this.networkComp.getPopNoeuds().select(n.getCoord(), threshold);
            for(Noeud nn: new ArrayList<Noeud>(nodes)){
              if(!transformations.containsKey(nn)){
                nodes.remove(nn);
              }
            }
          }
          Vecteur value = new Vecteur(0.,0.);
          double sum = 0.;
          for(Noeud nn: nodes){
            double d = nn.distance(n);
            d = 1./ Math.pow(d, 0.25);
            sum += d;
            value.setX(value.getX() + d * transformations.get(nn).getX() );
            value.setY(value.getY() + d * transformations.get(nn).getY() );
          }
          value.setX(value.getX() / sum);
          value.setY(value.getY() / sum);
          IDirectPosition newP = new DirectPosition(n.getCoord().getX() + value.getX(),
              n.getCoord().getY() + value.getY());         
          n.setCoord(newP);
        }
      }    

      // on transforme les arcs ...
      for(Arc arc: this.networkComp.getPopArcs()){
        if(arc.getNoeudFin().equals(arc.getNoeudIni())){
          Vecteur t = new Vecteur(arc.getNoeudIni().getCoord().getX() - arc.getGeometrie().getControlPoint(0).getX(),
              arc.getNoeudIni().getCoord().getY() - arc.getGeometrie().getControlPoint(0).getY());
          arc.getGeometrie().setControlPoint(0, arc.getNoeudIni().getCoord());
          arc.getGeometrie().setControlPoint(arc.getGeometrie().getControlPoint().size()-1, arc.getNoeudFin().getCoord());
          for (int i=1; i< arc.getGeometrie().getControlPoint().size()-1; i++) {
            IDirectPosition p = arc.getGeometrie().getControlPoint(i);
            IDirectPosition newP = new DirectPosition(t.getX() + p.getX(), t.getY() +  p.getY() );
            arc.getGeometrie().setControlPoint(i, newP);
          }
        }
        else{
          Matrix X = new Matrix(4, 4);
          X.set(0, 0, arc.getGeometrie().startPoint().getX());
          X.set(1, 0, arc.getGeometrie().startPoint().getY());
          X.set(2, 0, 1);
          X.set(3, 0, 0);
          X.set(0, 1, arc.getGeometrie().startPoint().getY());
          X.set(1, 1, -arc.getGeometrie().startPoint().getX());
          X.set(2, 1, 0);
          X.set(3, 1, 1);
          X.set(0, 2, arc.getGeometrie().endPoint().getX());
          X.set(1, 2,  arc.getGeometrie().endPoint().getY());
          X.set(2, 2, 1);
          X.set(3, 2, 0);
          X.set(0, 3,  arc.getGeometrie().endPoint().getY());
          X.set(1, 3, - arc.getGeometrie().endPoint().getX());
          X.set(2, 3, 0);
          X.set(3, 3, 1);
          Matrix Y = new Matrix(1, 4);
          Y.set(0, 0, arc.getNoeudIni().getCoord().getX());
          Y.set(0, 1, arc.getNoeudIni().getCoord().getY());
          Y.set(0, 2, arc.getNoeudFin().getCoord().getX());
          Y.set(0, 3, arc.getNoeudFin().getCoord().getY());
          Matrix A = Y.times(X.inverse());
          double a = A.get(0, 0);
          double b = A.get(0, 1);
          double c = A.get(0, 2);
          double d = A.get(0, 3);

          arc.getGeometrie().setControlPoint(0, arc.getNoeudIni().getCoord());
          arc.getGeometrie().setControlPoint(arc.getGeometrie().getControlPoint().size()-1, arc.getNoeudFin().getCoord());
          for (int i=1; i< arc.getGeometrie().getControlPoint().size()-1; i++) {
            IDirectPosition p = arc.getGeometrie().getControlPoint(i);
            IDirectPosition newP = new DirectPosition(a * p.getX() + b * p.getY() + c, -b
                * p.getX() + a * p.getY() + d);
            arc.getGeometrie().setControlPoint(i, newP);
          }
        }
      }
      // dernière passe de sécurité au cas ou la topologie aurait sauté ...
      this.networkComp.fusionNoeuds(0.05);
      this.networkComp.creeTopologieArcsNoeuds(0.05);
      this.networkComp.creeNoeudsManquants(0.05);
    }
    else{

      //il faut commencer par sauvegarde la cartetopo dans un shp local en ajoutant un attribut id sur les arcs et les sommets
      String rep = "tmp_" + (int)(Math.random() * 1000000) + "/";
      File frep = new File(rep);
      frep.mkdir();
      String outLocalEdges = rep+"edges_local_" + (int)(Math.random() * 1000000) + ".shp";
      String outLocalNodes = rep + "nodes_local_" + (int)(Math.random() * 1000000) + ".shp";

      String outLocalEdgesGeoreferenced = rep +"edges_localG_" + (Math.random() * 1000000) + ".shp";
      String outLocalNodesGeoreferenced = rep+"nodes_localG_" + (Math.random() * 1000000) + ".shp";

      IPopulation<IFeature> outEdges = new Population<IFeature>();
      Map<Integer, Arc> mappingIdEdges = new HashMap<Integer, Arc>();
      int idE= 1;
      for(Arc a : this.networkComp.getPopArcs()){
        IFeature f = new DefaultFeature(a.getGeometrie());
        AttributeManager.addAttribute(f, "id_local", idE, "Integer");
        mappingIdEdges.put(idE, a);
        outEdges.add(f);
        idE++;
      }

      IPopulation<IFeature> outNodes = new Population<IFeature>();
      Map<Integer, Noeud> mappingIdNodes = new HashMap<Integer, Noeud>();
      int idN= 1;
      for(Noeud n : this.networkComp.getPopNoeuds()){
        IFeature f = new DefaultFeature(n.getGeometrie());
        AttributeManager.addAttribute(f, "id_local", idN, "Integer");
        mappingIdNodes.put(idN, n);
        outNodes.add(f);
        idN++;
      }
      ShapefileWriter.write(outEdges, outLocalEdges);
      ShapefileWriter.write(outNodes, outLocalNodes);

      String commandEdges = "ogr2ogr -f \"ESRI Shapefile\" -overwrite "+ outLocalEdgesGeoreferenced+ " ";
      commandEdges+= command;
      commandEdges += " -tps " + outLocalEdges;
      String commandNodes = "ogr2ogr -f \"ESRI Shapefile\" -overwrite "+ outLocalNodesGeoreferenced+ " ";
      commandNodes+= command;
      commandNodes += " -tps " + outLocalNodes;

      System.out.println("Georeferencing .... ");

      this.execute(commandEdges);
      this.execute(commandNodes);
      System.out.println("Georeferencing done.");

      // puis on charge le fichier géoréférencé
      outEdges.clear();
      outEdges = ShapefileReader.read(outLocalEdgesGeoreferenced);
      outNodes = ShapefileReader.read(outLocalNodesGeoreferenced);

      for(IFeature edge: outEdges){
        int id = Integer.parseInt(edge.getAttribute("id_local").toString());
        Arc arc = mappingIdEdges.get(id);
        arc.setGeometrie(new GM_LineString(edge.getGeom().coord()));
      }

      for(IFeature node : outNodes){
        int id = Integer.parseInt(node.getAttribute("id_local").toString());
        Noeud sommet = mappingIdNodes.get(id);
        sommet.setCoord(node.getGeom().coord().get(0));
      }

      this.networkComp.fusionNoeuds(0.05);
      this.networkComp.creeTopologieArcsNoeuds(0.05);
      this.networkComp.creeNoeudsManquants(0.05);

      File [] fileList = frep.listFiles();
      for(int i = 0;i<fileList.length;i++){
        fileList[i].delete();
      }
      frep.delete();

    }
    
    //pour l'altération...
    double totalLength2 = 0.;
    double meanorientation2 = 0.;
    for(Arc a : this.networkComp.getPopArcs()){
      totalLength2 += a.longueur();
      for(int i=0; i< a.getGeometrie().getControlPoint().size() -1; i++){
        IDirectPositionList listP = new DirectPositionList();
        listP.add(a.getGeometrie().getControlPoint(i));
        listP.add(a.getGeometrie().getControlPoint(i+1));
        meanorientation2 += Operateurs.directionPrincipale(listP).getValeur()*180./Math.PI;
      }
    }
    meanorientation2 /= ((double)this.networkComp.getPopArcs().size());
    double meanLength2 = totalLength2 / ((double)this.networkComp.getPopArcs().size());
    this.totalLengthAlteration = 1. -  totalLength2 / totalLength;
    this.meanLengthAlteration =  meanLength2 - meanLength;
    this.orientationAlteration =  meanorientation2 - meanorientation;
    
    
    System.out.println("Vector georeferencing done.");
    System.out.println("************* ALTERATIONS *****************");
    System.out.println("Total edges length ratio (between -1 et 1): " + this.getTotalLengthAlteration());
    System.out.println("Mean edges length difference: " + this.getMeanLengthAlteration());
    System.out.println("Mean edges orientation (between 0 and pi/2) difference : " + this.getOrientationAlteration());

    
  }


  public double getMeanLengthAlteration() {
    return meanLengthAlteration;
  }

  public void setMeanLengthAlteration(double meanLengthAlteration) {
    this.meanLengthAlteration = meanLengthAlteration;
  }

  public double getTotalLengthAlteration() {
    return totalLengthAlteration;
  }

  public void setTotalLengthAlteration(double totalLengthAlteration) {
    this.totalLengthAlteration = totalLengthAlteration;
  }

  public double getOrientationAlteration() {
    return orientationAlteration;
  }

  public void setOrientationAlteration(double orientationAlteration) {
    this.orientationAlteration = orientationAlteration;
  }

  private void execute(String command){
    Runtime runtime = Runtime.getRuntime();

    try {
      String args[] = { System.getenv("SHELL"), "-c", command};
      final Process process= runtime.exec(args);
      process.waitFor();
      // Consommation de la sortie standard de l'application externe dans un Thread separe
      new Thread() {
        public void run() {
          try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = "";
            try {
              while((line = reader.readLine()) != null) {
                // Traitement du flux de sortie de l'application si besoin est
                System.out.println(line);

              }
            } finally {
              reader.close();
            }
          } catch(IOException ioe) {
            ioe.printStackTrace();
          }
        }
      }.start();

      // Consommation de la sortie d'erreur de l'application externe dans un Thread separe
      new Thread() {
        public void run() {
          try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String line = "";
            try {
              while((line = reader.readLine()) != null) {
                // Traitement du flux d'erreur de l'application si besoin est
                System.out.println(line);
              }
            } finally {
              reader.close();
            }
          } catch(IOException ioe) {
            ioe.printStackTrace();
          }
        }
      }.start();

    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }


  private void writeGeoreferencedNetwork(String shp) {
    ShapefileWriter.write(this.networkComp.getPopArcs(), shp);
  }


  public static void main(String[] args) {



    // TODO Auto-generated method stub

    //    String shp1 = "/media/bcostes/Data/Benoit/these/donnees/vecteur/filaires/filaires_corriges/jacoubet_l93_utf8.shp";
    //    String shp2 = "/media/bcostes/Data/Benoit/these/donnees/vecteur/filaires/filaires_corriges/poubelle_TEMPORAIRE_emprise_utf8_L93_v2.shp";
    String shp1 = "/media/bcostes/Data/Benoit/these/donnees/vecteur/filaires/filaires_corriges/vasserot_jacoubet_l93_utf8_corr.shp";
    //String shp1 = "/home/bcostes/Bureau/output.shp";
    //  String shp1 = "/home/bcostes/Bureau/HMM_matching/test_affine/new_arcs.shp";

    String shp2 = "/media/bcostes/Data/Benoit/these/donnees/vecteur/filaires/filaires_corriges/jacoubet_l93_utf8.shp";


    VectorGeoreferencing vg = new VectorGeoreferencing(shp2, shp1, true, 30.);
    vg.vector_georeferencing();
    vg.writeGeoreferencedNetwork("/home/bcostes/Bureau/edgesG23.shp");


    //    LocalAffineTransformation aft = new LocalAffineTransformation(networkRef, networkComp);
    //
    //    Map<Arc, Set<Arc>> clustersMatching = aft.matchClusters();
    //    aft.local_affine_transformations(clustersMatching);
    //
    //    IPopulation<IFeature> out = new Population<IFeature>();
    //
    //    for(Arc aref : clustersMatching.keySet()){
    //      IDirectPosition p = Operateurs.milieu(aref.getGeometrie());
    //      for(Arc acomp : clustersMatching.get(aref)){
    //
    //        IDirectPosition pproj1 = Operateurs.projection(p, acomp.getGeometrie());
    //
    //        IDirectPositionList list = new DirectPositionList();
    //        list.add(p);
    //        list.add(pproj1);
    //        out.add(new DefaultFeature(new GM_LineString(list)));
    //
    //
    //      }
    //    }
    //    ShapefileWriter.write(out, "/home/bcostes/Bureau/HMM_matching/test_affine/cluster_matching.shp");
    //



  }




}
