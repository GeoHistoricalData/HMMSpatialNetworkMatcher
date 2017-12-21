package fr.ign.cogit.morphogenesis.exploring_tool.model.indicators;

import fr.ign.cogit.morphogenesis.exploring_tool.model.indicators.morphology.global.ClusteringCoefficient;
import fr.ign.cogit.morphogenesis.exploring_tool.model.indicators.morphology.global.GlobalMorphologicalIndicator;
import fr.ign.cogit.morphogenesis.exploring_tool.model.indicators.morphology.local.BetweennessCentralityVisitor;
import fr.ign.cogit.morphogenesis.exploring_tool.model.indicators.morphology.local.CircuitCentralityVisitor;
import fr.ign.cogit.morphogenesis.exploring_tool.model.indicators.morphology.local.ClosenessCentralityVisitor;
import fr.ign.cogit.morphogenesis.exploring_tool.model.indicators.morphology.local.EfficiencyCentralityVisitor;
import fr.ign.cogit.morphogenesis.exploring_tool.model.indicators.morphology.local.LocalMorphologicalIndicator;
import fr.ign.cogit.morphogenesis.exploring_tool.model.indicators.morphology.local.LocalStraightnessCentralityVisitor;
import fr.ign.cogit.morphogenesis.exploring_tool.model.indicators.morphology.local.StraightnessCentralityVisitor;
import fr.ign.cogit.morphogenesis.exploring_tool.utils.GlobalMorphologicalIndicatorList;
import fr.ign.cogit.morphogenesis.exploring_tool.utils.LocalMorphologicalndicatorList;

public class IndicatorVisitorBuilder {

  public static LocalMorphologicalIndicator createLocalMorphologicalIndicator(
      String indicatorName) {
    if (indicatorName.equals(LocalMorphologicalndicatorList.CLOSENESS)) {
      return new ClosenessCentralityVisitor();
    }
    if (indicatorName.equals(LocalMorphologicalndicatorList.BETWEENNESS)) {
      return new BetweennessCentralityVisitor();
    }
    if (indicatorName.equals(LocalMorphologicalndicatorList.STRAIGHTNESS)) {
      return new StraightnessCentralityVisitor();
    }
    if (indicatorName.equals(LocalMorphologicalndicatorList.CIRCUIT)) {
      return new CircuitCentralityVisitor();
    }
    if (indicatorName.equals(LocalMorphologicalndicatorList.STRAIGHTNESSLOCAL)) {
      return new LocalStraightnessCentralityVisitor();
    }
    if (indicatorName.equals(LocalMorphologicalndicatorList.EFFICIENCY)) {
      return new EfficiencyCentralityVisitor();
    }
    return null;
  }

  public static GlobalMorphologicalIndicator createGlobalMorphologicalIndicator(
      String indicatorName) {
    if (indicatorName.equals(GlobalMorphologicalIndicatorList.CLUSTERING_COEFF)) {
      return new ClusteringCoefficient();
    }
    return null;
  }
}
