package model;

import java.io.File;
import java.util.List;

public class RepoFile {

    private String nameFile;
    private String pathOfFile;
    private List<Commit> relatedCommits;
    private Metrics fileMetrics;
    private Boolean itsBuggy;


    public RepoFile(String nameFile, String pathOfFile, List<Commit> relatedCommits, Metrics fileMetrics, Boolean itsBuggy){
        this.nameFile = nameFile;
        this.pathOfFile = pathOfFile;
        this.relatedCommits = relatedCommits;
        this.fileMetrics = fileMetrics;
        this.itsBuggy=itsBuggy;
    }

    public RepoFile(String nameFile, String pathOfFile, Boolean itsBuggy){
        this.nameFile = nameFile;
        this.pathOfFile = pathOfFile;
        this.itsBuggy=itsBuggy;
    }


    // Setter
    public void setItsBuggy(Boolean itsBuggy) {this.itsBuggy = itsBuggy;}
    public void setRelatedCommits(List<Commit> relatedCommits) {this.relatedCommits = relatedCommits;}
    public void setFileMetrics(Metrics fileMetrics) {this.fileMetrics = fileMetrics;}


    // Getter
    public String getPathOfFile() {return pathOfFile;}
    public List<Commit> getRelatedCommits() {return relatedCommits;}
    public Metrics getFileMetrics() {return fileMetrics;}
    public Boolean getItsBuggy() {return itsBuggy;}
    public String getNameFile() {return nameFile;}


    public String getItsBuggyCSV(){
        if(itsBuggy){return "YES";}
        return "NO";
    }
}
