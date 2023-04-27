import controller.FileRetriever;
import controller.ReleaseTagRetriever;
import controller.RepoHelper;
import model.Commit;
import model.ReleaseTag;
import model.Repo;
import model.RepoFile;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.json.JSONException;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

public class MainGit {
    private static final String PROJECT="BOOKKEEPER";

    public static void main(String[] args) throws IOException, JSONException, GitAPIException, ParseException {

        RepoHelper gtp = new RepoHelper();
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


        // Print dell'intera lista compilata
        for(ReleaseTag rlstIndex : tagRelesesToDoThinks){
            System.out.print("\n\n+----------------------------------------------------------------------------------------------------+\n" +
                    "+                             RELEASE REFERED BY TAG: " + rlstIndex.getGitTag()  +
                    "\n+----------------------------------------------------------------------------------------------------+\n\n");

            for(RepoFile rpfIndex : rlstIndex.getReferencedFilesList()){
                System.out.print("\n\n+ FILE: " + rpfIndex.getPathOfFile());
                System.out.print("\n+ NELLA RELEASE CON TAG: " + rlstIndex.getGitTag());
                System.out.print("\n+ POSSIEDE I SEGUENTI COMMIT:");

                for(Commit comIndex : rpfIndex.getRelatedCommits()){
                    if(rpfIndex.getRelatedCommits().isEmpty()){System.out.print("  NESSUN COMMIT ASSEGNATO"); continue;}
                    if(comIndex.getCommitFromJira() != null){System.out.print("\n   JIRA-| " + comIndex.getCommitFromJira().getNameKey());continue;}
                    System.out.print("\n    GIT-| " + comIndex.getCommitFromGit().getShortMessage());
                }
            }
        }


        // After all close current git handle and delete temp cloned repository
        gtp.deleteRepository(repoToDoThinks);
    }


}
