package fr.ign.cogit.v2.tag.enrichment.stgeocoding;

import geocodage.LexicalTools;

public class Soundex2 { 

  public static String soundex2( String sIn )
{
   // Si il n'y a pas de mot, on sort immédiatement
   if ( sIn.equals("")) return "    ";
   // On met tout en minuscule
   sIn = sIn.toUpperCase();
   // On supprime les accents
   sIn = LexicalTools.diacriticsSuppression(sIn);

   // On supprime tout ce qui n'est pas une lettre
   sIn = sIn.replaceAll( "[^A-Z]", "" );
   // Si la chaîne ne fait qu'un seul caractère, on sort avec.
   if ( sIn.length() ==1 ) {return (sIn + "   ");}
   // on remplace les consonnances primaires
   sIn = sIn.replaceAll( "GUI",  "KI");
   sIn = sIn.replaceAll( "GUE",  "CK");
   sIn = sIn.replaceAll( "GA",  "KA");
   sIn = sIn.replaceAll( "GO",  "KO");
   sIn = sIn.replaceAll( "GU",  "K");
   sIn = sIn.replaceAll( "CA",  "KA");
   sIn = sIn.replaceAll( "CO",  "KO");
   sIn = sIn.replaceAll( "CU",  "KU");
   sIn = sIn.replaceAll( "Q",  "K");
   sIn = sIn.replaceAll( "CC",  "K");
   sIn = sIn.replaceAll( "CK",  "K");

   // on remplace les voyelles sauf le Y et sauf la première par A
   sIn = sIn.replaceAll( "(?<!^)[EIOU]", "A" );
   // on remplace les préfixes puis on conserve la première lettre
   // et on fait les remplacements complémentaires
  
   sIn = sIn.replaceAll( "^KN",  "NN");
   sIn = sIn.replaceAll( "^(PH|PF)",  "FF");
   sIn = sIn.replaceAll( "^MAC",  "MCC");
   sIn = sIn.replaceAll( "^SCH",  "SSS");
   sIn = sIn.replaceAll( "^ASA",  "AZA");
   sIn = sIn.replaceAll( "(?<!^)KN",  "NN");
   sIn = sIn.replaceAll( "(?<!^)(PH|PF)",  "FF");
   sIn = sIn.replaceAll( "(?<!^)MAC",  "MCC");
   sIn = sIn.replaceAll( "(?<!^)SCH",  "SSS");
   sIn = sIn.replaceAll( "(?<!^)ASA",  "AZA");


   
   // suppression des H sauf CH ou SH
   sIn = sIn.replaceAll( "(?<![CS])H", "" );
   // suppression des Y sauf précédés d"un A
   sIn = sIn.replaceAll( "(?<!A)Y", "" );
   // on supprime les terminaisons A, T, D, S
   sIn = sIn.replaceAll( "[ATDS]$", "" );
   // suppression de tous les A sauf en tête
   sIn = sIn.replaceAll( "(?!^)A", "" );
   // on supprime les lettres répétitives
   sIn = sIn.replaceAll( "(.)\1", "$1" );
   // on ne retient que 4 caractères ou on complète avec des blancs
   return (sIn+"    ").substring( 0, 4);
}


  public static void main(String[] args) {
      String name = "wikipedia";
      String code = soundex2(name);
      System.out.println(code + ": " + name);
  }
}