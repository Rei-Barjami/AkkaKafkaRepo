package com.pr3V1;
import java.util.*;
import java.util.concurrent.Future;

import akka.actor.AbstractActor;
import akka.actor.Props;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;

public class TaskAddActor extends AbstractActor {
    private static final String defaultTopic = "topicA";
    private static final String serverAddr = "localhost:9092";
    private final Random r = new Random();
    private KafkaProducer<String, TaskMsg> producer;

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(TaskMsg.class, this::onTaskReceive).build();
    }

    public TaskAddActor(){
    }

    public void preStart(){
        final Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, serverAddr);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JavaSerializer.class.getName());
        this.producer = new KafkaProducer<>(props); //string string are key and value type

    }
    public void onTaskReceive(TaskMsg taskMsg){
        addTask(taskMsg);
    }

    public void addTask(TaskMsg msg) {

            final String topic = defaultTopic;
            final String key = "Key" + r.nextInt(1000);

            final ProducerRecord<String, TaskMsg> record = new ProducerRecord<>(topic, key, msg);

            final Future<RecordMetadata> future = this.producer.send(record);
            while(!future.isDone()){
                System.out.println("waiting");
            }
            System.out.println("sent number :"+key);

    }
    static Props props() {
        return Props.create(com.pr3V1.TaskAddActor.class);
    }

}