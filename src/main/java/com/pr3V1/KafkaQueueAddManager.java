package com.pr3V1;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;

public class KafkaQueueAddManager {
    private static final String defaultTopic = "topicA";

    private static final int numMessages = 100000;

    private static final String serverAddr = "localhost:9092";

    public static void main(String[] args) {

        Scanner sc= new Scanner(System.in);

        List<String> topics = args.length < 1 ?
                Collections.singletonList(defaultTopic) :
                Arrays.asList(args);

        final Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, serverAddr);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JavaSerializer.class.getName());

        final KafkaProducer<String, TaskMsg> producer = new KafkaProducer<>(props); //string string are key and value type
        final Random r = new Random();

        for (int i = 0; i < numMessages; i++) {
            final String topic = topics.get(r.nextInt(topics.size()));
            final String key = "Key" + r.nextInt(1000);

            //instead of creating here the message we should get it from network, test purposes next lines
            TaskMsg msg;
            int selection;
            int time,id;

            System.out.println("0 if audio, 1 if text formatting, otherwise image compression: ");
            selection=sc.nextInt();
            System.out.println("write time will take for task: ");
            time=sc.nextInt();
            System.out.println("write task id: ");
            id=sc.nextInt();

            if(selection==0)
                msg = new TaskMsg("AudioMerging",time,id);
            else if(selection==1)
                msg = new TaskMsg("TextFormatting",time,id);
            else
                msg = new TaskMsg("ImageCompression",time,id);

            final ProducerRecord<String, TaskMsg> record = new ProducerRecord<>(topic, key, msg);

            final Future<RecordMetadata> future = producer.send(record);

        }

        producer.close();
    }


}