package com.pr3V1;

//msg used to notify that the task has been executed succesfully
public class ResultMsg {
    private String type;
    private int id;

    public ResultMsg(String type,int id){
        this.type= type;
        this.id=id;
    }

    public String getType() {
        return type;
    }


    public int getId() {
        return id;
    }
}
