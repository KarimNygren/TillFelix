package com.passwordmanager;

import org.apache.cxf.endpoint.Server;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.passwordmanager.infrastructure.Dao;

import java.sql.SQLException;

public class PasswordMain {

    /**
     * This is the main class of the Password Manager service.
     */
    private static final Logger LOG = LoggerFactory.getLogger(PasswordMain.class);

    static {
        PropertyConfigurator.configure("src/main/resources/log4j.properties");
    }

    public static void main(String[] args) {
        LOG.info("Starting service");

        try {
            Dao dao = new Dao();

            ServiceStartup serviceStartup = new ServiceStartup();
            Server service = serviceStartup.startInsecureServices(dao);
            initDb(dao);

            GracefulShutDown gracefulShutDown = new GracefulShutDown(service, Thread.currentThread());
            Runtime.getRuntime().addShutdownHook(gracefulShutDown);

            LOG.info("Password Manager started! \\o/ ");
        } catch (Exception e) {
            LOG.warn("Couldn't start Password Manager!", e);
            System.exit(-1);
        }
    }

    /**
     * Initializes the database by:
     * Calling the init() method on the Data Access Object instance to initialize the database
     * Calling the initTables() method on the Data Access Object instance to initialize the database tables
     */
    private static void initDb(Dao dao) throws SQLException {
        dao.init();
        dao.initTables();
    }



}
