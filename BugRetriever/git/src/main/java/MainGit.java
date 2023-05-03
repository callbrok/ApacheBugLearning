import controller.FileRetriever;
import controller.JGitHelper;
import controller.ReleaseTagRetriever;
import model.*;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.json.JSONException;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class MainGit {
    private static final String PROJECT="BOOKKEEPER";

    public static void main(String[] args) throws IOException, JSONException, GitAPIException, ParseException {

        JGitHelper gtp = new JGitHelper();
        ReleaseTagRetriever gttr = new ReleaseTagRetriever();
        FileRetriever gtf = new FileRetriever();


        Repo repoToDoThinks = gtp.getJGitRepository(PROJECT);

        List<ReleaseTag> tagRelesesToDoThinks = gttr.makeTagReleasesList(repoToDoThinks);
        ReleaseTag previousTaggedRelease = new ReleaseTag();
        Boolean isFirst;


        // Set referenced files for every tagged release
        for (int i = 0; i < tagRelesesToDoThinks.size(); i++) {

            if(i==0){previousTaggedRelease=tagRelesesToDoThinks.get(i); isFirst=true;}
            else{previousTaggedRelease=tagRelesesToDoThinks.get(i-1); isFirst=false;}

            tagRelesesToDoThinks.get(i).setReferencedFilesList(gtf.getAllFilesOfTagRelease(tagRelesesToDoThinks.get(i), previousTaggedRelease, isFirst));
        }

        // Quindi ora ho tot oggetti ReleaseTag e ognuno ha associato la sua lista di file .java, di cui ognuno ha la sue lista
        // di commit riferiti che hanno un associazione con i bug di Jira


        // Set RepoFile's Bugginess
        List<ReleaseTag> tagRelesesWithBugginess = new ArrayList<>(tagRelesesToDoThinks);

        for(ReleaseTag rlsIndex : tagRelesesToDoThinks){
            for(RepoFile rpIndex : rlsIndex.getReferencedFilesList()){
                for(Commit cmIndex : rpIndex.getRelatedCommits()){
                    if(cmIndex.getCommitFromJira()!=null){
                        tagRelesesWithBugginess = gttr.setBugginess(tagRelesesWithBugginess, rpIndex.getNameFile(), cmIndex.getCommitFromJira().getAffectedVersions());
                    }
                }
            }
        }

        // Print dell'intera lista compilata
        for(ReleaseTag rlstIndex : tagRelesesWithBugginess){
            System.out.print("\n\n+----------------------------------------------------------------------------------------------------+\n" +
                    "+                             RELEASE REFERED BY TAG: " + rlstIndex.getGitTag()  +
                    "\n+----------------------------------------------------------------------------------------------------+\n\n");

            for(RepoFile rpfIndex : rlstIndex.getReferencedFilesList()){
                System.out.print("\n\n+ FILE: " + rpfIndex.getPathOfFile());
                System.out.print("\n+ NELLA RELEASE CON TAG: " + rlstIndex.getGitTag());
                System.out.print("\n+ BUGGINESS: " + rpfIndex.getItsBuggy());
                System.out.print("\n+ POSSIEDE I SEGUENTI [" + rpfIndex.getRelatedCommits().size() + "] COMMIT:");

                for(Commit comIndex : rpfIndex.getRelatedCommits()){
                    if(rpfIndex.getRelatedCommits().isEmpty()){System.out.print("  NESSUN COMMIT ASSEGNATO"); continue;}
                    if(comIndex.getCommitFromJira() != null){System.out.print("\n   JIRA-| " + comIndex.getCommitFromJira().getNameKey());continue;}
                    System.out.print("\n    GIT-| " + comIndex.getCommitFromGit().getShortMessage());
                }

                System.out.print("\n+ METRICHE:");
                System.out.print("\n+    LOC              -| " + rpfIndex.getFileMetrics().getLoc());
                System.out.print("\n+    LOC_ADDED        -| " + rpfIndex.getFileMetrics().getLocAdded());
                System.out.print("\n+    LOC_MAX_ADDED    -| " + rpfIndex.getFileMetrics().getLocMaxAdded());
                System.out.print("\n+    LOC_TOUCHED      -| " + rpfIndex.getFileMetrics().getLocTouched());
                System.out.print("\n+    N_REVISION       -| " + rpfIndex.getFileMetrics().getnRevision());
                System.out.print("\n+    AVG_LOC_ADDED    -| " + rpfIndex.getFileMetrics().getAverageLocAdded());
                System.out.print("\n+    N_AUTHORS        -| " + rpfIndex.getFileMetrics().getnAuth());
                System.out.print("\n+    CHURN            -| " + rpfIndex.getFileMetrics().getChurn());
                System.out.print("\n+    MAX_CHURN        -| " + rpfIndex.getFileMetrics().getMaxChurn());
                System.out.print("\n+    AVG_CHURN        -| " + rpfIndex.getFileMetrics().getAverageChurn());
            }
        }

        System.out.println("\n\nNUMERO COMMIT TOTALI: ");


        // After all close current git handle and delete temp cloned repository
        gtp.deleteRepository(repoToDoThinks);
    }


}
