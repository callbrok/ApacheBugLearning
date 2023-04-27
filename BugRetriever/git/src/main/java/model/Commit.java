package model;

import org.eclipse.jgit.revwalk.RevCommit;

public class Commit {


    private Bug commitFromJira;
    private RevCommit commitFromGit;

    public Commit(Bug jiraBug, RevCommit commitFromGit){
        this.commitFromJira = jiraBug;
        this.commitFromGit = commitFromGit;
    }


    // Getter
    public Bug getCommitFromJira() {return commitFromJira;}
}
