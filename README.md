
# ApacheBugLearning

ApacheBugLearning is a Java project structured in Maven modules, capable of retrieving all commits of any Java project from the Apache Software Foundation, linked with related Jira tickets, and through machine learning predicting classes affected by BUGs.



### _git_ Module

The _git_ module takes care, through the use of the Eclipse [JGit project](https://www.eclipse.org/jgit/), of retrieving all the conmmits, related releases and files inherent to each commit of each release.

The source repository can be pre-existing locally, thus indentified by an absolute path or be cloaned locally automatically, indicating the choice within the configuration .properties file.The projects that can be cloned automatically are: **TAJO**, **STORM**, **SYNCOPE**, **ZOOKEEPER**, **OPENJPA**, **AVRO** and **BOOKKEEPER**.

During reading, several metrics are calculated for each file and entered into a .csv file. The metrics used:

|     **Metric**     |             **Description**            |
|:------------------:|:--------------------------------------:|
|         `LOC`        | Number of lines of code                |
|      `LOC ADDED`     | Sum of LOCs added                      |
|    `LOC MAX ADDED`   | Maximum LOCs added                     |
|     `LOC TOUCHED`    | Sum of LOCs added and LOCs removed     |
| `NUMBER OF REVISION` | Number of revisions of the class       |
|  `AVERAGE LOC ADDED` | Average number of LOCs added           |
|  `NUMBER OF AUTHORS` | Number of reviewers of the class       |
|        `CHURN`       | \| LOC added - LOC removed \|          |
|      `MAX CHURN`     | Maximum CHURN                          |
|    `AVERAGE CHURN`   | Average CHURN                          |
|         `NPM`       | Number of public methods in the class  |
|        `NPVM`        | Number of private methods in the class |
|         `NSM`        | Number of static methods in the class  |
|         `NAM`        | Number of methods in the class         |
|        `NLOCM`       | Number of commented lines              |


Project test files are discarded; you can un-discard them by setting the flag in the configuration file.

### _jira_ Module

The _jira_ module deals with the management of the data recuoperated through the git module, in particular, it deals with associating each commit (object of type RevCommit) with the related Jira ticket, associating the commits and calculated metrics with the related files, and associating the files with the reference relaease. 

Ticket information is retrieved in groups of 1000 tickets, via the Jira REST API, using the query:

```
"https://issues.apache.org/jira/rest/api/2/search?jql=project%20%3D%20" + projectName +
"%20AND%20issuetype%20%3D%20Bug%20AND%20(%22status%22%20%3D%22resolved%22%20OR%20%22status" +
"%22%20%3D%20%22closed%22)%20AND%20%20%22resolution%22%20%3D%20%22fixed%22%20" + "%20ORDER%20BY%20key%20ASC" +
"&fields=key,resolutiondate,versions,created,fixVersions&startAt=" + i + "&maxResults=" + j
```

All tickets that refer to BUGs, and that have been closed as a result of the Bug fix, are considered. The variables `i` and `j` are used to retrieve tickets in groups of 1000 items.

A separate entity has been created for each of these items:

`commit` → **Bug** Entity

`release` → **Release** Entity

`file` → **RepoFile** Entity

`repository` → **Repo** Entity

Inconsistent tickets are discarded and if not present the injection version, necessary for class labeling, is calculated using proportion techniques (link paper). Under a threshold, set within the configuration file, proportion Cold Start is used otherwise porportion Increment.

### _dataset_ Module

The _dataset_ module deals with the prediction of Bugginness of classes, using the training set constructed with the previous modules and as a Machine Laerning tool [WEKA](https://www.cms.waikato.ac.nz/ml/weka/).

The linear combination of different techniques and classifiers was used for prediction. The **Classifiers** used were `Random Forest`, `Naive Bayes`, `IBk` in combination with **Feature Selection** `Best First` techniques (Backward Search and Forward Search), **Sampling** `Oversampling`, `Undersampling` and `SMOTE` techniques, and **Cost Sensitive** `Threshold` and `Learning` techniques.

For the evaluation of the classifiers, the **Walk Forward** technique was used; all the given data were divided into k Releases, ordered chronologically, and for each release a run was performed. At the k-th run, the k-th release was used as the testing set and all previous releases as the training set.

For each classifier used, a .csv file is generated containing for each combination of techniques used the values related to prediction:

|   **Metric**   |           **Description**          |
|:--------------:|:----------------------------------:|
|   `%-TRAINING`   | Number of lines of code            |
|    `PRECISION`   | Sum of LOCs added                  |
|     `RECALL`     | Maximum LOCs added                 |
|       `AUC`      | Sum of LOCs added and LOCs removed |
|      `KAPPA`     | Number of revisions of the class   |
|    `ACCURACY`    | Average number of LOCs added       |
|  `TRUE-NEGATIVE` | Number of reviewers of the class   |
|  `TRUE-POSITIVE` | \| LOC added - LOC removed \|      |
| `FALSE-POSITIVE` | Maximum CHURN                      |
| `FALSE-NEGATIVE` | Average CHURN                      |



## Configuration File

The configuration.properties file was used for configuration. The parameters are:
## Output Files
## Presentation :it:

An [expository presentation]() of the project has been created in Italian, preview images below.
