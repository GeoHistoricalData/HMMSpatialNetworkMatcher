package geocodage;

/**
 * @author E. Bergé
 * @translated from PHP to Java by Christophe Schutz
 * 
 */

public class Phonetic {

  /**
   * Permet de générer le soundex d'un string
   * @param str le string à traiter
   * @return str le soundex du string
   */

  /*
   * Brève description de l'algorithme
   * 
   * On formate d'abord le mot. Remplacement des accents et caractères spéciaux,
   * puis passage en majuscules. Certaines occurences linguistiques sont
   * traitées, puis on supprime toutes les répétitions de lettres. Quelques cas
   * particuliers sont traités. Ce sont des mots très courts qui supportent mal
   * la simplification.
   * 
   * Viennent ensuite les pré-traitements. Ce sont principalement des
   * terminaisons muettes et des combinaisons complexes de voyelles. La grosse
   * partie de la conversion peut commencer. Simplification des sons YEU, K,
   * WEU, GUEU, SION, UN, AN, O ainsi que les muettes.
   * 
   * Une fois le gros de la troupe passé, on procède aux retouches (le détail
   * est commenté dans le source).
   * 
   * On termine avec les... ... terminaisons! Suppression des muettes,
   * simplification de sons.
   * 
   * Certains mots courts auront beaucoup souffert de la simplification. C'est
   * pourquoi en début et fin d'algorithme (avant les terminaisons), on aura
   * gardé une copie du code obtenu. On tente alors une "récupération" du code.
   * Si le code n'est pas trop court, on le renvoie. Si le code n'est composé
   * que de consonnes, on suppose alors que c'est un sigle, qu'on renvoie aussi.
   * 
   * 
   * Il faut bien comprendre que cet algorithme a été développé de façon
   * empirique, avec un corpus de test et une grosse part d'intuition, car il
   * est aussi prévu pour fonctionner avec des mots mal orthographiés et/ou
   * inconnus. D'où son comportement de conversion en phonétique. Et quoi qu'il
   * en soit, ce programme n'est qu'une petite partie d'un moteur de recherche.
   * Il y a beaucoup d'astuces à ajouter pour réaliser un moteur de recherche
   * performant. Bon courage!
   */

  public static String genererPhonetic(String str) {

    try {

      String[][] conversion = { { "é", "É" }, { "è", "È" }, { "ë", "Ë" },
          { "ê", "Ê" }, { "á", "Á" }, { "â", "Â" }, { "à", "À" }, { "Ä", "A" },
          { "Â", "A" }, { "å", "Å" }, { "ã", "Ã" }, { "æ", "Æ" }, { "ï", "Ï" },
          { "î", "Î" }, { "ì", "Ì" }, { "í", "Í" }, { "ô", "Ô" }, { "ö", "Ö" },
          { "ò", "Ò" }, { "ó", "Ó" }, { "õ", "Õ" }, { "ø", "Ø" }, { "œ", "Œ" },
          { "ú", "Ú" }, { "ù", "Ù" }, { "û", "Û" }, { "ü", "Ü" }, { "ç", "Ç" },
          { "ñ", "Ñ" }, { "ß", "S" } };
      String soundex = str;
      for (int i = 0; i < conversion.length; i++) {
        soundex = soundex.replaceAll(conversion[i][0], conversion[i][1]);
      }

      conversion = new String[][] { { "É", "E" }, { "È", "E" }, { "Ë", "E" },
          { "Ê", "E" }, { "Á", "A" }, { "À", "A" }, { "Ä", "A" }, { "Â", "A" },
          { "Å", "A" }, { "Ã", "A" }, { "Æ", "E" }, { "Ï", "I" }, { "Î", "I" },
          { "Ì", "I" }, { "Í", "I" }, { "Ô", "O" }, { "Ö", "O" }, { "Ò", "O" },
          { "Ó", "O" }, { "Õ", "O" }, { "Ø", "O" }, { "Œ", "OEU" },
          { "Ú", "U" }, { "Ù", "U" }, { "Û", "U" }, { "Ü", "U" }, { "Ñ", "N" },
          { "Ç", "S" }, { "¿", "E" } };

      for (int i = 0; i < conversion.length; i++) {
        soundex = soundex.replaceAll(conversion[i][0], conversion[i][1]);
      }
      // $sIn = utf8_decode($sIn); // Selon votre implémentation, vous aurez
      // besoin de décoder ce qui arrive pour les caractères spéciaux

      soundex = soundex.toUpperCase();
      soundex = soundex.replaceAll("[^A-Z]", "");

      String soundexBack = soundex;// on sauve le code (utilisé pour les mots
                                   // très
                                   // courts)

      soundex = soundex.replaceAll("O[O]+", "OU"); // pré traitement OO... -> OU
      soundex = soundex.replaceAll("SAOU", "SOU"); // pré traitement SAOU -> SOU
      soundex = soundex.replaceAll("OES", "OS"); // pré traitement OES -> OS
      soundex = soundex.replaceAll("CCH", "K"); // pré traitement CCH -> K
      soundex = soundex.replaceAll("CC([IYE])", "KS$1"); // CCI CCY CCE
      soundex = soundex.replaceAll("(.)\\1", "$1"); // supression des
                                                    // répétitions
      // quelques cas particuliers
      if (soundex.equals("CD"))
        return (soundex);
      if (soundex.equals("BD"))
        return (soundex);
      if (soundex.equals("BV"))
        return (soundex);
      if (soundex.equals("TABAC"))
        return ("TABA");
      if (soundex.equals("FEU"))
        return ("FE");
      if (soundex.equals("FE"))
        return (soundex);
      if (soundex.equals("FER"))
        return (soundex);
      if (soundex.equals("FIEF"))
        return (soundex);
      if (soundex.equals("FJORD"))
        return (soundex);
      if (soundex.equals("GOAL"))
        return ("GOL");
      if (soundex.equals("FLEAU"))
        return ("FLEO");
      if (soundex.equals("HIER"))
        return ("IER");
      if (soundex.equals("HEU"))
        return ("E");
      if (soundex.equals("HE"))
        return ("E");
      if (soundex.equals("OS"))
        return (soundex);
      if (soundex.equals("RIZ"))
        return ("RI");
      if (soundex.equals("RAZ"))
        return ("RA");

      // pré-traitements
      soundex = soundex.replaceAll("OIN[GT]$", "OIN"); // terminaisons OING ->
                                                       // OIN
      soundex = soundex.replaceAll("E[RS]$", "E"); // supression des
                                                   // terminaisons
                                                   // infinitifs et participes
                                                   // pluriels
      soundex = soundex.replaceAll("(C|CH)OEU", "KE"); // pré traitement OEU ->
                                                       // EU
      soundex = soundex.replaceAll("MOEU", "ME"); // pré traitement OEU -> EU
      soundex = soundex
          .replaceAll("OE([UI]+)([BCDFGHJKLMNPQRSTVWXZ])", "E$1$2"); // pré
                                                                     // traitement
                                                                     // OEU
                                                                     // OEI
                                                                     // ->
                                                                     // E
      soundex = soundex.replaceAll("^GEN[TS]$", "JAN"); // pré traitement GEN ->
                                                        // JAN
      soundex = soundex.replaceAll("CUEI", "KEI"); // pré traitement accueil
      soundex = soundex.replaceAll("([^AEIOUYC])AE([BCDFGHJKLMNPQRSTVWXZ])",
          "$1E$2"); // pré traitement AE -> E
      soundex = soundex.replaceAll("AE([QS])", "E$1"); // pré traitement AE -> E
      soundex = soundex.replaceAll("AIE([BCDFGJKLMNPQRSTVWXZ])", "AI$1"); // pré-traitement
                                                                          // AIE(consonne)
                                                                          // ->
                                                                          // AI
      soundex = soundex.replaceAll("ANIEM", "ANIM"); // pré traitement NIEM ->
                                                     // NIM
      soundex = soundex.replaceAll("(DRA|TRO|IRO)P$", "$1"); // P terminal muet
      soundex = soundex.replaceAll("(LOM)B$", "$1"); // B terminal muet
      soundex = soundex.replaceAll("(RON|POR)C$", "$1"); // C terminal muet
      soundex = soundex.replaceAll("PECT$", "PET"); // C terminal muet
      soundex = soundex.replaceAll("ECUL$", "CU"); // L terminal muet
      soundex = soundex.replaceAll("(CHA|CA|E)M(P|PS)$", "$1N"); // P ou PS
                                                                 // terminal
                                                                 // muet
      soundex = soundex.replaceAll("(TAN|RAN)G$", "$1"); // G terminal muet

      // sons YEUX
      soundex = soundex.replaceAll("([^VO])ILAG", "$1IAJ");
      soundex = soundex.replaceAll("([^TRH])UIL(AR|E)(.+)", "$1UI$2$3");
      soundex = soundex.replaceAll("([G])UIL([AEO])", "$1UI$2");
      soundex = soundex.replaceAll("([NSPM])AIL([AEO])", "$1AI$2");

      conversion = new String[][] { { "DILAI", "DIAI" }, { "DILON", "DION" },
          { "DILER", "DIER" }, { "DILEM", "DIEM" }, { "RILON", "RION" },
          { "TAILE", "TAIE" }, { "GAILET", "GAIET" }, { "AILAI", "AIAI" },
          { "AILAR", "AIAR" }, { "OUILA", "OUIA" }, { "EILAI", "AIAI" },
          { "EILAR", "AIAR" }, { "EILER", "AIER" }, { "EILEM", "AIEM" },
          { "REILET", "RAIET" }, { "EILET", "EIET" }, { "AILOL", "AIOL" } };

      for (int i = 0; i < conversion.length; i++) {
        soundex = soundex.replaceAll(conversion[i][0], conversion[i][1]);
      }

      soundex = soundex.replaceAll("([^AEIOUY])(SC|S)IEM([EA])", "$1$2IAM$3"); // IEM
                                                                               // ->
                                                                               // IAM
      soundex = soundex.replaceAll("^(SC|S)IEM([EA])", "$1IAM$2"); // IEM -> IAM

      // MP MB -> NP NB
      conversion = new String[][] { { "OMB", "ONB" }, { "AMB", "ANB" },
          { "OMP", "ONP" }, { "AMP", "ANP" }, { "IMB", "INB" },
          { "EMP", "ANP" }, { "GEMB", "JANB" }, { "EMB", "ANB" },
          { "UMBL", "INBL" }, { "CIEN", "SIAN" } };
      for (int i = 0; i < conversion.length; i++) {
        soundex = soundex.replaceAll(conversion[i][0], conversion[i][1]);
      }

      // Sons en K
      soundex = soundex.replaceAll("^ECHO$", "EKO"); // cas particulier écho
      soundex = soundex.replaceAll("^ECEUR", "EKEUR"); // cas particulier écœuré
      // Choléra Chœur mais pas chocolat!
      soundex = soundex.replaceAll("^CH(OG+|OL+|OR+|EU+|ARIS|M+|IRO|ONDR)",
          "K$1"); // En début
                  // de mot
      soundex = soundex.replaceAll(
          "(YN|RI)CH(OG+|OL+|OC+|OP+|OM+|ARIS|M+|IRO|ONDR)", "$1K$2"); // Ou
                                                                       // devant
                                                                       // une
                                                                       // consonne
      soundex = soundex.replaceAll("CHS", "CH");
      soundex = soundex.replaceAll("CH(AIQ)", "K$1");
      soundex = soundex.replaceAll("^ECHO([^UIPY])", "EKO$1");
      soundex = soundex.replaceAll("ISCH(I|E)", "ISK$1");
      soundex = soundex.replaceAll("^ICHT", "IKT");
      soundex = soundex.replaceAll("ORCHID", "ORKID");
      soundex = soundex.replaceAll("ONCHIO", "ONKIO");
      soundex = soundex.replaceAll("ACHIA", "AKIA"); // retouche ACHIA -> AKIA
      soundex = soundex.replaceAll("([^C])ANICH", "$1ANIK"); // ANICH -> ANIK
                                                             // 1/2
      soundex = soundex.replaceAll("OMANIK", "OMANICH"); // cas particulier 2/2
      soundex = soundex.replaceAll("ACHY([^D])", "AKI$1");
      soundex = soundex.replaceAll("([AEIOU])C([BDFGJKLMNPQRTVWXZ])", "$1K$2"); // voyelle,
                                                                                // C,
                                                                                // consonne
                                                                                // sauf
                                                                                // H

      conversion = new String[][] { { "EUCHA", "EKA" }, { "YCHIA", "IKIA" },
          { "YCHA", "IKA" }, { "YCHO", "IKO" }, { "YCHED", "IKED" },
          { "ACHEO", "AKEO" }, { "RCHEO", "RKEO" }, { "RCHES", "RKES" },
          { "ECHN", "EKN" }, { "OCHTO", "OKTO" }, { "CHORA", "KORA" },
          { "CHONDR", "KONDR" }, { "CHORE", "KORE" }, { "MACHM", "MAKM" },
          { "BRONCHO", "BRONKO" }, { "LICHOS", "LIKOS" }, { "LICHOC", "LIKOC" } };
      for (int i = 0; i < conversion.length; i++) {
        soundex = soundex.replaceAll(conversion[i][0], conversion[i][1]);
      }

      // Weuh (perfectible)
      conversion = new String[][] { { "WA", "OI" }, { "WO", "O" },
          { "WI", "OUI" }, { "WHI", "OUI" }, { "WHY", "OUI" },
          { "WHA", "OUA" }, { "WHO", "OU" } };
      for (int i = 0; i < conversion.length; i++) {
        soundex = soundex.replaceAll(conversion[i][0], conversion[i][1]);
      }

      conversion = new String[][] { { "GNES", "NIES" }, { "GNET", "NIET" },
          { "GNER", "NIER" }, { "GNE", "NE" }, { "GI", "JI" }, { "GNI", "NI" },
          { "GNA", "NIA" }, { "GNOU", "NIOU" }, { "GNUR", "NIUR" },
          { "GY", "JI" }, { "OUGAIN", "OUGIN" }, { "AGEOL", "AJOL" },
          { "AGEOT", "AJOT" }, { "GEOLO", "JEOLO" }, { "GEOM", "JEOM" },
          { "GEOP", "JEOP" }, { "GEOG", "JEOG" }, { "GEOS", "JEOS" },
          { "GEORG", "JORJ" }, { "GEOR", "JEOR" }, { "NGEOT", "NJOT" },
          { "UGEOT", "UJOT" }, { "GEOT", "JEOT" }, { "GEOD", "JEOD" },
          { "GEOC", "JEOC" }, { "GEO", "JO" }, { "GEA", "JA" }, { "GE", "JE" },
          { "QU", "K" }, { "Q", "K" }, { "CY", "SI" }, { "CI", "SI" },
          { "CN", "KN" }, { "ICM", "IKM" }, { "CEAT", "SAT" }, { "CE", "SE" },
          { "CR", "KR" }, { "CO", "KO" }, { "CUEI", "KEI" }, { "CU", "KU" },
          { "VENCA", "VANSA" }, { "CA", "KA" }, { "CS", "KS" },
          { "CLEN", "KLAN" }, { "CL", "KL" }, { "CZ", "KZ" },
          { "CTIQ", "KTIK" }, { "CTIF", "KTIF" }, { "CTIC", "KTIS" },
          { "CTIS", "KTIS" }, { "CTIL", "KTIL" }, { "CTIO", "KSIO" },
          { "CTI", "KTI" }, { "CTU", "KTU" }, { "CTE", "KTE" },
          { "CTO", "KTO" }, { "CTR", "KTR" }, { "CT", "KT" }, { "PH", "F" },
          { "TH", "T" }, { "OW", "OU" }, { "LH", "L" }, { "RDL", "RL" },
          { "CHLO", "KLO" }, { "CHR", "KR" }, { "PTIA", "PSIA" } };

      // Gueu, Gneu, Jeu et quelques autres
      for (int i = 0; i < conversion.length; i++) {
        soundex = soundex.replaceAll(conversion[i][0], conversion[i][1]);
      }
      soundex = soundex.replaceAll("GU([^RLMBSTPZN])", "G$1"); // Gueu !
      soundex = soundex.replaceAll("GNO([MLTNRKG])", "NIO$1"); // GNO ! Tout
                                                               // sauf
                                                               // S pour gnos
      soundex = soundex.replaceAll("GNO([MLTNRKG])", "NIO$1"); // bis ->
                                                               // gnognotte! Si
                                                               // quelqu'un sait
                                                               // le faire en
                                                               // une
                                                               // seule
                                                               // regexp...

      // TI -> SI v2.0

      conversion = new String[][] { { "BUTIE", "BUSIE" }, { "BUTIA", "BUSIA" },
          { "BATIA", "BASIA" }, { "ANTIEL", "ANSIEL" }, { "RETION", "RESION" },
          { "ENTIEL", "ENSIEL" }, { "ENTIAL", "ENSIAL" }, { "ENTIO", "ENSIO" },
          { "ENTIAI", "ENSIAI" }, { "UJETION", "UJESION" },
          { "ATIEM", "ASIAM" }, { "PETIEN", "PESIEN" }, { "CETIE", "CESIE" },
          { "OFETIE", "OFESIE" }, { "IPESI", "BUTIA" },
          { "LBUTION", "LBUSION" }, { "BLUTION", "BLUSION" },
          { "LETION", "LESION" }, { "LATION", "LASION" },
          { "SATIET", "SASIET" } };
      for (int i = 0; i < conversion.length; i++) {
        soundex = soundex.replaceAll(conversion[i][0], conversion[i][1]);
      }

      soundex = soundex.replaceAll("(.+)ANTI(AL|O)", "$1ANSI$2"); // sauf
                                                                  // antialcoolique,
                                                                  // antialbumine,
                                                                  // antialarmer,
                                                                  // ...
      soundex = soundex.replaceAll("(.+)INUTI([^V])", "$1INUSI$2"); // sauf
                                                                    // inutilité,
                                                                    // inutilement,
                                                                    // diminutive,
                                                                    // ...
      soundex = soundex.replaceAll("([^O])UTIEN", "$1USIEN"); // sauf soutien,
                                                              // ...
      soundex = soundex.replaceAll("([^DE])RATI[E]$", "$1RASI$2"); // sauf
                                                                   // xxxxxcratique,
                                                                   // ...
      // TIEN TION -> SIEN SION v3.1
      soundex = soundex.replaceAll("([^SNEU]|KU|KO|RU|LU|BU|TU|AU)T(IEN|ION)",
          "$1S$2");

      // H muet
      soundex = soundex.replaceAll("([^CS])H", "$1"); // H muet
      soundex = soundex.replaceAll("ESH", "ES"); // H muet
      soundex = soundex.replaceAll("NSH", "NS"); // H muet
      soundex = soundex.replaceAll("SH", "CH"); // ou pas!
      // NASALES
      conversion = new String[][] { { "OMT", "ONT" }, { "IMB", "INB" },
          { "IMP", "INP" }, { "UMD", "OND" }, { "TIENT", "TIANT" },
          { "RIENT", "RIANT" }, { "DIENT", "DIANT" }, { "IEN", "IN" },
          { "YMU", "IMU" }, { "YMO", "IMO" }, { "YMA", "IMA" },
          { "YME", "IME" }, { "YMI", "IMI" }, { "YMN", "IMN" }, { "YM", "IN" },
          { "AHO", "AO" }, { "FAIM", "FIN" }, { "DAIM", "DIN" },
          { "SAIM", "SIN" }, { "EIN", "AIN" }, { "AINS", "INS" } };
      for (int i = 0; i < conversion.length; i++) {
        soundex = soundex.replaceAll(conversion[i][0], conversion[i][1]);
      }

      // AIN -> IN v2.0
      soundex = soundex.replaceAll("AIN$", "IN");
      soundex = soundex.replaceAll("AIN([BTDK])", "IN$1");
      // UN -> IN
      soundex = soundex.replaceAll("([^O])UND", "$1IND"); // aucun mot français
                                                          // ne
                                                          // commence par UND!
      soundex = soundex.replaceAll("([JTVLFMRPSBD])UN([^IAE])", "$1IN$2");
      soundex = soundex.replaceAll("([JTVLFMRPSBD])UN$", "$1IN");
      soundex = soundex.replaceAll("RFUM$", "RFIN");
      soundex = soundex.replaceAll("LUMB", "LINB");
      // EN -> AN
      soundex = soundex.replaceAll("([^BCDFGHJKLMNPQRSTVWXZ])EN", "$1AN");
      soundex = soundex.replaceAll("([VTLJMRPDSBFKNG])EN([BRCTDKZSVN])",
          "$1AN$2"); // deux
                     // fois
                     // pour les
                     // motifs
                     // recouvrants
                     // malentendu,
                     // pendentif,
                     // ...
      soundex = soundex.replaceAll("([VTLJMRPDSBFKNG])EN([BRCTDKZSVN])",
          "$1AN$2"); // si
                     // quelqu'un
                     // sait
                     // faire
                     // avec une
                     // seule
                     // regexp!
      soundex = soundex.replaceAll(
          "^EN([BCDFGHJKLNPQRSTVXZ]|CH|IV|ORG|OB|UI|UA|UY)", "AN$1");
      soundex = soundex.replaceAll("(^[JRVTH])EN([DRTFGSVJMP])", "$1AN$2");
      soundex = soundex.replaceAll("SEN([ST])", "SAN$1");
      soundex = soundex.replaceAll("^DESENIV", "DESANIV");
      soundex = soundex.replaceAll("([^M])EN(UI)", "$1AN$2");
      soundex = soundex.replaceAll("(.+[JTVLFMRPSBD])EN([JLFDSTG])", "$1AN$2");
      // EI -> AI
      soundex = soundex
          .replaceAll("([VSBSTNRLPM])E[IY]([ACDFRJLGZ])", "$1AI$2");
      // Histoire d'Ô

      conversion = new String[][] { { "EAU", "O" }, { "EU", "E" },
          { "Y", "I" }, { "EOI", "OI" }, { "JEA", "JA" }, { "OIEM", "OIM" },
          { "OUANJ", "OUENJ" }, { "OUA", "OI" }, { "OUENJ", "OUANJ" } };
      for (int i = 0; i < conversion.length; i++) {
        soundex = soundex.replaceAll(conversion[i][0], conversion[i][1]);
      }
      soundex = soundex.replaceAll("AU([^E])", "O$1"); // AU sans E qui suit
      // Les retouches!
      soundex = soundex.replaceAll("^BENJ", "BINJ"); // retouche BENJ -> BINJ
      soundex = soundex.replaceAll("RTIEL", "RSIEL"); // retouche RTIEL -> RSIEL
      soundex = soundex.replaceAll("PINK", "PONK"); // retouche PINK -> PONK
      soundex = soundex.replaceAll("KIND", "KOND"); // retouche KIND -> KOND
      soundex = soundex.replaceAll("KUM(N|P)", "KON$1"); // retouche KUMN KUMP
      soundex = soundex.replaceAll("LKOU", "LKO"); // retouche LKOU -> LKO
      soundex = soundex.replaceAll("EDBE", "EBE"); // retouche EDBE pied-bœuf
      soundex = soundex.replaceAll("ARCM", "ARKM"); // retouche SCH -> CH
      soundex = soundex.replaceAll("SCH", "CH"); // retouche SCH -> CH
      soundex = soundex.replaceAll("^OINI", "ONI"); // retouche début OINI ->
                                                    // ONI
      soundex = soundex.replaceAll("([^NDCGRHKO])APT", "$1AT"); // retouche APT
                                                                // ->
                                                                // AT
      soundex = soundex.replaceAll("([L]|KON)PT", "$1T"); // retouche LPT -> LT
      soundex = soundex.replaceAll("OTB", "OB"); // retouche OTB -> OB
                                                 // (hautbois)
      soundex = soundex.replaceAll("IXA", "ISA"); // retouche IXA -> ISA
      soundex = soundex.replaceAll("TG", "G"); // retouche TG -> G
      soundex = soundex.replaceAll("^TZ", "TS"); // retouche début TZ -> TS
      soundex = soundex.replaceAll("PTIE", "TIE"); // retouche PTIE -> TIE
      soundex = soundex.replaceAll("GT", "T"); // retouche GT -> T
      soundex = soundex.replaceAll("ANKIEM", "ANKILEM"); // retouche
                                                         // tranquillement
      soundex = soundex.replaceAll("(LO|RE)KEMAN", "$1KAMAN"); // KEMAN -> KAMAN
      soundex = soundex.replaceAll("NT(B|M)", "N$1"); // retouche TB -> B TM ->
                                                      // M
      soundex = soundex.replaceAll("GSU", "SU"); // retouche GS -> SU
      soundex = soundex.replaceAll("ESD", "ED"); // retouche ESD -> ED
      soundex = soundex.replaceAll("LESKEL", "LEKEL"); // retouche LESQUEL ->
                                                       // LEKEL
      soundex = soundex.replaceAll("CK", "K"); // retouche CK -> K

      // Terminaisons
      soundex = soundex.replaceAll("USIL$", "USI"); // terminaisons USIL -> USI
      soundex = soundex.replaceAll("X$|[TD]S$|[DS]$", ""); // terminaisons TS DS
                                                           // LS X T D S... v2.0
      soundex = soundex.replaceAll("([^KL]+)T$", "$1"); // sauf KT LT terminal
      soundex = soundex.replaceAll("^[H]", ""); // H pseudo muet en début de
                                                // mot,
                                                // je sais, ce n'est pas une
                                                // terminaison
      String soundexBack2 = soundex; // on sauve le code (utilisé pour les mots
                                     // très courts)
      soundex = soundex.replaceAll("TIL$", "TI"); // terminaisons TIL -> TI
      soundex = soundex.replaceAll("LC$", "LK"); // terminaisons LC -> LK
      soundex = soundex.replaceAll("L[E]?[S]?$", "L"); // terminaisons LE LES ->
                                                       // L
      soundex = soundex.replaceAll("(.+)N[E]?[S]?$", "$1N"); // terminaisons NE
                                                             // NES -> N
      soundex = soundex.replaceAll("EZ$", "E"); // terminaisons EZ -> E
      soundex = soundex.replaceAll("OIG$", "OI"); // terminaisons OIG -> OI
      soundex = soundex.replaceAll("OUP$", "OU"); // terminaisons OUP -> OU
      soundex = soundex.replaceAll("([^R])OM$", "$1ON"); // terminaisons OM ->
                                                         // ON
                                                         // sauf ROM
      soundex = soundex.replaceAll("LOP$", "LO"); // terminaisons LOP -> LO
      soundex = soundex.replaceAll("NTANP$", "NTAN"); // terminaisons NTANP ->
                                                      // NTAN
      soundex = soundex.replaceAll("TUN$", "TIN"); // terminaisons TUN -> TIN
      soundex = soundex.replaceAll("AU$", "O"); // terminaisons AU -> O
      soundex = soundex.replaceAll("EI$", "AI"); // terminaisons EI -> AI
      soundex = soundex.replaceAll("R[DG]$", "R"); // terminaisons RD RG -> R
      soundex = soundex.replaceAll("ANC$", "AN"); // terminaisons ANC -> AN
      soundex = soundex.replaceAll("KROC$", "KRO"); // terminaisons C muet de
                                                    // CROC, ESCROC
      soundex = soundex.replaceAll("HOUC$", "HOU"); // terminaisons C muet de
                                                    // CAOUTCHOUC
      soundex = soundex.replaceAll("OMAC$", "OMA"); // terminaisons C muet de
                                                    // ESTOMAC (mais pas HAMAC)
      soundex = soundex.replaceAll("([J])O([NU])[CG]$", "$1O$2");// terminaisons
                                                                 // C
                                                                 // et G muet de
                                                                 // OUC ONC OUG
      soundex = soundex.replaceAll("([^GTR])([AO])NG$", "$1$2N");// terminaisons
                                                                 // G
                                                                 // muet ANG ONG
                                                                 // sauf GANG
                                                                 // GONG
                                                                 // TANG TONG
      soundex = soundex.replaceAll("UC$", "UK"); // terminaisons UC -> UK
      soundex = soundex.replaceAll("AING$", "IN"); // terminaisons AING -> IN
      soundex = soundex.replaceAll("([EISOARN])C$", "$1K"); // terminaisons C ->
                                                            // K
      soundex = soundex.replaceAll("([ABD-MO-Z]+)[EH]+$", "$1"); // terminaisons
                                                                 // E
                                                                 // ou H sauf
                                                                 // pour
                                                                 // C et N
      soundex = soundex.replaceAll("EN$", "AN"); // terminaisons EN -> AN
                                                 // (difficile à faire avant
                                                 // sans
                                                 // avoir des soucis) Et encore,
                                                 // c'est pas top!
      soundex = soundex.replaceAll("(NJ)EN$", "$1AN"); // terminaisons EN -> AN
      soundex = soundex.replaceAll("^PAIEM", "PAIM"); // PAIE -> PAI
      // soundex = soundex.replaceAll( "([^NTB])EF$", ""); // F muet en fin de
      // mot
      soundex = soundex.replaceAll("(.)\\1", "$1"); // supression des
                                                    // répétitions
                                                    // (suite à certains
                                                    // remplacements)

      // cas particuliers, bah au final, je n'en ai qu'un ici
      soundex = soundex.replaceAll("FUEL", "FIOUL");

      // Ce sera le seul code retourné à une seule lettre!
      if (soundex.equals("O"))
        return (soundex);

      // seconde chance sur les mots courts qui ont souffert de la
      // simplification
      if (soundex.length() < 2) {
        // Sigles ou abréviations
        if (soundexBack
            .matches("[BCDFGHJKLMNPQRSTVWXYZ][BCDFGHJKLMNPQRSTVWXYZ][BCDFGHJKLMNPQRSTVWXYZ][BCDFGHJKLMNPQRSTVWXYZ]*"))
          return (soundexBack);

        if (soundexBack.matches(("[RFMLVSPJDF][AEIOU]"))) {
          if (soundexBack.length() == 3)
            return (soundexBack.substring(0, 2));// mots de trois lettres
                                                 // supposés
                                                 // simples
          if (soundexBack.length() == 4)
            return (soundexBack.substring(0, 3));// mots de quatre lettres
                                                 // supposés simples
        }

        if (soundexBack2.length() > 1)
          return soundexBack2;
      }

      if (soundex.length() > 1)
        return soundex;
      else
        return "";
    } catch (Exception e) {
      return str.toUpperCase();
    }
  }

  public static void main(String args[]) {
    String s1 = "rue regrattier";
    String s2 = "rue du vingt neuf juillet";

  }
}
