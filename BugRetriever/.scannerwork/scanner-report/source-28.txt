package model;

import org.eclipse.jgit.revwalk.RevCommit;

public class Commit {


    private Bug commitFromJira;
    private RevCommit commitFromGit;

    public Commit(Bug jiraBug, RevCommit commitFromGit){
        this.commitFromJira = jiraBug;
        this.commitFromGit = commitFromGit;
    }

    public Commit(){
        this.commitFromJira = null;
        this.commitFromGit = null;
    }

    public Commit(RevCommit commitFromGit){
        this.commitFromGit = commitFromGit;
    }


    // Setter
    public void setCommitFromGit(RevCommit commitFromGit) {this.commitFromGit = commitFromGit;}
    public void setCommitFromJira(Bug commitFromJira) {this.commitFromJira = commitFromJira;}


    // Getter
    public Bug getCommitFromJira() {return commitFromJira;}
    public RevCommit getCommitFromGit() {return commitFromGit;}
}
