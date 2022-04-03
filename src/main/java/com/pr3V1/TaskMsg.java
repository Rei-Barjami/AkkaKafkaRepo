package com.pr3V1;

import akka.actor.AbstractActor;
import akka.actor.Props;

import java.util.HashMap;

public class TaskMsg{
    private String name;
    private String resultDirectory;
    private int executionTimeSimulation;// this works as the payload, we pass the amount of time we think the execution will take,
    // for simulation purpose
    private int id;

    public String getName() {
        return name;
    }

    public TaskMsg(int time,int id){
        executionTimeSimulation = time;
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getResultDirectory() {
        return resultDirectory;
    }

    public void setResultDirectory(String resultDirectory) {
        this.resultDirectory = resultDirectory;
    }

    public int getExecutionTimeSimulation() {
        return executionTimeSimulation;
    }

    public void setExecutionTimeSimulation(int executionTimeSimulation) {
        this.executionTimeSimulation = executionTimeSimulation;
    }

    public int getId() {
        return id;
    }
}

