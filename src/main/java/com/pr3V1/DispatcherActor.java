package com.pr3V1;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Random;
import java.util.Stack;
import java.util.concurrent.Future;

public class DispatcherActor extends AbstractActor {
    Stack<ActorRef> processes;
    Stack<ActorRef> freeProcesses;
    private boolean canRun;
    private ActorRef extractor = null;
    int nProcesses = 4;

    private static final String defaultTopic = "completedTasks";
    private static final int numMessages = 100;
    private static final String serverAddr = "localhost:9092";
    private final Random r = new Random();
    private KafkaProducer<String, CompletitionOfTaskMsg> producer;

    //create processor actors and add them to the stack of idle processors(initially they execute nothing)
    public DispatcherActor(){
        freeProcesses = new Stack<ActorRef>();
        final ActorSystem sys = ActorSystem.create("System");
        final ActorRef process1 = sys.actorOf(ProcessActor.props(), "process1");
        final ActorRef process2 = sys.actorOf(ProcessActor.props(), "process2");
        final ActorRef process3 = sys.actorOf(ProcessActor.props(), "process3");
        final ActorRef process4 = sys.actorOf(ProcessActor.props(), "process4");
        freeProcesses.push(process1);
        freeProcesses.push(process2);
        freeProcesses.push(process3);
        freeProcesses.push(process4);
    }

    public void preStart(){
        final Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, serverAddr);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JavaSerializerComplMsg.class.getName());

        this.producer = new KafkaProducer<>(props); //string string are key and value type
    }

    @Override
    public Receive createReceive() {
        return canExecute();
    }

    private final Receive canExecute(){
        return receiveBuilder().match(ResultMsg.class, this::onResultReceive).match(TaskMsg.class,this::executeTask).match(MsgRef.class,this::getExtractor).build();
    }

    //do not accept execute messages if no idle processor is present
    private final Receive cannotExecute(){
        return receiveBuilder().match(ResultMsg.class, this::onResultReceive).match(TaskMsg.class, this::notExecutable).build();
    }


    public void getExtractor(MsgRef msg){
        this.extractor=msg.ref;
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

    static Props props() {
        return Props.create(com.pr3V1.DispatcherActor.class);
    }
}
