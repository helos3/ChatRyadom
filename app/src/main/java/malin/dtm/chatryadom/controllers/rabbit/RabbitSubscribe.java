package malin.dtm.chatryadom.controllers.rabbit;

import android.os.Bundle;
import android.os.Message;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;

import java.util.ArrayList;

import malin.dtm.chatryadom.models.rabbit.RabbitSubscribeData;

/**
 * Created by dmt on 14.09.2015.
 */
public class RabbitSubscribe implements Runnable{
    private final int INTERVAL_RECOVERY = 4000;

    private RabbitController context;
    private ArrayList<RabbitSubscribeData> listData;

    public RabbitSubscribe(RabbitController context, ArrayList<RabbitSubscribeData> listData) {
        this.context = context;
        this.listData = listData;
    }

    @Override
    public void run() {
        while(true) {
            try {
                Channel ch = context.newChanel();
                ch.basicQos(1);
                AMQP.Queue.DeclareOk q = ch.queueDeclare();
                for (RabbitSubscribeData data : listData) {
                    ch.queueBind(q.getQueue(), data.getExchange(), data.getRoutingKey());
                }
                QueueingConsumer consumer = new QueueingConsumer(ch);
                ch.basicConsume(q.getQueue(), true, consumer);

                // Process deliveries
                while (true) {
                    QueueingConsumer.Delivery delivery = consumer.nextDelivery();

                    String message = new String(delivery.getBody());

                    Message msg = context.getHandler().obtainMessage();
                    Bundle bundle = new Bundle();

                    bundle.putString("msg", message);
                    msg.setData(bundle);
                    context.getHandler().sendMessage(msg);
                }
            } catch (InterruptedException e) {
                break;
            } catch (Exception e1) {
                try {
                    Thread.sleep(INTERVAL_RECOVERY); //sleep and then try again
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    }
}
