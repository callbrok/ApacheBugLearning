package model;

import java.io.File;
import java.util.List;

public class RepoFile {

    private String nameFile;
    private File pathOfFile;
    private List<Commit> relatedCommits;
    private Metrics fileMetrics;
    private Boolean itsBuggy;


    public RepoFile(String nameFile, File pathOfFile, List<Commit> relatedCommits, Metrics fileMetrics, Boolean itsBuggy){
        this.nameFile = nameFile;
        this.pathOfFile = pathOfFile;
        this.relatedCommits = relatedCommits;
        this.fileMetrics = fileMetrics;
        this.itsBuggy=itsBuggy;
    }


    // Setter
    public void setItsBuggy(Boolean itsBuggy) {this.itsBuggy = itsBuggy;}


    // Getter
    public File getPathOfFile() {return pathOfFile;}
    public List<Commit> getRelatedCommits() {return relatedCommits;}
    public Metrics getFileMetrics() {return fileMetrics;}
    public Boolean getItsBuggy() {return itsBuggy;}
}
