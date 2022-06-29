package com.pr3V1;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;



public class ClientManager {
    final static ActorSystem sys = ActorSystem.create("System");
    final static ActorRef tAddManager = sys.actorOf(TaskAddActor.props(), "process1");
    final static ActorRef notificationHandler = sys.actorOf(TaskCompletitionActor.props(), "process2");

    public static ActorRef getRefAdder(){
        return tAddManager;
    }
    public static ActorRef getHandlerAdder(){
        return notificationHandler;
    }
    public static void main(String []args){
        //notificationHandler.tell(new StartMsg(),null);
        /**

        int []times= {300,100,1000,500,650};
        int userId=1;

        Thread thread = new Thread(){
            public void run(){
                for(int i=0;i<1000;i++) {
                    TaskMsg msg = null;
                    //instead of creating here the message we should get it from network, test purposes next lines
                    int selection=i%3;
                    int time;
                    String id;
                    String dir;
                    dir = "ciao";
                    time = times[i%5];
                    id = ""+userId+"-" + i;
                    if (selection == 0)
                        msg = new TaskMsg("AudioMerging", time, id, dir);
                    else if (selection == 1)
                        msg = new TaskMsg("TextFormatting", time, id, dir);
                    else
                        msg = new TaskMsg("ImageCompression", time, id, dir);
                    tAddManager.tell(msg,null);
                }            }
        };
        Thread thread2 = new Thread(){
            public void run(){
                for(int i=0;i<1000;i++) {
                    TaskMsg msg = null;
                    //instead of creating here the message we should get it from network, test purposes next lines
                    int selection=i%3;
                    int time;
                    String id;
                    String dir;
                    dir = "ciao";
                    time = times[i%5];
                    id = ""+2+"-" + i;
                    if (selection == 0)
                        msg = new TaskMsg("AudioMerging", time, id, dir);
                    else if (selection == 1)
                        msg = new TaskMsg("TextFormatting", time, id, dir);
                    else
                        msg = new TaskMsg("ImageCompression", time, id, dir);
                    tAddManager.tell(msg,null);
                }            }
        };
        Thread thread3 = new Thread(){
            public void run(){
                for(int i=0;i<300;i++) {
                    TaskMsg msg = null;
                    //instead of creating here the message we should get it from network, test purposes next lines
                    int selection=i%3;
                    int time;
                    String id;
                    String dir;
                    dir = "ciao";
                    time = times[i%5];
                    id = ""+3+"-" + i;
                    if (selection == 0)
                        msg = new TaskMsg("AudioMerging", time, id, dir);
                    else if (selection == 1)
                        msg = new TaskMsg("TextFormatting", time, id, dir);
                    else
                        msg = new TaskMsg("ImageCompression", time, id, dir);
                    tAddManager.tell(msg,null);
                }            }
        };

        thread.start();
        thread2.start();
        thread3.start();
        */
        final ActorRef client1 = sys.actorOf(ClientManagerActor.props(), "process3");
        final ActorRef client2 = sys.actorOf(ClientManagerActor.props(), "process4");
        final ActorRef client3 = sys.actorOf(ClientManagerActor.props(), "process5");
        //final ActorRef client4 = sys.actorOf(ClientManagerActor.props(), "process6");
        //final ActorRef client5 = sys.actorOf(ClientManagerActor.props(), "process7");
        //final ActorRef client6 = sys.actorOf(ClientManagerActor.props(), "process8");

    }
}
