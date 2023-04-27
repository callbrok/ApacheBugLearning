package model;

import java.io.File;
import java.util.List;

public class RepoFile {

    private String nameFile;
    private File pathOfFile;
    private List<Commit> relatedCommits;
    private Metrics fileMetrics;


    public RepoFile(String nameFile, File pathOfFile, List<Commit> relatedCommits, Metrics fileMetrics){
        this.nameFile = nameFile;
        this.pathOfFile = pathOfFile;
        this.relatedCommits = relatedCommits;
        this.fileMetrics = fileMetrics;
    }

    //Getter
    public File getPathOfFile() {return pathOfFile;}
    public List<Commit> getRelatedCommits() {return relatedCommits;}
    public Metrics getFileMetrics() {return fileMetrics;}
}
