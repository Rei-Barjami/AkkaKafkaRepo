package com.pr3V1;

import akka.actor.*;
import akka.japi.pf.DeciderBuilder;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Random;
import java.util.Stack;
import java.util.concurrent.Future;

public class DispatcherActor extends AbstractActor {
    Stack<ActorRef> freeProcesses;
    private ActorRef extractor = null;
    int nProcesses = 4;

    private static final String defaultTopic = "completedTasks";
    private static final String serverAddr = "localhost:9092";
    private static final String topicStartedExec = "startedTask";

    private final Random r1 = new Random();
    private final Random r = new Random();
    private KafkaProducer<String, CompletitionOfTaskMsg> producer;
    private KafkaProducer<String, TaskMsg>producerStartingTask;
    //create processor actors and add them to the stack of idle processors(initially they execute nothing)
    public DispatcherActor(){
        freeProcesses = new Stack<ActorRef>();

    }

    public void preStart(){
        final Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, serverAddr);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JavaSerializerComplMsg.class.getName());

        final Properties props2 = new Properties();
        props2.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, serverAddr);
        props2.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props2.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JavaSerializer.class.getName());

        this.producer = new KafkaProducer<>(props); //string string are key and value type
        this.producerStartingTask = new KafkaProducer<>(props2); //string string are key and value type

        final ActorSystem sys = ActorSystem.create("System");
        final ActorRef process1 = sys.actorOf(ProcessActor.props(), "process1");
        final ActorRef process2 = sys.actorOf(ProcessActor.props(), "process2");
        final ActorRef process3 = sys.actorOf(ProcessActor.props(), "process3");
        final ActorRef process4 = sys.actorOf(ProcessActor.props(), "process4");
        freeProcesses.push(process1);
        freeProcesses.push(process2);
        freeProcesses.push(process3);
        freeProcesses.push(process4);
        ActorRef s = self();
        process1.tell(new MsgRef(s),self());
        process2.tell(new MsgRef(s),self());
        process3.tell(new MsgRef(s),self());
        process4.tell(new MsgRef(s),self());

    }

    @Override
    public Receive createReceive() {
        return canExecute();
    }

    private final Receive canExecute(){
        return receiveBuilder().match(ResultMsg.class, this::onResultReceive).match(TaskMsg.class,this::executeTask)
                .match(MsgRef.class,this::getExtractor)
                .match(RestartTask.class,this::restartTask)
                .match(TestMsg.class,this::test).build();
    }

    //do not accept execute messages if no idle processor is present
    private final Receive cannotExecute(){
        return receiveBuilder().match(ResultMsg.class, this::onResultReceive).match(TaskMsg.class, this::notExecutable)
                .match(RestartTask.class,this::restartTask)
                .match(TestMsg.class,this::test).build();
    }


    public void getExtractor(MsgRef msg){
        this.extractor=msg.getRef();
    }

    //enter here after receiving a valid result, at least one process becomes idle so pass in canExecute state
    public void onResultReceive(ResultMsg msg){
        System.out.println("executed task with id: "+msg.getId()+"   in processor n: "+sender().path().name());
        updateStatistics(msg);
        boolean noIdleBefore=false;
        if(freeProcesses.size()==0)
            noIdleBefore=true;

        freeProcesses.push(sender()); // add the process that has just finished execution to the free ones
        getContext().become(canExecute());

        if (noIdleBefore) {
            extractor.tell(new StartMsg(), self());
        }
        // ADD CODE TO TELL THE CLIENT THAT EXECUTION WAS DONE

    }


    //if no idle processor is present put the actor in cannotExecute state
    public void executeTask(TaskMsg msg){
        if(freeProcesses.size()>0) {
            ActorRef process= freeProcesses.pop();
            process.tell(msg, self());
            insertMessageStarted(msg);
        }
        if(freeProcesses.size()==0){ // check after the pop
            sender().tell(new TaskDispatchedMsg(true),self());
            getContext().become(cannotExecute());
        }
        else
            sender().tell(new TaskDispatchedMsg(false),self());
    }

    public void updateStatistics(ResultMsg msg){
        final String topic = defaultTopic;
        final String key = "Key" + r.nextInt(1000);

        final ProducerRecord<String, CompletitionOfTaskMsg> record = new ProducerRecord<>(topic, key, new CompletitionOfTaskMsg(LocalDateTime.now(),msg.getId()));
        final Future<RecordMetadata> future = this.producer.send(record);

        while(!future.isDone()){
        }
    }

    //when a task arrives but it is not executable since there is no idle processor (so cannotExecute state)
    public void notExecutable(TaskMsg msg){
        //ADD THE TASK IN A QUEUE, SO TO EXECUTE IT WHEN A PROCESSOR IS IDLE?? USE STASH??
        System.out.println("task not executable now");
    }

    public void restartTask(RestartTask msg){
        ActorRef s=msg.getProcessor();
        ActorRef s1 = self();
        s.tell(new MsgRef(s1),self());
        s.tell(msg.getOldMsg(),self());
        insertMessageStarted(msg.getOldMsg());
    }

    public void insertMessageStarted(TaskMsg msg){
        final String topic = topicStartedExec;
        final String key = "Key" + r1.nextInt(1000);
        final ProducerRecord<String, TaskMsg> record = new ProducerRecord<>(topic, key, msg);
        this.producerStartingTask.send(record);
    }


    private static SupervisorStrategy strategy =
            new OneForOneStrategy(
                    10,
                    Duration.ofSeconds(1),
                    DeciderBuilder.match(Exception.class, e ->
                                    SupervisorStrategy.restart())
                            .build());

    @Override
    public SupervisorStrategy supervisorStrategy() {
        return strategy;
    }

    public void test(TestMsg msg){
        System.out.println("TEEEEEEEEEEEEEEEEST");
    }

    static Props props() {
        return Props.create(com.pr3V1.DispatcherActor.class);
    }
}