package controller;

import model.*;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommitRetriever {

    public List<ReleaseTag> listCommitRetriever(List<ReleaseTag> tagRelesesToDoThinks, HashMap<String, List<RevCommit>> commitsMap, Repo repoToDoThinks, List<Bug> bugList) throws IOException {
        Repository repository = repoToDoThinks.getjGitRepository();
        Git git = repoToDoThinks.getGitHandle();

        // For each file
        for(ReleaseTag rltIndex : tagRelesesToDoThinks){

            // For each commit
            for(RevCommit cmInd : commitsMap.get(rltIndex.getReleaseFromJira().getName())){

                // Do think if current commit has parent
                if(cmInd.getParentCount()>0){

                    DiffFormatter formatter = new DiffFormatter(DisabledOutputStream.INSTANCE);
                    formatter.setRepository(git.getRepository());
                    formatter.setDiffComparator(RawTextComparator.DEFAULT);
                    formatter.setDetectRenames(true);

                    ObjectId commitId = cmInd.getId();
                    RevCommit parent = cmInd.getParent(0);

                    if (parent != null) {
                        ObjectId parentId = parent.getId();
                        List<DiffEntry> diffs = formatter.scan(parentId, commitId);

                        for (DiffEntry diff : diffs) {

                            // For each file list of current ReleaseTag
                            for (RepoFile rpfIndex : rltIndex.getReferencedFilesList()) {

                                if (rpfIndex.getPathOfFile().equals(diff.getNewPath())) {

                                    List<Commit> tempCommitList = new ArrayList<>(rpfIndex.getRelatedCommits());

                                    // Insert commit and set list
                                    tempCommitList.add(new Commit(cmInd));
                                    rpfIndex.setRelatedCommits(tempCommitList);

                                    break;
                                }
                            }

                        }
                    }

                }

            }
        }

        return tagRelesesToDoThinks;
    }


    public Bug commitJiraGitLinker(RevCommit commit, List<Bug> bugList){

        // Scroll all valid bug and find match with passed commit
        for(Bug validBugIndex: bugList){
            // Setting regex for match the exact commit
            final Pattern pattern = Pattern.compile(validBugIndex.getNameKey()  + "(?!\\d)");
            final Matcher matcher = pattern.matcher(commit.getShortMessage());

            // If commit's message contains bug name like 'BOOKKEEPER-46', there is a match
            if(matcher.find()){return validBugIndex;}

        }

        // If there isn't match, return a default bug error
        return new Bug("NOMATCH");
    }


    public HashMap<String, List<RevCommit>> mapCommitsByRelease(Repo currentRepo, List<Release> listOfJiraReleases) throws IOException, GitAPIException {
        // Init RevCommit
        RevCommit commitPrevTag = null;
        RevCommit commitCurrTag = null;

        // Init HashMap
        HashMap<String, List<RevCommit>> commitsMap = new HashMap<String, List<RevCommit>>();

        // Retrieve all commits of the passed project repo
        Iterable<RevCommit> commits = currentRepo.getGitHandle().log().all().call();
        List<RevCommit> allCommits = new ArrayList<>();
        for(RevCommit cmt : commits){allCommits.add(cmt);}

        // Reverse commit list
        Collections.reverse(allCommits);


        for(int k=0; k<listOfJiraReleases.size(); k++){
            // Take data of current Tag
            Ref currentTag = currentRepo.getGitHandle().getRepository().exactRef(
                    currentRepo.getPrefixTagPath() + listOfJiraReleases.get(k).getName()
            );
            RevWalk walkCurrentTag = new RevWalk(currentRepo.getjGitRepository());
            commitCurrTag = walkCurrentTag.parseCommit(currentTag.getObjectId());

            if(k!=0){
                // Take data of previous Tag
                Ref prevTag = currentRepo.getGitHandle().getRepository().exactRef(
                        currentRepo.getPrefixTagPath() + listOfJiraReleases.get(k-1).getName()
                );
                RevWalk walkPrevTag = new RevWalk(currentRepo.getjGitRepository());
                commitPrevTag = walkPrevTag.parseCommit(prevTag.getObjectId());
            }

            // Init temp list of Commit objects
            List<RevCommit> tempRevCommitList = new ArrayList<>();

            for(RevCommit commit : allCommits) {

                // If it's the first Release
                if(k==0){
                    if(commit.getAuthorIdent().getWhen().before(commitCurrTag.getAuthorIdent().getWhen())){tempRevCommitList.add(commit);}
                    continue;
                }

                if(commit.getAuthorIdent().getWhen().after(commitPrevTag.getAuthorIdent().getWhen()) &&
                        commit.getAuthorIdent().getWhen().before(commitCurrTag.getAuthorIdent().getWhen())){

                    tempRevCommitList.add(commit);
                }

            }

            commitsMap.put(listOfJiraReleases.get(k).getName(), tempRevCommitList);
        }

        return commitsMap;
    }

}
