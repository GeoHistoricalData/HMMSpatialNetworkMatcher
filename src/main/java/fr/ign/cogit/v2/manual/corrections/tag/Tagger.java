package fr.ign.cogit.v2.manual.corrections.tag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nrc.fuzzy.FuzzySet;
import nrc.fuzzy.XValuesOutOfOrderException;
import nrc.fuzzy.YValueOutOfRangeException;
import v2.gh.Observation;
import v2.tagging.Tag;
import v2.tagging.TaggedEdge;
import v2.tagging.TaggingHypothesis;
import v2.tagging.TaggingSource;
import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPositionList;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ILineString;
import fr.ign.cogit.geoxygene.contrib.geometrie.Angle;
import fr.ign.cogit.geoxygene.contrib.geometrie.Operateurs;
import fr.ign.cogit.geoxygene.feature.DefaultFeature;
import fr.ign.cogit.geoxygene.feature.Population;
import fr.ign.cogit.geoxygene.matching.dst.evidence.ChoiceType;
import fr.ign.cogit.geoxygene.matching.dst.evidence.EvidenceResult;
import fr.ign.cogit.geoxygene.matching.dst.evidence.codec.DefaultCodec;
import fr.ign.cogit.geoxygene.matching.dst.operators.DecisionOp;
import fr.ign.cogit.geoxygene.matching.dst.operators.DempsterOp;
import fr.ign.cogit.geoxygene.matching.dst.operators.SmetsOp;
import fr.ign.cogit.geoxygene.matching.dst.util.Pair;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPositionList;
import fr.ign.cogit.geoxygene.spatial.coordgeom.GM_LineString;
import fr.ign.cogit.geoxygene.util.attribute.AttributeManager;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileWriter;
import fr.ign.cogit.stdb.util.AddAttribute;
import fr.ign.cogit.v2.io.TAGIoManager;
import fr.ign.cogit.v2.manual.corrections.tag.criterions.OrientationMassFunction;
import fr.ign.cogit.v2.manual.corrections.tag.criterions.SinuosityMassFunction;
import fr.ign.cogit.v2.manual.corrections.tag.criterions.WidthMassFunction;
import fr.ign.cogit.v2.manual.corrections.tag.sources.GeometricSource;
import fr.ign.cogit.v2.manual.corrections.tag.strokes.UpperHierarchicalStructureBuilder;
import fr.ign.cogit.v2.tag.FuzzyTemporalInterval;
import fr.ign.cogit.v2.tag.STEntity;
import fr.ign.cogit.v2.tag.STGraph;

public class Tagger {




    private ArrayList<TaggedEdge> edges_tagged;
    private STGraph stgraph;
    private FuzzyTemporalInterval tend;
    private List<TaggingSource> experts;
    private ArrayList<TaggingHypothesis> theta;
    private DefaultCodec<TaggingHypothesis> codec;
    private UpperHierarchicalStructureBuilder structBuilder;
    private boolean sinusoity = false;
    private boolean orientation = false;


    public Tagger(STGraph stgraph, FuzzyTemporalInterval tend ,
            float max_continuation_value, float min_error_value, Map<FuzzySet, String> shps){
        this.experts = new ArrayList<TaggingSource>();
        this.stgraph = stgraph;
        this.tend = tend;
        this.theta = new ArrayList<TaggingHypothesis>();
        this.theta.add(new TaggingHypothesis(Tag.FILIATION));
        this.theta.add(new TaggingHypothesis(Tag.ERROR));

        this.codec = new DefaultCodec<TaggingHypothesis>(this.theta);
        float delta = 15;

//        List<byte[]> geomFrame = new ArrayList<byte[]>();
//        geomFrame.add(this.codec.encode(this.theta.get(0)));
//        geomFrame.add(this.codec.encode(this.theta.get(1)));
//        geomFrame.add(this.codec.encode(this.theta.get(2)));
//        TaggingSource geos = new GeometricSource(geomFrame,new FrechetMassFunction(delta,max_continuation_value,min_error_value));
//
//        this.experts.add(geos);

//        
        List<byte[]> sinusosityFrame = new ArrayList<byte[]>();
        sinusosityFrame.add(this.codec.encode(this.theta.get(0),this.theta.get(1)));
        sinusosityFrame.add(this.codec.encode(this.theta.get(0)));
        this.structBuilder= new UpperHierarchicalStructureBuilder(this.stgraph, "name");
        TaggingSource sinuosity = new GeometricSource(sinusosityFrame,new SinuosityMassFunction());
        this.sinusoity = true;
        this.experts.add(sinuosity);
        
        List<byte[]> orientationFrame = new ArrayList<byte[]>();
        orientationFrame.add(this.codec.encode(this.theta.get(0)/*, this.theta.get(2), this.theta.get(3)*/) );
        orientationFrame.add(this.codec.encode(this.theta.get(1)));
        TaggingSource orientation = new GeometricSource(orientationFrame,new OrientationMassFunction());
        this.orientation = true;

        this.experts.add(orientation);


//        if(shps != null){
//            List<byte[]> widthFrame = new ArrayList<byte[]>();
////            widthFrame.add(this.codec.encode(this.theta.get(0)));
////            widthFrame.add(this.codec.encode(this.theta.get(1),this.theta.get(2)));
//            widthFrame.add(this.codec.encode( this.theta.get(0), this.theta.get(1)));
//
//            widthFrame.add(this.codec.encode( this.theta.get(2), this.theta.get(3)));
//
//            widthFrame.add(this.codec.encode( this.theta.get(0), this.theta.get(1),this.theta.get(2), this.theta.get(3)));
//
//            TaggingSource width = new GeometricSource(widthFrame, new WidthMassFunction(shps));
//
//            this.experts.add(width);
//
////        }
        
        


//        List<byte[]> themeFrame = new ArrayList<byte[]>();
//        themeFrame.add(this.codec.encode(this.theta.get(0),this.theta.get(1)));
//        //themeFrame.add(this.codec.encode(this.theta.get(1),this.theta.get(2)));
//        themeFrame.add(this.codec.encode(this.theta.get(0),this.theta.get(1),this.theta.get(2)));
//        TaggingSource names = new ThematicSource(themeFrame,new StreetNamesMassFunction());
//
//        this.experts.add(names);


    }

    public void addExpert(TaggingSource s){
        this.experts.add(s);
    }


    public void tag(){
        

        List<FuzzyTemporalInterval> times = new ArrayList<FuzzyTemporalInterval>(this.stgraph.getTemporalDomain().asList());
        times.remove(null);
        Collections.sort(times);
        if(times.size() == 1 || times.indexOf(this.tend) == 0){
            return;
        }
        for(STEntity e : this.stgraph.getEdgesAt(tend)){
            //on récupère la date d'existence précedente
            FuzzyTemporalInterval tbefore = null;
            List<FuzzyTemporalInterval>timesE = new ArrayList<FuzzyTemporalInterval>();
            for(FuzzyTemporalInterval t: e.getTimeSerie().getValues().keySet()){
                if(e.existsAt(t)){
                    timesE.add(t);
                }
            }
            Collections.sort(timesE);
            if(timesE.indexOf(tend) == 0){
                continue;
            }
            else{
                tbefore = timesE.get(timesE.indexOf(tend)-1);
            }
            edu.uci.ics.jung.graph.util.Pair<ILineString> lines = null;
            double s1= -1, s2 = -1;
            double d =-1;
            if(sinusoity  || orientation){
                lines = this.structBuilder.buildHStructure(e, tbefore, tend);
                if(lines != null){
                    if(sinusoity){
                    s1 = lines.getFirst().length() / (lines.getFirst().startPoint().distance(lines.getFirst().endPoint()));
                    s2 = lines.getSecond().length() / (lines.getSecond().startPoint().distance(lines.getSecond().endPoint()));
                    }
                    if(orientation){
                        Angle a1 = Operateurs.directionPrincipale(Operateurs.resampling(lines.getFirst(),5).coord());
                        Angle a2 = Operateurs.directionPrincipale(Operateurs.resampling(lines.getSecond(),5).coord());
                        d  = Angle.ecart(a1, a2).getValeur()* 180./Math.PI;
                    }
                }
                else{
                    if(sinusoity){
                    s1 = e.getGeometryAt(tbefore).toGeoxGeometry().length() / ((ILineString)(e.getGeometryAt(tbefore).toGeoxGeometry())).startPoint().distance(((ILineString)(e.getGeometryAt(tbefore).toGeoxGeometry())).endPoint());
                    s2 = e.getGeometryAt(tend).toGeoxGeometry().length() / ((ILineString)(e.getGeometryAt(tend).toGeoxGeometry())).startPoint().distance(((ILineString)(e.getGeometryAt(tend).toGeoxGeometry())).endPoint());
                    }
                    if(orientation){
                        Angle a1 = Operateurs.directionPrincipale(Operateurs.resampling((ILineString)e.getGeometryAt(tbefore).toGeoxGeometry(),5).coord());
                        Angle a2 = Operateurs.directionPrincipale(Operateurs.resampling((ILineString)e.getGeometryAt(tend).toGeoxGeometry(),5).coord());
                        d  = Angle.ecart(a1, a2).getValeur()* 180./Math.PI;
                    }
                    
                }

            }
            Observation to = new Observation();
            to.setId(e.getId());
            to.setGeom(e.getGeometryAt(tbefore).toGeoxGeometry());
            to.setTime(tbefore);
            if( e.getTAttributeByName("name").getValueAt(tbefore)!=null){
                AddAttribute.addAttribute(to, "NOM_ENTIER", e.getTAttributeByName("name").getValueAt(tbefore), "String");
            }
            else{
                AddAttribute.addAttribute(to, "NOM_ENTIER", "", "String");
            }
            Observation ho = new Observation();
            ho.setId(e.getId());
            ho.setGeom(e.getGeometryAt(tend).toGeoxGeometry());
            ho.setTime(tend);
            if( e.getTAttributeByName("name").getValueAt(tend)!=null){
                AddAttribute.addAttribute(ho, "NOM_ENTIER", e.getTAttributeByName("name").getValueAt(tend), "String");
            }
            else{
                AddAttribute.addAttribute(ho, "NOM_ENTIER", "", "String");
            }               
            if(sinusoity){
                AddAttribute.addAttribute(to, "sinuosity", s1, "Double");
                AddAttribute.addAttribute(ho, "sinuosity", s2, "Double");
            }
            if(orientation){
                AddAttribute.addAttribute(to, "orientation", d, "Double");
                AddAttribute.addAttribute(ho, "orientation", d, "Double");
            }
            TaggedEdge te = this.computeEdgeBelief(to,ho);
            if(te == null){
                te= new TaggedEdge();
                te.privilegied_tag = Tag.ERROR;
                te.tail = to;
                te.head = ho;
            }
            this.edges_tagged.add(te);

        }
    }

    public IPopulation<IFeature> tagIssues(){
        IPopulation<IFeature>  result = new Population<IFeature>();
        List<FuzzyTemporalInterval> times = new ArrayList<FuzzyTemporalInterval>(this.stgraph.getTemporalDomain().asList());
        times.remove(null);
        Collections.sort(times);
        if(times.size() == 1 || times.indexOf(this.tend) == 0){
            return result;
        }
        for(STEntity e : this.stgraph.getEdgesAt(tend)){
            //on récupère la date d'existence précedente
            FuzzyTemporalInterval tbefore = null;
            List<FuzzyTemporalInterval>timesE = new ArrayList<FuzzyTemporalInterval>();
            for(FuzzyTemporalInterval t: e.getTimeSerie().getValues().keySet()){
                if(e.existsAt(t)){
                    timesE.add(t);
                }
            }
            Collections.sort(timesE);
            if(timesE.indexOf(tend) == 0){
                continue;
            }
            else{
                tbefore = timesE.get(timesE.indexOf(tend)-1);
            }
            edu.uci.ics.jung.graph.util.Pair<ILineString> lines = null;
            double s1= -1, s2 = -1;
            double d= -1;
            if(sinusoity  || orientation){
                lines = this.structBuilder.buildHStructure(e, tbefore, tend);
                if(lines != null){
                    if(sinusoity){
                    s1 = lines.getFirst().length() / (lines.getFirst().startPoint().distance(lines.getFirst().endPoint()));
                    s2 = lines.getSecond().length() / (lines.getSecond().startPoint().distance(lines.getSecond().endPoint()));
                    }
                    if(orientation){
                        Angle a1 = Operateurs.directionPrincipale(Operateurs.resampling(lines.getFirst(),5).coord());
                        Angle a2 = Operateurs.directionPrincipale(Operateurs.resampling(lines.getSecond(),5).coord());
                        d  = Angle.ecart(a1, a2).getValeur()* 180./Math.PI;
                    }
                }
                else{
                    if(sinusoity){
                    s1 = e.getGeometryAt(tbefore).toGeoxGeometry().length() / ((ILineString)(e.getGeometryAt(tbefore).toGeoxGeometry())).startPoint().distance(((ILineString)(e.getGeometryAt(tbefore).toGeoxGeometry())).endPoint());
                    s2 = e.getGeometryAt(tend).toGeoxGeometry().length() / ((ILineString)(e.getGeometryAt(tend).toGeoxGeometry())).startPoint().distance(((ILineString)(e.getGeometryAt(tend).toGeoxGeometry())).endPoint());
                    }
                    if(orientation){
                        Angle a1 = Operateurs.directionPrincipale(Operateurs.resampling((ILineString)e.getGeometryAt(tbefore).toGeoxGeometry(),5).coord());
                        Angle a2 = Operateurs.directionPrincipale(Operateurs.resampling((ILineString)e.getGeometryAt(tend).toGeoxGeometry(),5).coord());
                        d  = Angle.ecart(a1, a2).getValeur()* 180./Math.PI;
                    }
                    
                }

            }
            Observation to = new Observation();
            to.setId(e.getId());
            to.setGeom(e.getGeometryAt(tbefore).toGeoxGeometry());
            to.setTime(tbefore);
            if( e.getTAttributeByName("name").getValueAt(tbefore)!=null){
                AddAttribute.addAttribute(to, "NOM_ENTIER", e.getTAttributeByName("name").getValueAt(tbefore), "String");
            }
            else{
                AddAttribute.addAttribute(to, "NOM_ENTIER", "", "String");
            }
            Observation ho = new Observation();
            ho.setId(e.getId());
            ho.setGeom(e.getGeometryAt(tend).toGeoxGeometry());
            ho.setTime(tend);
            if( e.getTAttributeByName("name").getValueAt(tend)!=null){
                AddAttribute.addAttribute(ho, "NOM_ENTIER", e.getTAttributeByName("name").getValueAt(tend), "String");
            }
            else{
                AddAttribute.addAttribute(ho, "NOM_ENTIER", "", "String");
            }      
            if(sinusoity){
                AddAttribute.addAttribute(to, "sinuosity", s1, "Double");
                AddAttribute.addAttribute(ho, "sinuosity", s2, "Double");
            }
            if(orientation){
                AddAttribute.addAttribute(to, "orientation", d, "Double");
                AddAttribute.addAttribute(ho, "orientation", d, "Double");
            }
            TaggedEdge te = this.computeEdgeBelief(to,ho);
            if(te == null){
                te= new TaggedEdge();
                te.privilegied_tag = Tag.ERROR;
                te.tail = to;
                te.head = ho;
            }
            this.edges_tagged.add(te);
            if(te.getTag().equals(Tag.ERROR) || te.getTag().equals(Tag.FILIATION)){
                IFeature f = new DefaultFeature(e.getGeometry().toGeoxGeometry().buffer(2));
                AttributeManager.addAttribute(f, "TAG", te.privilegied_tag, "String");
                AttributeManager.addAttribute(f, "pignistic", te.getPignisticValue(), "Double");
                AttributeManager.addAttribute(f, "conflict", te.getConflict(), "Double");
                result.add(f);
            }
        }
        return result;
    }

    //  public void detectConflicts(){
    //      for(int e : this.graph_to_tag.lg.getEdges().toIntArray()){
    //        IntSet tail = this.graph_to_tag.lg.getDirectedHyperEdgeTail(e);
    //        IntSet head = this.graph_to_tag.lg.getDirectedHyperEdgeHead(e);
    //      }
    //    
    //  }


    private TaggedEdge computeEdgeBelief(Observation to,Observation ho) {
        DempsterOp op = new DempsterOp(false);
        List<List<Pair<byte[], Float>>> all_masses = new ArrayList<List<Pair<byte[], Float>>>(); 
        for(TaggingSource s : this.experts){
            System.out.println("---");
            System.out.println(s.getMassFunction().toString());
            List<Pair<byte[], Float>> values = new ArrayList<Pair<byte[], Float>>();
            s.affectMassesToHypotheses(to,ho);
            for(int i = 0; i<s.fram_of_disc.size();i++){
                byte[] hyp = s.fram_of_disc.get(i);
                for(int p=0; p< hyp.length; p++){
                    System.out.print(hyp[p]+" ");
                }
                System.out.println();
                Float d = s.getMassOf(hyp);
                System.out.println(d);

                values.add(new Pair<byte[], Float>(hyp, d)); 
            }
            all_masses.add(values);
        }
        List<Pair<byte[], Float>> result = op.combine(all_masses);

        
        if(op.getConflict() == 1){
            return null;
        }
        DecisionOp<TaggingHypothesis> decop = new DecisionOp<TaggingHypothesis>(result, op.getConflict(), ChoiceType.PIGNISTIC, this.codec, true);
        EvidenceResult<TaggingHypothesis>  choice = decop.resolve();
        TaggedEdge e= new TaggedEdge();
        e.privilegied_tag = choice.getHypothesis().get(0).category;
        e.conflict = choice.getConflict();
        e.pignistic_value = choice.getValue();
        e.tail = to;
        e.head = ho;
        byte[] encprivhyp = codec.encode( choice.getHypothesis().get(0));
        e.massvalues = new float[this.experts.size()];
        for(int i =0; i < this.experts.size();i++){
            e.massvalues[i] = this.experts.get(i).getMassOf(encprivhyp);
        }
        return e;
    }

    public TaggedEdge getTagOfEdge(Observation o1, Observation o2){
        for(TaggedEdge e : this.edges_tagged){
            if(e.tail==o1 && e.head ==o2){
                return e;
            }
        }
        return null;
    }

    public void export(String shp){
        IPopulation<IFeature> out = new Population<IFeature>();
        for(TaggedEdge te: this.edges_tagged) {
            if(te != null){
                IDirectPosition p1 = Operateurs.milieu((ILineString)te.tail.getGeom());
                IDirectPosition p2 = Operateurs.milieu((ILineString)te.head.getGeom());
                IDirectPositionList l = new DirectPositionList();
                l.add(p1);
                l.add(p2);
                IFeature tag = new DefaultFeature(new GM_LineString(l));
                AttributeManager.addAttribute(tag, "TAG", te.getTag().toString(), "String");
                AttributeManager.addAttribute(tag, "pignistic", te.getPignisticValue(), "Double");
                AttributeManager.addAttribute(tag, "conflict", te.getConflict(), "Double");
                out.add(tag);
            }

        }
        ShapefileWriter.write(out, shp);
    }

    public void exportIssues(String shp){
        IPopulation<IFeature> out = new Population<IFeature>();
        for(TaggedEdge te: this.edges_tagged) {
            if(te.getTag().equals(Tag.ERROR) || te.getTag().equals(Tag.FILIATION))
                if(te != null){
                    IDirectPosition p1 = Operateurs.milieu((ILineString)te.tail.getGeom());
                    IDirectPosition p2 = Operateurs.milieu((ILineString)te.head.getGeom());
                    IDirectPositionList l = new DirectPositionList();
                    l.add(p1);
                    l.add(p2);
                    IFeature tag = new DefaultFeature(new GM_LineString(l));
                    AttributeManager.addAttribute(tag, "TAG", te.getTag().toString(), "String");
                    AttributeManager.addAttribute(tag, "pignistic", te.getPignisticValue(), "Double");
                    AttributeManager.addAttribute(tag, "conflict", te.getConflict(), "Double");
                    out.add(tag);
                }

        }
        ShapefileWriter.write(out, shp);
    }

    public void correctIssues(){
        for(TaggedEdge te: this.edges_tagged) {
            if(te.getTag().equals(Tag.ERROR) || te.getTag().equals(Tag.FILIATION)){
                return;
            }

        }
    }

    public static void main(String args[]){


        STGraph stgraph = TAGIoManager.deserialize("/home/bcostes/Bureau/test/etape1/tag_new.tag");
        List<FuzzyTemporalInterval> times = stgraph.getTemporalDomain().asList();
        times.remove(null);
        Collections.sort(times);
        FuzzyTemporalInterval t1 =times.get(0);
        FuzzyTemporalInterval t2 =times.get(times.size()-2);
        System.out.println(t1+" "+ t2);

        // ilotiers pour largeur
        Map<FuzzySet, String> shps = new HashMap<FuzzySet, String>();
        try {
            shps.put((FuzzySet) (new FuzzyTemporalInterval(new double[]{1784,1785,1789,1791},new double[]{0,1,1,0}, 4)), 
                    "/media/bcostes/Data/Benoit/these/donnees/vecteur/parcelaire/verniquet/verniquet_ilots.shp");
            shps.put((FuzzySet)(new FuzzyTemporalInterval(new double[]{1808,1810,1836,1853},new double[]{0,1,1,0}, 4)), 
                    "/media/bcostes/Data/Benoit/these/donnees/vecteur/parcelaire/vasserot_ilots_l93/ilots_vasserot.shp");
            shps.put((FuzzySet)(new FuzzyTemporalInterval(new double[]{2008,2010,2010,2014},new double[]{0,1,1,0}, 4)), 
                    "/media/bcostes/Data/Benoit/these/donnees/vecteur/parcelaire/BDPARCELAIRE/PARCELLE_L93.SHP");

        } catch (XValuesOutOfOrderException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (YValueOutOfRangeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        Tagger tagger = new Tagger(stgraph, t2, 10, 20, shps);
        tagger.tag();
        tagger.export("/home/bcostes/Bureau/tag.shp");
        
        //élargissements
        
    }

}

