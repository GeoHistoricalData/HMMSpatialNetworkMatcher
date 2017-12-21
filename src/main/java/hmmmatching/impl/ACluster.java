package hmmmatching.impl;

import java.util.ArrayList;
import java.util.List;

import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ILineString;
import fr.ign.cogit.geoxygene.contrib.cartetopo.Arc;
import fr.ign.cogit.geoxygene.contrib.geometrie.Operateurs;

/**
 * Cluster d'arc pouvant être fusionnées (la topologie doit être vérifiée en amont)
 * @author bcostes
 *
 */
public class ACluster extends Arc{
  

  private List<Arc> arcs;
  private ILineString geometrie;
  
  public ACluster(List<Arc> arcs){
    this.arcs = new ArrayList<Arc>();
    this.arcs.addAll(arcs);
    List<ILineString> l = new ArrayList<ILineString>();
    for(Arc a : this.arcs){
      l.add(a.getGeometrie());
    }
    this.geometrie = Operateurs.union(l);
  }
  
   
  public ILineString getGeometrie() {
    return geometrie;
  }


  public void setGeometrie(ILineString geometrie) {
    this.geometrie = geometrie;
  }


  public List<Arc> getArcs() {
    return arcs;
  }

  public void setArcs(List<Arc> arcs) {
    this.arcs = arcs;
  }
  
  @Override
  public boolean equals(Object o){
    if(!(o instanceof ACluster)){
      return false;
    }
    ACluster ao = (ACluster)o;
    return (ao.getGeometrie().equals(this.getGeometrie()));
  }

  public boolean equals(ACluster o){
    return (o.getGeometrie().equals(this.getGeometrie()));
  }
  
  @Override
  public int hashCode(){
    return this.getGeometrie().hashCode();
  }

}
