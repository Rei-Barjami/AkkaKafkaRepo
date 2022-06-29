package com.pr3V1;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class ClientManagerActor extends AbstractActor {
    private ActorRef notifactionHandler;
    private ActorRef tAddManager;
    int id;

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(StartMsg.class,this::startService).
        match(ResultMsg.class,this::notifyCompleted).build();
    }

    @Override
    public void preStart() throws Exception {
        notifactionHandler = ClientManager.getHandlerAdder();
        ActorRef myRef = self();
        notifactionHandler.tell(new MsgRef(myRef), self());//contact the notifiaction handler, to tell him the actorsRef
        tAddManager = ClientManager.getRefAdder();
    }


    public void notifyCompleted(ResultMsg msg){
        System.out.println("your task with id :"+id+"-"+msg.getId()+" was succesfully executed");
    }

    public void startService(StartMsg sMsg){
        this.id = sMsg.getId();
        int []times= {3000,1000,10000,5000,6500};
        String []directories = {"dir1","dir2","dir3"};
        Random r= new Random();
        for(int i=0;i<100;i++) {
            TaskMsg msg;

            //instead of creating here the message we should get it from network, test purposes next lines
            int selection=i%3;
            int time;
            String id;
            String dir;
            dir = directories[r.nextInt(3)];
            time = times[r.nextInt(5)];
            id = ""+this.id+"-" + i;
            if (selection == 0)
                msg = new TaskMsg("AudioMerging", time, id, dir);
            else if (selection == 1)
                msg = new TaskMsg("TextFormatting", time, id, dir);
            else
                msg = new TaskMsg("ImageCompression", time, id, dir);
            tAddManager.tell(msg,self());
        }

    }

    static Props props() {
        return Props.create(com.pr3V1.ClientManagerActor.class);
    }

}
