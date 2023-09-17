package controller;

import model.Bug;
import model.Release;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Proportion {
    private static final Logger LOGGER = Logger.getLogger( Proportion.class.getName() );

    private static final Boolean APROXIMATE=true;

    private static final int THRESHOLD=5;
    private static final List<String> PROJECTODOCOLDSTART = List.of("TAJO","STORM","SYNCOPE","ZOOKEEPER","OPENJPA","AVRO");
    private static final Boolean SETTALLP = false;



    public List<Bug> proportionAlgoHelper(List<Bug> actualValidBugList, List<Release> released) throws IOException, ParseException {
        // Calcola la injected version qui
        // A seconda della situazione capisci quale metodo per calcolare p scegliere

        // ------------------------------------------------------------------------------------------------------

        // Init p to calculate the injected version
        float p = 0;

        // Init list for proportion increment checker
        List<Bug> bugToCheckIncrement;


        // Scroll through the valid bug list
        for (Bug validBugP : actualValidBugList) {

            // If the bug is not flagged to do proportion skip it
            if(!validBugP.getAffectedVersions().get(0).getName().equals("DOPROPORTION")){continue;}

            // Init p for indicated projects for proportion cold start approach
            List<Float> projectsP = coldStart(validBugP.getCreationTicketDate());

            bugToCheckIncrement = incrementChecker(actualValidBugList, validBugP);

            // Calculate the number of bug valid for do proportion increment, and check if it's larger than
            // threshold constant (THRESHOLD = 5)
            //
            if(bugToCheckIncrement.size() > THRESHOLD){
                LOGGER.log(Level.INFO, ("\n\nSCATTATO INCREMENT PER IL BUG --> " + validBugP.getNameKey() + "\n"));
                p = increment(bugToCheckIncrement);
            }else{
                LOGGER.log(Level.INFO, ("\n\nSCATTATO COLD START PER IL BUG --> " + validBugP.getNameKey() + "\n"));
                p = takeMedianPColdStart(projectsP);
            }

            // Calculate Injected Version with P and proportion algorithm
            Release injectedVersion = getInjectedVersionFromP(p, validBugP, released);

            // Set calculated affected version by proportion algorithms
            validBugP.setAffectedAndInjectedVersions(injectedVersion, released);

            // Print checking
            LOGGER.log(Level.INFO, ("\nCALCULATED AV: "));
            for(Release rlsIndex : validBugP.getAffectedVersions()){LOGGER.log(Level.INFO, (rlsIndex.getIndex() + " "));}
            LOGGER.log(Level.INFO, ("\n-------------------\n\n"));

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

        for (Bug validBugP : actualValidBugList) {
            if(validBugP.getFixedVersions().getIndex() > currrentBug.getOpeningVersion().getIndex() || validBugP.getAffectedVersions().get(0).getName().equals("DOPROPORTION") || Boolean.TRUE.equals(currrentBug.getPropotionaled())){continue;}
            bugToDoIncrement.add(validBugP);
        }

        return bugToDoIncrement;
    }


    private Release getInjectedVersionFromP(float p, Bug currBugToCalcProportion, List<Release> released){
        // Do the IV proportion formula:
        //      IV = FV - (FV - OV) * P
        // and return the associated Release Object

        float injectedVersionIndex;

        // If Opening Version it's equals to Fixed Version set (FV-OV)=1
        int subFvOv = currBugToCalcProportion.getFixedVersions().getIndex() - currBugToCalcProportion.getOpeningVersion().getIndex();

        if(subFvOv == 0){
            injectedVersionIndex = currBugToCalcProportion.getFixedVersions().getIndex() - (1 * p);
        }else{injectedVersionIndex = currBugToCalcProportion.getFixedVersions().getIndex() - (subFvOv * p);}


        if(injectedVersionIndex<1){injectedVersionIndex=1;}

        BigDecimal iv = BigDecimal.valueOf(injectedVersionIndex);

        // Approximate 'injectedVersionIndex' by defect
        if(Boolean.FALSE.equals(APROXIMATE)){iv = iv.setScale(0, RoundingMode.HALF_DOWN);}
        // Approximate 'injectedVersionIndex' by excess
        if(Boolean.TRUE.equals(APROXIMATE)){iv = iv.setScale(0, RoundingMode.HALF_UP);}


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
        float finalP = 0;

        // Init counter of valid bug which we use for calc P
        int pBugCounter = 0;

        // Printing check

        // Scroll through the valid bug list
        for (Bug validBugIncrement : validBugListForIncrement) {
            float currentP = pFormula(validBugIncrement);

            // Printing check

            finalP = finalP + currentP;
            pBugCounter = pBugCounter + 1;
        }

        if(pBugCounter==0){pBugCounter=1;}

        return finalP/pBugCounter;
    }

    private List<Float> coldStart(Date creationTicket) throws IOException, ParseException {
        BugRetriever gtb = new BugRetriever();
        List<Bug> validBug;
        List<Float> projectsP = new ArrayList<>();

        // Scroll through the project's names list
        for (String projectName : PROJECTODOCOLDSTART) {
            float projectP = 0;
            int pBugCounter = 0;


            ReleaseRetriever rls = new ReleaseRetriever();
            validBug = gtb.getBug(projectName, true, rls.getReleaseFromProject(projectName, false, "ALL"));

            for(Bug bugIndex : validBug){

                // Print every single p of valid bug not proportioned
                if(Boolean.FALSE.equals(SETTALLP) && bugIndex.getCreationTicketDate().after(creationTicket)){continue;}

                // Else calc p for the current bug
                projectP = projectP + pFormula(bugIndex);
                pBugCounter = pBugCounter + 1;
            }

            if(pBugCounter==0){pBugCounter=1;}
            // Store the project's calculated p (mean of all p of the current project) to a float array
            projectsP.add(projectP/pBugCounter);

            // Print current project's p
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

        int numeratore;
        int denominatore;

        numeratore = bugToCalculateP.getFixedVersions().getIndex() - bugToCalculateP.getInjectedVersion().getIndex();
        denominatore = bugToCalculateP.getFixedVersions().getIndex() - bugToCalculateP.getOpeningVersion().getIndex();

        // Check if FV it's equals to OV, if the subtraction is zero force (FV-OV)=1
        if(denominatore == 0){denominatore=1;}

        return (float) numeratore/denominatore;
    }

}