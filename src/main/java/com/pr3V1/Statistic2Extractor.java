package com.pr3V1;

import akka.actor.ActorRef;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class Statistic2Extractor {

    final static String defaultGroupId = "groupA";
    final static String defaultGroupId2 = "groupB";

    final static String topicCompleted = "completedTasks";
    final static String topicRequired = "topicA";

    static KafkaConsumer<String, CompletitionOfTaskMsg> consumerCompleted;
    static KafkaConsumer<String, TaskMsg> consumerRequested;

    final static String serverAddr = "localhost:9092";
    final static boolean autoCommit = true;
    final static int autoCommitIntervalMs = 10000;

    // Default is "latest": try "earliest" instead
    final static String offsetResetStrategy = "earliest";


    public static void main( String [] args) throws InterruptedException {

        ArrayList<String> idsPending= new ArrayList<String>();

        final Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, serverAddr);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, defaultGroupId);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, String.valueOf(autoCommit));
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, String.valueOf(autoCommitIntervalMs));

        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, offsetResetStrategy);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JavaDeserializerComplMsg.class.getName());

        consumerCompleted = new KafkaConsumer<>(props);

        consumerCompleted.subscribe(Collections.singletonList(topicCompleted));


        final Properties props2 = new Properties();
        props2.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, serverAddr);
        props2.put(ConsumerConfig.GROUP_ID_CONFIG, defaultGroupId2);
        props2.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, String.valueOf(autoCommit));
        props2.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, String.valueOf(autoCommitIntervalMs));

        props2.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, offsetResetStrategy);
        props2.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props2.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JavaDeserializer.class.getName());

        consumerRequested = new KafkaConsumer<>(props2);
        consumerRequested.subscribe(Collections.singletonList(topicRequired));


        while (true) {
            TimeUnit.SECONDS.sleep(5);
            final ConsumerRecords<String, TaskMsg> records = consumerRequested.poll(Duration.of(1, ChronoUnit.SECONDS));
            for (final ConsumerRecord<String, TaskMsg> record : records) {
                TaskMsg tmp=record.value();
                idsPending.add(tmp.getId()); //add from the kafka queue of requested
            }
            final ConsumerRecords<String, CompletitionOfTaskMsg> records2 = consumerCompleted.poll(Duration.of(1, ChronoUnit.SECONDS));
            for (final ConsumerRecord<String, CompletitionOfTaskMsg> record : records2) {
                if(record.value()!=null) {
                    CompletitionOfTaskMsg tmp = record.value();
                    int idx = idsPending.indexOf(tmp.getId());
                    if (idx != -1) {
                        idsPending.remove(idx); //remove already completed records
                    }
                }
            }

            System.out.println("number of pending tasks = "+ idsPending.size());
        }
    }

}
