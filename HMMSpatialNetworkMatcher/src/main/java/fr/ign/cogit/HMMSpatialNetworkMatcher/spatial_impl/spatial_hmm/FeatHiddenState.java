package fr.ign.cogit.HMMSpatialNetworkMatcher.spatial_impl.spatial_hmm;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import fr.ign.cogit.HMMSpatialNetworkMatcher.matching.root.AbstractHiddenState;
import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IFeatureCollection;
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.api.feature.Representation;
import fr.ign.cogit.geoxygene.api.feature.type.GF_AssociationRole;
import fr.ign.cogit.geoxygene.api.feature.type.GF_AttributeType;
import fr.ign.cogit.geoxygene.api.feature.type.GF_FeatureType;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IEnvelope;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ILineString;
import fr.ign.cogit.geoxygene.api.spatial.geomroot.IGeometry;
import fr.ign.cogit.geoxygene.api.spatial.toporoot.ITopology;
import fr.ign.cogit.geoxygene.feature.DefaultFeature;

/**
 * Implementation of AbstractHiddenState for spatial networks.
 * The geometry of the wrapped IFeature MUST implements ILineString
 * @author bcostes
 *
 */
public class FeatHiddenState extends AbstractHiddenState implements IFeature{

  
  /**
   * IFeature wrapped in this hidden state.
   */
  private IFeature feature;


  public FeatHiddenState(IGeometry geom) {
    this.setFeature(new DefaultFeature(geom));
  }

  @Override
  public String toString() {
    return "Feature hidden state : " + this.getGeom().toString();
  }

  @Override
  public ILineString getGeom() {
    return (ILineString) this.feature.getGeom();
  }


  @Override
  public int hashCode() {
    return Objects.hash(this.getGeom());
  }


  @Override
  public boolean equals(Object o) {
    if(o == null) {
      return false;
    }
    if(! (o instanceof FeatHiddenState)) {
      return false;
    }
    FeatHiddenState s = (FeatHiddenState)o;
    return this.getGeom().equals(s.getGeom());
  }


  public IFeature getFeature() {
    return feature;
  }


  public void setFeature(IFeature feature) {
    this.feature = feature;
  }

  

  @Override
  public void setGeom(IGeometry g) {
    this.feature.setGeom(g);
  }


  @Override
  public boolean hasGeom() {
    // TODO Auto-generated method stub
    return false;
  }


  @Override
  public ITopology getTopo() {
    // TODO Auto-generated method stub
    return null;
  }


  @Override
  public void setTopo(ITopology t) {
    // TODO Auto-generated method stub

  }


  @Override
  public boolean hasTopo() {
    // TODO Auto-generated method stub
    return false;
  }


  @Override
  public IFeature cloneGeom() throws CloneNotSupportedException {
    // TODO Auto-generated method stub
    return this.feature.cloneGeom();
  }


  @Override
  public List<IFeatureCollection<IFeature>> getFeatureCollections() {
    // TODO Auto-generated method stub
    return null;
  }


  @Override
  public IFeatureCollection<IFeature> getFeatureCollection(int i) {
    // TODO Auto-generated method stub
    return null;
  }


  @Override
  public List<IFeature> getCorrespondants() {
    // TODO Auto-generated method stub
    return this.feature.getCorrespondants();
  }


  @Override
  public void setCorrespondants(List<IFeature> L) {
    this.feature.setCorrespondants(L);
  }


  @Override
  public IFeature getCorrespondant(int i) {
    return this.feature.getCorrespondant(i);
  }


  @Override
  public void addCorrespondant(IFeature O) {
    this.feature.addCorrespondant(O);
  }


  @Override
  public void removeCorrespondant(IFeature O) {
    // TODO Auto-generated method stub

  }


  @Override
  public void clearCorrespondants() {
    // TODO Auto-generated method stub

  }


  @Override
  public void addAllCorrespondants(Collection<IFeature> c) {
    // TODO Auto-generated method stub

  }


  @Override
  public Collection<IFeature> getCorrespondants(
      IFeatureCollection<? extends IFeature> pop) {
    // TODO Auto-generated method stub
    return null;
  }


  @Override
  public IPopulation<? extends IFeature> getPopulation() {
    // TODO Auto-generated method stub
    return null;
  }


  @Override
  public void setPopulation(IPopulation<? extends IFeature> population) {
    // TODO Auto-generated method stub

  }


  @Override
  public void setFeatureType(GF_FeatureType featureType) {
    // TODO Auto-generated method stub

  }


  @Override
  public GF_FeatureType getFeatureType() {
    // TODO Auto-generated method stub
    return null;
  }


  @Override
  public Object getAttribute(GF_AttributeType attribute) {
    // TODO Auto-generated method stub
    return null;
  }


  @Override
  public void setAttribute(GF_AttributeType attribute, Object valeur) {
    // TODO Auto-generated method stub

  }


  @Override
  public List<? extends IFeature> getRelatedFeatures(GF_FeatureType ftt,
      GF_AssociationRole role) {
    // TODO Auto-generated method stub
    return null;
  }


  @Override
  public Object getAttribute(String nomAttribut) {
    // TODO Auto-generated method stub
    return null;
  }


  @Override
  public List<? extends IFeature> getRelatedFeatures(String nomFeatureType,
      String nomRole) {
    // TODO Auto-generated method stub
    return null;
  }


  @Override
  public Representation getRepresentation() {
    // TODO Auto-generated method stub
    return null;
  }


  @Override
  public void setRepresentation(Representation rep) {
    // TODO Auto-generated method stub

  }


  @Override
  public boolean isDeleted() {
    // TODO Auto-generated method stub
    return false;
  }


  @Override
  public void setDeleted(boolean deleted) {
    // TODO Auto-generated method stub

  }


  @Override
  public boolean intersecte(IEnvelope env) {
    // TODO Auto-generated method stub
    return false;
  }


  @Override
  public int getId() {
    // TODO Auto-generated method stub
    return 0;
  }


  @Override
  public void setId(int Id) {
    // TODO Auto-generated method stub
    
  }


}
