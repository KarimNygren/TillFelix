package com.passwordmanager;

import com.passwordmanager.infrastructure.Dao;
import com.passwordmanager.security.InInterceptor;
import com.passwordmanager.services.Ledger;
import com.passwordmanager.services.PasswordService;
import com.passwordmanager.services.PasswordServiceImpl;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.lifecycle.SingletonResourceProvider;
import org.apache.cxf.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

public class ServiceStartup {

    /**
     * The purpose of this class is to start up the service
     */
    private static final Logger LOG = LoggerFactory.getLogger(ServiceStartup.class);
    private static final String HTTP = "http://localhost:";
    private static final int INSECURE_PORT = 8080;

    /**
     * startInsecureServices method publishes the services on the specified URL.
     * The PasswordService interface and interceptor is passed in publish.
     */
    public Server startInsecureServices(Dao dao) {
        Ledger ledger = new Ledger(dao);
        PasswordService passwordService = new PasswordServiceImpl(ledger);

        String URL = HTTP + INSECURE_PORT;
        LOG.info("Publishing services on " + URL);

        InInterceptor interceptor = new InInterceptor();

        return publish(URL,
                List.of(passwordService),
                List.of(interceptor));
    }

    /**
     * Publish Method that takes URL, List of services and list of Interceptor as parameters.
     * Creates JAXRSServerFactoryBean, create() is called on the bean which creates the JAX-RS Server instance.
     */
    public Server publish(String URL, List<Object> services, List<Interceptor<Message>> interceptors) {
        final JAXRSServerFactoryBean sf = new JAXRSServerFactoryBean();
        sf.setResourceClasses(services.stream().map(Object::getClass).collect(Collectors.toList()));
        services.forEach(s -> sf.setResourceProvider(s.getClass(), new SingletonResourceProvider(s)));
        sf.setAddress(URL);
        sf.getInInterceptors().addAll(interceptors);
        return sf.create();
    }


}
