package com.passwordmanager.security;

import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.transport.http.AbstractHTTPDestination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;

/**
 * The InInterceptor is an Interceptor class. When the service is invoked, the interceptor will handle the message.
 */
public class InInterceptor extends AbstractPhaseInterceptor<Message> {

    // Logger for InInterceptor class
    private static final Logger LOG = LoggerFactory.getLogger(InInterceptor.class);

    //Constructor - RECEIVE Phase
    public InInterceptor() {
        super(Phase.RECEIVE);
    }

    /**
     * handleMessage() method takes a Message object.
     *
     * It creates an instance of HttpServletRequest and then gets:
     *
     * The Method with getMethod()
     * The URL with getRequestURL()
     * The Content Type with getHeader(Message.CONTENT_TYPE)
     * The Remote Address with getRemoteAddr()
     *
     * Logs the values with LOG.info()
     */
    @Override
    public void handleMessage(final Message message) throws Fault {
        final HttpServletRequest request = (HttpServletRequest) message.get(AbstractHTTPDestination.HTTP_REQUEST);

        final String method = request.getMethod();
        final String url = request.getRequestURL().toString();
        final String contentType = request.getHeader(Message.CONTENT_TYPE);
        final String remoteAddr = request.getRemoteAddr();

        LOG.info("[INCOMING REQUEST] Method: {}, URL: {}, Content-Type: {}, RemoteAddr: {}", method, url, contentType, remoteAddr);
    }


}
