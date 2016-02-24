package com.wso2.support;

import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSession;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Properties;

/**
 * Establish queue subscription with message broker and initialize message consumer object
 */
public class MessageBrokerQueueSubscription {

    private static final String QPID_ICF = "org.wso2.andes.jndi.PropertiesFileInitialContextFactory";
    private static final String CF_NAME_PREFIX = "connectionfactory.";
    private static final String CF_NAME = "qpidConnectionfactory";
    private static final String QUEUE_NAME_PREFIX = "queue.";
    private String host;
    private String port;
    private String username;
    private String password;
    private String name;
    private QueueConnection queueConnection;
    private QueueSession queueSession;
    private MessageConsumer subscriber;

    /**
     * Queue subscription constructor
     *
     * @param host host name of the broker
     * @param port port of the broker
     * @param username username to establish connection with broker
     * @param password password to establish connection with broker
     * @param name queue/topic name to register subscription
     */
    public MessageBrokerQueueSubscription(String host, String port, String username, String password, String name) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.name = name;
    }

    /**
     * Register queue subscription with message broker
     *
     * @return message consumer object
     * @throws NamingException
     * @throws JMSException
     */
    public MessageConsumer registerSubscription() throws NamingException, JMSException {
        Properties properties = getProperties();
        InitialContext ctx = new InitialContext(properties);
        QueueConnectionFactory connectionFactory = (QueueConnectionFactory) ctx.lookup(CF_NAME);
        queueConnection = connectionFactory.createQueueConnection();
        queueConnection.start();
        queueSession = queueConnection.createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE);
        Queue queue = (Queue) ctx.lookup(name);
        subscriber = queueSession.createConsumer(queue);
        return subscriber;
    }

    /**
     * Properties to establish connection with message broker
     *
     * @return properties
     */
    private Properties getProperties() {
        Properties properties = new Properties();
        properties.put(Context.INITIAL_CONTEXT_FACTORY, QPID_ICF);
        properties.put(CF_NAME_PREFIX + CF_NAME, Utils.getTCPConnectionURL(host, port, username, password));
        properties.put(QUEUE_NAME_PREFIX + name, name);
        return properties;
    }

    /**
     * Close all queue subscription connections
     *
     * @throws JMSException
     */
    public void closeAll() throws JMSException {
        subscriber.close();
        queueSession.close();
        queueConnection.stop();
        queueConnection.close();
    }

}
