package fr.ign.cogit.v2.manual.corrections.tag.criterions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import nrc.fuzzy.FuzzySet;
import nrc.fuzzy.XValuesOutOfOrderException;
import nrc.fuzzy.YValueOutOfRangeException;
import v2.gh.Observation;
import v2.tagging.TaggingMassFunction;
import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPositionList;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ILineString;
import fr.ign.cogit.geoxygene.api.spatial.geomroot.IGeometry;
import fr.ign.cogit.geoxygene.contrib.geometrie.Operateurs;
import fr.ign.cogit.geoxygene.contrib.geometrie.Vecteur;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPosition;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPositionList;
import fr.ign.cogit.geoxygene.spatial.coordgeom.GM_LineString;
import fr.ign.cogit.geoxygene.spatial.geomprim.GM_Point;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileReader;
import fr.ign.cogit.geoxygene.util.index.Tiling;

public class WidthMassFunction implements TaggingMassFunction{

  
  public WidthMassFunction(){}

    Map<FuzzySet, IPopulation<IFeature>> polygons;


    FuzzySet continuation_filiation_error_filter;
    //FuzzySet filiation_error_filter;

    FuzzySet continuation_filiation_error_filter2;
   // FuzzySet filiation_error_filter2;

    public WidthMassFunction(Map<FuzzySet, String> shps) {

        //TODO : vérifier ici qu'on a bien des polygones
        this.polygons = new HashMap<FuzzySet, IPopulation<IFeature>>();
        for(FuzzySet t: shps.keySet()){
            IPopulation<IFeature> poly = ShapefileReader.read(shps.get(t));
            poly.initSpatialIndex(Tiling.class, false);
            this.polygons.put(t, poly);
            
            System.out.println(t.toString());
            System.out.println(poly.size());
        }

        try {
            // width < 10
            double[] cxval = new double[] { 1, 2, 2.5 };
            double[] cyval = new double[] { 0, 1, 1 };
            this.continuation_filiation_error_filter = new FuzzySet(cxval, cyval, 3);

//            double[] fxval = new double[] {1.5,2};
//            double[] fyval = new double[] { 0,1};
//            this.filiation_error_filter = new FuzzySet(fxval, fyval, 2);

            // width >= 10
            double[] cxval2 = new double[] { 1, 1.5, 2 };
            double[] cyval2 = new double[] { 0, 0, 1 };
            this.continuation_filiation_error_filter2= new FuzzySet(cxval2, cyval2, 3);

//            double[] fxval2 = new double[] {1.25,1.5};
//            double[] fyval2 = new double[] { 0,1};
//            this.filiation_error_filter2 = new FuzzySet(fxval2, fyval2, 2);

        } catch (XValuesOutOfOrderException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (YValueOutOfRangeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public float massOf(byte[] hypothesis, Observation to, Observation ho) {
        FuzzySet tbefore = to.getTime();
        FuzzySet tafter = ho.getTime();
        
        int indexBefore = -1;
        int indexAfter = -1;

        //on choisit l'ilotier juste avant et juste après
        IPopulation<IFeature> polygonBefore = null;
        IPopulation<IFeature> polygonAfter = null;
        List<FuzzySet> times = new ArrayList<FuzzySet>(this.polygons.keySet());
        Collections.sort(times, new Comparator<FuzzySet>(){
            @Override
            public int compare(FuzzySet o1, FuzzySet o2) {
                return WidthMassFunction.compare(o1, o2);
            }
        });
        if(times.contains(tbefore)){
            polygonBefore = this.polygons.get(tbefore);
        }
        else{
            for(int i=0; i< times.size() -1; i++){
                if(WidthMassFunction.compare(times.get(i), tbefore) == -1 &&
                        WidthMassFunction.compare(times.get(i+1), tbefore) == 1 ){
                    polygonBefore = this.polygons.get(times.get(i));
                    indexBefore=i;
                }
            }
        }
        if(times.contains(tafter)){
            polygonAfter = this.polygons.get(tafter);
        }
        else{
            for(int i=0; i< times.size() -1; i++){
                if(WidthMassFunction.compare(times.get(i), tafter) == -1 &&
                        WidthMassFunction.compare(times.get(i+1), tafter) == 1 ){
                    polygonAfter = this.polygons.get(times.get(i+1));
                    indexAfter = i+1;

                }
            }
        }

        
        


        
        if(polygonBefore != null && polygonAfter != null){
            float width1 = this.getWidth((ILineString) to.getGeometry(), polygonBefore);
            float width2 = this.getWidth( (ILineString) ho.getGeometry(), polygonAfter);
            
            while(width1 <=0 && indexBefore >0){
                indexBefore--;
                polygonBefore = this.polygons.get(times.get(indexBefore));
                width1 = this.getWidth((ILineString) to.getGeometry(), polygonBefore);
            }
            while(width2 <=0 && indexAfter < times.size()-1){
                indexAfter++;
                polygonAfter= this.polygons.get(times.get(indexAfter));
                width2 = this.getWidth((ILineString) to.getGeometry(), polygonAfter);
            }
            



            if( width1>0 && width2>0){
                if(width2 < width1){
                    //pas de rétreceissement a priori
                    if (Arrays.equals(hypothesis, new byte[] { 0,1})) {
                        return 1f;
                    }
                    return 0f;
                }
//                if (Arrays.equals(hypothesis, new byte[] { 1, 1, 1 ,1})) {
//                    return 0f;
//                }
                double d1 = width1;
                double d2 =width2;
                double ratio = d2 / d1;
                if(d1 <= 10){
                    if (Arrays.equals(hypothesis, new byte[] { 1,0})) {
                        return (float) this.continuation_filiation_error_filter.getMembership(ratio);
                    } else if (Arrays.equals(hypothesis, new byte[] { 0,1 })) {
                        return (float)(1f- this.continuation_filiation_error_filter.getMembership(ratio));
                    }
                    return 0;
                }

                else{
                    if (Arrays.equals(hypothesis, new byte[] { 1,0})) {
                        return (float) this.continuation_filiation_error_filter2.getMembership(ratio);
                    } else if (Arrays.equals(hypothesis, new byte[] { 0,1 })) {
                        return (float) (1f- this.continuation_filiation_error_filter2.getMembership(ratio));
                    }
                    return 0;
                }
            }
            else{
                if (Arrays.equals(hypothesis, new byte[] { 0,1})) {
                    return 1f;
                }
                return 0f;
            }
        }
        else{
            if (Arrays.equals(hypothesis, new byte[] { 0,1})) {
                return 1f;
            }
            return 0f;
        }

    }


    private float getWidth(ILineString le, IPopulation<IFeature> polygons){

        // géométrie de la rue
        Collection<IFeature> closestPolygons = polygons.select(le, 0);
        if(!closestPolygons.isEmpty()){
            // on regarde si cette intersection contient au moins la moitié de la ligne
            Iterator<IFeature> it = closestPolygons.iterator();
            IGeometry union = it.next().getGeom();
            while(it.hasNext()){
                union = union.union(it.next().getGeom());
            }
            if(union != null && union.intersection(le) != null && union.intersection(le).length() > 0.75* le.length()){
                return -1f;
            }
        }
        List<Double> values = new ArrayList<Double>();
        for(int i=20; i<=80; i+=5){
            IDirectPosition point = Operateurs.pointEnAbscisseCurviligne(le, le.length() * ((double)i)/100.);
            //milieu
            closestPolygons = polygons.select(point, 0);
            if(closestPolygons.isEmpty()){
                // milieu pas dans un ilot
                double v = this.getNormalWidth(point, le, i, polygons);
                if(v >0){
                    values.add(v);
                }
            }             
        }

        if(values.size() != 0){
            //moyenne, écart type
            double moyenne = 0;
            double sigma = 0;
            for (Double t : values) {
                moyenne += t;
            }
            moyenne /= (double) values.size();
            for (Double t : values) {
                sigma += (moyenne - t) * (moyenne - t);
            }
            sigma /= (double) values.size();
            sigma = Math.sqrt(sigma);
            //on retire tout ce qui est supérieur à moyenne +- 3sigma
            double width =0;
            double cpt=0.; 
            for (Double t : values) {
                if(t <= moyenne + 2*sigma && t >= moyenne - 2*sigma){
                    width += t;
                    cpt += 1.;
                }
            }
            return  (float)(width / cpt);
        }
        return -1;
    }


    public double getNormalWidth(IDirectPosition middle, ILineString line, double abscisse, IPopulation<IFeature> polygons){      
        IDirectPosition before = Operateurs.pointEnAbscisseCurviligne(line, line.length() * ((double)(abscisse-1.)/100.) );
        //vecteur normaux
        double x1 = middle.getX();
        double y1 = middle.getY();
        double x2 = before.getX();
        double y2 = before.getY();

        //vecteurs normaux
        Vecteur n1 = new Vecteur(y1-y2, x2-x1);
        n1 = n1.getNormalised();
        Vecteur n2 = new Vecteur(y2-y1, x1-x2);
        n2 = n2.getNormalised();
        IDirectPosition pnew1 = new DirectPosition(middle.getX() + n1.multConstante(70).getX(), middle.getY() + n1.multConstante(70).getY());
        IDirectPosition pnew2 = new DirectPosition(middle.getX() + n2.multConstante(70).getX(), middle.getY() + n2.multConstante(70).getY());

        //intersections avec les ilots
        IDirectPositionList l1 = new DirectPositionList();
        l1.add(middle);
        l1.add(pnew1);
        IDirectPositionList l2 = new DirectPositionList();
        l2.add(middle);
        l2.add(pnew2);
        ILineString line1 = new GM_LineString(l1);
        ILineString line2 = new GM_LineString(l2);
        Collection<IFeature> closestPolygons1 = polygons.select(line1,0);
        Collection<IFeature> closestPolygons2 = polygons.select(line2,0);
        for(IFeature f: new ArrayList<IFeature>(closestPolygons1)){
            if(!f.getGeom().intersects(line1) /*|| f.getGeom().intersection(line1)  == null*/){
                closestPolygons1.remove(f);
            }
        }
        for(IFeature f: new ArrayList<IFeature>(closestPolygons2)){
            if(!f.getGeom().intersects(line2) /*|| f.getGeom().intersection(line2)  == null*/){
                closestPolygons2.remove(f);
            }
        }
        if(closestPolygons1.isEmpty() || closestPolygons2.isEmpty() ){
            return this.getDirectedWidth(middle, line, abscisse, polygons);
        }
        //sens 1
        double d1 = -1.;
        IFeature close1 = null;
        if(closestPolygons1.size() == 1){
            //intersection
            close1 = closestPolygons1.iterator().next();
            d1 = close1.getGeom().distance(new GM_Point(middle));
        }
        else{
            Iterator<IFeature> itF = closestPolygons1.iterator();
            close1 = itF.next();
            d1 =  close1.getGeom().distance(new GM_Point(middle));
            while(itF.hasNext()){
                IFeature pol =itF.next();
                // intersetion  = pol.getGeom().intersection(line1);
                double d= -1;
                d =  pol.getGeom().distance(new GM_Point(middle));
                if(d < d1){
                    d1 = d;
                    close1 = pol;
                }
            }

        }
        //sens 2
        double d2 = -1.;
        IFeature close2 = null;
        if(closestPolygons2.size() == 1){
            //intersection
            close2 = closestPolygons2.iterator().next();
            d2 = close2.getGeom().distance(new GM_Point(middle));
        }
        else{
            Iterator<IFeature> itF = closestPolygons2.iterator();
            close2 = itF.next();
            d2 = close2.getGeom().distance(new GM_Point(middle));

            while(itF.hasNext()){
                IFeature pol =itF.next();
                double d= -1;
                d = pol.getGeom().distance(new GM_Point(middle));
                if(d < d2){
                    d2 = d;
                    close2 = pol;
                }
            }
        }    
        if(close1.equals(close2)){
            IGeometry intersetion1  = close1.getGeom().intersection(line1);
            IGeometry intersetion2  = close1.getGeom().intersection(line2);
            d1 = intersetion1.distance(new GM_Point(middle));
            d2 = intersetion2.distance(new GM_Point(middle));
        }

        double dNormal = d1+d2;
        double dDirected = this.getDirectedWidth(middle, line, abscisse, polygons);
        if(dDirected >0){
            return Math.min(dNormal, dDirected);
        }
        else{
            return dNormal;
        }

    }

    private  double getDirectedWidth(IDirectPosition middle, ILineString line, double abscisse, IPopulation<IFeature> polygons){      
        IDirectPosition before = Operateurs.pointEnAbscisseCurviligne(line, line.length() * ((double)(abscisse-1.)/100.) );
        //vecteur normaux
        double x1 = middle.getX();
        double y1 = middle.getY();
        double x2 = before.getX();
        double y2 = before.getY();

        //vecteurs directeurs
        Vecteur n1 = new Vecteur(x2-x1, y2-y1);
        n1 = n1.getNormalised();
        Vecteur n2 = new Vecteur(x1-x2, y1-y2);
        n2 = n2.getNormalised();
        IDirectPosition pnew1 = new DirectPosition(middle.getX() + n1.multConstante(70).getX(), middle.getY() + n1.multConstante(70).getY());
        IDirectPosition pnew2 = new DirectPosition(middle.getX() + n2.multConstante(70).getX(), middle.getY() + n2.multConstante(70).getY());

        //intersections avec les ilots
        IDirectPositionList l1 = new DirectPositionList();
        l1.add(middle);
        l1.add(pnew1);
        IDirectPositionList l2 = new DirectPositionList();
        l2.add(middle);
        l2.add(pnew2);
        ILineString line1 = new GM_LineString(l1);
        ILineString line2 = new GM_LineString(l2);
        Collection<IFeature> closestPolygons1 = polygons.select(line1,0);
        Collection<IFeature> closestPolygons2 = polygons.select(line2,0);
        for(IFeature f: new ArrayList<IFeature>(closestPolygons1)){
            if(!f.getGeom().intersects(line1) /*|| f.getGeom().intersection(line1)  == null*/){
                closestPolygons1.remove(f);
            }
        }
        for(IFeature f: new ArrayList<IFeature>(closestPolygons2)){
            if(!f.getGeom().intersects(line2) /*|| f.getGeom().intersection(line2)  == null*/){
                closestPolygons2.remove(f);
            }
        }
        if(closestPolygons1.isEmpty() || closestPolygons2.isEmpty() ){
            return -1;
        }
        //sens 1
        double d1 = -1.;
        IFeature close1 = null;
        if(closestPolygons1.size() == 1){
            //intersection
            close1 = closestPolygons1.iterator().next();
            d1 = close1.getGeom().distance(new GM_Point(middle));
        }
        else{
            Iterator<IFeature> itF = closestPolygons1.iterator();
            close1 = itF.next();
            d1 =  close1.getGeom().distance(new GM_Point(middle));
            while(itF.hasNext()){
                IFeature pol =itF.next();
                // intersetion  = pol.getGeom().intersection(line1);
                double d= -1;
                d =  pol.getGeom().distance(new GM_Point(middle));
                if(d < d1){
                    d1 = d;
                    close1 = pol;
                }
            }

        }
        //sens 2
        double d2 = -1.;
        IFeature close2 = null;
        if(closestPolygons2.size() == 1){
            //intersection
            close2 = closestPolygons2.iterator().next();
            d2 = close2.getGeom().distance(new GM_Point(middle));
        }
        else{
            Iterator<IFeature> itF = closestPolygons2.iterator();
            close2 = itF.next();
            d2 = close2.getGeom().distance(new GM_Point(middle));

            while(itF.hasNext()){
                IFeature pol =itF.next();
                double d= -1;
                d = pol.getGeom().distance(new GM_Point(middle));
                if(d < d2){
                    d2 = d;
                    close2 = pol;
                }
            }
        }    
        if(close1.equals(close2)){
            IGeometry intersetion1  = close1.getGeom().intersection(line1);
            IGeometry intersetion2  = close1.getGeom().intersection(line2);
            d1 = intersetion1.distance(new GM_Point(middle));
            d2 = intersetion2.distance(new GM_Point(middle));
        }

        return (d1+d2);

    }
    public String toString() {
        return "WidthFunc";
    }

    public static int compare(FuzzySet o1, FuzzySet o2){
        double a1 = o1.getX(0);
        double a2 = o2.getX(0);
        if(a1 < a2){
            return -1;
        }
        else if(a1>a2){
            return 1;
        }
        double d1 = o1.getX(3);
        double d2 = o2.getX(3);
        if(d1 < d2){
            return -1;
        }
        else if(d1>d2){
            return 1;
        }
        double b1 = o1.getX(1);
        double b2 = o2.getX(1);
        if(b1 < b2){
            return -1;
        }
        else if(b1>b2){
            return 1;
        }
        double c1 = o1.getX(2);
        double c2 = o2.getX(2);
        if(c1 < c2){
            return -1;
        }
        else if(c1>c2){
            return 1;
        }
        return 0;

    }
}