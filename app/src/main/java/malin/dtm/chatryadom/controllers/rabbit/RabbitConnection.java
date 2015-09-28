package malin.dtm.chatryadom.controllers.rabbit;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 * Created by dmt on 14.09.2015.
 */
public class RabbitConnection {

    private ConnectionFactory factory;
    private Connection connection;

    protected RabbitConnection() {
        try
        {
            String uri = "amqp://root:root@178.62.233.195/%2f";
            factory = new ConnectionFactory();
            factory.setAutomaticRecoveryEnabled(false);
            factory.setUri(uri);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized Channel newChanel(){
        Channel channel = null;
        try
        {
            if(connection == null || !connection.isOpen()) {
                connection = factory.newConnection();
            }
            channel = connection.createChannel();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return channel;
    }

    protected void close() {
        try
        {
            if(connection != null && connection.isOpen()) {
                connection.close();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
