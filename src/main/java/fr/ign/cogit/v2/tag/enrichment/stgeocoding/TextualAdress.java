package fr.ign.cogit.v2.tag.enrichment.stgeocoding;

import java.util.List;

import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.v2.tag.FuzzyTemporalInterval;
import fr.ign.cogit.v2.tag.STEntity;

public class TextualAdress {
  
  public TextualAdress(int id, String num, String type, String name, String originalName) {
    this.id = id;
    this.num = num;
    this.name = name;
    this.type = type;
    this.originalName = originalName;
  }

  private int id;
  
  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  private String num;
  private String type;
  private String name;
  private String originalName;
  
  
  private GeocodeType geocodeType;
  private double trust;
  private FuzzyTemporalInterval date_geoc_ad;
  
  public FuzzyTemporalInterval getDate_geoc_ad() {
    return date_geoc_ad;
  }

  public void setDate_geoc_ad(FuzzyTemporalInterval date_geoc_ad) {
    this.date_geoc_ad = date_geoc_ad;
  }

  public GeocodeType getGeocodeType() {
    return geocodeType;
  }

  public void setGeocodeType(GeocodeType geocodeType) {
    this.geocodeType = geocodeType;
  }

  public double getTrust() {
    return trust;
  }

  public void setTrust(double trust) {
    this.trust = trust;
  }

  public String getOriginalName() {
    return originalName;
  }

  public void setOriginalName(String originalName) {
    this.originalName = originalName;
  }

  private double date;
  // coordonnées issues du géocodage
  private IDirectPosition coord;
  // stedge auquel cet objet est asssocié en sortie du géocodage
  private STEntity stedge;
  
  public STEntity getStedge() {
    return stedge;
  }

  public void setStedge(STEntity stedge) {
    this.stedge = stedge;
  }

  public IDirectPosition getCoord() {
    return coord;
  }

  public void setCoord(IDirectPosition coord) {
    this.coord = coord;
  }

  public double getDate() {
    return date;
  }

  public void setDate(double date) {
    this.date = date;
  }

  private String phoneticName;
  private List<String> phoneticNameParsed;
  private List<String> nameParsed;

  public void setNum(String num) {
    this.num = num;
  }

  public String getNum() {
    return num;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getType() {
    return this.type;
  }
  
  public void setPhoneticName(String phoneticName) {
    this.phoneticName = phoneticName;
  }

  public String getPhoneticName() {
    return phoneticName;
  }

  public void setPhoneticNameParsed(List<String> phoneticNameParsed) {
    this.phoneticNameParsed = phoneticNameParsed;
  }

  public List<String> getPhoneticNameParsed() {
    return phoneticNameParsed;
  }

  public void setNameParsed(List<String> nameParsed) {
    this.nameParsed = nameParsed;
  }

  public List<String> getNameParsed() {
    return nameParsed;
  }

}
