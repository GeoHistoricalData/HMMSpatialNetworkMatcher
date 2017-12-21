package fr.ign.cogit.v2.tag.enrichment.stgeocoding;

import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.v2.tag.FuzzyTemporalInterval;

public class GeocodedCandidate{
  
  public GeocodeType type;
  public IDirectPosition pos;
  public String name;
  public FuzzyTemporalInterval date;
  
  
  public String toString(){
    String  s = "--------- Geocoded Candidate ----------\n";
    s += this.type +"\n"+this.pos+"\n"+this.name+"\n"+this.date;
    return s;
  }
  
}