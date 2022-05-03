package com.pr3V1;

import akka.actor.AbstractActor;
import akka.actor.Props;

public class ProcessActor extends AbstractActor {

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(TaskMsg.class, this::onTaskReceive).build();
    }


    //method called when the actor receives a Task from the dispatcher
    void onTaskReceive(TaskMsg msg) {
        System.out.println("executing task : " + msg.getName());

        try {   //sleep, simulate execution of task
            Thread.sleep(msg.getExecutionTimeSimulation());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //send back to the dispatcher the result, telling also that the process is now idle and can execute another task
        sender().tell(new ResultMsg(msg.getName(),msg.getId()), self());
    }

    static Props props() {
        return Props.create(com.pr3V1.ProcessActor.class);
    }

}
