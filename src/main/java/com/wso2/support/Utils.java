package com.wso2.support;

/**
 * Util methods to implement broker connection
 */
public class Utils {

    private static final String CARBON_CLIENT_ID = "carbon";
    private static final String CARBON_VIRTUAL_HOST_NAME = "carbon";

    /**
     * Return AMQP connection URL to create connection with message broker
     *
     * @param host     message broker host name
     * @param port     message broker port
     * @param username message broker username
     * @param password message broker password
     * @return AMQP connection URL
     */
    public static String getTCPConnectionURL(String host, String port, String username, String password) {
        return new StringBuffer()
                .append("amqp://").append(username).append(":").append(password)
                .append("@").append(CARBON_CLIENT_ID)
                .append("/").append(CARBON_VIRTUAL_HOST_NAME)
                .append("?brokerlist='tcp://").append(host).append(":").append(port).append("'")
                .toString();
    }
}
