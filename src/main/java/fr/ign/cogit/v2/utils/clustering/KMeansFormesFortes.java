package fr.ign.cogit.v2.utils.clustering;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.feature.DefaultFeature;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPosition;
import fr.ign.cogit.geoxygene.spatial.geomprim.GM_Point;




/**
 * <p>Description : Cette classe permet de calculer plusieurs KMeans afin de calculer les formes fortes. Les barycentres des formes fortes serviront
 *  a initialiser un dernier KMeans qui sera le resultat que l'on souhaite.</p>
 * <p>Packages necessaires : fichiersdossiers, mathematiques, utils.</p>
 * <p>Copyright : Copyright (c) 2007.</p>
 * <p>Laboratoire : LSIS.</p>
 * <p>Equipe : Image et Modele, I&M (ex LXAO).</p>
 * <p>Dernieres modifications :<br>
 * 30 Avril 2008 => Creation.</p>
 * 
 * @author Guillaume THIBAULT
 * @version 1.0
 */

public class KMeansFormesFortes
{

    /** Le KMeans qui servira aux calculs.*/
    private KMeans kmeans = null ;

    /** Tableau contenant les clusters resultat de chaque individu.*/
    private int[][] ClusterResultat = null ;

    /** Tableau qui contiendra les resultats des composantes fortes.*/
    private int[] FormesFortes = null ;

    /** Tableau contenant les barycentres des formes fortes réduites au nombre de cluster
     * (on prend les nbClusters plus grandes formes fortes).*/
    private double[][] Barycentres = null ;

    /** Tableau contenant les barycentres de l'ensemble des formes fortes et formes faibles (partitions de taille 1)
     */
    private double[][] AllBarycentres = null ;

    /** Tableau qui contiendra tous les parametres.*/
    private double[][] Tableau = null ;




    /** Un simple constructeur qui va instancier le Kmeans qui sera utilise, a l'aide de la metrique passee en argument.
     * @param metrique La metrique a utiliser pendant les calculs.*/
    public KMeansFormesFortes(Metric metrique)
    {
        if ( metrique == null ) throw new Error("Métrique = null") ;
        kmeans = new KMeans(metrique) ;
    }


    /** Un simple constructeur auquel on passe directement le tableau de double.
     * @param Tableau Le tableau sur lequel on va travailler, contient les donnees.
     * @param nbKMeans Le nombre de KMeans que l'on doit calculer pour trouver les formes fortes.
     * @param nbClusters Le nombre de classes (clusters) du KMeans.
     * @param Epsilon La valeur de convergence du KMeans.
     * @param UnIndividuParClusterMinimum Est ce que l'on doit utiliser l'heuristique afin de n'avoir aucun cluster vide.
     * @param CentrerReduire Est ce que l'on doit centrer/reduire les donnees ?
     * @param Chrono Le chronometre pour mesurer le temps d'execution.*/
    public void Calculer(double[][] Tableau, int nbKMeans, int nbClusters, double Epsilon,
            boolean UnIndividuParClusterMinimum, boolean CentrerReduire)
    {
        if ( nbKMeans <= 1 ) throw new Error("Nombre de KMeans incorrect : " + nbKMeans + ", attendu [2..N].") ;

        if ( Tableau == null ) throw new Error("Tabeau = null") ;
        this.Tableau = Tableau ;

        CalculerFormesFortes(nbKMeans, nbClusters, Epsilon, UnIndividuParClusterMinimum, CentrerReduire) ;
    }


    /**
     * Méthode utilise seulement si on veut récupérer les barycentres de l'ensemble des formes
     * fortes (et formes faibles). Si on veut juste lancer le kmean surt les barcycentre des nbCluster
     * plus grandes formes fortes, utiliser directement la méthode CalculerFormesFortes.
     * @author bcostes
     */
    public void calculateAllStablePartition(double[][] Tableau,   int nbKMeans, int nbClusters, double Epsilon, boolean UnIndividuParClusterMinimum, boolean CentrerReduire){
        int compteur, nb, nb2, Taille, i, j, k ;
        int nbDimensions = Tableau[0].length ;
        int nbIndividus = Tableau.length ;
        double taille = Math.pow(nbClusters, nbKMeans) ;
        int[] clusters = null ;
        boolean ok ;
        QuickSort qs = new QuickSort() ;

        if ( Double.isInfinite(taille) ) throw new Error("Nombre de composantes fortes trop grand : Math.pow(nbClusters, nbKMeans).") ;
        if ( taille > Integer.MAX_VALUE -1 ) throw new Error("Taille supérieure au MAX_INT.") ;


        ClusterResultat = null ;
        ClusterResultat = new int[nbKMeans][] ;

        for (i=0 ; i < nbKMeans ; i++)
        {
            kmeans.Calculer(Tableau, nbClusters, Epsilon, UnIndividuParClusterMinimum, CentrerReduire, null) ;
            ClusterResultat[i] = kmeans.getClusterResultat() ;
        }

        Taille = (int)taille ;
        FormesFortes = null ;
        FormesFortes = new int[Taille] ; // On alloue le tableau des formes fortes, code dans la base nbClusters.
        for (i=0 ; i < Taille ; i++) FormesFortes[i] = 0 ;

        for (i=0 ; i < nbIndividus ; i++) // On remplit le tableau des composantes fortes.
        {
            nb = 0 ;
            for (k=0 ; k <  nbKMeans ; k++)
                nb = nb * nbClusters + ClusterResultat[k][i] ;
            FormesFortes[nb]++ ;
        }

        nb = 0 ;
        for (i=0 ; i < Taille ; i++) // On compte le nombre de cases non vides.
            if ( FormesFortes[i] != 0 ) nb++ ;

        if ( nb < nbClusters ) throw new Error("Nombre de formes fortes inférieur au nombre de clusters.") ;

        int[] FF = new int[nb] ; // Tableau contenant les nombre d'individus dans les formes fortes.
        int[] Indices = new int[nb] ; // Tableau contenant les indices des formes fortes qui ne sont pas vides.



        nb = 0 ;
        for (i=0 ; i < Taille ; i++) // On sauvegarde les cases et indices du tableau des composantes fortes qui ne sont pas nul.
            if ( FormesFortes[i] != 0 )
            {
                FF[nb] = FormesFortes[i] ;
                Indices[nb++] = i ;
            }

        qs.Sort(FF, Indices, 0, nb-1) ; // On tri afin de connaître les plus grand.




        boolean[] trouve = new boolean[nbIndividus] ; // Tableau permettant de savoir si cet individu a deja servie dans le calcul d'un barycentre.
        for (i=0 ; i < nbIndividus ; i++) trouve[i] = false ;
        AllBarycentres = null ;
        AllBarycentres = new double[FF.length][nbDimensions] ; // Le tableau qui contiendra les barycentres résultats.

        compteur = 0 ; // On compte les barycentres calculés.
        for (k=nb-1 ; k >= 0 ; k--) // On travaille sur les plus grand pour trouver les barycentres. 
        {
            clusters = Decomposer(Indices[k], nbKMeans, nbClusters) ; // On décompose les nombre pour savoir quels sont les clusters qui les ont généré.

            nb2 = 0 ;
            for (i=0 ; nb2 < FF[k] && i < nbIndividus ; i++) // On parcours tous les individus pour trouver ceux qui sont dans cette forme forte.
                if ( !trouve[i] ) // S'il n'a pas déjà été utilisé.
                {
                    ok = true ;
                    for (j=0 ; ok && j < nbKMeans ; j++) // On regarde s'il correspond.
                        if ( ClusterResultat[j][i] != clusters[j] ) ok = false ;
                    if ( ok ) // Il correspond...
                    {
                        for (j=0 ; j < nbDimensions ; j++) // On ajoute ses coordonnées au barycentre de la forme forte.
                            AllBarycentres[compteur][j] += Tableau[i][j] ;
                        trouve[i] = true ; // On ne travaillera plus sur lui...
                        nb2++ ; // Un individu de moins à trouvé.
                    }
                }

            if ( nb2 != FF[k] ) throw new Error("nb2 != CF[k]") ; // On a pas trouvé tous les individu => bug.

            for (j=0 ; j < nbDimensions ; j++) // On calcule le barycentre.
                AllBarycentres[compteur][j] /= (double)nb2 ;
            compteur++ ; // Une forme forte de traitée, à la suivante...
        }
    }

    /**
     * @author bcostes
     * @return
     */
    public double[][] getAllBarycentres() {
        return AllBarycentres;
    }

    /**
     * @author bcostes
     * @param allBarycentres
     */
    public void setAllBarycentres(double[][] allBarycentres) {
        AllBarycentres = allBarycentres;
    }


    /** Methode qui va effectuer le calcul des formes fortes.
     * @param nbKMeans Le nombre de KMeans que l'on doit calculer pour trouver les formes fortes.
     * @param nbClusters Le nombre de classes (clusters) du KMeans.
     * @param Epsilon La valeur de convergence du KMeans.
     * @param UnIndividuParClusterMinimum Est ce que l'on doit utiliser l'heuristique afin de n'avoir aucun cluster vide.
     * @param CentrerReduire Est ce que l'on doit centrer/reduire les donnees ?
     * @param Chrono Le chronometre pour mesurer le temps d'execution.*/
    private void CalculerFormesFortes(int nbKMeans, int nbClusters, double Epsilon, boolean UnIndividuParClusterMinimum, boolean CentrerReduire)
    {
        int compteur, nb, nb2, Taille, i, j, k ;
        int nbDimensions = Tableau[0].length ;
        int nbIndividus = Tableau.length ;
        double taille = Math.pow(nbClusters, nbKMeans) ;
        int[] clusters = null ;
        boolean ok ;
        QuickSort qs = new QuickSort() ;

        if ( Double.isInfinite(taille) ) throw new Error("Nombre de composantes fortes trop grand : Math.pow(nbClusters, nbKMeans).") ;
        if ( taille > Integer.MAX_VALUE -1 ) throw new Error("Taille supérieure au MAX_INT.") ;


        ClusterResultat = null ;
        ClusterResultat = new int[nbKMeans][] ;

        for (i=0 ; i < nbKMeans ; i++)
        {
            kmeans.Calculer(Tableau, nbClusters, Epsilon, UnIndividuParClusterMinimum, CentrerReduire, null) ;
            ClusterResultat[i] = kmeans.getClusterResultat() ;
        }

        Taille = (int)taille ;
        FormesFortes = null ;
        FormesFortes = new int[Taille] ; // On alloue le tableau des formes fortes, code dans la base nbClusters.
        for (i=0 ; i < Taille ; i++) FormesFortes[i] = 0 ;

        for (i=0 ; i < nbIndividus ; i++) // On remplit le tableau des composantes fortes.
        {
            nb = 0 ;
            for (k=0 ; k <  nbKMeans ; k++)
                nb = nb * nbClusters + ClusterResultat[k][i] ;
            FormesFortes[nb]++ ;
        }

        nb = 0 ;
        for (i=0 ; i < Taille ; i++) // On compte le nombre de cases non vides.
            if ( FormesFortes[i] != 0 ) nb++ ;

        if ( nb < nbClusters ) throw new Error("Nombre de formes fortes inférieur au nombre de clusters.") ;

        int[] FF = new int[nb] ; // Tableau contenant les nombre d'individus dans les formes fortes.
        int[] Indices = new int[nb] ; // Tableau contenant les indices des formes fortes qui ne sont pas vides.

        nb = 0 ;
        for (i=0 ; i < Taille ; i++) // On sauvegarde les cases et indices du tableau des composantes fortes qui ne sont pas nul.
            if ( FormesFortes[i] != 0 )
            {
                FF[nb] = FormesFortes[i] ;
                Indices[nb++] = i ;
            }

        qs.Sort(FF, Indices, 0, nb-1) ; // On tri afin de connaître les plus grand.




        boolean[] trouve = new boolean[nbIndividus] ; // Tableau permettant de savoir si cet individu a deja servie dans le calcul d'un barycentre.
        for (i=0 ; i < nbIndividus ; i++) trouve[i] = false ;
        Barycentres = null ;
        Barycentres = new double[nbClusters][nbDimensions] ; // Le tableau qui contiendra les barycentres résultats.

        compteur = 0 ; // On compte les barycentres calculés.
        for (k=nb-1 ; k >= nb-nbClusters ; k--) // On travaille sur les plus grand pour trouver les barycentres. 
        {
            clusters = Decomposer(Indices[k], nbKMeans, nbClusters) ; // On décompose les nombre pour savoir quels sont les clusters qui les ont généré.

            nb2 = 0 ;
            for (i=0 ; nb2 < FF[k] && i < nbIndividus ; i++) // On parcours tous les individus pour trouver ceux qui sont dans cette forme forte.
                if ( !trouve[i] ) // S'il n'a pas déjà été utilisé.
                {
                    ok = true ;
                    for (j=0 ; ok && j < nbKMeans ; j++) // On regarde s'il correspond.
                        if ( ClusterResultat[j][i] != clusters[j] ) ok = false ;
                    if ( ok ) // Il correspond...
                    {
                        for (j=0 ; j < nbDimensions ; j++) // On ajoute ses coordonnées au barycentre de la forme forte.
                            Barycentres[compteur][j] += Tableau[i][j] ;
                        trouve[i] = true ; // On ne travaillera plus sur lui...
                        nb2++ ; // Un individu de moins à trouvé.
                    }
                }

            if ( nb2 != FF[k] ) throw new Error("nb2 != CF[k]") ; // On a pas trouvé tous les individu => bug.

            for (j=0 ; j < nbDimensions ; j++) // On calcule le barycentre.
                Barycentres[compteur][j] /= (double)nb2 ;

            compteur++ ; // Une forme forte de traitée, à la suivante...
        }

        // On calcule le kmeans avec les barycentres des formes fortes.
        kmeans.Calculer(Tableau, nbClusters, Epsilon, UnIndividuParClusterMinimum, CentrerReduire, Barycentres) ;
    }




    /** Methode qui decompose un nombre afin de retrouver les numeros de clusters qui l'on produit.
     * @param num Le numero a decomposer.
     * @param nbKMeans Le nombre de KMeans qui a ete effectue, c'est aussi le nombre de "nombre" a trouver, donc la taille du tableau resultat.
     * @param nbClusters Le nombre de clusters, utile pour connaitre la base qui a servi a generer le nombre.
     * @return Un tableau contenant les numeros de clusters qui ont genere le nombre.*/
    private int[] Decomposer(int num, int nbKMeans, int nbClusters)
    {
        int[] res = new int[nbKMeans] ;

        for (int nb=nbKMeans-1 ; nb >= 0 ; nb--)
        {
            res[nb] = num % nbClusters ;
            num = (num - res[nb]) / nbClusters ;
        }

        return res ;
    }



    /** Methode qui retourne le kmeans resultat, celui qui a ete calcule avec les donnees, mais initialise avec les barycentres des formes fortes.
     * @return Le kmeans resultat.*/
    public KMeans getKMeansResultat()
    {
        return kmeans ;
    }

    /**
     * Exporte les barycentre de l'ensemble des formes fortes dans un fichier texte
     * @author bcostes
     * @param file
     */
    public void exportStablePartitions(String file){
        if(this.AllBarycentres != null && this.AllBarycentres.length !=0){
            String s ="x;y\n";
            for(int i=0; i< this.getAllBarycentres().length; i++){
                s += this.getAllBarycentres()[i][0]+ ";"+ this.getAllBarycentres()[i][1]+"\n";
            }
            FileWriter fr;
            try {
                fr = new FileWriter(file);
                BufferedWriter br = new BufferedWriter(fr);
                br.write(s);
                br.flush();
                br.close();
                fr.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
