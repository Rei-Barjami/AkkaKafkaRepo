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

public class Statistic1Extractor {

    final static String defaultGroupId = "GroupStat1";
    final static String defaultTopic = "completedTasks";
    static KafkaConsumer<String, CompletitionOfTaskMsg> consumer;
    final static String serverAddr = "localhost:9092";
    final static boolean autoCommit = true;
    final static int autoCommitIntervalMs = 10000;

    // Default is "latest": try "earliest" instead
    final static String offsetResetStrategy = "earliest";
    static HashMap<String,Integer> mapDays;
    static HashMap<String,Integer> mapWeeks;
    static HashMap<String,Integer> mapHours;
    static HashMap<String,Integer> mapYears;
    static HashMap<String,Integer> mapMonths;



    public static void main( String [] args) throws InterruptedException {


        mapDays = new HashMap<>();
        mapHours = new HashMap<>();
        mapWeeks = new HashMap<>();
        mapYears = new HashMap<>();
        mapMonths = new HashMap<>();

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

        while (true) {
            TimeUnit.MINUTES.sleep(1);
            final ConsumerRecords<String, CompletitionOfTaskMsg> records = consumer.poll(Duration.of(10, ChronoUnit.SECONDS));
            for (final ConsumerRecord<String, CompletitionOfTaskMsg> record : records) {
                CompletitionOfTaskMsg tmp=record.value();
                updateCompletedStatistics(tmp);
            }
            printUpdatedStatistics();
        }
    }

    public static void updateCompletedStatistics(CompletitionOfTaskMsg msg){
        if(!mapDays.containsKey(msg.getDay()+"")){
            mapDays.put(msg.getDay()+"",1);
        }
        else{
            int old=mapDays.get(msg.getDay()+"");
            int a = old+1;
            mapDays.replace(msg.getDay()+"",old,a);
        }


        if(!mapHours.containsKey(msg.getHour()+"")){
            mapHours.put(msg.getHour()+"",1);
        }
        else{
            int old=mapHours.get(msg.getHour()+"");
            mapHours.replace(msg.getHour()+"",old,old+1);
        }


        if(!mapMonths.containsKey(msg.getMonth()+"")){
            mapMonths.put(msg.getMonth()+"",1);
        }
        else{
            int old=mapMonths.get(msg.getMonth()+"");
            mapMonths.replace(msg.getMonth()+"",old,old+1);
        }


        if(!mapYears.containsKey(msg.getYear()+"")){
            mapYears.put(msg.getYear()+"",1);
        }
        else{
            int old=mapYears.get(msg.getYear()+"");
            int a = old+1;
            mapYears.replace(msg.getYear()+"",old,a);
        }

        if(!mapWeeks.containsKey(msg.getWeek()+"")){
            mapWeeks.put(msg.getWeek()+"",1);
        }
        else{
            int old=mapWeeks.get(msg.getWeek()+"");
            mapWeeks.replace(msg.getWeek()+"",old,old+1);
        }
    }
    public static void printUpdatedStatistics(){
        for (String name: mapHours.keySet()) {
            String key = name.toString();
            String value = mapHours.get(name).toString();
            System.out.println("completed on hour:" +key + ", : " + value);
        }
        for (String name: mapYears.keySet()) {
            String key = name.toString();
            String value = mapYears.get(name).toString();
            System.out.println("completed on year:" +key + ", : " + value);
        }
        for (String name: mapMonths.keySet()) {
            String key = name.toString();
            String value = mapMonths.get(name).toString();
            System.out.println("completed on Month:" +key + ", : " + value);
        }
        for (String name: mapDays.keySet()) {
            String key = name.toString();
            String value = mapDays.get(name).toString();
            System.out.println("completed on day:" +key + ", : " + value);
        }
        for (String name: mapWeeks.keySet()) {
            String key = name.toString();
            String value = mapWeeks.get(name).toString();
            System.out.println("completed on week:" +key + ", : " + value);
        }
    }
}
