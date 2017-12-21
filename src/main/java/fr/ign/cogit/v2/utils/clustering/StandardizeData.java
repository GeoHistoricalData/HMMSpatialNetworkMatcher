package fr.ign.cogit.v2.utils.clustering;




/**
* <p>Description : Cette classe permet de centrer et reduire des donnees.</p>
* <p>Packages necessaires : </p>
* <p>Copyright : Copyright (c) 2007.</p>
* <p>Laboratoire : LSIS.</p>
* <p>Equipe : Image et Modele, I&M (ex LXAO).</p>
* <p>Dernieres modifications :<br>
* 5 Mars 2009 => Correction bug, precisions javadoc.<br>
* 7 Aout 2008, V1.1 => Changement des noms des methodes, ajout des methodes pour les fichiers tabules.<br>
* 30 Avril 2008 => Changement de nom de la classe, de son type et ajout des getters.<br>
* 17 Avril 2008 => Creation : StandardizeData.</p>
* 
* @author Guillaume THIBAULT
* @version 1.1
*/

public class StandardizeData
{

/** Tableau qui contient les moyennes des colonnes.*/
private double[] Moyennes = null ;

/** Tableau qui contient les ecarts types des colonnes.*/
private double[] EcartsTypes = null ;



/** Un constructeur vide.*/
public StandardizeData()
       {

       }



/** Methode qui centre (moyenne nulle) et reduit (ecart type egal a 1) les donnees. Le calcul est le suivant : pour chaque colonne C,
*      elle calcule la moyenne "m" et l'ecart type "ec", puis sur chaque element E de la colonne C, calcule E = (E-m)/ec ;
* @param Data Le tableau contenant les donnees : les individus sont ranges en lignes.*/
public void Compute(double[][] Data)
       {
       int i, j ;
       int Largeur = Data[0].length ;
       int Hauteur = Data.length ;

       Moyennes = null ;
       EcartsTypes = null ;
       Moyennes = new double[Largeur] ;
       EcartsTypes = new double[Largeur] ;

       for (i=0 ; i < Largeur ; i++)
               {
               Moyennes[i] = EcartsTypes[i] = 0.0 ;
               for (j=0 ; j < Hauteur ; j++)
                       Moyennes[i] += Data[j][i] ;
               Moyennes[i] /= (double)Hauteur ;

               for (j=0 ; j < Hauteur ; j++)
                       EcartsTypes[i] += Math.pow(Data[j][i] - Moyennes[i], 2.0) ;
               EcartsTypes[i] = Math.sqrt(EcartsTypes[i]/(double)Hauteur) ;

               for (j=0 ; j < Hauteur ; j++)
                       Data[j][i] = (Data[j][i] - Moyennes[i]) / EcartsTypes[i] ;
               }
       }

/** Methode qui retourne le tableau contenant les moyennes calculees pour chaque colonne lors de l'utilisation de la methode Compute(double[][] Data).
* @return Le tableau des moyennes.*/
public double[] getMoyennes()
       {
       return Moyennes ;
       }

/** Methode qui retourne le tableau contenant les ecarts types calcules pour chaque colonne lors de l'utilisation de la methode Compute(double[][] Data).
* @return Le tableau des moyennes.*/
public double[] getEcartsTypes()
       {
       return EcartsTypes ;
       }
}