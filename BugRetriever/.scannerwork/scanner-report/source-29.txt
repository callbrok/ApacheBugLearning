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


    public Metrics(int loc, int locTouched, int nAuth, int locAdded, int locMaxAdded, int nRevision,
                   float averageLocAdded, int churn, int maxChurn, float averageChurn, int nPublicMethods,
                   int nPrivateMethods, int nStaticMethods, int nMethods, int nCommentedLines){

        this.loc = loc;
        this.locTouched = locTouched;
        this.nAuth = nAuth;
        this.locAdded = locAdded;
        this.locMaxAdded = locMaxAdded;
        this.nRevision = nRevision;
        this.averageLocAdded = averageLocAdded;
        this.churn = churn;
        this.maxChurn = maxChurn;
        this.averageChurn = averageChurn;

        // Me Metrics
        this.nPublicMethods = nPublicMethods;
        this.nPrivateMethods = nPrivateMethods;
        this.nStaticMethods = nStaticMethods;
        this.nMethods = nMethods;
        this.nCommentedLines = nCommentedLines;
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
