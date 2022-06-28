package com.pr3V1;

import akka.actor.ActorRef;

public class RestartTask {
    private TaskMsg oldMsg;
    private ActorRef processor;
    public RestartTask(TaskMsg oldMsg,ActorRef processor) {
        this.oldMsg = oldMsg;
        this.processor = processor;
    }

    public ActorRef getProcessor() {
        return processor;
    }

    public TaskMsg getOldMsg() {
        return oldMsg;
    }

}
