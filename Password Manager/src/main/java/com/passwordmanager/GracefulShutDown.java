package com.passwordmanager;

import org.apache.cxf.endpoint.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class purpose is for service to shut down gracefully
 */
public class GracefulShutDown extends Thread {

    private static final Logger LOG = LoggerFactory.getLogger(GracefulShutDown.class);

    private final Server insecureService;

    private final Thread mainThread;

    /**
     * Constructor for GracefulShutDown
     */
    GracefulShutDown(Server insecure, Thread mainThread){
        super("GracefulShutDownThread");
        this.insecureService = insecure;
        this.mainThread = mainThread;
    }

    /**
     * run() method which shuts down service
     */
    @Override
    public void run(){
        LOG.info("Starting shutdown");
        this.insecureService.stop();

        try{
            Thread.sleep(1000L);
            this.insecureService.destroy();
            this.mainThread.join();
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ShutdownException(e);
        }
    }

    static class ShutdownException extends RuntimeException {
        ShutdownException(Exception e) {
            super(e);
        }

    }
}