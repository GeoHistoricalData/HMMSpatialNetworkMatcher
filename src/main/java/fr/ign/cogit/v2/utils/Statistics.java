package fr.ign.cogit.v2.utils;

import java.util.HashMap;
import java.util.Map;

public class Statistics<T> {


  public Map<T, Double> centrerRéduire(Map<T, Double> values) {
    Map<T, Double> result = new HashMap<T, Double>();
    double moyenne = 0;
    double sigma = 0;
    for (T t : values.keySet()) {
      moyenne += values.get(t);
    }
    moyenne /= (double) values.size();
    for (T t : values.keySet()) {
      sigma += (moyenne - values.get(t)) * (moyenne - values.get(t));
    }
    sigma /= (double) values.size();
    sigma = Math.sqrt(sigma);
    for (T t : values.keySet()) {
      double varCR = (values.get(t) - moyenne) / sigma;
      result.put(t, varCR);
    }
    return result;
  }

  public Map<T, Double> normalize(Map<T, Double> values) {

    double min = Double.MAX_VALUE;
    double max = Double.MIN_VALUE;
    for (T t : values.keySet()) {
      if (values.get(t) < min) {
        min = values.get(t);
      }
      if (values.get(t) > max) {
        max = values.get(t);
      }
    }
    Map<T, Double> result = new HashMap<T, Double>();
    for (T t : values.keySet()) {
      double value = (values.get(t) - min) / (max - min);
      result.put(t, value);
    }
    return result;

  }

}
