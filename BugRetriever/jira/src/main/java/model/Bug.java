package model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Bug {

    private String nameKey;
    private Date creationTicketDate;
    private Date resolutionTicketDate;
    private Release injectedVersion;
    private List<Release> affectedVersions;
    private Release openingVersion;
    private Release fixedVersion;
    private Boolean valid;  // Check if bug is valid or not, so discard it
    private Boolean propotionaled; // Check if IV of the bug it was calculated by proportion algorithm



    public Bug(String nameKey){this.nameKey = nameKey;}
    public Bug(){}



    public String getNameKey() {return nameKey;}
    public Date getCreationTicketDate() {return creationTicketDate;}
    public Date getResolutionTicketDate() {return resolutionTicketDate;}
    public Release getInjectedVersion() {return injectedVersion;}
    public List<Release> getAffectedVersions() {return affectedVersions;}
    public Release getFixedVersions() {return fixedVersion;}
    public Release getOpeningVersion() {return openingVersion;}
    public Boolean getValid(){return valid;}
    public Boolean getPropotionaled() {return propotionaled;}

    public void setBug(String nameKey, Date resolutionTicketDate, Date creationTicketDate, Release openingVersion, Release fixedVersion) {
        // The Bug is set to VALID by default
        this.valid=true;

        // Set propotionaled to False by default
        this.propotionaled=false;

        // Set key bug
        this.nameKey = nameKey;

        // Check integrity conditions, the bug will be discarded if respect at least one condition
        // explained in "validCheckerHelperFirst" method
        if(Boolean.FALSE.equals(validCheckerHelperFirst(openingVersion, fixedVersion))){
            this.valid = false;
            return;
        }

        // Take the first release tagged released after the resolution ticket date
        this.fixedVersion = fixedVersion;

        // Set opening version
        this.openingVersion = openingVersion;

        // Set resolution and creation date
        this.creationTicketDate = creationTicketDate;
        this.resolutionTicketDate = resolutionTicketDate;
    }


    public void setAffectedAndInjectedVersions(Release injectedVersion, List<Release> released){
        // If the first Affected Version Release, so the injected version passed, name is "DOPROPORTION", set it and return the bug
        // to do proportion later
        if(injectedVersion.getName().equals("DOPROPORTION")){this.affectedVersions = List.of(injectedVersion); return;}

        // Set Injected Version
        this.injectedVersion = injectedVersion;

        // Set Affected Versions Releases list
        this.affectedVersions = calculatedAffectedVersions(released);


        // Check integrity conditions, the bug will be discarded if respect at least one condition
        // explained in "validCheckerHelperSecond" method
        if(Boolean.FALSE.equals(validCheckerHelperSecond(this.openingVersion, this.fixedVersion, this.injectedVersion))){
            this.valid = false;
        }
    }

    private List<Release> calculatedAffectedVersions(List<Release> released){
        // Init Affected Version release object list
        List<Release> affectedVersionReleases = new ArrayList<>();

        // Retrieve affected version Release object list, releases between IV and OV
        // extremes included (IV - first element and OV - last element)
        for (Release releaseIndex : released) {
            if( (releaseIndex.getIndex() >= this.injectedVersion.getIndex()) && (releaseIndex.getIndex() <= this.openingVersion.getIndex()) ){
                affectedVersionReleases.add(releaseIndex);
            }
        }

        return affectedVersionReleases;
    }

    public void setPropotionaled(Boolean propotionaled) {this.propotionaled = propotionaled;}


    private Boolean validCheckerHelperFirst(Release openingVersion, Release fixedVersion){
        // Check integrity conditions, the bug will be discarded if:
        //      1. the Opening Version is missed
        //      2. the Fixed Version is missed
        //      3. the Opening Version Date > the Fixed Version Date\
        //      6. the Fixed Version and the Opening Versione are equals to 1, so the Injected Version it's forced
        //         to 1, and finally so FV = OV = IV

        // CONDITION 1
        if(openingVersion.getName().equals("NULL")){return false;}

        // CONDITION 2
        if(fixedVersion.getName().equals("NULL")){return false;}

        // CONDITION 3
        if(openingVersion.getReleaseDate().after(fixedVersion.getReleaseDate())){return false;}

        // CONDITION 6 or Return the positive valid condition
        return (fixedVersion.getIndex() != 1) || (openingVersion.getIndex() != 1);
    }

    private Boolean validCheckerHelperSecond(Release openingVersion, Release fixedVersion, Release injectedVersion){
        // Check integrity conditions, the bug will be discarded if:
        //      4. If the Fixed Version, Injected version and the Opening Version are equals, the bug is not valid
        //      5. If the Injected Version Date > the Opening Version Date
        //

        // CONDITION 4
        if(fixedVersion.getName().equals(injectedVersion.getName()) && injectedVersion.getName().equals(openingVersion.getName())){return false;}

        // CONDITION 5 or Return the positive valid condition
        return injectedVersion.getIndex() <= openingVersion.getIndex();
    }


}
