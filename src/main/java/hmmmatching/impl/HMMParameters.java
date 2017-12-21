package hmmmatching.impl;

public class HMMParameters {
  
  /**
   * Seuil de sélection des candidats par buffer
   */
  public final double selection;
  /**
   * Longueur minimale tolérée pour un stroke (en nombre de tronçons le constituant)
   */
  public final int stroke_length;
  /**
   * Est-ce qu'on découpe les réseaux les uns avec les autres pour essayer d'homogénéiser
   * leur niveau de détail ? Déconseillé
   */
  public final boolean resampling;
  
  /**
   * Est-ce qu'on veut utiliser le processus d'optimlisation sous contraintes pour filtrer les résultats 
   * de l'appariement par HMM ? Conseillé ...
   */
  public boolean lpsolving;

  
  public HMMParameters(double selection, int stroke_length, boolean resampling,
      boolean lpsolving) {
    super();
    this.selection = selection;
    this.stroke_length = stroke_length;
    this.resampling = resampling;
    this.lpsolving = lpsolving;
  }
  
  

}
