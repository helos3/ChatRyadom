package malin.dtm.chatryadom.controllers.rabbit;

import com.google.gson.Gson;
import com.rabbitmq.client.Channel;

import java.util.concurrent.BlockingDeque;

import malin.dtm.chatryadom.models.rabbit.RabbitPublishData;

/**
 * Created by dmt on 14.09.2015.
 */
public class RabbitPublish implements Runnable {
    private final int INTERVAL_RECOVERY = 5000;

    private RabbitController context;

    public RabbitPublish(RabbitController context) {
        this.context = context;
    }

    @Override
    public void run() {
        while(true) {
            try {
                BlockingDeque<RabbitPublishData> queue = context.getPublishQueue();

                Channel ch = context.newChanel();
                ch.confirmSelect();

                while (true) {
                    RabbitPublishData data = queue.takeFirst();
                    try{
                        String message = new Gson().toJson(data.getChatMessage());
                        ch.basicPublish(data.getExchange(), data.getRoutingKey(), null, message.getBytes());
                        ch.waitForConfirmsOrDie();
                    } catch (Exception e){
                        queue.putFirst(data);
                        throw e;
                    }
                }
            } catch (InterruptedException e) {
                break;
            } catch (Exception e) {
                try {
                    Thread.sleep(INTERVAL_RECOVERY); //sleep and then try again
                } catch (InterruptedException e1) {
                    break;
                }
            }
        }
    }
}
