package fr.ign.cogit.v2.manual.corrections.tag.sources;

import java.util.List;

import v2.tagging.TaggingSource;
import fr.ign.cogit.v2.manual.corrections.tag.criterions.FrechetMassFunction;
import fr.ign.cogit.v2.manual.corrections.tag.criterions.OrientationMassFunction;
import fr.ign.cogit.v2.manual.corrections.tag.criterions.SinuosityMassFunction;
import fr.ign.cogit.v2.manual.corrections.tag.criterions.WidthMassFunction;

public class GeometricSource extends TaggingSource{

    public GeometricSource(List<byte[]> frame,FrechetMassFunction func) {
        super(frame,func);

    }

    public GeometricSource(List<byte[]> frame,OrientationMassFunction func) {
        super(frame,func);

    }

    public GeometricSource(List<byte[]> frame, WidthMassFunction func) {
        super(frame,func);

    }
    
    public GeometricSource(List<byte[]> frame, SinuosityMassFunction func) {
        super(frame,func);

    }
}


