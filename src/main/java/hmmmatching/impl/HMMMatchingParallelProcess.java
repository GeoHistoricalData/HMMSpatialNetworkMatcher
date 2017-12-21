package hmmmatching.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.RecursiveTask;

import com.bmw.hmm.SequenceState;
import com.bmw.hmm.Transition;
import com.bmw.hmm.ViterbiAlgorithm;

import fr.ign.cogit.geoxygene.contrib.cartetopo.Arc;
import fr.ign.cogit.geoxygene.contrib.cartetopo.CarteTopo;


public class HMMMatchingParallelProcess extends RecursiveTask<Map<Arc, List<ACluster>>> {
  
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  
  
  //Uniquement pour affichage
  private static int CPT = 0;
  //Uniquement pour affichage
  private static int CPT2 = 0;
  //Uniquement pour affichage

  private int id;
  private boolean REVERSE_PROCESS;
  private Map<Arc, List<ACluster>> matching;
  private List<List<Arc>> strokes;
  private List<Arc> stroke;
  private List<Arc> strokeReverse;
  private CarteTopo net;
  private HMMParameters parameters;
  private boolean firstIt;


  public HMMMatchingParallelProcess(List<List<Arc>> strokes, CarteTopo net,
      HMMParameters parameters) {
    CPT ++;
    this.id = new Integer(CPT);
    this.strokes = strokes;
    this.net = net;
    this.parameters = parameters;
    this.matching = new HashMap<>();
    this.REVERSE_PROCESS = false;
    this.firstIt = true;
  }
  



  /**
   * Une itération du processus d'appariement correspond à l'appariement d'un ensemble d'observations
   * @param stroke le stroke étudié (la suite d'observation du HMM)
   * @param net le réseau à apparier, contenant les états cachés du HMM
   * @param matching 
   * @param parameters
   */
  public void iteration(){

    if(stroke.size() < parameters.stroke_length){
      //TODO : reverse
      if(!this.REVERSE_PROCESS){
        this.REVERSE_PROCESS = true;
        this.stroke = this.strokeReverse;
        this.iteration();
      }
      //Le stroke a traiter n'ets plus assez long, on sort
      return;
    }

    // Les états cachés du HMM
    Map<ACluster, MatchingState> states = new HashMap<>();
    // Mapping entre état et leur contenu
    Map<MatchingState, ACluster> reverseStates = new HashMap<>();
    // Les observations du HMM et mapping entre observation et leur contenu
    Map<MatchingObservation, Arc> observations = new HashMap<>();

    // Première observation de la liste
    Iterator<Arc> itObservations = stroke.iterator();
    Arc currentO = itObservations.next();
    MatchingObservation hmmo = new MatchingObservation(currentO);   
    observations.put(hmmo, currentO);

    // On cherche les états possibles associés à l'observation
    AClusterCollection currentCandidates = hmmo.candidates(net, parameters.selection);
    List<MatchingState> currentStatescandidates = new ArrayList<>();
    // Les proba d'émission initiales
    Map<MatchingState, Double> startP = new HashMap<MatchingState, Double>();

    // pour vérifier qu'il y a au moins un candidat initial possible
    // TODO : à supprimer si DOUBLE.MIN_VALUE dans calcul des proba d'émission ?
    boolean notAllCandidatesInfinity = false;
    for(ACluster cc  :currentCandidates){     
      MatchingState state = null;
      if(!states.containsKey(cc)){
        state = new MatchingState(cc);
        states.put(cc, state);
        reverseStates.put(state, cc);
      }
      else{
        state = states.get(cc);
      }
      currentStatescandidates.add(state);
      double emissionP = hmmo.computeEmissionProbability(state, parameters);
      if(Double.isFinite(emissionP)){
        notAllCandidatesInfinity = true;
      }
      startP.put(state, emissionP);
    }

    // si pas de candidats, ou candidats tous impossible (proba emissions toutes infinies) 
    if(currentStatescandidates.isEmpty() || !notAllCandidatesInfinity){
      // on supprime cette observation de la séquence
      stroke.remove(currentO);
      // et on ressaye avec la séquence ainsi modifiée
      this.iteration();
      return;
    }

    // Structure qui va exécuter l'algo de Viterbi
    ViterbiAlgorithm<MatchingState, MatchingObservation, MatchingTransitionDescriptor> viterbi = new ViterbiAlgorithm<>(true);
    // Initialisation avec les proba d'émission initiales
    viterbi.startWithInitialObservation(hmmo, currentStatescandidates,
        startP);

    boolean viterbiIsBroken = false;

    while(itObservations.hasNext()){
      // Tant qu'on peut continuer, on considère l'observation suivante dans la séquence
      Arc nextO = itObservations.next();
      MatchingObservation nextHmmo = new MatchingObservation(nextO);

      observations.put(nextHmmo, nextO);    

      AClusterCollection nextCandidates = nextHmmo.candidates(net, parameters.selection);

      List<MatchingState> nextStatescandidates = new ArrayList<>();

      if(nextCandidates.isEmpty()){
        // Pas d'états cachés possibles pour cette observation .. le Viterbri est brisé. On sort.
        viterbiIsBroken = true;
        break;
      }

      // Calcul des proba d'émission pour cette observation et ses états cachés candidats
      Map<MatchingState, Double> emissionP = new HashMap<MatchingState, Double>();
      for(ACluster cc  :nextCandidates){

        MatchingState state = null;
        if(!states.containsKey(cc)){
          state = new MatchingState(cc);
          states.put(cc, state);
          reverseStates.put(state, cc);
        }
        else{
          state = states.get(cc);
        }
        nextStatescandidates.add(state);
        emissionP.put(state, nextHmmo.computeEmissionProbability(state, parameters));
      }

      // Calcul des probabilités de transition
      final Map<Transition<MatchingState>, Double> transitionLogProbabilities = new LinkedHashMap<>();
      //   final Map<Transition<MatchingState>, MatchingTransitionDescriptor> transitionDescriptors = new LinkedHashMap<>();

      for(MatchingState state1 :currentStatescandidates){
        for(MatchingState state2: nextStatescandidates){
          transitionLogProbabilities.put(new Transition<MatchingState>(state1, state2),
              state1.computeTransitionProbability(state2, hmmo, nextHmmo, parameters));
          // transitionDescriptors.put(new Transition<MatchingState>(state1, state2), new MatchingTransitionDescriptor(state1, state2));

        }
      }

      viterbi.nextStep(nextHmmo, nextStatescandidates, emissionP,
          transitionLogProbabilities);


      if(viterbi.isBroken()){
        // Le Viterbi est broken: pas de transitions possibles. On interompt 
        // le processus à cette étape
        viterbiIsBroken = true;
        break;
      }

      currentO = nextO;
      hmmo = nextHmmo;
      currentCandidates = nextCandidates;
      currentStatescandidates = nextStatescandidates;
    }

    // On calcul le chemin le plus probable dans le graphe des transitions / émissions
    // Fait pour la séquence d'observation entière, ou jusqu'à celle qui introduit une rupture
    // dans le Viterbi
    final List<SequenceState<MatchingState, MatchingObservation, MatchingTransitionDescriptor>> result =
        viterbi.computeMostLikelySequence();

    Set<Arc> arcsDones = new HashSet<Arc>();
    for(SequenceState<MatchingState, MatchingObservation, MatchingTransitionDescriptor> s : result){
      Arc obs = observations.get(s.observation);
      arcsDones.add(obs);
      ACluster state = reverseStates.get(s.state);
      if(matching.containsKey(obs) && !matching.get(obs).contains(state)){
        matching.get(obs).add(state);
      }
      else{
        List<ACluster> l = new ArrayList<>();
        l.add(state);
        matching.put(obs, l);
      }
    }



    if(viterbiIsBroken){
      // si le Viterbri a été broken, on supprime les arcs traités jusqu'à l'observation
      // qui introduit la rupture, et on recommence à partir de la suivante.
      stroke.removeAll(arcsDones);
      this.iteration();
    }
  }


  /**
   * Surcharge de compute() pour gérer la paralélisation
   */
  @Override
  protected Map<Arc, List<ACluster>> compute() {
    if(this.strokes.size() !=1){
      List<HMMMatchingParallelProcess>hmmIterations = new ArrayList<>();
      for(List<Arc> stroke: this.strokes){
        List<List<Arc>> st = new ArrayList<>();
        st.add(stroke);
        HMMMatchingParallelProcess hmmIt = new HMMMatchingParallelProcess(st, net, parameters);
        hmmIterations.add(hmmIt);
      }
      for(HMMMatchingParallelProcess hmmIt:hmmIterations){
        hmmIt.fork();
      }
      Map<Arc, List<ACluster>> result = new HashMap<>();
      for(HMMMatchingParallelProcess hmmIt: hmmIterations){
        result = this.compile(result, hmmIt.join());
      }

      return result;

    }
    else{
      if(this.firstIt){
        // Le traitement de la première itération est un peu différent
        this.firstIt = false;
        this.stroke = new ArrayList<>();
        this.stroke.addAll(this.strokes.get(0));
        this.strokeReverse = new ArrayList<>();
        this.strokeReverse.addAll(this.stroke);
        Collections.reverse(this.strokeReverse);
      }

      this.iteration();
      CPT2++;
      System.out.println("Local process " + this.id+ " done - " + CPT2 +" achieved" );
      return this.matching;
    }
  }


  /**
   * Méthode permettant de compiler les résultats d'appariement des différents clusters appariés
   * parallélement
   * @param result1
   * @param result2
   * @return
   */
  private Map<Arc, List<ACluster>> compile(Map<Arc, List<ACluster>> result1,
      Map<Arc, List<ACluster>> result2) {
    Map<Arc, List<ACluster>> result = new HashMap<>();
    for(Arc a : result1.keySet()){
      result.put(a, result1.get(a));
    }
    for(Arc a : result2.keySet()){
      if(!result.containsKey(a)){
        result.put(a, result2.get(a));
      }
      else{
        for(ACluster clust: result2.get(a)){
          if(!result.get(a).contains(clust)){
            result.get(a).add(clust);
          }
        }
      }
    }
    return result;
  }



}
