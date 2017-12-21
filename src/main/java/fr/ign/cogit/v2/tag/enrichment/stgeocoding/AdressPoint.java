package fr.ign.cogit.v2.tag.enrichment.stgeocoding;

import java.util.List;

import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.v2.tag.FuzzyTemporalInterval;

public class AdressPoint {
  
  public AdressPoint(String num, String type, String nom, IDirectPosition pos) {
    this.num = num;
    this.nom = nom;
    this.type = type;
    this.originalName = nom;
    this.pos = pos;
  }

  private IDirectPosition pos;
  private FuzzyTemporalInterval time;
  public FuzzyTemporalInterval getTime() {
    return time;
  }

  public void setTime(FuzzyTemporalInterval time) {
    this.time = time;
  }

  private String num;
  private String type;
  private String nom;
  private String phoneticName;
  private List<String> phoneticNameParsed;
  private List<String> nameParsed;
  
  private String originalName;

  public String getOriginalName() {
    return originalName;
  }

  public void setOriginalName(String originalName) {
    this.originalName = originalName;
  }

  public IDirectPosition getPosition() {
    return pos;
  }

  public void setPosition(IDirectPosition pos) {
    this.pos = pos;
  }

  public void setNum(String num) {
    this.num = num;
  }

  public String getNum() {
    return num;
  }

  public void setName(String nom) {
    this.nom = nom;
  }

  public String getName() {
    return nom;
  }
  

  public void setType(String type) {
    this.type = type;
  }

  public String getType() {
    return type;
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
