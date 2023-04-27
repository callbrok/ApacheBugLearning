package model;

import java.util.List;

public class ReleaseTag {

    private String gitTag;
    private Release releaseFromJira;
    private Repo currentRepo;
    private List<RepoFile> referencedFilesList;


    public ReleaseTag(String gitTag, Release releaseFromJira, Repo currentRepo){
        this.gitTag = gitTag;
        this.releaseFromJira = releaseFromJira;
        this.currentRepo = currentRepo;
    }

    public ReleaseTag(){}


    //Getter
    public Repo getCurrentRepo() {return currentRepo;}
    public String getGitTag() {return gitTag;}
    public List<RepoFile> getReferencedFilesList() {return referencedFilesList;}

    // Setter
    public void setReferencedFilesList(List<RepoFile> referencedFilesList) {this.referencedFilesList = referencedFilesList;}
}
