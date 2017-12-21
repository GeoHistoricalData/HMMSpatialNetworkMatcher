package fr.ign.cogit.v2.indicators.dynamics.eventsfinder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DeleteMEd {
  
//taille de la liste à construire
static int size = 4; 

//table des elements utilisés 
static boolean[] elements = new boolean[size];

//liste construite par la recursion  
static int[] liste = new int[size];

static List<int[]> result = new ArrayList<int[]>();

//construction recursive des listes possibles
public static void permut(int rank) {
   if (rank>=size) {
       // la liste est construite -> FIN 
     System.out.println(Arrays.toString(liste));
       result.add(Arrays.copyOf(liste, size));
       return;
   }

   // parcours les elements
   for(int i=0;i<size;i++) {
       // deja utilisé -> suivant
       if (elements[i]) continue;
       // sinon on choisi cet element
       elements[i]=true;
       // on l'ajoute a la liste
       liste[rank]=i;
       // on construit le reste de la liste par recursion
       permut(rank+1);
       // on libere cet element
       elements[i]=false;
   }

}

public static void main(String[] args) {
   permut(0);
//   System.out.println(result);
//   for(int[] t : result){
//     System.out.println(t[0]+
//         " "+t[1]+ " "+ t[2]);
//   }
}
}
