package model;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;

import java.io.File;

public class Repo {


    private final String apacheProjectName;
    private final String gitUrl;
    private File pathOfLocalRepo;
    private Repository jGitRepository;
    private Git gitHandle;
    private String prefixTagPath;


    public Repo(String apacheProjectName, String gitUrl){
        this.apacheProjectName = apacheProjectName;
        this.gitUrl = gitUrl;
    }

    // Getter
    public String getApacheProjectName() {return apacheProjectName;}
    public String getGitUrl() {return gitUrl;}
    public Repository getjGitRepository() {return jGitRepository;}
    public File getPathOfLocalRepo() {return pathOfLocalRepo;}
    public Git getGitHandle() {return gitHandle;}
    public String getPrefixTagPath() {return prefixTagPath;}


    // Setter
    public void setjGitRepository(Repository jGitRepository) {this.jGitRepository = jGitRepository;}
    public void setPathOfRepo(File pathOfRepo) {this.pathOfLocalRepo = pathOfRepo;}
    public void setGitHandle(Git gitHandle) {this.gitHandle = gitHandle;}
    public void setPrefixTagPath(String prefixTagPath) {this.prefixTagPath = prefixTagPath;}
}
