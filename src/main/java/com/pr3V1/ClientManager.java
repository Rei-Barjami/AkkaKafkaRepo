package com.pr3V1;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;

public class ClientManager {

    public static void main(String []args){
        final ActorSystem sys = ActorSystem.create("System");
        final ActorRef tAddManager = sys.actorOf(TaskAddActor.props(), "process1");

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

    }
}
