package com.pr3V1;

import akka.actor.ActorRef;

public class MsgRef {
    ActorRef ref;

    public MsgRef(ActorRef ref) {
        this.ref = ref;
    }
}
