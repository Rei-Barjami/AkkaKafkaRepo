package com.pr3V1;

//msg used to notify that the task has been executed succesfully
public class ResultMsg {
    private String type;
    private String id;

    public ResultMsg(String type,String id){
        this.type= type;
        this.id=id;
    }

    public String getType() {
        return type;
    }


    public String getId() {
        return id;
    }
}
