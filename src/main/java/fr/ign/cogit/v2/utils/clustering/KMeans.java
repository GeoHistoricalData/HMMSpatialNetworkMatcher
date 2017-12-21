package fr.ign.cogit.v2.utils.clustering;


import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.feature.DefaultFeature;
import fr.ign.cogit.geoxygene.feature.Population;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPosition;
import fr.ign.cogit.geoxygene.spatial.geomprim.GM_Point;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileWriter;

/**
 * <p>Description : Cette classe implemente l'algorithme des K-moyennes (K-Means). Une heuristique est disponible afin que si on le souhaite, aucun cluster
 *  (classe) ne soit vide (sans aucun individu).<br>
 *  Lire "Dat mining et statistiques decisionnelle" par Stephane Tuffery, chez les editions TECHNIP.</p>
 * <p>Packages necessaires : affichages, mathematiques.</p>
 * <p>Copyright : Copyright (c) 2007.</p>
 * <p>Laboratoire : LSIS.</p>
 * <p>Equipe : Image et Modele, I&M (ex LXAO).</p>
 * <p>Dernieres modifications :<br>
 * 20 Juillet 2008, 2.0 => Separation entre "Centres mobiles" et "K-means".<br>
 * 30 Avril 2008 => Correction de bugs lies a la standardisation des donnees & Ajout des methodes "ComputeAndReturnBestRepresentant" et du chrono.<br>
 * 29 Avril 2008, 1.2 => Ajout de la possibilite de d'initialiser les barycentre avant calculs.<br>
 * 21 Avril 2008 => Correction de bug majeur pour le cas "UnIndividuParClusterMinimum", ajout d'une securite pour un cas non gere.<br>
 *               => Ajout d'une securite pour le cas des nombre "NaN".<br>
 * 18 Avril 2008, 1.1 => Ajout de la possibilite de passer un fichier tabule dans un constructeur.<br>
 * 24 Octobre 2007 => Ajout d'une metrique en parametre. Elle sera utilisee pour tout ce qui est calcul de distances.<br>
 *                 => Ajout du calcul des erreurs de clustering : Inertie inter classe et intra classe.<br>
 * 15 Octobre 2007 => Creation.</p>
 * 
 * @author Guillaume THIBAULT
 * @version 2.0
 */

public class KMeans
{

    /** Nombre d'iterations qui furent necessaires pour converger lors du dernier calcul.*/
    protected int nbIterations = 0 ;
    /** L'erreur a minimiser pour pouvoir finir.*/
    protected double Epsilon = 1.0 ;
    /** Le tableau contenant les donnees sur lesquelles ont va travailler.*/
    protected double[][] Tableau = null ;
    /** Tableau contenant les barycentres des clusters. Il est utile pour calculer le deplacement entre deux iteraitons.*/
    protected double[][] Barycentres = null ;
    /** Nombre de cluster (de classes).*/
    protected int nbClusters = 0 ;
    /** Nombre d'individus (d'instances) sur lesquels on va travailler <=> Nombre de lignes du tableau.*/
    protected int nbIndividus = 0 ;
    /** Dimensions du probleme <=> Nombre de colonnes <=> Taille du vecteur caracteristique.*/
    protected int nbDimensions = 0 ;
    /** Tableau qui contiendra pour chaque individu a quel cluster (classe) il appartient.*/
    protected int[] ClusterResultat = null ;
    /** Tableau contenant la distance entre un individu et le barycentre de sa classe.*/
    protected double[] DistanceResultat = null ;
    protected double InertieIntraClasse = -1.0 ;
    protected double InertieInterClasse = -1.0 ;
    /**Tableau contenant tous les clusters (classes).*/
    protected Cluster[] Clusters = null ;
    /** Metrique a utiser pour le calcul.*/
    protected Metric metrique = null ;
    /** Tableau contenant l'indice du meilleur representant pour chaque classe.*/
    protected int[] BestRepresentant = null ;
    /** Classe permettant de standardiser les donnees.*/
    protected StandardizeData sd = null ;



    /** Un constructeur (le seul) auquel il faut passer imperativement une metrique qui sera celle utilise par cette instanciation.
     * @param metrique La metrique a utiliser pour calculer les distances entre barycentres.*/
    public KMeans(Metric metrique)
    {
        if ( metrique == null ) throw new Error("Métrique = null") ;
        this.metrique = metrique ;
        sd = new StandardizeData() ;
    }


    /** Lance le calcul de kmean à partir d'un tableau de données.
     *  En entrée, un fichier texte de la classification CAH obtenue sur les formes fortes.
     *  chaque ligne : une forme forte => x, y, z, ...., classe
     *  Le nombre de classe est donc donné par ce fichier. On va calculer les barycentre de chaque classe avant
     *  de lancer la classification.
     *  Délimiteur : ";"
     * @author bcostes
     * @param Tableau
     * @param Epsilon
     * @param UnIndividuParClusterMinimum
     * @param CentrerReduire
     * @param file
     */
    public void Calculer(double[][] Tableau, double Epsilon, boolean UnIndividuParClusterMinimum, boolean CentrerReduire,
            String file)
    {
        if ( Tableau == null ) throw new Error("Tableau = null.") ;

        this.nbIndividus = Tableau.length ;
        this.nbDimensions = Tableau[0].length ;
        this.Epsilon = Epsilon ;

        this.Tableau = null ;
        this.Tableau = new double[Tableau.length][Tableau[0].length] ;
        for (int i=0 ; i < Tableau.length ; i++)
            for (int d=0 ; d < Tableau[0].length ; d++)
                this.Tableau[i][d] = Tableau[i][d] ;


        FileReader fr;
        try {
            fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            String line ="";
            Map<Integer, List<double[]>> data = new HashMap<Integer, List<double[]>>();
            while((line = br.readLine()) !=null){
                double[] coord = new double[this.nbDimensions ];
                StringTokenizer tokenizer = new StringTokenizer(line, ";");
                int cpt=0;
                while(tokenizer.hasMoreTokens()){
                    if(cpt >=this.nbDimensions ){
                        int classe =  Integer.parseInt(tokenizer.nextToken());
                                               
                        if(data.containsKey(classe)){
                            data.get(classe).add(coord);
                        }
                        else{
                            List<double[]> obs = new ArrayList<double[]>();
                            obs.add(coord);
                            data.put(classe, obs);
                        }
                        break;
                    }
                    else{
                        coord[cpt] = Double.parseDouble(tokenizer.nextToken());
                        cpt++;
                    }
                }
            }
            //nombre de classes
            List<Integer> l = new ArrayList<Integer>(data.keySet());
            Collections.sort(l);
            this.nbClusters = l.get(l.size()-1); // -
            
            
            double[][] Barycentres = new double[this.nbClusters][this.nbDimensions ];
            for(int i=0; i< this.nbClusters; i++){
                for(int j=0; j< this.nbDimensions; j++){
                    Barycentres[i][j] = 0.;
                }
            }
            int cpt=0;
            for(Integer numClasse : data.keySet()){
                for(int j=0; j< this.nbDimensions; j++){
                    for(int k=0; k< data.get(numClasse).size(); k++){
                        Barycentres[cpt][j] += data.get(numClasse).get(k)[j];
                    }
                    Barycentres[cpt][j] /= ((double)data.get(numClasse).size());
                }
                cpt++;
            }
            
            
            IPopulation<IFeature> bar = new Population<IFeature>();
            for(int i=0; i< Barycentres.length; i++){
                IDirectPosition p = new DirectPosition(Barycentres[i][0], Barycentres[i][1]);
                bar.add(new DefaultFeature(new GM_Point(p)));
            }
            ShapefileWriter.write(bar, "/home/bcostes/Bureau/barycentres.shp");
            
            
            Calculer(nbClusters, Epsilon, UnIndividuParClusterMinimum, CentrerReduire, Barycentres) ;

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NumberFormatException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }





/** Une methode qui va faire les verifications necessaires puis lancer les calculs.
 * @param Tableau Le tableau contenant toutes les valeurs des individus : les individus sont sur les lignes et leurs caracteristiques en colonnes.
 * @param nbClusters Nombre de clusters que l'on souhaite.
 * @param Epsilon Seuil d'arret des iterations.
 * @param UnIndividuParClusterMinimum Heuristique pour forcer qu'il y ait au moins un individu par cluster.
 * @param CentrerReduire Doit on centrer/reduire les donnees pour effectuer le calcul ?.
 * @param Barycentres Liste des barycentres, au cas on l'utilisateur veut preciser une initialisation particuliere. Si null, les barycentres sont places
 *  aleatoirement.
 * @param Chrono Le chronometre pour mesurer le temps d'execution.*/
public void Calculer(double[][] Tableau, int nbClusters, double Epsilon, boolean UnIndividuParClusterMinimum, boolean CentrerReduire,
        double[][] Barycentres)
{
    if ( Tableau == null ) throw new Error("Tableau = null.") ;

    this.nbIndividus = Tableau.length ;
    this.nbDimensions = Tableau[0].length ;
    this.nbClusters = nbClusters ;
    this.Epsilon = Epsilon ;

    this.Tableau = null ;
    this.Tableau = new double[Tableau.length][Tableau[0].length] ;
    for (int i=0 ; i < Tableau.length ; i++)
        for (int d=0 ; d < Tableau[0].length ; d++)
            this.Tableau[i][d] = Tableau[i][d] ;

    Calculer(nbClusters, Epsilon, UnIndividuParClusterMinimum, CentrerReduire, Barycentres) ;
}



/** Une methode qui prend en parametre une liste de points, puis la transforme en tableau afin d'effectuer les calculs.
 * Elle va faire les verifications necessaires puis lancer le calcul.
 * @param Liste Une liste de points 2D que l'on va convertir en tableau representant les individus et leurs descripteurs.
 * @param nbClusters Nombre de clusters que l'on souhaite.
 * @param Epsilon Seuil d'arret des iterations.
 * @param UnIndividuParClusterMinimum Heuristique pour forcer qu'il y ait au moins un individu par cluster.
 * @param CentrerReduire Doit on centrer/reduire les donnees pour effectuer le calcul ?.
 * @param Barycentres Liste des barycentres, au cas on l'utilisateur veut preciser une initialisation particuliere. Si null, les barycentres sont places
 *  aleatoirement.*/
public void Calculer(List<PointND> Liste, int nbClusters, double Epsilon, boolean UnIndividuParClusterMinimum, boolean CentrerReduire, double[][] Barycentres)
{
    if ( Liste == null ) throw new Error("Liste = null") ;

    this.nbIndividus = Liste.size() ;
    this.nbDimensions = Liste.get(0).Dimension() ;
    this.nbClusters = nbClusters ;
    this.Epsilon = Epsilon ;

    Tableau = null ;
    Tableau = new double[nbIndividus][nbDimensions] ;

    int nb = 0 ;
    PointND point = null ;
    Iterator<PointND> iter = Liste.iterator() ;
    while ( iter.hasNext() )
    {
        point = iter.next() ;
        for (int j=0 ; j < nbDimensions ; j++)
            Tableau[nb][j] = point.get(j) ;
        nb++ ;
    }

    Calculer(nbClusters, Epsilon, UnIndividuParClusterMinimum, CentrerReduire, Barycentres) ;
}





/** Methode qui gere la totalite des calculs.
 * @param nbClusters Le nombre de Clusters que l'on souhaite.
 * @param Epsilon Le seuil d'arret : si la somme des distances entre les anciens barycentres des clusters et les nouveaux est inferieure a Epsilon,
 *  on arrete.
 * @param UnIndividuParClusterMinimum Heuristique pour forcer qu'il y ait au moins un individu par cluster.
 * @param CentrerReduire Doit on centrer/reduire les donnees pour effectuer le calcul ?.
 * @param Barycenters Liste des barycentres, au cas on l'utilisateur veut preciser une initialisation particuliere. Si null, les barycentres sont places
 *  aleatoirement.
 * @param Chrono Le chronometre pour mesurer le temps d'execution.*/
private void Calculer(int nbClusters, double Epsilon, boolean UnIndividuParClusterMinimum, boolean CentrerReduire, double[][] Barycenters)
{
    Clusters = null ;
    ClusterResultat = null ;
    DistanceResultat = null ;

    if ( nbClusters < 2 ) throw new Error("Nombre de clusters incorrects : " + nbClusters + " (attendu > 1).") ;
    if ( nbIndividus < nbClusters ) throw new Error("Il y a plus de clusters (" + nbClusters + ") que d'individus (" + nbIndividus + ").") ;


    if ( CentrerReduire ) sd.Compute(Tableau) ;

    int i, j ;
    Clusters = new Cluster[nbClusters] ;
    for (i=0 ; i < nbClusters ; i++) Clusters[i] = new Cluster(i, nbDimensions) ;

    if ( Barycenters == null ) PlacerBarycentresAleatoirement() ;
    else
    {
        if ( Barycenters[0].length != nbDimensions ) throw new Error("Dimension des barycentres différente du nombre de dimensions (caractéristiques).") ;
        if ( Barycenters.length != nbClusters ) throw new Error("Nombre de barycentre différent du nombre de classes (individus).") ;
        if ( CentrerReduire )
        {
            double val ;
            double[] moyennes = sd.getMoyennes() ;
            double[] ecartstypes = sd.getEcartsTypes() ;
            for (i=0 ; i < nbClusters ; i++) // On affecte les barycentres en standardisant les valeurs.
                for (j=0 ; j < nbDimensions ; j++)
                {
                    val = Barycenters[i][j] ;
                    val = (val - moyennes[j]) / ecartstypes[j] ;
                    Clusters[i].getBarycentre().set(j, val) ;
                }
        }
        else
            for (i=0 ; i < nbClusters ; i++)
                Clusters[i].setBarycentre(Barycenters[i]) ;
    }

    ClusterResultat = new int[nbIndividus] ;
    DistanceResultat = new double[nbIndividus] ;

    Barycentres = new double[nbClusters][nbDimensions] ; // On crée le tableau contenant les barycentres et on copie (sauvegarde) les valeurs.
    double[] bary = null ;
    for (i=0 ; i < nbClusters ; i++)
    {
        bary = Clusters[i].getBarycentre().get() ;
        for (j=0 ; j < nbDimensions ; j++)
            Barycentres[i][j] = bary[j] ;
    }
    bary = null ;

    for (i=0 ; i < nbClusters ; i++) Clusters[i].setNbIndividus(0) ;

    nbIterations = 0 ;
    while ( Converger(UnIndividuParClusterMinimum) > Epsilon ) nbIterations++ ;

    CalculerInertieIntraClasse() ;
    CalculerErreurInterClasse() ;

}



/** Methode qui place aleatoirement le barycentre de chaque cluster pour l'initialisation.*/
private void PlacerBarycentresAleatoirement()
{
    int x, y ;
    double[] min = new double[nbDimensions] ;
    double[] max = new double[nbDimensions] ;
    double[] barycentre = new double[nbDimensions] ;

    for (x=0 ; x < nbDimensions ; x++) min[x] = max[x] = Tableau[0][x] ;

    for (x=0 ; x < nbDimensions ; x++)
        if ( Double.isNaN(min[x]) ) throw new Error("NaN, ligne 0, colonne " + x) ;

    for (y=1 ; y < nbIndividus ; y++)
        for (x=0 ; x < nbDimensions ; x++)
        {
            if ( Double.isNaN(Tableau[y][x]) ) throw new Error("NaN, ligne " + y + ", colonne " + x) ;
            if ( min[x] > Tableau[y][x] ) min[x] = Tableau[y][x] ;
            if ( max[x] < Tableau[y][x] ) max[x] = Tableau[y][x] ;
        }

    for (y=0 ; y < nbClusters ; y++)
    {
        for (x=0 ; x < nbDimensions ; x++)
            barycentre[x] = min[x] + Math.random()*(max[x]-min[x]) ;
        Clusters[y].setBarycentre(barycentre) ;
    }
}



/** Methode que l'on va iterer jusqu'a converger vers la solution (jusqu'a ce que l'on soit inferieur au critere d'arret).
 * @param UnIndividuParClusterMinimum Heuristique pour forcer qu'il y ait au moins un individu par cluster.
 * @return La distance totale de deplacement entre les anciens barycentres des clusters et les nouveaux.*/
private double Converger(boolean UnIndividuParClusterMinimum)
{
    if ( nbIterations == 0 ) PremiereIteration(UnIndividuParClusterMinimum) ;
    else TrouverClusterPlusProche(UnIndividuParClusterMinimum) ;
    return CalculerDeplacements() ;
}



/** Methode qui trouve le Cluster le plus proche de chaque individu.
 * @param UnIndividuParClusterMinimum Est ce que l'on applique l'heuristique ?*/
private void PremiereIteration(boolean UnIndividuParClusterMinimum)
{
    int i, c ;
    double Distance ;

    for (i=0 ; i < nbIndividus ; i++)
    {
        ClusterResultat[i] = 0 ;
        DistanceResultat[i] = metrique.Distance(Tableau[i], Clusters[0].getBarycentre().get()) ;
        if ( Double.isNaN(DistanceResultat[i]) ) throw new Error("NaN trouvé lors d'un calcul de distance => individu " + i + ", cluster 0.") ;
        for (c=1 ; c < nbClusters ; c++)
        {
            Distance = metrique.Distance(Tableau[i], Clusters[c].getBarycentre().get()) ;
            if ( Double.isNaN(Distance) ) throw new Error("NaN trouvé lors d'un calcul de distance => individu " + i + ", cluster " + c + ".") ;
            if ( Distance < DistanceResultat[i] )
            {
                DistanceResultat[i] = Distance ;
                ClusterResultat[i] = c ;
            }
        }
        AjouterIndividu(i, ClusterResultat[i]) ;
    }

    if ( UnIndividuParClusterMinimum )
    {
        int nummax ;
        boolean Erreur = true ;
        while ( Erreur )
        {
            Erreur = false ;
            for (c=0 ; c < nbClusters ; c++)
                if ( Clusters[c].getNbIndividus() == 0 ) // Si un cluster n'a pas d'individu
                {
                    nummax = 0 ;
                    for (i=1 ; i < nbIndividus ; i++) // On trouve l'individu qui est le plus loin (de son cluster).
                        if ( DistanceResultat[i] > DistanceResultat[nummax] ) nummax = i ;
                    Clusters[c].setBarycentre(Tableau[nummax]) ; // On lui ajoute celui est le plus loin de son cluster.
                    Clusters[c].setNbIndividus(1) ; // On met a un son nombre d'individu, car maintenant il en a un.
                    Clusters[ClusterResultat[nummax]].DecrementerNbIndividus(1) ; // On decremente le compteur du cluster auquel il appartenait.
                    if ( Clusters[ClusterResultat[nummax]].getNbIndividus() == 0 ) Erreur = true ;
                    ClusterResultat[nummax] = c ; // On modifie son Cluster d'appartenance.
                    DistanceResultat[nummax] = 0.0 ; // Il est SUR son nouveau Cluster, donc sa distance est nulle.
                }
        }
    }

}


/** Methode qui trouve le Cluster le plus proche de chaque individu.
 * @param UnIndividuParClusterMinimum Est ce que l'on applique l'heuristique ?*/
private void TrouverClusterPlusProche(boolean UnIndividuParClusterMinimum)
{
    int i, c, ClusterR ;
    double Distance, DistanceR ;

    for (i=0 ; i < nbIndividus ; i++)
    {
        ClusterR = 0 ;
        DistanceR = metrique.Distance(Tableau[i], Clusters[0].getBarycentre().get()) ;
        if ( Double.isNaN(DistanceResultat[i]) ) throw new Error("NaN trouvé lors d'un calcul de distance => individu " + i + ", cluster 0.") ;
        for (c=1 ; c < nbClusters ; c++)
        {
            Distance = metrique.Distance(Tableau[i], Clusters[c].getBarycentre().get()) ;
            if ( Double.isNaN(Distance) ) throw new Error("NaN trouvé lors d'un calcul de distance => individu " + i + ", cluster " + c + ".") ;
            if ( Distance < DistanceR )
            {
                DistanceR = Distance ;
                ClusterR = c ;
            }
        }
        AjouterIndividu(i, ClusterR) ;
        SupprimerIndividu(i, ClusterResultat[i]) ;
        ClusterResultat[i] = ClusterR ;
        DistanceResultat[i] = DistanceR ;
    }
}



/** Methode qui permet d'ajouter un individu a un cluster : incremente son compteur et met a jour son barycentre.
 * @param num Le numero de l'individu.
 * @param cluster Le numero du cluster auquel ajouter l'individu.*/ 
protected void AjouterIndividu(int num, int cluster)
{
    double nb = Clusters[cluster].getNbIndividus() ;
    double[] barycentre = Clusters[cluster].getBarycentre().get() ;
    for (int i=0 ; i < nbDimensions ; i++)
        barycentre[i] = (barycentre[i]*nb + Tableau[num][i]) / (nb + 1.0) ;
    Clusters[cluster].IncrementerNbIndividus(1) ;
}



/** Methode qui permet de supprimer un individu d'un cluster : decremente son compteur et met a jour son barycentre.
 * @param num Le numero de l'individu.
 * @param cluster Le numero du cluster auquel ajouter l'individu.*/ 
protected void SupprimerIndividu(int num, int cluster)
{
    double nb = Clusters[cluster].getNbIndividus() ;
    double[] barycentre = Clusters[cluster].getBarycentre().get() ;

    if ( Clusters[cluster].getNbIndividus() <= 0 ) throw new Error("Gros souci...") ; // ---------------------------------------------------------

    if ( Clusters[cluster].getNbIndividus() == 1 )
        for (int i=0 ; i < nbDimensions ; i++) barycentre[i] = 0.0 ;
    else
        for (int i=0 ; i < nbDimensions ; i++)
            barycentre[i] = (barycentre[i]*nb - Tableau[num][i]) / (nb - 1.0) ;
    Clusters[cluster].DecrementerNbIndividus(1) ;
}



/** Methode qui calcule la somme des deplacements des barycentres des clusters.
 * @return La somme des distances (deplacements) entre les anciens barycentres des clusters et les nouveaux.*/
private double CalculerDeplacements()
{
    int i, j ;
    double Distance = 0.0 ;
    double[] bary = null ;

    for (i=0 ; i < nbClusters ; i++) // On calcule la somme des déplacements.
        Distance += metrique.Distance(Clusters[i].getBarycentre().get(), Barycentres[i]) ;

    for (i=0 ; i < nbClusters ; i++) // On sauvegarde les nouveaux barycentres.
    {
        bary = Clusters[i].getBarycentre().get() ;
        for (j=0 ; j < nbDimensions ; j++)
            Barycentres[i][j] = bary[j] ;
    }
    bary = null ;

    return Distance ;
}






/** Cette methode calcule l'inertie intra-classe de chaque cluster : La somme du carre des distance entre chaque individu d'une classe et le barycentre 
 *  de cette classe.*/
private void CalculerInertieIntraClasse()
{
    int i ;
    double[] TabInertieIntraClasse = new double[nbClusters] ;

    for (i=0 ; i < nbClusters ; i++)
    {
        TabInertieIntraClasse[i] = 0.0 ;
        Clusters[i].setNbIndividus(0) ;
    }

    for (i=0 ; i < nbIndividus ; i++)
    {
        TabInertieIntraClasse[ClusterResultat[i]] += Math.pow(metrique.Distance(Clusters[ClusterResultat[i]].getBarycentre().get(), Tableau[i]), 2.0) ;
        Clusters[ClusterResultat[i]].IncrementerNbIndividus(1) ;
    }

    InertieIntraClasse = 0.0 ;
    for (i=0 ; i < nbClusters ; i++)
    {
        TabInertieIntraClasse[i] /= (double)Clusters[i].getNbIndividus() ;
        InertieIntraClasse += TabInertieIntraClasse[i] ;
        Clusters[i].setInertieIntraClasse(TabInertieIntraClasse[i]) ;
    }

    TabInertieIntraClasse = null ;
}



/** Methode qui calcule inertie inter-classe : la somme du carre des distance entre le barycentre de tous les individus et le barycentre de chaque Cluster.*/
private void CalculerErreurInterClasse()
{
    int i, j ;
    double[] Barycentre = new double[nbDimensions] ;

    for (i=0 ; i < nbIndividus ; i++)
        for (j=0 ; j < nbDimensions ; j++)
            Barycentre[j] += Tableau[i][j] ;

    InertieInterClasse = 0.0 ;
    for (i=0 ; i < nbClusters ; i++)
        InertieInterClasse += Clusters[i].getNbIndividus() * Math.pow(metrique.Distance(Barycentre, Clusters[i].getBarycentre().get()), 2.0) ;

    InertieInterClasse /= (double)nbIndividus ;
    Barycentre = null ;
}




/** Methode permettant de calculer le meilleur representant de chaque classe. Attention le calcul ne peut etre fait dans le cas d'un fichier tabule
 *  contenant des instances non valides.*/
public void ComputeBestRepresentant()
{
    int i ;
    BestRepresentant = new int[nbClusters] ;

    for (i=0 ; i < nbIndividus ; i++)
        BestRepresentant[ClusterResultat[i]] = i ;

    for (i=0 ; i < nbIndividus ; i++)
        if ( DistanceResultat[i] < DistanceResultat[BestRepresentant[ClusterResultat[i]]] )
            BestRepresentant[ClusterResultat[i]] = i ;
}





/** Methode qui permet de calculer puis d'extraire les meilleurs representants d'un ensemble d'instances.<br>
 * Attention : methode ne pouvant pas fonctionner si la source etait un fichier avec des instances exclues (invalides).
 * @return Un tableau de double contenant le meilleur representant de chaque classe.*/
public double[][] ComputeAndReturnBestRepresentants()
{
    int i, j, best ;
    double[][] Resultat = new double[nbClusters][nbDimensions] ;

    ComputeBestRepresentant() ; // On calcule les meilleurs representants.

    for (i=0 ; i < nbClusters ; i++)
    {
        best = BestRepresentant[i] ;
        for (j=0 ; j < Tableau[i].length ; j++)
            Resultat[i][j] = Tableau[best][j] ;
    }

    return Resultat ;
}






/* ------------------------------------------------------------ Les getters ------------------------------------------------------------ */

/** Methode qui renvoit le tableau contenant des individus etant les meilleurs representants de leurs classes (les plus proches).
 * @return Les numeros d'individus.*/
public int[] getBestRepresentant()
{
    return BestRepresentant ;
}

/** Methode qui renvoit le tableau contenant les numeros de clusters (classes) auquels appartiennent les instances.*/
public int[] getClusterResultat()
{
    return ClusterResultat ;
}

/** Methode qui renvoit le tableau contenant tous les clusters.
 * @return Les clusters.*/
public Cluster[] getClusters()
{
    return Clusters ;
}

/** Methode qui renvoit les distances entres les individus et le barycentre du cluster auquel ils appartiennent.
 * @return Les distances.*/
public double[] getDistanceResultat()
{
    return DistanceResultat ;
}

public double getEpsilon()
{
    return Epsilon ;
}

public void setEpsilon(double Epsilon)
{
    this.Epsilon = Epsilon ;
}

public int getNbClusters()
{
    return nbClusters ;
}

/** Nombre de dimensions (caracteristiques <=> nb colonnes) du probleme.
 * @return Le nombre de dimensions.*/
public int getNbDimensions()
{
    return nbDimensions ;
}

/** Nombre d'instances du tableau (de lignes).
 * @return Le nombre de lignes.*/
public int getNbIndividus()
{
    return nbIndividus ;
}

/** Methode qui retourne l'inertie inter classe de la population, peut egalement etre appele l'erreur inter classe.<br>
 * La somme du carre des distance ponderees entre le barycentre de tous les individus et le barycentre de chaque Cluster.
 * @return L'inertie inter classe.*/
public double getInertieInterClasse()
{
    return InertieInterClasse ;
}

/** Methode qui retourne l'inertie intra classe de la population : La somme du carre des distance entre chaque individu d'une classe et le barycentre 
 *  de cette classe.
 * @return L'inertie intra classe.*/
public double getInertieIntraClasse()
{
    return InertieIntraClasse ;
}

public int getNbIterations()
{
    return nbIterations ;
}

/** Le tableau sur lequel a ete effectue le clustering.
 * @return Le tableau de double.*/
public double[][] getTableau()
{
    return Tableau ;
}
}