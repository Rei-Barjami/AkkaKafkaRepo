package com.pr3V1;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.pattern.Patterns;
import akka.util.Timeout;
import scala.concurrent.Await;


import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import scala.concurrent.Future;

public class TaskExtractorActor extends AbstractActor {
    private ActorRef dispatcher;

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(StartMsg.class, this::onStart).build();
    }

    public TaskExtractorActor(){
        final ActorSystem sys = ActorSystem.create("System");
        this.dispatcher = sys.actorOf(DispatcherActor.props(), "dispatcher");
    }


    //method on which the actor extracts tasks from the kafka queue and calls dispatcher to execute them
    //when dispatcher says that no processor is idle the method stops, and gets again invoked when the dispatcher notifies
    //that a process is idle
    void onStart(StartMsg msg) {
        TaskDispatchedMsg futureMsg;

        boolean stop=false;
        while(!stop){
            //CODE TO EXTRACT FROM KAFKA THE TASK
            TaskMsg msg2= new TaskMsg(100,10);
            dispatcher.tell(msg2,self());
            //use ask pattern to wait for a response from the dispatcher
            Timeout t = new Timeout(3600, TimeUnit.SECONDS);
            Future<Object> fut = Patterns.ask(dispatcher, msg2, t);
            TaskDispatchedMsg response= null;
            try {
                response = (TaskDispatchedMsg) Await.result(fut, t.duration());
            } catch (TimeoutException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            stop=response.getDispatched();

            System.out.println(response);
        }
    }

    static Props props() {
        return Props.create(com.pr3V1.ProcessActor.class);
    }

}
