package com.pr3V1;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.pattern.Patterns;
import akka.util.Timeout;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import scala.concurrent.Await;


import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import scala.concurrent.Future;

public class TaskExtractorActor extends AbstractActor {
    private ActorRef dispatcher;
    private static final String defaultGroupId = "groupA";
    private static final String defaultTopic = "topicB";

    private static final String serverAddr = "localhost:9092";
    private static final boolean autoCommit = true;
    private static final int autoCommitIntervalMs = 15000;

    // Default is "latest": try "earliest" instead
    private static final String offsetResetStrategy = "latest";

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(StartMsg.class, this::onStart).build();
    }

    public TaskExtractorActor(){
        final ActorSystem sys = ActorSystem.create("System");
        this.dispatcher = sys.actorOf(DispatcherActor.props(), "dispatcher");
    }


    //method on which the actor extracts tasks from the kafka queue and calls dispatcher to execute them
    //when dispatcher says that no processor is idle the method stops, and gets again invoked when the dispatcher notifies
    //that a process is idle
    void onStart(StartMsg msg) {
        TaskDispatchedMsg futureMsg;
        boolean stop=false;
        while(!stop){
            //CODE TO EXTRACT FROM KAFKA THE TASK
            TaskMsg msg2= getMsg();

            dispatcher.tell(msg2,self());
            //use ask pattern to wait for a response from the dispatcher
            Timeout t = new Timeout(3600, TimeUnit.SECONDS);
            Future<Object> fut = Patterns.ask(dispatcher, msg2, t);
            TaskDispatchedMsg response= null;
            try {
                response = (TaskDispatchedMsg) Await.result(fut, t.duration());
            } catch (TimeoutException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            stop=response.getDispatched();

            System.out.println(response);
        }
    }

    public TaskMsg getMsg(){
        // If there are arguments, use the first as group and the second as topic.
        // Otherwise, use default group and topic.
        String groupId = defaultGroupId;
        String topic = defaultTopic;

        final Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, serverAddr);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, String.valueOf(autoCommit));
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, String.valueOf(autoCommitIntervalMs));

        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, offsetResetStrategy);

        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        KafkaConsumer<String, TaskMsg> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList(topic));

        while (true) {
            final ConsumerRecords<String, TaskMsg> records = consumer.poll(Duration.of(5, ChronoUnit.MINUTES));
            for (final ConsumerRecord<String, TaskMsg> record : records) {
                return record.value();
            }
        }
    }

    static Props props() {
        return Props.create(com.pr3V1.TaskExtractorActor.class);
    }

}
