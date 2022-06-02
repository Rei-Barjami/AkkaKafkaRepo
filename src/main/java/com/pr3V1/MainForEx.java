package com.pr3V1;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;

public class MainForEx {
    public static void main(String[] args) {
        final ActorSystem sys = ActorSystem.create("System");
        final ActorRef server = sys.actorOf(TaskExtractorActor.props(), "dispatcher");
        server.tell(new StartMsg(), ActorRef.noSender());
    }
}
