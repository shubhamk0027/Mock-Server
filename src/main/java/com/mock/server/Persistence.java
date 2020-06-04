package com.mock.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

// order of logging of the create and delete team is guaranteed since during creating and deleting process
// no other logging request is possible
// and logging is getting handled by a separate thread and in a queue
//  https://www.journaldev.com/1034/java-blockingqueue-example

@Service
public class Persistence {
    // it is possible that take and add can occur at the same time! :. Blocking queue
    private final BlockingQueue <String> tasks;
    private final int maxTries;

    private static final Logger logger= LoggerFactory.getLogger("OperationsLogger");

    Persistence(@Value("${maxTries}") int maxTries,@Value("${QCapacity}") int capacity) throws IOException {
        tasks = new ArrayBlockingQueue <>(capacity);
        this.maxTries=maxTries;
    }


    public void add(String s) throws InterruptedException {
        tasks.put(s);
    }

    public void run() throws InterruptedException {
        int currTries=0;
        logger.info("INF Persisting data started!");
        while(currTries<maxTries){
            try{
                String msg= tasks.take();
                logger.info(msg);
                currTries=0; // successful read
            }catch(InterruptedException e){
                e.getStackTrace();
                currTries++;
                if(currTries==maxTries) {
                    logger.info("INF Done persisting the data!");
                    throw e;
                }
            }
        }
    }

}
