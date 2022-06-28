package com.pr3V1;

public class ClientMain {

    public static void main(String []args){
        int []times= {300,100,1000,500,650};
        int userId=1;
        KafkaQueueAddManager kqa=new KafkaQueueAddManager();
        for(int i=100;i<200;i++) {
            TaskMsg msg = null;
            //instead of creating here the message we should get it from network, test purposes next lines
            int selection=i%3;
            int time;
            String id;
            String dir;
            dir = "ciao";
            time = times[i%5];
            id = ""+userId+"-" + i;
            if (selection == 0)
                msg = new TaskMsg("AudioMerging", time, id, dir);
            else if (selection == 1)
                msg = new TaskMsg("TextFormatting", time, id, dir);
            else
                msg = new TaskMsg("ImageCompression", time, id, dir);
            kqa.addTask(msg);
        }
    }
}
