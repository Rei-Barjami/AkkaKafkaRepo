package com.pr3V1;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;

public class TaskCreatorMain {

    private static final int numMessages = 1000;

    public static void main(String[] args) {

        final ActorSystem sys = ActorSystem.create("System");
        final ActorRef server = sys.actorOf(DispatcherActor.props(), "dispatcher");

        int selection;
        Scanner sc= new Scanner(System.in);
        int time,id;
        String dir;
        TaskMsg msg;

        for (int i = 0; i < numMessages; i++) {
            System.out.println("0 if audio, 1 if text formatting, otherwise image compression: ");
            selection=sc.nextInt();
            System.out.println("write result directory: ");
            dir=sc.next();
            System.out.println("write time will take for task: ");
            time=sc.nextInt();
            System.out.println("write task id: ");
            id=sc.nextInt();


            if(selection==0)
                msg = new TaskMsg("AudioMerging",time,id,dir);
            else if(selection==1)
                msg = new TaskMsg("TextFormatting",time,id,dir);
            else
                msg = new TaskMsg("ImageCompression",time,id,dir);

            server.tell(msg,ActorRef.noSender());

        }


        // Wait for all messages to be sent and received
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }

        sys.terminate();

    }

}
