package model;

public class Metrics {

    private int Loc;
    private int LocTouched;
    private int nAuth;


    public Metrics(){}


    // Setter
    public void setLoc(int loc) {Loc = loc;}
    public void setLocTouched(int locTouched) {LocTouched = locTouched;}
    public void setnAuth(int nAuth) {this.nAuth = nAuth;}


    // Getter
    public int getLoc() {return Loc;}
    public int getLocTouched() {return LocTouched;}
    public int getnAuth() {return nAuth;}
}
