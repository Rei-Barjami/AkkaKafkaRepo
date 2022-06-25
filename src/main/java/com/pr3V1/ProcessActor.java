package com.pr3V1;

import akka.actor.AbstractActor;
import akka.actor.Props;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ProcessActor extends AbstractActor {
    private String PATH = "/project3/server/";
    @Override
    public Receive createReceive() {
        return receiveBuilder().match(TaskMsg.class, this::onTaskReceive).build();
    }


    //method called when the actor receives a Task from the dispatcher
    void onTaskReceive(TaskMsg msg) {

        try {   //sleep, simulate execution of task
            Thread.sleep(msg.getExecutionTimeSimulation());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        createDir(msg);
        //send back to the dispatcher the result, telling also that the process is now idle and can execute another task
        sender().tell(new ResultMsg(msg.getName(),msg.getId()), self());
    }
    private void createDir(TaskMsg msg){
        String directoryName = PATH.concat(msg.getResultDirectory());
        File directory = new File(directoryName);
        if (! directory.exists()){
            directory.mkdirs();
            // If you require it to make the entire directory path including parents,
            // use directory.mkdirs(); here instead.
        }
        File file = new File(directoryName + "/" + msg.getId());
        try{
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write("result of process with id: "+msg.getId());
            bw.close();
        }
        catch (IOException e){
            e.printStackTrace();
            System.exit(-1);
        }

    }
    static Props props() {
        return Props.create(com.pr3V1.ProcessActor.class);
    }

}
