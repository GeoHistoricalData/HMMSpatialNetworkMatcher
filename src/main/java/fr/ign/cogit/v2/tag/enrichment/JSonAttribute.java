package fr.ign.cogit.v2.tag.enrichment;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.type.GF_AttributeType;
import fr.ign.cogit.geoxygene.api.spatial.geomroot.IGeometry;
import fr.ign.cogit.geoxygene.util.conversion.ParseException;
import fr.ign.cogit.geoxygene.util.conversion.WktGeOxygene;

public class JSonAttribute  implements Serializable{

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  /**
   * Nom de l'attribut
   */
  private String name;
  private boolean hasGeometry;
  private JSONObject jsonO;


  public JSonAttribute(){
    this.name = "";
    this.hasGeometry = false;
    this.jsonO = new JSONObject();
  }

  public JSonAttribute(String name){
    this.name = name.toLowerCase();
    this.jsonO = new JSONObject();
    this.hasGeometry = false;
  } 

  public String getName() {
    return name;
  }

  public void setName(String name) {

    this.name = name.toLowerCase();
  }

  public boolean hasGeometry() {
    return hasGeometry;
  }

  public void hasGeometry(boolean hasGeometry) {
    this.hasGeometry = hasGeometry;
  }

  public IGeometry decodeGeometry(){ 
    IGeometry geom = null;
    if(!this.hasGeometry){
      return geom;
    }
    try {
      geom = WktGeOxygene.makeGeOxygene(this.getJson().getString("geometry"));
    } catch (ParseException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (JSONException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return geom;
  }

  public JSONObject getJson() {
    return jsonO;
  }

  public void setJson(JSONObject jsonO) {
    this.jsonO = jsonO;
  }

  /**
   * Donne la liste des attributs sont le nom est name dans la liste set
   * @param name
   * @param set
   * @return
   */
  public static Set<JSonAttribute> getJsonAttributeByName(String name, Set<JSonAttribute> set){
    Set<JSonAttribute>  result = new HashSet<JSonAttribute>();
    for(JSonAttribute jsonA: set){
      if(jsonA.getName().equals(name.toLowerCase())){
        result.add(jsonA);
      }
    }
    return result;
  }

  /**
   * Fonction de création d'un objet JSon à partir d'un feature
   * @param name
   * @param f
   */
  public static JSonAttribute createJSonAttribute(String name, IFeature f){
    name = name.toLowerCase();
    JSonAttribute jsonA =new JSonAttribute(name);
    jsonA.hasGeometry(true);
    jsonA.putO("name", name);
    jsonA.putO("geometry", WktGeOxygene.makeWkt(f.getGeom()));
    if(f.getFeatureType() == null){
      return jsonA;
    }
    for(GF_AttributeType att: f.getFeatureType().getFeatureAttributes()){
      String attName = att.getMemberName();
      Object attvalue = f.getAttribute(att);
      jsonA.putO(attName, attvalue);
    }
    return jsonA;
  }

  public void putB(String key, boolean value){
    try {
      this.jsonO.put(key, value);
    } catch (JSONException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public void putC(String key, Collection<?> value){
    try {
      this.jsonO.put(key, value);
    } catch (JSONException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public void putD(String key, double value){
    try {
      this.jsonO.put(key, value);
    } catch (JSONException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }


  public void putI(String key, int value){
    try {
      this.jsonO.put(key, value);
    } catch (JSONException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public void putO(String key, Object value){
    try {
      this.jsonO.put(key, value);
    } catch (JSONException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public void putI(String key, Map<?,?> value){
    try {
      this.jsonO.put(key, value);
    } catch (JSONException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  

  private void readObject(final ObjectInputStream ois) throws IOException,
  ClassNotFoundException {
    try {
      this.jsonO = new JSONObject((String) ois.readObject());
      this.name = (String) ois.readObject();
      this.hasGeometry = (Boolean) ois.readObject();;
    } catch (JSONException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  private void writeObject(final ObjectOutputStream oos) throws IOException {
    oos.writeObject(this.jsonO.toString());
    oos.writeObject(this.name);
    oos.writeObject(this.hasGeometry);
  }

  public static void main(String args[]){
    //    String inputStg ="/media/bcostes/Data/Benoit/these/analyses/TAG/TAG_v2/final_ind/tag_corrected_ind.tag";
    //    STGraph stg = TAGIoManager.deserialize(inputStg);
    //    
    //    for(STEntity e : stg.getEdges()){
    //      e.setJsonAttributes(new HashSet<JSonAttribute>());
    //      JSonAttribute jsonA = new JSonAttribute("test_attJson");
    //      jsonA.put("Attribut1", Math.random());
    //      e.getJsonAttributes().add(jsonA);
    //    }
    //    
    //    TAGIoManager.serializeBinary(stg,"/home/bcostes/Bureau/deleteme.tag");
    //    
    //    
//        JSonAttribute json = new JSonAttribute("Test");
//        json.putO("att1","test_att1");
//        ObjectOutputStream oos = null;
//        try {
//            final FileOutputStream fichier = new FileOutputStream("/home/bcostes/Bureau/stag_json/test.bim");
//            oos = new ObjectOutputStream(fichier);
//            oos.writeObject(json);
//            oos.flush();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                if (oos != null) {
//                    oos.flush();
//                    oos.close();
//                }
//            } catch (final IOException ex) {
//                ex.printStackTrace();
//            }
//        }
//    ObjectInputStream ois = null;
//    try {
//      final FileInputStream fichier = new FileInputStream("/home/bcostes/Bureau/stag_json/test.bim");
//      ois = new ObjectInputStream(fichier);
//      JSonAttribute json = (JSonAttribute) ois.readObject();
//      ois.close();      
//      //stg.updateGeometries();
//      System.out.println(json.getName());
//      System.out.println(json.getJson().toString());
//
//    } catch (IOException e) {
//      e.printStackTrace();
//    } catch (ClassNotFoundException e) {
//      // TODO Auto-generated catch block
//      e.printStackTrace();
//    }
  }



}
