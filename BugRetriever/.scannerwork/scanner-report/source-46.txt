package model;

import java.util.Date;

public class Release {

    private int id;
    private int index;
    private String name;
    private Date releaseDate;


    //getter
    public int getId() {return id;}
    public int getIndex() {return index;}
    public String getName(){return name;}
    public Date getReleaseDate() {return releaseDate;}


    //setter
    public void setIndex(int index){this.index=index;}

    public Release(int id, String name, Date releaseDate){
        this.id=id;
        this.name=name;
        this.releaseDate=releaseDate;
    }

    public Release(String name){this.name=name;}
}
