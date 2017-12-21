package fr.ign.cogit.morphogenesis.network.utils.topologie;

import org.apache.log4j.Logger;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IFeatureCollection;
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.api.feature.type.GF_AttributeType;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.contrib.cartetopo.Arc;
import fr.ign.cogit.geoxygene.contrib.cartetopo.CarteTopo;
import fr.ign.cogit.geoxygene.contrib.cartetopo.Noeud;
import fr.ign.cogit.geoxygene.feature.DefaultFeature;
import fr.ign.cogit.geoxygene.feature.FT_FeatureCollection;
import fr.ign.cogit.geoxygene.feature.Population;
import fr.ign.cogit.geoxygene.schema.schemaConceptuelISOJeu.AttributeType;
import fr.ign.cogit.geoxygene.schema.schemaConceptuelISOJeu.FeatureType;
import fr.ign.cogit.geoxygene.spatial.coordgeom.GM_LineString;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileReader;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileWriter;
import fr.ign.cogit.geoxygene.util.index.Tiling;

public class CorrectionTopologieLineaires {

  private static Logger logger = Logger
      .getLogger(CorrectionTopologieLineaires.class);

  /**
   * Correction topologique d'un réseau
   * @param inputFile
   * @param outputFile
   * @param prec seuil fusion des noeuds proches
   */
  public static void run(String inputFile, String outputFile,
      FeatureType featT, double prec) {

    IPopulation<IFeature> inputFeatureCollection = ShapefileReader
        .read(inputFile);
    inputFeatureCollection.initSpatialIndex(Tiling.class, false);
    // Création de la carte topo et corrections de la topologie
    CarteTopo topo = new CarteTopo("");
    IPopulation<Arc> arcs = topo.getPopArcs();
    for (IFeature feature : inputFeatureCollection) {
      Arc arc = arcs.nouvelElement();
      try {
        GM_LineString line = new GM_LineString(feature.getGeom().coord());
        arc.setGeometrie(line);
        arc.addCorrespondant(feature);
      } catch (ClassCastException e) {
        e.printStackTrace();
      }
    }
    topo.filtreDoublons(prec);
    topo.creeTopologieArcsNoeuds(prec);
    topo.creeNoeudsManquants(prec);
    topo.rendPlanaire(prec);
    topo.fusionNoeuds(prec);
    topo.filtreArcsDoublons();
    topo.filtreNoeudsIsoles();
    topo.fusionNoeuds(prec);        
    topo.filtreNoeudsSimples();


    // vérifications manuelles
    IFeatureCollection<IFeature> colNoeudSimples = new Population<IFeature>();
    IFeatureCollection<IFeature> colPlusieursCorrespondants = new Population<IFeature>();

    // Recherche de noeuds simples avec des att identiques
    //    List<Noeud> toRemove = new ArrayList<Noeud>();
    //    for (Noeud n : topo.getListeNoeuds()) {
    //      if (n.getEntrants().size() + n.getSortants().size() == 2) {
    //        boolean sameAtt = true;
    //        List<Arc> l = new ArrayList<Arc>();
    //        for (Arc a : n.getEntrants()) {
    //          l.add(a);
    //        }
    //        for (Arc a : n.getSortants()) {
    //          l.add(a);
    //        }
    //        Arc arc1 = l.get(0);
    //        Arc arc2 = l.get(1);
    //        IFeature featCorrespondant1 = arc1.getCorrespondant(0);
    //        IFeature featCorrespondant2 = arc2.getCorrespondant(0);
    //        for (GF_AttributeType att : featT.getFeatureAttributes()) {
    //          if (!featCorrespondant1.getAttribute(att).toString()
    //              .equals(featCorrespondant2.getAttribute(att).toString())) {
    //            sameAtt = false;
    //            break;
    //          }
    //        }
    //        if (sameAtt) {
    //          colNoeudSimples.add(new DefaultFeature(n.getGeom()));
    //
    //          Noeud narc2 = arc2.getNoeudIni().equals(n) ? arc2.getNoeudFin()
    //              : arc2.getNoeudIni();
    //          IDirectPositionList pts = arc2.getCoord();
    //          if (n.equals(arc2.getNoeudFin())) {
    //            pts = pts.reverse();
    //          }
    //          pts.remove(0);
    //          if (arc1.getNoeudFin().equals(n)) {
    //            arc1.getCoord().addAll(pts);
    //            arc1.setNoeudFin(narc2);
    //          } else {
    //
    //            for (int i = 0; i < pts.size(); i++) {
    //              arc1.getCoord().add(0, pts.get(i));
    //            }
    //            arc1.setNoeudIni(narc2);
    //          }
    //          topo.enleveArc(arc2);
    //          toRemove.add(n);
    //        }
    //      }
    //    }
    //
    //    topo.enleveNoeuds(toRemove);

    // TRAITEMENT

    // sortie
    IFeatureCollection<IFeature> result = new FT_FeatureCollection<IFeature>();
    int cpt = 1;
    int size = topo.getListeArcs().size();
    for (Arc arc : topo.getListeArcs()) {
      IFeature featCorrespondant = arc.getCorrespondant(0);
      if (arc.getCorrespondants().size() > 1) {
        colPlusieursCorrespondants.add(new DefaultFeature(arc.getGeom()));
      }
      /*
       * IFeature featCorrespondant = null; logger.info(cpt + " / " + size);
       * cpt++; if (arc.getCorrespondants().isEmpty()) { Collection<IFeature>
       * col = inputFeatureCollection.select(arc.getGeom(), 5); for (IFeature f
       * : col) { if (f.getGeom().buffer(0.05).contains(arc.getGeom()) ||
       * arc.getGeom().buffer(0.5).contains(f.getGeom()) ||
       * f.getGeom().buffer(0.05).intersection(arc.getGeom()).length() > 10) {
       * featCorrespondant = f; break; } } if (featCorrespondant == null) {
       * System.out.println(col.size()); logger.error("Pas de correspondant : "
       * + arc.toString()); return; } } else { if
       * (arc.getCorrespondants().size() > 1) {
       * logger.warn("Plusieurs correspondants : " + arc.toString()); }
       * 
       * featCorrespondant = arc.getCorrespondant(0); }
       */

      IFeature featArc = new LocalFeatureTopologie(arc.getGeom());
      featArc.setFeatureType(featT);

      // les attributs



      for (GF_AttributeType attribute : featT.getFeatureAttributes()) {
        if (featCorrespondant.getAttribute(attribute) == null
            || featCorrespondant.getAttribute(attribute).equals("NULL")
            || featCorrespondant.getAttribute(attribute).equals("null")) {
          if (attribute.getValueType().equals("String")) {
            featArc.setAttribute(attribute, "NULL");
            continue;
          } else if (attribute.getValueType().equals("Integer")) {
            featArc.setAttribute(attribute, -1);
            continue;
          }
        }

        if (arc.getCorrespondants().size() > 1) {
          if(attribute.getMemberName().toLowerCase().equals("id")){
            String id = "";
            for(IFeature ff: arc.getCorrespondants()){
              id += ff.getAttribute(attribute).toString()+",";
            }
            id = id.substring(0,id.length()-1);
            featArc.setAttribute(attribute,
                id);
            continue;

          }
          else{

            featArc.setAttribute(attribute,
                featCorrespondant.getAttribute(attribute));
          }

        }
        else{

          featArc.setAttribute(attribute,
              featCorrespondant.getAttribute(attribute));
        }



      }


      if (arc.getCorrespondants().size() > 1) {
        IDirectPosition pini = arc.getGeometrie().getControlPoint(0);
        IDirectPosition pfin = arc.getGeometrie().getControlPoint(arc.getGeometrie().getControlPoint().size()-1);

        IFeature featIni = null, featFin = null;
        for(IFeature f : arc.getCorrespondants()){
          if(f.getGeom().coord().get(0).equals(pini) || f.getGeom().coord().get(f.getGeom().coord().size()-1).equals(pini) ){
            featIni = f;
            break;
          }
        }
        for(IFeature f : arc.getCorrespondants()){
          if(f.getGeom().coord().get(0).equals(pfin) || f.getGeom().coord().get(f.getGeom().coord().size()-1).equals(pfin) ){
            featFin = f;
            break;
          }
        }
        if(featIni != null && featFin != null){




          String attIni1 = "";
          String attIni2 = "";
          String attFin1 = "";
          String attFin2 = "";
          if(arc.getGeometrie().getControlPoint(0).equals(featIni.getGeom().coord().get(0))){
            attIni1 = "ADR_DD88";
            attIni2 = "ADR_DG88";
          }
          else{
            attIni1 = "ADR_FD88";
            attIni2 = "ADR_FG88";
          }
          if(arc.getGeometrie().getControlPoint(arc.getGeometrie().getControlPoint().size()-1).equals(featFin.getGeom().coord().get(0))){
            attFin1 = "ADR_DD88";
            attFin2 = "ADR_DG88";
          }
          else{
            attFin1 = "ADR_FD88";
            attFin2 = "ADR_FG88";
          }


          if (featIni.getAttribute(attIni1)== null
              || featIni.getAttribute(attIni1).equals("NULL")
              || featIni.getAttribute(attIni1).equals("null")) {
            featArc.setAttribute(featArc.getFeatureType().getFeatureAttributeByName("ADR_DD88"), "NULL");
          }
          else{
            featArc.setAttribute(featArc.getFeatureType().getFeatureAttributeByName("ADR_DD88"), featIni.getAttribute(attIni1));
          }

          if (featIni.getAttribute(attIni2)== null
              || featIni.getAttribute(attIni2).equals("NULL")
              || featIni.getAttribute(attIni2).equals("null")) {
            featArc.setAttribute(featArc.getFeatureType().getFeatureAttributeByName("ADR_DG88"), "NULL");
          }
          else{
            featArc.setAttribute(featArc.getFeatureType().getFeatureAttributeByName("ADR_DG88"), featIni.getAttribute(attIni2));
          }

          if (featFin.getAttribute(attFin1)== null
              || featFin.getAttribute(attFin1).equals("NULL")
              || featFin.getAttribute(attFin1).equals("null")) {
            featArc.setAttribute(featArc.getFeatureType().getFeatureAttributeByName("ADR_FD88"), "NULL");
          }
          else{
            featArc.setAttribute(featArc.getFeatureType().getFeatureAttributeByName("ADR_FD88"), featFin.getAttribute(attFin1));
          }

          if (featFin.getAttribute(attFin2)== null
              || featFin.getAttribute(attFin2).equals("NULL")
              || featFin.getAttribute(attFin2).equals("null")) {
            featArc.setAttribute(featArc.getFeatureType().getFeatureAttributeByName("ADR_FG88"), "NULL");
          }
          else{
            featArc.setAttribute(featArc.getFeatureType().getFeatureAttributeByName("ADR_FG88"), featFin.getAttribute(attFin2));
          }
        }
        else{
          System.out.println("WAT");
          // break;
          //System.exit(-1);
        }
      }



      result.add(featArc);

    }
    ShapefileWriter.write(result, outputFile);

  }

  public static void main(String args[]) {

    String inputFile = "/home/bcostes/Bureau/test_poubelle.shp";
    String outputFile = "/media/bcostes/Data/Benoit/these/donnees/vecteur/filaires/filaires_corriges/poubelle_TEMPORAIRE_emprise_utf8_L93_v2.shp";

    // Le feature type
    FeatureType featT = new FeatureType();

    /*
     * AttributeType att = new AttributeType(); att.setMemberName("NOM_ENTIER");
     * att.setNomField("NOM_ENTIER"); att.setValueType("String");
     * featT.addFeatureAttribute(att);
     */

    /*
     * AttributeType att = new AttributeType(); att.setMemberName("TYPE_VOIE");
     * att.setNomField("TYPE_VOIE"); att.setValueType("String");
     * featT.addFeatureAttribute(att);
     * 
     * att = new AttributeType(); att.setMemberName("PARTICULE");
     * att.setNomField("PARTICULE"); att.setValueType("String");
     * featT.addFeatureAttribute(att);
     * 
     * att = new AttributeType(); att.setMemberName("NOM_VOIE");
     * att.setNomField("NOM_VOIE"); att.setValueType("String");
     * featT.addFeatureAttribute(att);
     * 
     * att = new AttributeType(); att.setMemberName("PREFIXE");
     * att.setNomField("PREFIXE"); att.setValueType("String");
     * featT.addFeatureAttribute(att);
     * 
     * att = new AttributeType(); att.setMemberName("RENSEIGNEM");
     * att.setNomField("RENSEIGNEM"); att.setValueType("String");
     * featT.addFeatureAttribute(att);
     */

    // les attributs
    /*
     * AttributeType att = new AttributeType(); att = new AttributeType();
     * att.setMemberName("id_alpage"); att.setNomField("id_alpage");
     * att.setValueType("Integer"); featT.addFeatureAttribute(att);
     */
    AttributeType att = new AttributeType();

    att = new AttributeType();
    att.setMemberName("NOM_ENTIER");
    att.setNomField("NOM_ENTIER");
    att.setValueType("String");
    featT.addFeatureAttribute(att);

//    att = new AttributeType();
//    att.setMemberName("TYPE_VOIE");
//    att.setNomField("TYPE_VOIE");
//    att.setValueType("String");
//    featT.addFeatureAttribute(att);
//
//    att = new AttributeType();
//    att.setMemberName("PARTICULE");
//    att.setNomField("PARTICULE");
//    att.setValueType("String");
//    featT.addFeatureAttribute(att);
//
//    att = new AttributeType();
//    att.setMemberName("NOM_VOIE");
//    att.setNomField("NOM_VOIE");
//    att.setValueType("String");
//    featT.addFeatureAttribute(att);
//
//    att = new AttributeType();
//    att.setMemberName("ADR_FG88");
//    att.setNomField("ADR_FG88");
//    att.setValueType("String");
//    featT.addFeatureAttribute(att);
//
//    att = new AttributeType();
//    att.setMemberName("ADR_FD88");
//    att.setNomField("ADR_FD88");
//    att.setValueType("String");
//    featT.addFeatureAttribute(att);
//
//    att = new AttributeType();
//    att.setMemberName("ADR_DG88");
//    att.setNomField("ADR_DG88");
//    att.setValueType("String");
//    featT.addFeatureAttribute(att);
//
//    att = new AttributeType();
//    att.setMemberName("ADR_DD88");
//    att.setNomField("ADR_DD88");
//    att.setValueType("String");
//    featT.addFeatureAttribute(att);


    att = new AttributeType();
    att.setMemberName("ID");
    att.setNomField("ID");
    att.setValueType("String");
    featT.addFeatureAttribute(att);

    /*
     * att = new AttributeType(); att.setMemberName("ADR_DG88");
     * att.setNomField("ADR_DG88"); att.setValueType("String");
     * featT.addFeatureAttribute(att);
     * 
     * att = new AttributeType(); att.setMemberName("ADR_FG88");
     * att.setNomField("ADR_FG88"); att.setValueType("String");
     * featT.addFeatureAttribute(att);
     * 
     * att = new AttributeType(); att.setMemberName("ADR_DD88");
     * att.setNomField("ADR_DD88"); att.setValueType("String");
     * featT.addFeatureAttribute(att);
     * 
     * att = new AttributeType(); att.setMemberName("ADR_FD88");
     * att.setNomField("ADR_FD88"); att.setValueType("String");
     * featT.addFeatureAttribute(att);
     */

    /*
     * AttributeType att = new AttributeType(); att.setMemberName("type_voie");
     * att.setNomField("type_voie"); att.setValueType("String");
     * featT.addFeatureAttribute(att);
     * 
     * att = new AttributeType(); att.setMemberName("particule");
     * att.setNomField("particule"); att.setValueType("String");
     * featT.addFeatureAttribute(att);
     * 
     * att = new AttributeType(); att.setMemberName("nom_voie");
     * att.setNomField("nom_voie"); att.setValueType("String");
     * featT.addFeatureAttribute(att);
     * 
     * att = new AttributeType(); att.setMemberName("prefixe");
     * att.setNomField("prefixe"); att.setValueType("String");
     * featT.addFeatureAttribute(att);
     */

    /*
     * AttributeType att = new AttributeType(); att.setMemberName("valide");
     * att.setNomField("valide"); att.setValueType("String");
     * featT.addFeatureAttribute(att);
     * 
     * att = new AttributeType(); att.setMemberName("pres_1888");
     * att.setNomField("pres_1888"); att.setValueType("String");
     * featT.addFeatureAttribute(att);
     * 
     * att = new AttributeType(); att.setMemberName("pres_1999");
     * att.setNomField("pres_1999"); att.setValueType("String");
     * featT.addFeatureAttribute(att);
     * 
     * att = new AttributeType(); att.setMemberName("adr_dg88");
     * att.setNomField("adr_dg88"); att.setValueType("Integer");
     * featT.addFeatureAttribute(att);
     * 
     * att = new AttributeType(); att.setMemberName("adr_dd88");
     * att.setNomField("adr_dd88"); att.setValueType("Integer");
     * featT.addFeatureAttribute(att);
     * 
     * att = new AttributeType(); att.setMemberName("adr_fg88");
     * att.setNomField("adr_fg88"); att.setValueType("Integer");
     * featT.addFeatureAttribute(att);
     * 
     * att = new AttributeType(); att.setMemberName("adr_fd88");
     * att.setNomField("adr_fd88"); att.setValueType("Integer");
     * featT.addFeatureAttribute(att);
     * 
     * att = new AttributeType(); att.setMemberName("nom_g1888");
     * att.setNomField("nom_g1888"); att.setValueType("String");
     * featT.addFeatureAttribute(att);
     * 
     * att = new AttributeType(); att.setMemberName("nom_d1888");
     * att.setNomField("nom_d1888"); att.setValueType("String");
     * featT.addFeatureAttribute(att);
     * 
     * att = new AttributeType(); att.setMemberName("adr_dg88");
     * att.setNomField("adr_dg88"); att.setValueType("Integer");
     * featT.addFeatureAttribute(att);
     * 
     * att = new AttributeType(); att.setMemberName("adr_dd88");
     * att.setNomField("adr_dd88"); att.setValueType("Integer");
     * featT.addFeatureAttribute(att);
     * 
     * att = new AttributeType(); att.setMemberName("adr_fg88");
     * att.setNomField("adr_fg88"); att.setValueType("Integer");
     * featT.addFeatureAttribute(att);
     * 
     * att = new AttributeType(); att.setMemberName("adr_fd88");
     * att.setNomField("adr_fd88"); att.setValueType("Integer");
     * featT.addFeatureAttribute(att);
     * 
     * att = new AttributeType(); att.setMemberName("nom_g1888");
     * att.setNomField("nom_g1888"); att.setValueType("String");
     * featT.addFeatureAttribute(att);
     * 
     * att = new AttributeType(); att.setMemberName("nom_d1888");
     * att.setNomField("nom_d1888"); att.setValueType("String");
     * featT.addFeatureAttribute(att);
     * 
     * att = new AttributeType(); att.setMemberName("nums_1888");
     * att.setNomField("nums_1888"); att.setValueType("String");
     * featT.addFeatureAttribute(att);
     * 
     * att = new AttributeType(); att.setMemberName("nums_1999");
     * att.setNomField("nums_1999"); att.setValueType("String");
     * featT.addFeatureAttribute(att);
     * 
     * att = new AttributeType(); att.setMemberName("id_georout");
     * att.setNomField("id_georout"); att.setValueType("Double");
     * featT.addFeatureAttribute(att);
     */

    CorrectionTopologieLineaires.run(inputFile, outputFile, featT, 2);
  }
}
