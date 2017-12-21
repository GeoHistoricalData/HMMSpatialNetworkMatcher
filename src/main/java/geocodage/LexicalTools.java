package geocodage;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import org.apache.xmlbeans.impl.common.Levenshtein;

import com.ibm.icu.text.RuleBasedNumberFormat;

import fr.ign.cogit.v2.tag.enrichment.stgeocoding.Soundex2;

/**
 * Cette classe contient les fonctions utilisées pour la manipulation de chaînes
 * de caractères : suppresion d'accent, élimination de mot parasites et de
 * symboles spéciaux, calcul de distances entre chaînes, etc.
 * @author BCostes
 * 
 */
public class LexicalTools {

  /**
   * Le dictionnaire des mots parasites (prépositions, articles, etc.)
   */
  private final static String[] wordsDictionary = { "le", "la", "les", "de",
      "des", "du", "dans", "ou", "et", "a", /*"au", "aux",*/ "un", "une", "en", "b", "c", "d",
      "e","f","g","h","i","j","k","l","m","n","o","p","s","t","u","v","w","x","y","z"};
  /**
   * Le dictionnaire des caractères parasites
   */
  private final static char[] carDictionary = { '\'', ' ', ',', '.', ';', '_',
      '\t', '\n', '-', ',', '/' };

  /**
   * Le dictionnaire des abréviations
   */
  private final static HashMap<String, String> abreviations = new HashMap<String, String>() {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    {
      put("pt", "petit");
      put("gd", "grand");
      put("st", "saint");
      put("ste", "sainte");
      put("all", "allee");
      put("av", "avenue");
      put("bd", "boulevard");
      put("crs", "cours");
      put("est", "esplanade");
      put("imp", "impasse");
      put("pas", "passage");
      put("mar", "marche");
      put("pl", "place");
      put("q", "quai");
      put("qu", "quai");
      put("sq", "square");
      put("r", "rue");
      put("fbg", "faubourg");
    }
  };
  

  /**
   * Le dictionnaire des mots parasites (prépositions, articles, etc.)
   */
  public final static List<String> typeDictionary = Arrays.asList(new String[]{ "rue", "avenue", "boulevard", "impasse", "passage", "quai", "marché",
    "square", "cours", "esplanade", "place", "chemin", "allee", "chaussee", "cour", "port", "pont","passerelle","porte", "ruelle",
    "route","voie","villa","terasse","cite"});

  /**
   * Le dictionnaire des mots à suppprimer
   */
  private final static List<String> wordToDelete = new ArrayList<String>() {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    {
      add("petit");
      add("petits");
      add("grand");
      add("grands");
      add("grande");
      add("grandes");
      add("neuve");
      add("nouvelle");
      add("nouvelles");
      add("nouveau");
      add("nouveaux");
    }
  };

  /** Pour l'élminination des accents : Index du 1er caractere accentué **/
  private static final int MIN = 192;
  /** Pour l'élminination des accents : Index du dernier caractere accentué **/
  private static final int MAX = 255;
  /**
   * Pour l'élminination des accents : Vecteur de correspondance entre accent /
   * sans accent
   **/
  private static final Vector<?> map = initMap();

  // *******************************************************************************
  // ***********************Méthodes de modification de
  // chaînes*********************
  // *******************************************************************************

  /**
   * Découpe une chaîne de caractères en enlevant accent,s majuscules, chiffres,
   * caractères spéciaux et mots définis par les dictionnaires
   * @param s : la chaîne à parser
   * @return Ue liste de mots issus du découpage de la chaîne d'origine
   */
  public static ArrayList<String> parse1(String s) {
    // Découpage selon les caractères spéciaux en enlevant les mots parasites
    int size = s.length();
    ArrayList<String> results = new ArrayList<String>(); // variable à renvoyer
    String ss = "";

    for (int i = 0; i < size; i++) {
      if (containsCar(s.charAt(i))) {
        // si le tableau carDictionary contient le caractère courant
        boolean dicFound = false;

        for (int j = 0; j < wordsDictionary.length; j++) {
          if (ss.equals(wordsDictionary[j])) {
            dicFound = true;
            break;
          }
        }
        if (!dicFound && ss.length() != 0) {
          // Vérification des abréviation
          if (abreviations.containsKey(ss)) {
            ss = abreviations.get(ss);
          }
          results.add(ss);
        }
        ss = "";
      } else if (s.charAt(i) == carDictionary[0]) {
        ss = "";
      } else {
        ss += s.charAt(i);
      }
    }
    if (ss.length() != 0) {
      if (abreviations.containsKey(ss)) {
        ss = abreviations.get(ss);
      }
      results.add(ss);
    }
    for (String word : wordToDelete) {
      results.remove(word);
    }
    return results;
  }
  
  /**
   * idem parse1 mais sans supprimer les neuves, petits, etc.
   * @param s
   * @return
   */
  public static ArrayList<String> parse2(String s) {
    s = s.toLowerCase();
    // Découpage selon les caractères spéciaux en enlevant les mots parasites
    int size = s.length();
    ArrayList<String> results = new ArrayList<String>(); // variable à renvoyer
    String ss = "";

    for (int i = 0; i < size; i++) {
      if (containsCar(s.charAt(i))) {
        // si le tableau carDictionary contient le caractère courant
        boolean dicFound = false;

        for (int j = 0; j < wordsDictionary.length; j++) {
          if (ss.equals(wordsDictionary[j])) {
            dicFound = true;
            break;
          }
        }
        if (!dicFound && ss.length() != 0) {
          // Vérification des abréviation
          if (abreviations.containsKey(ss)) {
            ss = abreviations.get(ss);
          }
          results.add(ss);
        }
        ss = "";
      } else if (s.charAt(i) == carDictionary[0]) {
        ss = "";
      } else {
        ss += s.charAt(i);
      }
    }
    if (ss.length() != 0) {
      if (abreviations.containsKey(ss)) {
        ss = abreviations.get(ss);
      }
      results.add(ss);
    }
    return results;
  }

  /**
   * Vérifie si un caaractère existe dans le tableau carDictionary
   * @param c le caractère
   * @return vrai si il y est, faux sinon
   */
  private static boolean containsCar(char c) {
    for (int i = 1; i < carDictionary.length; i++) {
      if (carDictionary[i] == c) {
        return true;
      }
    }
    return false;
  }

  /**
   * Pour l'élminination des accents : initialise le mapping accent / sans
   * accent
   */
  private static Vector<String> initMap() {
    Vector<String> Result = new Vector<String>();
    java.lang.String car = null;

    car = new java.lang.String("A");
    Result.add(car); /* '\u00C0' À alt-0192 */
    Result.add(car); /* '\u00C1' Á alt-0193 */
    Result.add(car); /* '\u00C2' Â alt-0194 */
    Result.add(car); /* '\u00C3' Ã alt-0195 */
    Result.add(car); /* '\u00C4' Ä alt-0196 */
    Result.add(car); /* '\u00C5' Å alt-0197 */
    car = new java.lang.String("AE");
    Result.add(car); /* '\u00C6' Æ alt-0198 */
    car = new java.lang.String("C");
    Result.add(car); /* '\u00C7' Ç alt-0199 */
    car = new java.lang.String("E");
    Result.add(car); /* '\u00C8' È alt-0200 */
    Result.add(car); /* '\u00C9' É alt-0201 */
    Result.add(car); /* '\u00CA' Ê alt-0202 */
    Result.add(car); /* '\u00CB' Ë alt-0203 */
    car = new java.lang.String("I");
    Result.add(car); /* '\u00CC' Ì alt-0204 */
    Result.add(car); /* '\u00CD' Í alt-0205 */
    Result.add(car); /* '\u00CE' Î alt-0206 */
    Result.add(car); /* '\u00CF' Ï alt-0207 */
    car = new java.lang.String("D");
    Result.add(car); /* '\u00D0' Ð alt-0208 */
    car = new java.lang.String("N");
    Result.add(car); /* '\u00D1' Ñ alt-0209 */
    car = new java.lang.String("O");
    Result.add(car); /* '\u00D2' Ò alt-0210 */
    Result.add(car); /* '\u00D3' Ó alt-0211 */
    Result.add(car); /* '\u00D4' Ô alt-0212 */
    Result.add(car); /* '\u00D5' Õ alt-0213 */
    Result.add(car); /* '\u00D6' Ö alt-0214 */
    car = new java.lang.String("*");
    Result.add(car); /* '\u00D7' × alt-0215 */
    car = new java.lang.String("0");
    Result.add(car); /* '\u00D8' Ø alt-0216 */
    car = new java.lang.String("U");
    Result.add(car); /* '\u00D9' Ù alt-0217 */
    Result.add(car); /* '\u00DA' Ú alt-0218 */
    Result.add(car); /* '\u00DB' Û alt-0219 */
    Result.add(car); /* '\u00DC' Ü alt-0220 */
    car = new java.lang.String("Y");
    Result.add(car); /* '\u00DD' Ý alt-0221 */
    car = new java.lang.String("Þ");
    Result.add(car); /* '\u00DE' Þ alt-0222 */
    car = new java.lang.String("B");
    Result.add(car); /* '\u00DF' ß alt-0223 */
    car = new java.lang.String("a");
    Result.add(car); /* '\u00E0' à alt-0224 */
    Result.add(car); /* '\u00E1' á alt-0225 */
    Result.add(car); /* '\u00E2' â alt-0226 */
    Result.add(car); /* '\u00E3' ã alt-0227 */
    Result.add(car); /* '\u00E4' ä alt-0228 */
    Result.add(car); /* '\u00E5' å alt-0229 */
    car = new java.lang.String("ae");
    Result.add(car); /* '\u00E6' æ alt-0230 */
    car = new java.lang.String("c");
    Result.add(car); /* '\u00E7' ç alt-0231 */
    car = new java.lang.String("e");
    Result.add(car); /* '\u00E8' è alt-0232 */
    Result.add(car); /* '\u00E9' é alt-0233 */
    Result.add(car); /* '\u00EA' ê alt-0234 */
    Result.add(car); /* '\u00EB' ë alt-0235 */
    car = new java.lang.String("i");
    Result.add(car); /* '\u00EC' ì alt-0236 */
    Result.add(car); /* '\u00ED' í alt-0237 */
    Result.add(car); /* '\u00EE' î alt-0238 */
    Result.add(car); /* '\u00EF' ï alt-0239 */
    car = new java.lang.String("d");
    Result.add(car); /* '\u00F0' ð alt-0240 */
    car = new java.lang.String("n");
    Result.add(car); /* '\u00F1' ñ alt-0241 */
    car = new java.lang.String("o");
    Result.add(car); /* '\u00F2' ò alt-0242 */
    Result.add(car); /* '\u00F3' ó alt-0243 */
    Result.add(car); /* '\u00F4' ô alt-0244 */
    Result.add(car); /* '\u00F5' õ alt-0245 */
    Result.add(car); /* '\u00F6' ö alt-0246 */
    car = new java.lang.String("/");
    Result.add(car); /* '\u00F7' ÷ alt-0247 */
    car = new java.lang.String("0");
    Result.add(car); /* '\u00F8' ø alt-0248 */
    car = new java.lang.String("u");
    Result.add(car); /* '\u00F9' ù alt-0249 */
    Result.add(car); /* '\u00FA' ú alt-0250 */
    Result.add(car); /* '\u00FB' û alt-0251 */
    Result.add(car); /* '\u00FC' ü alt-0252 */
    car = new java.lang.String("y");
    Result.add(car); /* '\u00FD' ý alt-0253 */
    car = new java.lang.String("þ");
    Result.add(car); /* '\u00FE' þ alt-0254 */
    car = new java.lang.String("y");
    Result.add(car); /* '\u00FF' ÿ alt-0255 */
    Result.add(car); /* '\u00FF' alt-0255 */

    return Result;
  }

  /**
   * Diacritics suppresion : Transforme une chaine pouvant contenir des accents
   * dans une version sans accent
   * @param chaine Chaine a convertir sans accent
   * @return Chaine dont les accents ont été supprimé
   **/
  public static java.lang.String diacriticsSuppression(java.lang.String chaine) {
    java.lang.StringBuffer Result = new StringBuffer(chaine);

    for (int bcl = 0; bcl < Result.length(); bcl++) {
      int carVal = chaine.charAt(bcl);
      if (carVal >= MIN && carVal <= MAX) { // Remplacement
        java.lang.String newVal = (java.lang.String) map.get(carVal - MIN);
        Result.replace(bcl, bcl + 1, newVal);
      }
    }
    return Result.toString();
  }

  /**
   * Digit suppression : supprime les chiffres
   * @param s1 la chaîne à modifier
   * @return la chaîne modifiée sans chiffres
   */
  public static String digitSuppression(String s1) {
    String s2 = "";
    for (int i = 0; i < s1.length(); i++) {
      if (s1.charAt(i) == '0' || s1.charAt(i) == '1' || s1.charAt(i) == '1'
          || s1.charAt(i) == '2' || s1.charAt(i) == '3' || s1.charAt(i) == '4'
          || s1.charAt(i) == '5' || s1.charAt(i) == '6' || s1.charAt(i) == '7'
          || s1.charAt(i) == '8' || s1.charAt(i) == '9') {
      } else {
        s2 += s1.charAt(i);
      }
    }
    return s2;
  }

  /**
   * True si contient un chiffre
   */
  public static boolean containsDigit(String s1) {
    for (int i = 0; i < s1.length(); i++) {
      if (s1.charAt(i) == '0' || s1.charAt(i) == '1' || s1.charAt(i) == '1'
          || s1.charAt(i) == '2' || s1.charAt(i) == '3' || s1.charAt(i) == '4'
          || s1.charAt(i) == '5' || s1.charAt(i) == '6' || s1.charAt(i) == '7'
          || s1.charAt(i) == '8' || s1.charAt(i) == '9') {
        return true;
      }

    }
    return false;
  }

  // *******************************************************************************
  // *************************Méthodes de calcul de
  // distances***********************
  // *******************************************************************************

  /**
   * @param s1 la première chaine
   * @param s2 la seconde chaine
   * @return la distance de Damareau-Levenshtein entre les deux chaines
   */
  public static int dDamarauLevenshtein(String s1, String s2) {
    if (s1.equals("") || s2.equals("")) {
      return 1;
    }
    int d[][] = new int[s1.length() + 1][s2.length() + 1];
    for (int i = 0; i < s1.length() + 1; i++) {
      d[i][0] = i;
    }
    for (int j = 0; j < s2.length() + 1; j++) {
      d[0][j] = j;
    }

    int cout = 0;
    for (int i = 1; i < s1.length() + 1; i++) {
      for (int j = 1; j < s2.length() + 1; j++) {
        if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
          cout = 0;
        } else {
          cout = 1;
        }
        d[i][j] = Math.min(d[i - 1][j] + 1,
            Math.min(d[i][j - 1] + 1, d[i - 1][j - 1] + cout));

        if (i > 1 && j > 1 && (s1.charAt(i - 1) == s2.charAt(j - 2))
            && (s1.charAt(i - 2) == s2.charAt((j - 1)))) {
          d[i][j] = Math.min(d[i][j], d[i - 2][j - 2] + cout); // transposition
        }
      }
    }
    return d[s1.length()][s2.length()];
  }

  /**
   * @param s1 la première chaine
   * @param s2 la seconde chaine
   * @return la distance de Damarau-Levenshtein normalisée entre deux chaînes
   */
  public static double dDamarauLevenshteinNormalized(String s1, String s2) {
    double result = (double) dDamarauLevenshtein(s1, s2)
        / Math.max(s1.length(), s2.length());
    return result;
  }

  /**
   * 
   * @param s1 premier groupe de mots
   * @param s2 second groupe de mot
   * @return le coefficient de similarité normalisé entre les deux groupes de
   *         mots
   */
  public static double lexicalSimilarityCoeff(List<String> s1,
      List<String> s2) {
    int min = Math.min(s1.size(), s2.size());
    int max = Math.max(s1.size(), s2.size());
    List<String> sMax, sMin;
    // sMax : phrase la plus longue
    // sMin : phrase la plus courte
    if (max == s1.size()) {
      sMax = s1;
      sMin = s2;
    } else {
      sMax = s2;
      sMin = s1;
    }
    // initialisation de la matrice mot / mot
    double mat[][] = new double[max][min];
    double result = 0;
    for (int i = 0; i < max; i++) {
      double minI = 1;
      for (int j = 0; j < min; j++) {
        mat[i][j] = dDamarauLevenshteinNormalized(sMax.get(i), sMin.get(j));
        if (mat[i][j] < minI) {
          minI = mat[i][j];
        }
      }
      result += minI;
    }
    result = 2 * result / (min + max);
    return result;
  }

  /**
   * Utilisé lorsqu'on compare un nom de classe à un att. nature
   * @param s1 : la classe
   * @param s2: l'att. nature
   */
  public static double lexicalSimilarityCoeff2(List<String> s1, List<String> s2) {
    int min = Math.min(s1.size(), s2.size());
    int max = Math.max(s1.size(), s2.size());
    List<String> sMax, sMin;
    // sMax : phrase la plus longue
    // sMin : phrase la plus courte
    if (max == s1.size()) {
      sMax = s1;
      sMin = s2;
    } else {
      sMax = s2;
      sMin = s1;
    }
    // initialisation de la matrice mot / mot
    double mat[][] = new double[min][max];
    double result = 0;
    for (int i = 0; i < min; i++) {
      double minI = 1;
      for (int j = 0; j < max; j++) {
        mat[i][j] = dDamarauLevenshteinNormalized(sMin.get(i), sMax.get(j));
        if (mat[i][j] < minI) {
          minI = mat[i][j];
        }
      }
      result += minI;
    }
    result = 2 * result / (min + max);
    return result;
  }

  public static List<String> parseComma(String s) {
    List<String> result = new ArrayList<String>();
    String ss = "";
    for (int i = 0; i < s.length(); i++) {
      if (s.charAt(i) != ',') {
        ss += s.charAt(i);
      } else {
        result.add(ss);
        ss = "";
      }
    }
    result.add(ss);
    return result;
  }

  public static ArrayList<File> listeRepertoire(File repertoire) {
    ArrayList<File> result = new ArrayList<File>();
    if (repertoire.isDirectory()) {
      File[] list = repertoire.listFiles();
      if (list != null) {
        for (int i = 0; i < list.length; i++) {
          // Appel récursif sur les sous-répertoires
          ArrayList<File> result2 = new ArrayList<File>();
          result2 = listeRepertoire(list[i]);
          for (int j = 0; j < result2.size(); j++) {
            result.add(result2.get(j));
          }
        }
      } else {
        System.err.println(repertoire + " : Erreur de lecture.");
      }
    }
    result.add(repertoire);
    return result;
  }

  public static double distanceTriGram(String s1, String s2) {

    if (s1 == null || s2 == null) {
      return 0.;
    }

    int l1 = s1.length() - 3 + 1;
    int l2 = s2.length() - 3 + 1;
    int found = 0;
    for (int i = 0; i < l1; i++) {
      for (int j = 0; j < l2; j++) {
        int k = 0;
        for (; (k < 3) && (s1.charAt(i + k) == s2.charAt(j + k)); k++)
          ;
        if (k == 3)
          found++;
      }
    }
    return (1 - 2 * ((double) found) / ((l1 + l2)));
  }

  public static String phoneticName(String s1) {

    ArrayList<String> s1P = LexicalTools.parse1(s1);

    s1 = "";
    for (String s : s1P) {
      if (LexicalTools.containsDigit(s)
          && LexicalTools.digitSuppression(s).isEmpty()) {
        RuleBasedNumberFormat formatter = new RuleBasedNumberFormat(Locale.FRANCE,
            RuleBasedNumberFormat.SPELLOUT);
        s1 += formatter.format(Integer.parseInt(s)) + " ";
      } else {
        s1 += s + " ";
      }
    }

    s1 = s1.substring(0, s1.length());

    return Phonetic.genererPhonetic(s1);
  }
  public static float longestSubstrSim(String s_, String t_) {
    int max_size = Math.max(s_.replaceAll(" ", "").length(), //$NON-NLS-1$ //$NON-NLS-2$
        t_.replaceAll(" ", "").length()); //$NON-NLS-1$ //$NON-NLS-2$
    return (float) longestSubstr(s_, t_) / (float) max_size;
  }

  public static int longestSubstr(String s_, String t_) {
    String s = s_.toLowerCase();
    s = s.replaceAll("[\\W]|_\\- ", ""); //$NON-NLS-1$ //$NON-NLS-2$

    String t = t_.toLowerCase();
    t = t.replaceAll("[\\W]|_\\- ", ""); //$NON-NLS-1$ //$NON-NLS-2$
    s = s.trim();
    t = t.trim();
    if (s.isEmpty() || t.isEmpty()) {
      return 0;
    }

    int m = s.length();
    int n = t.length();
    int cost = 0;
    int maxLen = 0;
    int[] p = new int[n];
    int[] d = new int[n];

    for (int i = 0; i < m; ++i) {
      for (int j = 0; j < n; ++j) {
        if (s.charAt(i) != t.charAt(j)) {
          cost = 0;
        } else {
          if ((i == 0) || (j == 0)) {
            cost = 1;
          } else {
            cost = p[j - 1] + 1;
          }
        }
        d[j] = cost;

        if (cost > maxLen) {
          maxLen = cost;
        }
      }
      int[] swap = p;
      p = d;
      d = swap;
    }

    return maxLen;
  }

  public static List<String> parsePhoneticName(String s) {
    List<String> l = new ArrayList<String>();
    for (String ss : LexicalTools.parse1(s)) {
      l.add(LexicalTools.phoneticName(ss));
    }

    return l;
  }

  public static void main(String args[]) {

    String s1 = "fontaines au roi";
    String s2 = "fontaine au roy";
   
    List<String> l1 = LexicalTools.parse2(s1);
    List<String> l2 = LexicalTools.parse2(s2);
    
    String ss1 = "", ss2 = "";
    String sss1 = "", sss2 = "";

    for(String s : l1){
      ss1 += s+" ";
      sss1+= Soundex2.soundex2(s);
      sss1=sss1.trim();
    }
    ss1  =ss1.trim();
    for(String s : l2){
      ss2 += s+" ";
      sss2+= Soundex2.soundex2(s);
      sss2=sss2.trim();
    }
    ss2  =ss2.trim();
    
    System.out.println(ss1+" " +ss2);
    
    System.out.println(sss1);
    System.out.println(sss2);


    
    System.out.println(Levenshtein.distance(ss1, ss2));
    System.out.println(Levenshtein.distance(sss1, sss2));
    
    System.out.println(LexicalTools.longestSubstrSim(sss1, sss2));
    System.out.println(LexicalTools.lexicalSimilarityCoeff2(l1, l2));

    
  }
}
