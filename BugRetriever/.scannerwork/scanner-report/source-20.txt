package model;

import java.io.File;
import java.util.List;

public class RepoFile {

    private String nameFile;
    private File pathOfFile;
    private List<Commit> relatedCommits;


    public RepoFile(String nameFile, File pathOfFile, List<Commit> relatedCommits){
        this.nameFile = nameFile;
        this.pathOfFile = pathOfFile;
        this.relatedCommits = relatedCommits;
    }

    //Getter
    public File getPathOfFile() {return pathOfFile;}
    public List<Commit> getRelatedCommits() {return relatedCommits;}

}
