package fr.ign.cogit.v2.tag.enrichment.stgeocoding.evidence;

import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlTransient;

import fr.ign.cogit.geoxygene.function.ConstantFunction;
import fr.ign.cogit.geoxygene.function.Function1D;
import fr.ign.cogit.geoxygene.function.FunctionEvaluationException;
import fr.ign.cogit.geoxygene.function.LinearFunction;
import fr.ign.cogit.geoxygene.matching.dst.evidence.codec.EvidenceCodec;
import fr.ign.cogit.geoxygene.matching.dst.sources.GeoSource;
import fr.ign.cogit.geoxygene.matching.dst.sources.Source;
import fr.ign.cogit.geoxygene.matching.dst.sources.punctual.EuclidianDist;
import fr.ign.cogit.geoxygene.matching.dst.sources.text.LevenshteinDist;
import fr.ign.cogit.geoxygene.matching.dst.util.Pair;
import fr.ign.cogit.v2.tag.enrichment.stgeocoding.TextualAdress;

public abstract class GeocodeSource  implements Source<TextualAdress, GeocodeHypothesis> {

  /**
   * Default constructor.
   */
  public GeocodeSource() {
    super();
  }
  /**
   * @param candidates
   * @param encoded
   */
  @Override
  public List<Pair<byte[], Float>> evaluate(TextualAdress reference, final List<GeocodeHypothesis> candidates,
      EvidenceCodec<GeocodeHypothesis> codec) {
    return null;
  }

  @XmlTransient
  protected String name = this.getClass().getName().substring(
      this.getClass().getName().lastIndexOf('.') + 1);

  /** . */
  private Function1D[] masseAppCi;

  /** . */
  private Function1D[] masseAppPasCi;

  /** . */
  private Function1D[] masseIgnorance;



  public void setMasseAppCi(Function1D... masseAppCi) {
    this.masseAppCi = masseAppCi;
  }

  @XmlElementWrapper(name = "MasseAppCi")
  @XmlAnyElement
  public Function1D[] getMasseAppCi() {
    return this.masseAppCi;
  }

  public void setMasseAppPasCi(Function1D... masseAppPasCi) {
    this.masseAppPasCi = masseAppPasCi;
  }

  @XmlElementWrapper
  @XmlAnyElement
  public Function1D[] getMasseAppPasCi() {
    return this.masseAppPasCi;
  }

  public void setMasseIgnorance(Function1D... masseIgnorance) {
    this.masseIgnorance = masseIgnorance;
  }

  @XmlElementWrapper
  @XmlAnyElement
  public Function1D[] getMasseIgnorance() {
    return this.masseIgnorance;
  }

  @Override
  public String toString() {
    return this.getName();
  }



  /**
   * Source name is used to identify hypothessi values.
   * @return
   */
  @Override
  public String getName() {
    return "Default Source";
  }

  @Override
  public double[] evaluate(TextualAdress reference, final GeocodeHypothesis candidates) {
    return null;
  }

  public double[] getMasses(double distance) {

    double[] masses = new double[3];

    // Fonction EstApparie
    float masse1 = 0.0f;
    if (masseAppCi != null) {
      for (Function1D f : masseAppCi) {
        if (f.isBetween(distance)) {
          try {
            masse1 = f.evaluate(distance).floatValue();
          } catch (FunctionEvaluationException e) {
            e.printStackTrace();
          }
        }
      }
    }
    masses[0] = masse1;

    // Fonction NonApparie
    float masse2 = 0.0f;
    if (masseAppPasCi != null) {
      for (Function1D f : masseAppPasCi) {
        if (f.isBetween(distance)) {
          try {
            masse2 = f.evaluate(distance).floatValue();
          } catch (FunctionEvaluationException e) {
            e.printStackTrace();
          }
        }
      }
    }
    masses[1] = masse2;

    // Fonction PrononcePas
    float masse3 = 0.0f;
    if (masseIgnorance != null) {
      for (Function1D f : masseIgnorance) {
        if (f.isBetween(distance)) {
          try {
            masse3 = f.evaluate(distance).floatValue();
          } catch (FunctionEvaluationException e) {
            e.printStackTrace();
          }
        }
      }
    }
    masses[2] = masse3;

    return masses;
  }

  public void marshall() {
    try {
      JAXBContext jc = JAXBContext.newInstance(GeoSource.class, EuclidianDist.class, LevenshteinDist.class,
          LinearFunction.class, ConstantFunction.class);
      Marshaller marshaller = jc.createMarshaller();
      marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
      marshaller.marshal(this, System.out);
    } catch (JAXBException e1) {
      e1.printStackTrace();
    }
  }

  public void marshall(String filename) {
    try {
      JAXBContext context = JAXBContext.newInstance(GeoSource.class, EuclidianDist.class, LevenshteinDist.class,
          LinearFunction.class, ConstantFunction.class);
      Marshaller marshaller = context.createMarshaller();
      marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
      marshaller.marshal(this, new File(filename));
    } catch (JAXBException e1) {
      e1.printStackTrace();
    }
  }

  public void marshall(StringWriter w) {
    try {
      JAXBContext context = JAXBContext.newInstance(GeoSource.class, EuclidianDist.class, LevenshteinDist.class,
          LinearFunction.class, ConstantFunction.class);
      Marshaller marshaller = context.createMarshaller();
      marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
      marshaller.marshal(this, w);
    } catch (JAXBException e1) {
      e1.printStackTrace();
    }
  }

  public static GeoSource unmarshall(File file) throws Exception { 
    try {
      JAXBContext context = JAXBContext.newInstance(GeoSource.class, EuclidianDist.class, LevenshteinDist.class,
          LinearFunction.class, ConstantFunction.class);
      Unmarshaller unmarshaller = context.createUnmarshaller(); 
      GeoSource root = (GeoSource) unmarshaller.unmarshal(file); 
      return root; 
    } catch (Exception e1) { 
      e1.printStackTrace(); throw e1; 
    } 
  }

  public static GeoSource unmarshall(String inputXML) throws Exception { 
    try { 
      JAXBContext context = JAXBContext.newInstance(GeoSource.class, EuclidianDist.class, LevenshteinDist.class,
          LinearFunction.class, ConstantFunction.class);
      Unmarshaller msh = context.createUnmarshaller(); 
      StringReader reader = new StringReader(inputXML); 
      GeoSource root = (GeoSource)msh.unmarshal(reader); 
      return root; 
    } catch (Exception e1) {
      e1.printStackTrace(); 
      throw e1;
    }
  }
}