package com.pr3V1;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.ex2.SeverActor;

import java.util.ArrayList;
import java.util.Stack;

public class DispatcherActor extends AbstractActor {
    Stack<ActorRef> processes;
    Stack<ActorRef> freeProcesses;
    private boolean canRun;
    private ActorRef extractor;
    int nProcesses = 4;

    //create processor actors and add them to the stack of idle processors(initially they execute nothing)
    public DispatcherActor(){
        freeProcesses = new Stack<ActorRef>();
        final ActorSystem sys = ActorSystem.create("System");
        final ActorRef process1 = sys.actorOf(ProcessActor.props(), "process1");
        final ActorRef process2 = sys.actorOf(ProcessActor.props(), "process2");
        final ActorRef process3 = sys.actorOf(ProcessActor.props(), "process3");
        final ActorRef process4 = sys.actorOf(ProcessActor.props(), "process4");
        freeProcesses.push(process1);
        freeProcesses.push(process2);
        freeProcesses.push(process3);
        freeProcesses.push(process4);
    }

    @Override
    public Receive createReceive() {
        return canExecute();
    }

    private final Receive canExecute(){
        return receiveBuilder().match(ResultMsg.class, this::onResultReceive).match(TaskMsg.class,this::executeTask).build();
    }

    //do not accept execute messages if no idle processor is present
    private final Receive cannotExecute(){
        return receiveBuilder().match(ResultMsg.class, this::onResultReceive).match(TaskMsg.class, this::notExecutable).build();
    }

    //enter here after receiving a valid result, at least one process becomes idle so pass in canExecute state
    public void onResultReceive(ResultMsg msg){
        boolean noIdleBefore=false;
        if(freeProcesses.size()==0)
            noIdleBefore=true;

        freeProcesses.push(sender()); // add the process that has just finished execution to the free ones

        getContext().become(canExecute());

        if (noIdleBefore)
            extractor.tell(new StartMsg(),self());

        System.out.println("executed task with id: "+msg.getId()+"   in processor n: "+sender().path().name());
        // ADD CODE TO TELL THE CLIENT THAT EXECUTION WAS DONE

    }


    //if no idle processor is present put the actor in cannotExecute state
    public void executeTask(TaskMsg msg){
        this.extractor = sender();
        ActorRef process= freeProcesses.pop();
        if(freeProcesses.size()<=0) {
            System.out.println("no idle processor");
            sender().tell(new TaskDispatchedMsg(true),self());
            getContext().become(cannotExecute());
        }
        else {
            sender().tell(new TaskDispatchedMsg(false),self());
            process.tell(msg, self());
        }
    }

    //when a task arrives but it is not executable since there is no idle processor (so cannotExecute state)
    public void notExecutable(TaskMsg msg){
        //ADD THE TASK IN A QUEUE, SO TO EXECUTE IT WHEN A PROCESSOR IS IDLE?? USE STASH??
        System.out.println("task not executable now");
    }

    static Props props() {
        return Props.create(com.pr3V1.DispatcherActor.class);
    }
}
