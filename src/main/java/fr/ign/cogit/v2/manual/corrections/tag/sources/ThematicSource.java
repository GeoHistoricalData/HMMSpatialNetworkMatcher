package fr.ign.cogit.v2.manual.corrections.tag.sources;

import java.util.List;

import v2.tagging.TaggingSource;
import fr.ign.cogit.v2.manual.corrections.tag.criterions.StreetNamesMassFunction;

public class ThematicSource extends TaggingSource {

    public ThematicSource(List<byte[]> frame, StreetNamesMassFunction func) {
      super(frame, func);
    }

  }