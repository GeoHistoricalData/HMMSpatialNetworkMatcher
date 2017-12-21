package fr.ign.cogit.morphogenesis.generation.barthelemy;

import java.util.Random;

import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPosition;

public class RandomGenerator {

  /**
   * 99.7% of the values lie in the 3-sigma range => sigma = radius / 3
   * @param radius the radius around 0
   * @return
   */
  public static double randomGaussian(double radius) {
    Random r = new Random();
    double sigma = radius / 3.;
    double random = r.nextGaussian() * sigma;

    if (random > 1) {
      random = 1;
    }
    if (random < -1) {
      random = -1;
    }
    return random;
  }

  public static double randomExponential(double lambda) {
    Random r = new Random();

    double random = -1. / lambda * Math.log(r.nextDouble());

    double signe = r.nextDouble();

    random = signe >= 0.5 ? random : -random;

    if (random > 1) {
      random = 1;
    }
    if (random < -1) {
      random = -1;
    }

    return random;
  }

  public static double randomUniform(double min, double max) {
    Random r = new Random();
    double random = min + (max - min) * r.nextDouble();
    return random;
  }

  public static void test(IDirectPosition a) {
    a = new DirectPosition(0, 0);
  }

}
