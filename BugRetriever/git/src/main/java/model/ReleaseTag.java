package model;

import java.util.List;

public class ReleaseTag {

    private String gitTag;
    private String refObjectIdGetName; // The value of this ref at the last time we read it.
    private Release releaseFromJira;
    private Repo currentRepo;
    private List<RepoFile> referencedFilesList;


    public ReleaseTag(String gitTag, Release releaseFromJira, Repo currentRepo, String refObjectIdGetName){
        this.gitTag = gitTag;
        this.releaseFromJira = releaseFromJira;
        this.currentRepo = currentRepo;
        this.refObjectIdGetName = refObjectIdGetName;
    }

    public ReleaseTag(){}


    //Getter
    public Repo getCurrentRepo() {return currentRepo;}
    public String getGitTag() {return gitTag;}
    public List<RepoFile> getReferencedFilesList() {return referencedFilesList;}
    public String getRefObjectIdGetName() {return refObjectIdGetName;}


    // Setter
    public void setReferencedFilesList(List<RepoFile> referencedFilesList) {this.referencedFilesList = referencedFilesList;}
}
