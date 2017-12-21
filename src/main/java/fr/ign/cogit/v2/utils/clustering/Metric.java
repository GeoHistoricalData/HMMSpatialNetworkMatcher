package fr.ign.cogit.v2.utils.clustering;


/**
* <p>Description : Cette interface defini une metrique.</p>
* <p>Package necessaires : Jama.</p>
* <p>Dernieres modifications :<br>
* 7 Mars 2009 => Ajout d'une methode avec des int.<br>
* 9 Janvier 2009, 1.1 => renommee (traduite) et ajout des methodes permettant le calcul de distance entre vecteurs, points et coordonnees.<br>
* 21 Octobre 2007 => Creation.</p>
* <p>Copyright : Copyright (c) 2007.</p>
* <p>Laboratoire : LSIS.</p>
* <p>Equipe : Image et Modele, I&M (ex LXAO).</p>
* 
* @author Guillaume THIBAULT
* @version 1.1
*/

public interface Metric {

/** Calcule la distance entre les deux vecteurs passes en argument.
* @param firstVector first input vector
* @param secondVector second input vector
* @return La distance.*/
public double Distance(double[] firstVector, double[] secondVector) ;

}