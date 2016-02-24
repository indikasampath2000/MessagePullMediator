package com.wso2.support;


import javax.jms.JMSException;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Properties;

/**
 * Establish topic subscription with message broker and initialize message consumer object
 */
public class MessageBrokerTopicSubscription {

    private static final String QPID_ICF = "org.wso2.andes.jndi.PropertiesFileInitialContextFactory";
    private static final String CF_NAME_PREFIX = "connectionfactory.";
    private static final String CF_NAME = "qpidConnectionfactory";
    private static final String TOPIC_NAME_PREFIX = "topic.";
    private String host;
    private String port;
    private String username;
    private String password;
    private String name;
    private String subscriptionId;
    private TopicConnection topicConnection;
    private TopicSession topicSession;
    private TopicSubscriber subscriber;

    /**
     * Queue subscription constructor
     *
     * @param host     host name of the broker
     * @param port     port of the broker
     * @param username username to establish connection with broker
     * @param password password to establish connection with broker
     * @param name     queue/topic name to register subscription
     */
    public MessageBrokerTopicSubscription(String host, String port, String username, String password, String name,
                                          String subscriptionId) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.name = name;
        this.subscriptionId = subscriptionId;
    }

    /**
     * Register durable topic subscription with message broker
     *
     * @return durable topic subscriber object
     * @throws NamingException
     * @throws JMSException
     */
    public TopicSubscriber registerSubscription() throws NamingException, JMSException {
        Properties properties = getProperties();
        InitialContext ctx = new InitialContext(properties);
        TopicConnectionFactory connectionFactory = (TopicConnectionFactory) ctx.lookup(CF_NAME);
        topicConnection = connectionFactory.createTopicConnection();
        topicConnection.start();
        topicSession = topicConnection.createTopicSession(false, TopicSession.AUTO_ACKNOWLEDGE);
        Topic topic = (Topic) ctx.lookup(name);
        subscriber = topicSession.createDurableSubscriber(topic, subscriptionId);
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
        properties.put(TOPIC_NAME_PREFIX + name, name);
        return properties;
    }

    /**
     * Close all queue subscription connections
     *
     * @throws JMSException
     */
    public void closeAll() throws JMSException {
        subscriber.close();
        topicSession.close();
        topicConnection.stop();
        topicConnection.close();
    }

}
