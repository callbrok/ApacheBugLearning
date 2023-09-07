package model;

public class Metrics {

    private int loc;
    private int locAdded;
    private int locMaxAdded;
    private int locTouched;
    private int nRevision;
    private float averageLocAdded;
    private int nAuth;
    private int churn;
    private int maxChurn;
    private float averageChurn;

    // Added by Me
    private int nPublicMethods;
    private int nPrivateMethods;
    private int nStaticMethods;
    private int nMethods;
    private int nCommentedLines;


    public Metrics(){
        // Init metrics object
    }


    // Setter
    public void setLoc(int loc) {this.loc = loc;}
    public void setLocTouched(int locTouched) {this.locTouched = locTouched;}
    public void setnAuth(int nAuth) {this.nAuth = nAuth;}
    public void setLocAdded(int locAdded) {this.locAdded = locAdded;}
    public void setLocMaxAdded(int locMaxAdded) {this.locMaxAdded = locMaxAdded;}
    public void setnRevision(int nRevision) {this.nRevision = nRevision;}
    public void setAverageLocAdded(float averageLocAdded) {this.averageLocAdded = averageLocAdded;}
    public void setChurn(int churn) {this.churn = churn;}
    public void setMaxChurn(int maxChurn) {this.maxChurn = maxChurn;}
    public void setAverageChurn(float averageChurn) {this.averageChurn = averageChurn;}

    public void setnPublicMethods(int nPublicMethods) {this.nPublicMethods = nPublicMethods;}
    public void setnPrivateMethods(int nPrivateMethods) {this.nPrivateMethods = nPrivateMethods;}
    public void setnStaticMethods(int nStaticMethods) {this.nStaticMethods = nStaticMethods;}
    public void setnMethods(int nMethods) {this.nMethods = nMethods;}
    public void setnCommentedLines(int nCommentedLines) {this.nCommentedLines = nCommentedLines;}


    // Getter
    public int getLoc() {return loc;}
    public int getLocTouched() {return locTouched;}
    public int getnAuth() {return nAuth;}
    public int getLocAdded() {return locAdded;}
    public int getLocMaxAdded() {return locMaxAdded;}
    public int getnRevision() {return nRevision;}
    public float getAverageLocAdded() {return averageLocAdded;}
    public int getChurn() {return churn;}
    public int getMaxChurn() {return maxChurn;}
    public float getAverageChurn() {return averageChurn;}

    public int getnPublicMethods() {return nPublicMethods;}
    public int getnPrivateMethods() {return nPrivateMethods;}
    public int getnStaticMethods() {return nStaticMethods;}
    public int getnMethods() {return nMethods;}
    public int getnCommentedLines() {return nCommentedLines;}
}
