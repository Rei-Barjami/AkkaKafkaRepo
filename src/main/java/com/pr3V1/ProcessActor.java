package com.pr3V1;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Optional;
import java.util.Random;

public class ProcessActor extends AbstractActor {
    private String PATH = "/project3/server/";
    private ActorRef dispatcherRef=null;
    private TaskMsg currentlyHandled;
    @Override
    public Receive createReceive() {
        return receiveBuilder().match(TaskMsg.class, this::onTaskReceive).match(MsgRef.class, this::saveDispatcherRef).build();
    }

    //method called when the actor receives a Task from the dispatcher
    void onTaskReceive(TaskMsg msg) throws Exception {
        currentlyHandled = msg;
        try {   //sleep, simulate execution of task
            Thread.sleep(msg.getExecutionTimeSimulation());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Random rand = new Random();
        int j=rand.nextInt(100);
        if(j<5){
            System.out.println("THROWING EXCEPTION ON TASK: "+msg.getId());
            throw new Exception("Exception message");
        }

        createDir(msg);
        //send back to the dispatcher the result, telling also that the process is now idle and can execute another task
        dispatcherRef.tell(new ResultMsg(msg.getName(),msg.getId()), self());
    }

    public void preRestart(Throwable reason, Optional<Object> message) throws Exception {
        System.out.println("ENTRA IN PRE RESTART");
        dispatcherRef.tell(new RestartTask(currentlyHandled,self()),self());
        super.preRestart(reason,message);
    }

    private void saveDispatcherRef(MsgRef msg){
        this.dispatcherRef=msg.getRef();
    }

    private void createDir(TaskMsg msg) {

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
    public void postRestart(Throwable reason) throws Exception {
        System.out.println("HAS RESTARTED");
        super.postRestart(reason);
    }
    static Props props() {
        return Props.create(com.pr3V1.ProcessActor.class);
    }
}
