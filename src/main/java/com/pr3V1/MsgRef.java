package com.pr3V1;

import akka.actor.ActorRef;

public class MsgRef {
    private ActorRef ref;

    public MsgRef(ActorRef ref) {
        this.ref = ref;
    }

    public ActorRef getRef() {
        return ref;
    }
}
