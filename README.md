#Message Pull Mediator

Supported JDK version 1.7 or higher

Message pull mediator connect to WSO2 message broker and pull message from queue/durable topic on-demand.

 1. Installation
  - Build the project and copy message-pull-mediator-1.0.0.jar to wso2esb-4.9.0/repository/components/lib/
  - Copy andes-client-3.0.1.jar, geronimo-jms_1.1_spec-1.1.0.wso2v1.jar, org.wso2.securevault-1.0.0-wso2v2.jar from
wso2mb-3.0.0/client-lib to wso2esb-4.9.0/repository/components/lib/
  - Add below entry to  wso2esb-4.9.0/repository/conf/log4j.properties file
  log4j.logger.org.wso2.andes.client.AMQConnectionDelegate_8_0=ERROR
  - Start wso2esb-4.9.0

 2. Invoke mediator
  - You can use message pull mediator in proxy service. Please note that you must set MANDATORY properties
before calling class mediator in mediation flow. Those properties are,

 brokerHost 
 brokerPort 
 brokerUsername 
 brokerPassword 
 brokerDestinationName 
 brokerDestinationType 
 subscriptionId (Mandatory only if you want pull messages from durable topic) 

Below is the sample proxy service used to invoke message pull mediator.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<proxy xmlns="http://ws.apache.org/ns/synapse"
       name="MessagePullProxy"
       transports="http,https"
       statistics="disable"
       trace="disable"
       startOnLoad="true">
   <target>
      <inSequence>
         <property name="brokerHost" expression="$trp:X-HOST" type="STRING"/>
         <property name="brokerPort" expression="$trp:X-PORT" type="STRING"/>
         <property name="brokerUsername" expression="$trp:X-USER" type="STRING"/>
         <property name="brokerPassword" expression="$trp:X-PWD" type="STRING"/>
         <property name="brokerDestinationName" expression="$trp:X-DSTN" type="STRING"/>
         <property name="brokerDestinationType" expression="$trp:X-DSTT" type="STRING"/>
         <property name="NO_ENTITY_BODY" scope="axis2" action="remove"/>
         <class name="com.wso2.support.MessagePullMediator"/>
         <respond/>
      </inSequence>
      <faultSequence>
         <makefault version="pox">
            <reason xmlns:ns="http://org.apache.synapse/xsd"
                    expression="get-property('ERROR_MESSAGE')"/>
         </makefault>
         <send/>
      </faultSequence>
   </target>
   <description/>
</proxy>
```

In above proxy service, values taken from custom transport headers. You can use query parameter or request payload
based on the requirement.

Invoking proxy service

For queue:
curl -H "X-HOST:localhost" -H "X-PORT:5673" -H "X-USER:admin" -H "X-PWD:admin" -H "X-DSTN:myQueue" -H "X-DSTT:queue" http://localhost:8280/services/MessagePullProxy

For durable topic:
curl -H "X-HOST:localhost" -H "X-PORT:5673" -H "X-USER:admin" -H "X-PWD:admin" -H "X-DSTN:myTopic" -H "X-DSTT:topic" -H "X-SUBID:myId1" http://localhost:8280/services/MessagePullProxy

Please note that empty response will return when there is no messages in queue/durable topic.
