package com.pr3V1;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashMap;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class TaskCompletitionActor extends AbstractActor {
    private int idCount=1;
    private HashMap<String, ActorRef> clientsToNotify = new HashMap<>();
    private final  String defaultGroupId = "groupNotificator";
    private final  String defaultTopic = "completedTasks";
    private KafkaConsumer<String, CompletitionOfTaskMsg> consumer;
    private final String serverAddr = "localhost:9092";
    private final boolean autoCommit = true;
    private final int autoCommitIntervalMs = 10000;
    private final String offsetResetStrategy = "earliest";

    @Override
    public Receive createReceive() {
        return receiveBuilder()//.match(StartMsg.class, this::startNotificationService)
                .match(MsgRef.class,this::addHandled)
                .match(StartMsg.class,this::notifyStart)
                .build();
    }

    public void preStart(){
        final Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, serverAddr);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, defaultGroupId);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, String.valueOf(autoCommit));
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, String.valueOf(autoCommitIntervalMs));

        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, offsetResetStrategy);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JavaDeserializerComplMsg.class.getName());

        consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList(defaultTopic));
    }

    public void addHandled(MsgRef msgRef){
        StartMsg s=new StartMsg();
        s.setId(idCount);
        clientsToNotify.put(""+idCount, msgRef.getRef());
        idCount++;
        sender().tell(s,self());
        self().tell(new StartMsg(),self());
    }

    public void notifyStart(StartMsg msg){
        while (true) {
            final ConsumerRecords<String, CompletitionOfTaskMsg> records = consumer.poll(Duration.of(1, ChronoUnit.MINUTES));
            for (final ConsumerRecord<String, CompletitionOfTaskMsg> record : records) {
                    if(record!=null) {
                        CompletitionOfTaskMsg tmp = record.value();
                        String id = tmp.getId();
                        sendNotification(id);
                    }
            }
        }
    }

    public void sendNotification(String id){
        String[] parts = id.split("-");
        String part1 = parts[0]; // 004
        String part2 = parts[1]; // 034556
        ActorRef actorToNotify = clientsToNotify.get(part1);
        ResultMsg msg=new ResultMsg("a",part2);
        actorToNotify.tell(msg,self());

    }

    static Props props() {
        return Props.create(com.pr3V1.TaskCompletitionActor.class);
    }

}
