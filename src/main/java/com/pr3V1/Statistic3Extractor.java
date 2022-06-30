package com.pr3V1;

import akka.actor.ActorRef;
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

public class Statistic3Extractor {

    final static String defaultGroupId = "GroupStat3";
    final static String defaultTopic = "startedTask";
    static KafkaConsumer<String, TaskMsg> consumer;
    final static String serverAddr = "localhost:9092";
    final static boolean autoCommit = true;
    final static int autoCommitIntervalMs = 10000;

    // Default is "latest": try "earliest" instead
    final static String offsetResetStrategy = "earliest";
    private static HashMap<String,Integer> executingTimes; // for each task id how many times it executed


    public static void main( String [] args) throws InterruptedException {
        final Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, serverAddr);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, defaultGroupId);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, String.valueOf(autoCommit));
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, String.valueOf(autoCommitIntervalMs));

        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, offsetResetStrategy);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JavaDeserializer.class.getName());

        consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList(defaultTopic));
        executingTimes = new HashMap<>();

        while (true) {
            TimeUnit.MINUTES.sleep(1);
            final ConsumerRecords<String, TaskMsg> records = consumer.poll(Duration.of(1, ChronoUnit.MINUTES));
            for (final ConsumerRecord<String, TaskMsg> record : records) {
                TaskMsg tmp=record.value();
                updateStartedStatistics(tmp);
            }
            printStatistics();
        }
    }
    public static void updateStartedStatistics(TaskMsg msg){
        if(!executingTimes.containsKey(msg.getId())){
            executingTimes.put(msg.getId(),1);
        }
        else{
;           int old = executingTimes.get(msg.getId());
            executingTimes.replace(msg.getId(),old,old+1);
        }
    }
    public static void printStatistics(){
        double tot=0;
        int count=0;
        for (String name: executingTimes.keySet()) {
            int value = executingTimes.get(name);
            tot+=value;
            count ++;
        }
        System.out.println("the average number of times a task starts executing is:"+tot/count);
    }

}
