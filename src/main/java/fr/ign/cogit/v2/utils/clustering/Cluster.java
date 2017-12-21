package fr.ign.cogit.v2.utils.clustering;



/**
* <p>Description : Classe qui represente un Cluster pour les Kmeans.</p>
* <p>Packages necessaires : mathematiques.</p>
* <p>Copyright : Copyright (c) 2007.</p>
* <p>Laboratoire : LSIS.</p>
* <p>Equipe : Image et Modele, I&M (ex LXAO).</p>
* <p>Dernieres modifications :<br>
* 17 Avril 2008 => Correction d'un bug dans le constructeur : "if ( Dimension < 1 )" et ajout JavaDoc.<br>
* 24 Octobre 2007 => Ajout JavaDoc et DistanceInterClasse pour mesurer l'erreur de clustering.<br>
* 15 Octobre 2007 => Creation.</p>
* 
* @author Guillaume THIBAULT
* @version 1.0
* @see KMeans
*/

public class Cluster
{

/** Numero du cluster.*/
private int Numero = 0 ;
/** Dimension du cluster = dimension du barycentre.*/
private int Dimension = 0 ;
/** Nombre d'individus (d'intances) appartenant au cluster.*/
private int nbIndividus = 0 ;
/** Le barycentre du cluster.*/
private PointND Barycentre = null ;
/** Erreur d'inertie entre les membres du cluster.*/
private double InertieIntraClasse = -1.0 ;





/** Un constructeur qui initialise le Cluster.
* @param Numero Numero du Cluster.
* @param Dimension La dimension du Cluster, donc la dimension de son barycentre.*/
public Cluster(int Numero, int Dimension)
       {
       if ( Dimension < 1 ) throw new Error("Dimension incorrecte : " + Dimension + " (attendu > 0).") ;

       this.Numero = Numero ;
       this.Dimension = Dimension ;
       Barycentre = new PointND(Dimension) ;
       }







/* ------------------------------------------------------------ Les getters & setters ------------------------------------------------------------ */
/**
* @return Le barycentre.*/
public PointND getBarycentre()
       {
       return Barycentre ;
       }

/** Affecte le barycentre.
* @param barycentre Le barycentre a affecter.*/
public void setBarycentre(PointND barycentre)
       {
       Barycentre.set(barycentre.get()) ;
       }

/** Affecte le barycentre de ce Cluster.
* @param barycentre Le tableau contenant les coordonnees du barycentre.*/
public void setBarycentre(double[] barycentre)
       {
       Barycentre.set(barycentre) ;
       }

/** La dimension d'un Cluster est la dimension des individus que le compose. Si les points sont dans R^15 => Dimension = 15
* @return Le dimension du Cluster.*/
public int getDimension()
       {
       return Dimension ;
       }

/** L'inertie dans ce Cluster => Somme{Distance(Xi,B)^2}. On peut egalement l'appeler Erreur Inter Classe.
* @return L'erreur de distance dans le Cluster.*/
public double getInertieIntraClasse()
       {
       return InertieIntraClasse ;
       }

/** Affecte l'inertie intra-classe.
* @param InertieIntraClasse La nouvelle erreur a affecter.*/
public void setInertieIntraClasse(double InertieIntraClasse)
       {
       this.InertieIntraClasse = InertieIntraClasse ;
       }

/**
* @return Le numero du Cluster.*/
public int getNumero()
       {
       return Numero ;
       }

/** 
* @return Le nombre d'individu qui sont dans ce Cluster.*/
public int getNbIndividus()
       {
       return nbIndividus ;
       }

/** Affecte le nombre d'individu a ce Cluster. Aucune verification n'est effectue sur la valeur affectee.
* @param nbIndividus Le nombre a affecter.*/
public void setNbIndividus(int nbIndividus)
       {
       this.nbIndividus = nbIndividus ;
       }

/** Incremente le nombre d'individu appartenant au Cluster. Aucune verification n'est effectuee sur la valeur d'increment.
* @param Increment De combien doit on incrementer le nombre d'individu.*/
public void IncrementerNbIndividus(int Increment)
       {
       nbIndividus += Increment ;
       }

/** Decremente le nombre d'individu appartenant au Cluster. Aucune verification n'est effectuee sur la valeur d'increment.
* @param Increment De combien doit on decrementer le nombre d'individu.*/
public void DecrementerNbIndividus(int Increment)
       {
       nbIndividus -= Increment ;
       }
}