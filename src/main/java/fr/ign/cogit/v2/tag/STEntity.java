package fr.ign.cogit.v2.tag;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ILineString;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPosition;
import fr.ign.cogit.v2.geometry.LightDirectPosition;
import fr.ign.cogit.v2.geometry.LightGeometry;
import fr.ign.cogit.v2.geometry.LightLineString;
import fr.ign.cogit.v2.mergeProcess.mergeGeometries.AbstractMergeAlgorithm;
import fr.ign.cogit.v2.mergeProcess.mergeGeometries.MatchingHomologousPointsMerge;
import fr.ign.cogit.v2.mergeProcess.mergeGeometries.snapping.AbstractSnappingAlgorithm;
import fr.ign.cogit.v2.mergeProcess.mergeGeometries.snapping.HelmertSnapping;
import fr.ign.cogit.v2.snapshot.GraphEntity;
import fr.ign.cogit.v2.tag.enrichment.JSonAttribute;
import fr.ign.cogit.v2.utils.GeometryUtils;


/**
 * Modèle d'une entité spatio-temporelle (sommet ou arc)
 * @author bcostes
 *
 */
public class STEntity implements Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = 6510848314713029335L;

  /**
   * identifiant unique
   */
  private int id;

  /**
   * géométrie fusionnée (allégée) 
   */
  private LightGeometry geometryMerged;
  /**
   * Géométries (allégées) pour chaque date
   */
  private STProperty<LightGeometry> Tgeometry;
  /**
   * Poids aux différentes dates
   */
  private STProperty<Double> Tweights;
  /**
   * Les indicateurs (locaux) pour chaque date
   */
  private Set<STProperty<Double>> TIndicators;
  /**
   * Les attributs temporels
   */
  private Set<STProperty<String>> TAttributes;
  /**
   * Domaine d'existence de l'entité ST (série temporelle)
   */
  private STProperty<Boolean> timeSerie;
  /**
   * Type de l'entité : arc ou sommet
   */
  private int type;

  /**
   * Présence d'une transformation de la rue: alignement et/ou élargissement
   */
  private STProperty<Transformation> transformations;
  
  /**
   * Propriétés json attachées au STEntity (données extérieurs, comme traavaux de voirie polygonales par ex)
   */
  private Set<JSonAttribute> jsonAttributes;

  /**
   * Variables utilisées pour l'attribution d'un identifiant unique à chaque entité
   */
  public static final int NODE = 1000000;
  public static final int EDGE = 2000000;
  private static int _IDNODE = NODE;
  private static int _IDEDGE = EDGE;
  private static int CURRENT_TYPE = STEntity.NODE;

  public STEntity(){
    this.id = (CURRENT_TYPE == STEntity.NODE ? _IDNODE : _IDEDGE);
    this.type = CURRENT_TYPE;
    updateID();
    this.timeSerie = new STProperty<Boolean>(STProperty.PROPERTIE_FINAL_TYPES.TimeSerie, null);
    this.setTGeometries(new STProperty<LightGeometry>(STProperty.PROPERTIE_FINAL_TYPES.Geometry, null));
    this.setTWeight(new STProperty<Double>(STProperty.PROPERTIE_FINAL_TYPES.Weight, null));
    this.setTIndicators(new HashSet<STProperty<Double>>());
    this.setTAttributes(new HashSet<STProperty<String>>());
    this.transformations = new STProperty<Transformation>(STProperty.PROPERTIE_FINAL_TYPES.Transformation, null);
    this.setJsonAttributes(new HashSet<JSonAttribute>());
  }



  public STEntity(STProperty<Boolean> timeSerie) {
    this.id = (CURRENT_TYPE == STEntity.NODE ? _IDNODE : _IDEDGE);
    this.type = CURRENT_TYPE;
    this.timeSerie = new STProperty<Boolean>(STProperty.PROPERTIE_FINAL_TYPES.TimeSerie, null);
    this.timeSerie.setValues(timeSerie.copy().getValues());
    this.setTGeometries(new STProperty<LightGeometry>(STProperty.PROPERTIE_FINAL_TYPES.Geometry, null));
    for (FuzzyTemporalInterval t : timeSerie.getValues().keySet()) {
      this.Tgeometry.setValueAt(t, null);
    }
    this.setTWeight(new STProperty<Double>(STProperty.PROPERTIE_FINAL_TYPES.Weight, null));
    for (FuzzyTemporalInterval t : timeSerie.getValues().keySet()) {
      this.Tweights.setValueAt(t, null);
    }
    this.setTIndicators(new HashSet<STProperty<Double>>());
    this.setTAttributes(new HashSet<STProperty<String>>());
    this.transformations = new STProperty<Transformation>(STProperty.PROPERTIE_FINAL_TYPES.Transformation, null);
    for (FuzzyTemporalInterval t : timeSerie.getValues().keySet()) {
      this.transformations.setValueAt(t, Transformation.NONE);
    }
    this.setJsonAttributes(new HashSet<JSonAttribute>());
    updateID();
  }

  public STEntity(int type, STProperty<Boolean> timeSerie) {
    if (type != STEntity.EDGE && type != STEntity.NODE) {
      return;
    }
    setCurrentType(type);
    this.id = CURRENT_TYPE == STEntity.NODE ? _IDNODE : _IDEDGE;
    this.type = CURRENT_TYPE;
    this.timeSerie = new STProperty<Boolean>(STProperty.PROPERTIE_FINAL_TYPES.TimeSerie, null);
    this.timeSerie.setValues(timeSerie.copy().getValues());
    this.setTGeometries(new STProperty<LightGeometry>(STProperty.PROPERTIE_FINAL_TYPES.Geometry, null));
    for (FuzzyTemporalInterval t : timeSerie.getValues().keySet()) {
      this.Tgeometry.setValueAt(t, null);
    }
    this.setTWeight(new STProperty<Double>(STProperty.PROPERTIE_FINAL_TYPES.Weight, null));
    for (FuzzyTemporalInterval t : timeSerie.getValues().keySet()) {
      this.Tweights.setValueAt(t, null);
    }
    for (FuzzyTemporalInterval t : timeSerie.getValues().keySet()) {
      this.transformations.setValueAt(t, Transformation.NONE);
    }
    this.setTIndicators(new HashSet<STProperty<Double>>());
    this.setTAttributes(new HashSet<STProperty<String>>());
    this.setJsonAttributes(new HashSet<JSonAttribute>());
    updateID();
  }

  //*********************************************************************************************
  //************************************* Identifiant *******************************************
  //*********************************************************************************************

  private static void updateID() {
    if (CURRENT_TYPE == STEntity.NODE) {
      _IDNODE++;
    } else {
      _IDEDGE++;
    }
  }

  public static void updateIDS(int IDNODE, int IDEDGE) {
    _IDEDGE = 2000000 + IDEDGE +1 ;
    _IDNODE = 1000000 + IDNODE +1 ;
  }

  public void setId(int id) {
    this.id = id;
  }

  public int getId() {
    if(type == STEntity.EDGE){
      return (this.id - STEntity.EDGE);
    }
    else{
      return (this.id - STEntity.NODE);
    }
  }

  //*********************************************************************************************
  //************************************* Type **************************************************
  //*********************************************************************************************

  public static int getCurrentType() {
    return CURRENT_TYPE;
  }

  public static void switchCurrentType() {
    if (CURRENT_TYPE == STEntity.NODE) {
      CURRENT_TYPE = STEntity.EDGE;
    } else {
      CURRENT_TYPE = STEntity.NODE;
    }
  }

  public static void setCurrentType(int type) {
    if (type != STEntity.EDGE && type != STEntity.NODE) {
      return;
    }
    CURRENT_TYPE = type;
  }

  public void setType(int type) {
    this.type = type;
  }

  public int getType() {
    return type;
  }

  //*********************************************************************************************
  //************************************* Time Serie ********************************************
  //*********************************************************************************************


  public void setTimeSerie(STProperty<Boolean> timeSerie) {
    this.timeSerie = new STProperty<Boolean>(STProperty.PROPERTIE_FINAL_TYPES.TimeSerie, null);
    this.timeSerie.setValues(timeSerie.copy().getValues());
  }


  public STProperty<Boolean> getTimeSerie() {
    return timeSerie;
  }

  public boolean existsAt(FuzzyTemporalInterval t) {
    return this.timeSerie.getValueAt(t);
  }

  public void existsAt(FuzzyTemporalInterval t, boolean exists) {
    this.timeSerie.setValueAt(t, exists);
  }


  //*********************************************************************************************
  //*********************************** Merged Geometry *****************************************
  //*********************************************************************************************

  public void setGeometry(LightGeometry geometry) {
    this.geometryMerged = geometry;
  }

  public LightGeometry getGeometry() {
    return geometryMerged;
  }

  //*********************************************************************************************
  //************************************* STAttributes ******************************************
  //*********************************************************************************************

  public Set<STProperty<String>> getTAttributes() {
    return this.TAttributes;
  }

  public void setTAttributes(Set<STProperty<String>> tAttributes) {
    this.TAttributes = tAttributes;
  }

  /**
   * Cherche un STAttribute par so nom
   * Renvoie null si n'existe pas
   * @param name
   * @return
   */
  public STProperty<String> getTAttributeByName(String name){
    for(STProperty<String> statt: this.TAttributes){
      if(statt.getName().toLowerCase().equals(name.toLowerCase())){
        return statt;
      }
    }
    return null;
  }



  //*********************************************************************************************
  //************************************* STIndicators ******************************************
  //*********************************************************************************************

  public Set<STProperty<Double>> getTIndicators() {
    return this.TIndicators;
  }

  public void setTIndicators(Set<STProperty<Double>> tIndicators) {
    this.TIndicators = tIndicators;
  }

  /**
   * Cherche un STIndicator par son nom
   * Renvoie null si n'existe pas
   * @param name
   * @return
   */
  public STProperty<Double> getTIndicatorByName(String name){
    for(STProperty<Double> stind: this.TIndicators){
      if(stind.getName().toLowerCase().equals(name.toLowerCase())){
        return stind;
      }
    }
    return null;
  }

  /** Valeur du STIndicator dont le nom est name à la date t
   * @param name
   * @param t
   * @return
   */
  public Double getIndicatorAt(String name, FuzzyTemporalInterval t){
    STProperty<Double> stind = this.getTIndicatorByName(name);
    if(stind == null){
      return null;
    }
    return stind.getValueAt(t);
  }

  /**
   * Met à jour la valeur d'un indicateur pour une date t donnée
   * @param name
   * @param t
   * @param value
   */
  public void setIndicatorAt(String name, FuzzyTemporalInterval t, double value){
    STProperty<Double> stind = this.getTIndicatorByName(name);
    if(stind == null){
      return;
    }
    stind.setValueAt(t, value);
  }



  //*********************************************************************************************
  //************************************* STGeometries ******************************************
  //*********************************************************************************************

  public STProperty<LightGeometry> getTGeometry() {
    return this.Tgeometry;
  }

  public void setTGeometries(STProperty<LightGeometry>  tgeometry) {
    this.Tgeometry = tgeometry;
  }

  /**
   * LightGeometry à la date t
   * @param t
   * @return
   */
  public LightGeometry getGeometryAt(FuzzyTemporalInterval t){
    return this.Tgeometry.getValueAt(t);
  }

  /**
   * Met à jour la valeur d'un indicateur pour une date t donnée
   * @param name
   * @param t
   * @param value
   */
  public void setGeometryAt(FuzzyTemporalInterval t, LightGeometry geom){
    this.Tgeometry.setValueAt(t, geom);
  }

  //*********************************************************************************************
  //************************************* Transformations ***************************************
  //*********************************************************************************************


  public void setTransformations(STProperty<Transformation> transformations) {
    this.transformations = new STProperty<Transformation>(STProperty.PROPERTIE_FINAL_TYPES.TimeSerie, null);
    this.transformations.setValues(transformations.copy().getValues());
  }


  public STProperty<Transformation> getTransformations() {
    return transformations;
  }

  public Transformation getTransformationAt(FuzzyTemporalInterval t) {
    return this.transformations.getValueAt(t);
  }

  public String getStringTransformationAt(FuzzyTemporalInterval t) {
    return this.transformations.getValueAt(t).toString();
  }

  public void setTransformationAt(FuzzyTemporalInterval t, Transformation tr) {
    this.transformations.setValueAt(t, tr);
  }


  //*********************************************************************************************
  //************************************* Poids *************************************************
  //*********************************************************************************************

  public STProperty<Double> getTWeight() {
    return this.Tweights;
  }

  public void setTWeight(STProperty<Double> tweights) {
    this.Tweights = tweights;
  }

  /**
   * LightGeometry à la date t
   * @param t
   * @return
   */
  public Double getWeightAt(FuzzyTemporalInterval t){
    return this.Tweights.getValueAt(t);
  }

  public void setWeightAt(FuzzyTemporalInterval t, double weight){
    this.Tweights.setValueAt(t, weight);
  }



  // **************************************************************
  // ************************** Autres ****************************
  // **************************************************************

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof STEntity)) {
      return false;
    }
    STEntity e = (STEntity) o;
    if (e.getType() != this.getType()) {
      return false;
    }
    if (e.getId() != this.getId()) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    if (this.getType() == STEntity.NODE) {
      return this.getId();
    } else {
      return 1000000 + this.getId();
    }
  }

  @Override
  public String toString() {
    return Integer.toString(this.getId());
  }

  /**
   * Conversion d'un STEntity en une entité statique GraphEntity
   * @param t
   * @return
   */
  public GraphEntity toGraphEntity(FuzzyTemporalInterval t) {
    if (this.getType() == STEntity.EDGE) {
      GraphEntity.setCurrentType(GraphEntity.EDGE);
    } else {
      GraphEntity.setCurrentType(GraphEntity.NODE);
    }
    GraphEntity gr = new GraphEntity();
    gr.setId(this.getId());
    if (this.getGeometryAt(t) != null) {
      gr.setGeometry(this.getGeometryAt(t));
    }
    if(this.getWeightAt(t) != null){
      gr.setWeight(this.getWeightAt(t));
    }
    for(STProperty<String> att : this.TAttributes){
      gr.getAttributes().put(att.getName(), att.getValueAt(t));
    }
    if (!this.TIndicators.isEmpty()) {
      for (STProperty<Double> indicator : this.TIndicators) {
        gr.getLocalIndicators().put(indicator.getName(),
            indicator.getValueAt(t));
      }
    }
    return gr;
  }

  public STEntity copy() {
    STEntity.setCurrentType(this.getType());
    STEntity newE = new STEntity(this.getTimeSerie().copy());
    newE.setTGeometries(this.getTGeometry().copy());
    newE.setTWeight(this.getTWeight().copy());
    newE.setTIndicators(new HashSet<STProperty<Double>>());
    newE.setTransformations(this.getTransformations().copy());
    for(STProperty<Double> i: this.getTIndicators()){
      newE.getTIndicators().add(i.copy());
    }
    newE.setTAttributes(new HashSet<STProperty<String>>());
    for(STProperty<String> a: this.getTAttributes()){
      newE.getTAttributes().add(a.copy());
    } 
    return newE;
  }

  /**
   * Modifie la géométrie fusionnée de self
   * @param g
   */
  public void updateGeometry(STGraph g) {
    if (this.getType() == STEntity.NODE) {
      /*
       * La géométrie fusionnée d'un sommet ST correspond au barycentre pondéré
       * des coordonnées du sommet aux dates auxquelles il existe
       */
      IDirectPosition p = new DirectPosition();
      double x = 0, y = 0;
      double weight = 0.;
      for (FuzzyTemporalInterval t : this.getTimeSerie().getValues().keySet()) {
        if (this.getGeometryAt(t) != null) {
          if (this.getGeometryAt(t) instanceof LightDirectPosition) {
            x += g.getAccuracies().getValueAt(t) * ((LightDirectPosition) this.getGeometryAt(t)).getX();
            y += g.getAccuracies().getValueAt(t) * ((LightDirectPosition) this.getGeometryAt(t)).getY();
            weight += g.getAccuracies().getValueAt(t);
          } else {
            x += g.getAccuracies().getValueAt(t) * this.getGeometryAt(t).toGeoxGeometry().coord().get(0).getX();
            y +=g.getAccuracies().getValueAt(t) *  this.getGeometryAt(t).toGeoxGeometry().coord().get(0).getY();
            weight += g.getAccuracies().getValueAt(t);
          }
        }
      }
      x /= weight;
      y /= weight;

      p.setX(x);
      p.setY(y);
      LightDirectPosition pp = new LightDirectPosition(p);

      this.setGeometry(pp);
    } else {
      //d'abord on vérie que pour tout t, lex extrémité correspondent aux sommets
      // à la meme date
      //            for(FuzzyTemporalInterval t : this.getTimeSerie().asList().keySet()){
      //                if (this.getGeometryAt(t) != null) {
      //                    ILineString ll = ((LightLineString) this.getGeometryAt(t))
      //                            .toGeoxGeometry();
      //  
      //                    IDirectPosition p1 =  g.getEndpoints(this)
      //                            .getFirst().getGeometryAt(t).toGeoxGeometry().coord().get(0);
      //                    IDirectPosition p2 =  g.getEndpoints(this)
      //                            .getSecond().getGeometryAt(t).toGeoxGeometry().coord().get(0);
      //                    if(!p1.equals(ll.startPoint()) && !p1.equals(ll.endPoint()) || 
      //                            !p2.equals(ll.startPoint()) && !p2.equals(ll.endPoint())){
      //                        //une erreur s'est glissée dans le traitement ... on corrige les coord
      //                        if(p1.distance(ll.startPoint()) < p1.distance(ll.endPoint())){
      //                            //p1 est plus proche de start point
      //                            if(!p1.equals(ll.startPoint())) {
      //                                ll.setControlPoint(0, p1);
      //                            }
      //                            if(!p2.equals(ll.endPoint())){
      //                                ll.setControlPoint(ll.getControlPoint().size()-1, p2);
      //                            }
      //                        }
      //                        else{
      //                            //p1 est plus proche de end point
      //                            if(!p1.equals(ll.endPoint())) {
      //                                ll.setControlPoint(ll.getControlPoint().size()-1, p1);
      //                            }
      //                            if(!p2.equals(ll.startPoint())){
      //                                ll.setControlPoint(0, p2);
      //                            }
      //                        }
      //                        this.putGeometryAt(t, new LightLineString(ll.coord()));
      //                    }
      //                }
      //
      //            }
      /*
       * La géométrie fusionnée d'un arc ST
       */
      Map<ILineString, Double> l = new HashMap<ILineString, Double>();
      for (FuzzyTemporalInterval t : this.getTimeSerie().getValues().keySet()) {
        if (this.getGeometryAt(t) != null) {
          ILineString ll = ((LightLineString) this.getGeometryAt(t))
              .toGeoxGeometry();

          if (ll.coord().size() == 1) {
            System.out.println("line.coord.size == 1");
            System.out.println(ll.toString());
            continue;
          }
          l.put(ll, g.getAccuracies().getValueAt(t));
        }
      }
      if (!l.isEmpty()) { // on
        // récupère lees géomé des noeuds
        IDirectPosition p1 = ((LightDirectPosition) g.getEndpoints(this)
            .getFirst().getGeometry()).toGeoxDirectPosition();
        IDirectPosition p2 = ((LightDirectPosition) g.getEndpoints(this)
            .getSecond().getGeometry()).toGeoxDirectPosition();
        
        // TODO:  - ---------- test------------------------
//        Set<ILineString> lines = new HashSet<ILineString>();
//        lines.addAll(l.keySet());
//        AbstractMergeAlgorithm mergeA = new MatchingHomologousPointsMerge(lines, l ,15);
//        ILineString lineMerged = mergeA.merge();
//        AbstractSnappingAlgorithm snap = new HelmertSnapping(lines, p1, p2);
//        lineMerged = snap.transform(lineMerged);
//        this.setGeometry(new LightLineString(lineMerged.coord()));
        // --------------------- end test -------------------------------
        
        ILineString newL = GeometryUtils.mergeLineString(g, l,
            p1, p2);
        newL.setControlPoint(0, p1);
        newL.setControlPoint(newL.coord().size()-1, p2);
        this.setGeometry(new LightLineString(newL.coord()));

      } else {
        System.out.println("AIE");
        System.exit(-1);
      }
    }
  }

  public void updateFuzzyTemporalInterval(FuzzyTemporalInterval told,
      FuzzyTemporalInterval tnew) {
    // time serie
    this.timeSerie.updateFuzzyTemporalInterval(told,tnew);
    //tgéometrie
    this.Tgeometry.updateFuzzyTemporalInterval(told,tnew);
    //Tweight
    this.Tweights.updateFuzzyTemporalInterval(told,tnew);
     // transfo
    this.transformations.updateFuzzyTemporalInterval(told, tnew);
    //local indicator
    for(STProperty<Double> ind: this.TIndicators){
      ind.updateFuzzyTemporalInterval(told,tnew);
    }
    //Tattribute
    for(STProperty<String> att : this.TAttributes){
      att.updateFuzzyTemporalInterval(told,tnew);
    }
  }

  public void addFuzzyTemporalInterval(FuzzyTemporalInterval t) {
    // time serie
    this.timeSerie.setValueAt(t,false);
    //tgéometrie
    this.Tgeometry.setValueAt(t, null);
    //Tweight
    this.Tweights.setValueAt(t, null);
    //transfo
    this.transformations.setValueAt(t, Transformation.NONE);
    //local indicator
    for(STProperty<Double> ind: this.TIndicators){
      ind.setValueAt(t, null);
    }        //Tattribute
    for(STProperty<String> att : this.TAttributes){
      att.setValueAt(t, null);
    }
  }

  /**
   * renvoie true si sommet fictif
   * @return
   */
  public boolean isFictive() {
    if(this.getType() == STEntity.EDGE){
      return false;
    }
    for(FuzzyTemporalInterval t : this.getTimeSerie().getValues().keySet()){
      if(!this.existsAt(t) && this.getGeometryAt(t) != null){
        return true;
      }
    }
    return false;
  }



  public Set<JSonAttribute> getJsonAttributes() {
    return jsonAttributes;
  }



  public void setJsonAttributes(Set<JSonAttribute> jsonAttributes) {
    this.jsonAttributes = jsonAttributes;
  }



}

