package com.wso2.support;

import org.apache.axis2.AxisFault;
import org.apache.axis2.transport.jms.JMSUtils;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.TopicSubscriber;

/**
 * Messages are receive on-demand for each request. Necessary input has to send as request headers. This mediator
 * class create connection with message broker based on request header values. Once connection established it receive
 * message, auto acknowledge message, set message to message context and close connection with broker. Payload should
 * send as response to caller after invoke class mediator in mediation logic.
 */
public class MessagePullMediator extends AbstractMediator {

    private static Logger log = LoggerFactory.getLogger(MessagePullMediator.class);

    /**
     * Established connection with broker, receive message and set to message context, finally close connection
     *
     * @param context Message Context
     * @return mediation success or not
     */
    public boolean mediate(MessageContext context) {
        try {
            String brokerHost = (String) context.getProperty("brokerHost");
            String brokerPort = (String) context.getProperty("brokerPort");
            String brokerUsername = (String) context.getProperty("brokerUsername");
            String brokerPassword = (String) context.getProperty("brokerPassword");
            String destinationName = (String) context.getProperty("brokerDestinationName");
            String destinationType = (String) context.getProperty("brokerDestinationType");
            String subscriptionId = (String) context.getProperty("subscriptionId");

            validateRequestHeadersForPullMessage(brokerHost, brokerPort, brokerUsername, brokerPassword,
                    destinationName, destinationType, subscriptionId);

            if (destinationType.equalsIgnoreCase("queue")) {
                MessageBrokerQueueSubscription subscription = new MessageBrokerQueueSubscription(brokerHost, brokerPort,
                        brokerUsername, brokerPassword, destinationName);
                MessageConsumer messageConsumer = subscription.registerSubscription();
                Message message = messageConsumer.receive(15000L);
                setMessageContext((Axis2MessageContext) context, message);
                subscription.closeAll();
            } else if (destinationType.equalsIgnoreCase("topic")) {
                MessageBrokerTopicSubscription subscription = new MessageBrokerTopicSubscription(brokerHost,
                        brokerPort, brokerUsername, brokerPassword, destinationName, subscriptionId);
                TopicSubscriber topicSubscriber = subscription.registerSubscription();
                Message message = topicSubscriber.receive(15000L);
                setMessageContext((Axis2MessageContext) context, message);
                subscription.closeAll();
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new SynapseException(e);
        }
        return true;
    }

    /**
     * Set JMS message to Message context
     *
     * @param context message context
     * @param message JMS message
     * @throws AxisFault
     * @throws JMSException
     */
    private void setMessageContext(Axis2MessageContext context, Message message) throws AxisFault, JMSException {
        if (message != null) {
            JMSUtils.setSOAPEnvelope(message, context.getAxis2MessageContext(), null);
        }
    }

    /**
     * Validate request headers to create connection with broker
     *
     * @param brokerHost      broker host name
     * @param brokerPort      broker port
     * @param brokerUsername  username to connect to broker
     * @param brokerPassword  password to connect to broker
     * @param destinationName queue/topic name to pull message
     * @param destinationType destination type
     * @param subscriptionId  if durable topic, then subscription id
     * @throws RuntimeException
     */
    private void validateRequestHeadersForPullMessage(String brokerHost, String brokerPort, String brokerUsername,
                                                      String brokerPassword, String destinationName,
                                                      String destinationType, String subscriptionId) throws RuntimeException {

        String errorMessage = "";
        if (brokerHost == null) {
            errorMessage = errorMessage + " Broker host name is null. ";
        }
        if (brokerPort == null) {
            errorMessage = errorMessage + " Broker port number is null. ";
        }
        if (brokerUsername == null) {
            errorMessage = errorMessage + " Username is null. ";
        }
        if (brokerPassword == null) {
            errorMessage = errorMessage + " Password is null. ";
        }
        if (destinationName == null) {
            errorMessage = errorMessage + " Destination name is null. ";
        }
        if (destinationType == null) {
            errorMessage = errorMessage + " Destination type is null. ";
        }
        if (destinationType != null) {
            if (destinationType.equalsIgnoreCase("topic")) {
                if (subscriptionId == null) {
                    errorMessage = errorMessage + " Subscription id is null. ";
                }
            }
        }
        if (!errorMessage.isEmpty()) {
            throw new RuntimeException(errorMessage);
        }
    }


}
