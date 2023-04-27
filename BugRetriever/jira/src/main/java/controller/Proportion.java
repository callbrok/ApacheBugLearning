package controller;

import model.Bug;
import model.Release;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Proportion {
    // NOTE TO THINKING OF:
    //      Da discutere quando approssimo per difetto e quando per eccesso, potrei fare de studi
    //      per vedere quanti falsi positivi e negativi ho e come mi conviene
    //
    // APROXIMATE constant meaning value:
    //      true   -  Approximate by excess   (DEFAULT choice)
    //      false  -  Approximate by defect
    private static final Boolean APROXIMATE=true;

    private static final int THRESHOLD=5;
    private static final List<String> PROJECTODOCOLDSTART = List.of("TAJO", "STORM");

    // NOTE TO THINKING OF:
    //      In cold start per calcolare p del bug corrente non dovrei usare bug di altri progetti che hanno
    //      riferita una data piu' futura rispetto a quella del bug corrente, NON POSSO USARE DATI DEL FUTURO PER
    //      PREDIRRE IL PASSATO, il problema pero' è che potrei avere, rispetto alla data del bug corrente, pochi bug
    //      prima di altri progetti o potrei non averli proprio.
    //
    //      Quindi setto la condizione a 'false' se voglio imprescindibilmente rispettare il dogma !NON POSSO USARE I DATI
    //      DEL FUTURO PER PREDIRRE IL PASSATO! Altrimenti se settato a 'true' si calcola p supponendo che tutti i progetti
    //      presi in cosiderazione hanno una data di chiusura prima del bug corrente.
    private static final Boolean DONTCAREFUTUREONPASTDATE=true;

    public List<Bug> proportionAlgoHelper(List<Bug> actualValidBugList, List<Release> released) throws IOException, ParseException {
        // Calcola la injected version qui
        // A seconda della situazione capisci quale metodo per calcolare p scegliere

        // NOTE TO THINKING OF:
        //      Ovviamente se il bug su cui fare proportion è il primo non posso usare increment dato che non ho
        //      bug/dati passati su cui basarmi... usato quindi un altro metodo tipo cold start

        // ------------------------------------------------------------------------------------------------------

        // Init p to calculate the injected version
        float p = 0;

        // Init list for proportion increment checker
        List<Bug> bugToCheckIncrement = new ArrayList<>();

        // Init p for indicated projects for proportion cold start approach
        List<Float> projectsP = coldStart();

        // Scroll through the valid bug list
        for (Bug validBugP : actualValidBugList) {

            // If the bug is not flagged to do proportion skip it
            if(!validBugP.getAffectedVersions().get(0).getName().equals("DOPROPORTION")){continue;}

            bugToCheckIncrement = incrementChecker(actualValidBugList, validBugP);

            // Calculate the number of bug valid for do proportion increment, and check if it's larger than
            // threshold constant (THRESHOLD = 5)
            //
            // #NOTE TO THINKING OF:
            //      Perche prendo 5 come dice il paper?
            if(bugToCheckIncrement.size() > THRESHOLD){
                System.err.print("\n\nSCATTATO INCREMENT PER IL BUG --> " + validBugP.getNameKey() + "\n");
                p = increment(bugToCheckIncrement);
            }else{
                System.err.print("\n\nSCATTATO COLD START PER IL BUG --> " + validBugP.getNameKey() + "\n");
                p = takeMedianPColdStart(projectsP);
            }

            // Calculate Injected Version with P and proportion algorithm
            Release injectedVersion = getInjectedVersionFromP(p, validBugP, released);

            // Set calculated affected version by proportion algorithms
            validBugP.setAffectedAndInjectedVersions(injectedVersion, released);

            // Print checking
            System.err.print("\nCALCULATED AV: ");
            for(Release rlsIndex : validBugP.getAffectedVersions()){System.err.print(rlsIndex.getIndex() + " ");}
            System.err.print("\n-------------------\n\n");

            // Flag the bug which use proportion
            validBugP.setPropotionaled(true);
        }

        return actualValidBugList;
    }

    private List<Bug> incrementChecker(List<Bug> actualValidBugList, Bug currrentBug){
        // Return Bugs list, that has Bug objects that:
        //      1. are closed in a release that is before than the opening release of the current bug
        //      2. has Affected Version not calculated by proportion
        //
        List<Bug> bugToDoIncrement = new ArrayList<>();

        //GetBugInfo gtb = new GetBugInfo();

        for (Bug validBugP : actualValidBugList) {
            if(validBugP.getFixedVersions().getIndex() > currrentBug.getOpeningVersion().getIndex() || validBugP.getAffectedVersions().get(0).getName().equals("DOPROPORTION") || currrentBug.getPropotionaled()){continue;}
            bugToDoIncrement.add(validBugP);

            //gtb.printBugInformation(validBugP, 0);
        }

        return bugToDoIncrement;
    }


    private Release getInjectedVersionFromP(float p, Bug currBugToCalcProportion, List<Release> released){
        // Do the IV proportion formula:
        //      IV = FV - (FV - OV) * P
        // and return the associated Release Object

        float injectedVersionIndex = (float)0;

        // If Opening Version it's equals to Fixed Version set (FV-OV)=1
        // #NOTE-TO-THINKING-OF:
        //      Dato che se OV e FV sono uguali, quando faccio la sottrazione mi viene uguale, se mi viene uguale
        //      e svolgo la formula di p IV mi verra sempre uguale a FV, e quindi poi verranno scartati alla fine perche
        //      IV=OV=FV --- TUTTO QUESTO NEL CALCOLO DELL'IV DEL BUG A CUI STO APPLICANDO PROPORTION INCREMENTAL
        float subFvOv = currBugToCalcProportion.getFixedVersions().getIndex() - currBugToCalcProportion.getOpeningVersion().getIndex();

        if(subFvOv == 0){
            injectedVersionIndex = currBugToCalcProportion.getFixedVersions().getIndex() - (1 * p);
        }else{injectedVersionIndex = currBugToCalcProportion.getFixedVersions().getIndex() - (subFvOv * p);}

        // If the Injected Version is < 1, it's set to 1
        //
        // #NOTE-TO-THINKING-OF:
        //      Se la injected version calcolata e' minore di 1 potrebbe significare che la injected faccia
        //      riferimento ad una release in beta comunque non rilasciata
        if(injectedVersionIndex<1){injectedVersionIndex=1;}

        // Approximate the injection version index calculated by proportion, by excess or defect
        //
        // #NOTE-TO-THINKING-OF:
        //      Approssimo per difetto o per eccesso? cosa comporta una o l'altra scelta,
        //      parliamo sempre di approssimazioni
        //
        //      il parametro newScale di Big decimal mi dice quale valore dopo la virgola devo
        //      approssimare
        //
        BigDecimal iv = new BigDecimal(injectedVersionIndex);

        // Approximate 'injectedVersionIndex' by defect
        if(!APROXIMATE){iv = iv.setScale(0, RoundingMode.HALF_DOWN);}
        // Approximate 'injectedVersionIndex' by excess
        if(APROXIMATE){iv = iv.setScale(0, RoundingMode.HALF_UP);}

        System.err.print(currBugToCalcProportion.getNameKey() + "| FV: " + currBugToCalcProportion.getFixedVersions().getIndex() + " | OV: " + currBugToCalcProportion.getOpeningVersion().getIndex() + "| P: " + p + "| IV: " + injectedVersionIndex + " --> " + iv.intValue() );


        // Check if the calculated Injected Version's index match with one of Released tagged
        // release's index, and then return the associated Release object
        for (Release releaseIndex : released) {
            if(releaseIndex.getIndex() == iv.intValue()){
                return releaseIndex;
            }
        }

        // If the injected version not match with of released tagged release
        // there was a problem, return a default errore Release object
        return new Release("NULL");
    }

    private float increment(List<Bug> validBugListForIncrement){
        // Do Increment Proportion Algorithm which use 'actualValidBugList' that it's the list
        // of valid bug calculate until proportion algorithm's call

        // Init the P variable to return for Injected Version calc
        float finalP = (float)0;

        // Init counter of valid bug which we use for calc P
        int pBugCounter = 0;

        // Printing check
        // System.err.println("\n  --------- Increment si basera' su i seguenti bug:");

        // Scroll through the valid bug list
        for (Bug validBugIncrement : validBugListForIncrement) {
            float currentP = pFormula(validBugIncrement);

            // Printing check
            //System.err.print("\n     PER IL BUG: " + validBugIncrement.getNameKey() + " P E'UGUALE A " + currentP);

            finalP = finalP + currentP;
            pBugCounter = pBugCounter + 1;
        }
        // System.err.println("  --------- Fine increment pe questo Bug\n");

        return finalP/pBugCounter;
    }

    private List<Float> coldStart() throws IOException, ParseException {
        BugRetriever gtb = new BugRetriever();
        List<Bug> validBug;
        List<Float> projectsP = new ArrayList<>();

        // Scroll through the project's names list
        for (String projectName : PROJECTODOCOLDSTART) {
            float projectP = (float)0;
            int pBugCounter = 0;

            // #NOTE-TO-THINKING-OF:
            //      Per effettuare cold start prendo tutte le release per il calcolo di p medio
            //      negli altri progetti e non la meta ?
            //
            validBug = gtb.getBug(projectName, true, false);

            for(Bug bugIndex : validBug){

                // Print every single p of valid bug not proportioned
                //System.err.println(bugIndex.getNameKey() + " | " + pFormula(bugIndex));

                // Else calc p for the current bug
                projectP = projectP + pFormula(bugIndex);
                pBugCounter = pBugCounter + 1;
            }

            // Store the project's calculated p (mean of all p of the current project) to a float array
            projectsP.add(projectP/pBugCounter);

            // Print current project's p
            System.err.print("\nCALCULATED P FOR PROJECT '" + projectName + "' ---> P: " + projectP/pBugCounter);
        }

        // return projects p
        return projectsP;
    }

    private float takeMedianPColdStart(List<Float> projectsP){
        // Order the p list in ascending order
        Collections.sort(projectsP);

        if (projectsP.size() % 2 == 1)
            return projectsP.get((projectsP.size() + 1) / 2 - 1);
        else {
            float lower = projectsP.get(projectsP.size() / 2 - 1);
            float upper = projectsP.get(projectsP.size() / 2);

            return (lower + upper) / 2;
        }
    }

    private float pFormula(Bug bugToCalculateP){
        // Do the p variable formula:
        //      P = (FV - IV) / (FV - OV)

        int numeratore, denominatore;

        numeratore = bugToCalculateP.getFixedVersions().getIndex() - bugToCalculateP.getInjectedVersion().getIndex();
        denominatore = bugToCalculateP.getFixedVersions().getIndex() - bugToCalculateP.getOpeningVersion().getIndex();

        // Check if FV it's equals to OV, if the subtraction is zero force (FV-OV)=1
        if(denominatore == 0){denominatore=1;}

        return (float) numeratore/denominatore;
    }

}
