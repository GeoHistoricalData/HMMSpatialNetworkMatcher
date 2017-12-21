package fr.ign.cogit.morphogenesis.network.utils.topologie;

import fr.ign.cogit.geoxygene.api.spatial.geomroot.IGeometry;
import fr.ign.cogit.geoxygene.feature.FT_Feature;

public class LocalFeatureTopologie extends FT_Feature {

  // Les attributs des objets dont la topologie a été corrigée
  /*
   * private String valide; private String pres_1888; private String pres_1999;*/
//    private String ADR_DD88;
//    private String ADR_DG88;
//    private String ADR_FD88;
//    private String ADR_FG88;
    private String ID;
    
    /* private String nom_g1888; private String nom_d1888; private
   * int adr_dg99; private int adr_dd99; private int adr_fg99; private int
   * adr_fd99; private String nom_g1999; private String nom_d1999; private
   * String nums_1888; private String nums_1999; private double id_georout;
   */

//  public String getADR_DD88() {
//        return ADR_DD88;
//    }
//
//    public void setADR_DD88(String aDR_DD88) {
//        ADR_DD88 = aDR_DD88;
//    }
//
//    public String getADR_DG88() {
//        return ADR_DG88;
//    }
//
//    public void setADR_DG88(String aDR_DG88) {
//        ADR_DG88 = aDR_DG88;
//    }
//
//    public String getADR_FD88() {
//        return ADR_FD88;
//    }
//
//    public void setADR_FD88(String aDR_FD88) {
//        ADR_FD88 = aDR_FD88;
//    }

    public String getID() {
        return ID;
    }

    public void setID(String iD) {
        ID = iD;
    }

//private String NOM_1888;
//  private String TYPE_VOIE;
//  private String PARTICULE;
  private String NOM_ENTIER;

  public String getNOM_ENTIER() {
    return NOM_ENTIER;
  }

  public void setNOM_ENTIER(String nOM_ENTIER) {
    NOM_ENTIER = nOM_ENTIER;
  }

//  public String getNOM_1888() {
//    return NOM_1888;
//  }
//
//  public void setNOM_1888(String nOM_1888) {
//    NOM_1888 = nOM_1888;
//  }
//
//  public String getTYPE_VOIE() {
//    return TYPE_VOIE;
//  }
//
//  public void setTYPE_VOIE(String tYPE_VOIE) {
//    TYPE_VOIE = tYPE_VOIE;
//  }
//
//  public String getPARTICULE() {
//    return PARTICULE;
//  }
//
//  public void setPARTICULE(String pARTICULE) {
//    PARTICULE = pARTICULE;
//  }
//
//  public String getNOM_VOIE() {
//    return NOM_VOIE;
//  }
//
//  public void setNOM_VOIE(String nOM_VOIE) {
//    NOM_VOIE = nOM_VOIE;
//  }
//
  public LocalFeatureTopologie(IGeometry geom) {
    super();
    this.setGeom(geom);
  }
//
//public String getADR_FG88() {
//    return ADR_FG88;
//}
//
//public void setADR_FG88(String aDR_FG88) {
//    ADR_FG88 = aDR_FG88;
//}

}
