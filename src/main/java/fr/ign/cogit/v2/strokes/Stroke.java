package fr.ign.cogit.v2.strokes;

import java.util.HashSet;
import java.util.Set;

import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ILineString;
import fr.ign.cogit.v2.tag.STEntity;

/**
 * Un stroke => un set de stentity, une linestring 
 * @author bcostes
 *
 */
public class Stroke {
    
    private int id;
    private ILineString geom;
    private Set<STEntity> entities;
    
    public Stroke(){
      this.entities = new HashSet<STEntity>();
    }
    
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public ILineString getGeom() {
        return geom;
    }
    public void setGeom(ILineString geom) {
        this.geom = geom;
    }
    public Set<STEntity> getEntities() {
        return entities;
    }
    public void setEntities(Set<STEntity> entities) {
        this.entities = entities;
    }

}
