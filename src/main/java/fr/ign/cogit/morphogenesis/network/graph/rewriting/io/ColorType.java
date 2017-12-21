package fr.ign.cogit.morphogenesis.network.graph.rewriting.io;

import java.util.ArrayList;
import java.util.List;

public class ColorType {

  public static final int VALUES = 1;
  public static final int QUANTIL = 2;
  public static final int INTERVAL = 3;
  public static final int LOG_VALUES = 4;
  public static final int LOG_INTERVAL = 5;
  public static final int MORE_CENTRAL = 6;
  public static final int LESS_CENTRAL = 7;
  public static final int LENGTH_QUANTIL = 8;

  public static int getType(String s) {
    if (s.equals("VALUES")) {
      return ColorType.VALUES;
    } else if (s.equals("QUANTIL")) {
      return ColorType.QUANTIL;
    } else if (s.equals("INTERVAL")) {
      return ColorType.INTERVAL;
    } else if (s.equals("LOG_VALUES")) {
      return ColorType.LOG_VALUES;
    } else if (s.equals("LOG_INTERVAL")) {
      return ColorType.LOG_INTERVAL;
    } else if (s.equals("MORE_CENTRAL")) {
      return ColorType.MORE_CENTRAL;
    } else if (s.equals("LESS_CENTRAL")) {
      return ColorType.LESS_CENTRAL;
    } else if (s.equals("LENGTH_QUANTIL")) {
      return ColorType.LENGTH_QUANTIL;
    }
    return -1;
  }

  public static String getType(int i) {
    switch (i) {
      case ColorType.VALUES:
        return "VALUES";
      case ColorType.QUANTIL:
        return "QUANTIL";
      case ColorType.INTERVAL:
        return "INTERVAL";
      case ColorType.LOG_VALUES:
        return "LOG_VALUES";
      case ColorType.LOG_INTERVAL:
        return "LOG_INTERVAL";
      case ColorType.MORE_CENTRAL:
        return "MORE_CENTRAL";
      case ColorType.LESS_CENTRAL:
        return "LESS_CENTRAL";
      case ColorType.LENGTH_QUANTIL:
        return "LENGTH_QUANTIL";
      default:
        return "";
    }
  }

  public static List<String> getTypes() {
    List<String> types = new ArrayList<String>();
    types.add("VALUES");
    types.add("QUANTIL");
    types.add("INTERVAL");
    types.add("LOG_VALUES");
    types.add("LOG_INTERVAL");
    types.add("MORE_CENTRAL");
    types.add("LESS_CENTRAL");
    types.add("LENGTH_QUANTIL");

    return types;
  }

}
