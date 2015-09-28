package malin.dtm.chatryadom.controllers.rabbit;

import android.os.Handler;

import java.util.ArrayList;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import malin.dtm.chatryadom.models.User;
import malin.dtm.chatryadom.models.messages.ChatMessage;
import malin.dtm.chatryadom.models.rabbit.RabbitPublishData;
import malin.dtm.chatryadom.models.rabbit.RabbitSubscribeData;

/**
 * Created by dmt on 14.09.2015.
 */
public class RabbitController extends RabbitConnection {

    private ArrayList<RabbitSubscribeData> subscribeData = new ArrayList<>();
    private Thread subscribeThread;

    private BlockingDeque<RabbitPublishData> publishQueue = new LinkedBlockingDeque<>();
    private Thread publishThread;
    private Handler handler;


    public RabbitController(Handler handler) {
        super();
        this.handler = handler;
    }

    public void buildSubscribe(User user, String quadRoutingKey) {
        subscribeData = buildSubscribeData(user, quadRoutingKey);
        subscribeThread = new Thread(new RabbitSubscribe(this, subscribeData));
        subscribeThread.start();
    }

    /**
     * собираем данные для подписки на публичный и приватный чат
     * @param user - пользователь
     * @param quadRoutingKey - ключ квадранта
     * @return - данные подписки
     */
    private ArrayList<RabbitSubscribeData> buildSubscribeData(User user, String quadRoutingKey) {
        RabbitSubscribeData privateData = new RabbitSubscribeData();
        privateData.buildPrivateData(user.getKey());
        RabbitSubscribeData publicData = new RabbitSubscribeData();
        publicData.buildPublicData(quadRoutingKey);
        ArrayList<RabbitSubscribeData> data = new ArrayList<>();
        data.add(privateData);
        data.add(publicData);
        return data;
    }


    public void buildPublish() {
        publishThread = new Thread(new RabbitPublish(this));
        publishThread.start();
    }

    private void publish(RabbitPublishData data) {
        //Adds a message to internal blocking queue
        try {
            publishQueue.putLast(data);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void publicPublish(ChatMessage chatMessage, String quadIndex) {
        RabbitPublishData data = new RabbitPublishData(chatMessage);
        data.buildPublicData(quadIndex);
        publish(data);
    }

    public void privatePublish(ChatMessage chatMessage) {
        RabbitPublishData data = new RabbitPublishData(chatMessage);
        data.buildPrivateData();
        publish(data);
    }


    public Handler getHandler() {
        return handler;
    }

    public BlockingDeque<RabbitPublishData> getPublishQueue() {
        return publishQueue;
    }


    public void destroyPublish() {
        if(publishThread != null) {
            publishThread.interrupt();
        }
    }

    public void destroySubscribe() {
        if (subscribeThread != null) {
            subscribeThread.interrupt();
        }
    }

    public void close() {
        destroyPublish();
        destroySubscribe();
        super.close();
    }

    public void open() {
        subscribeThread = new Thread(new RabbitSubscribe(this, subscribeData));
        subscribeThread.start();
        publishThread = new Thread(new RabbitPublish(this));
        publishThread.start();
    }
}
