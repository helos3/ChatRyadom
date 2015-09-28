package malin.dtm.chatryadom.controllers.mongo;

import android.os.Bundle;
import android.os.Message;

import com.mongodb.Block;

import org.bson.Document;

import java.util.ArrayList;
import java.util.Date;

import malin.dtm.chatryadom.models.messages.ChatMessage;
import malin.dtm.chatryadom.models.mongo.MongoLoadHistoryParameter;
import malin.dtm.chatryadom.models.mongo.MongoMessage;

import static com.mongodb.client.model.Filters.*;
//import static com.mongodb.client.model.Projections.*;

/**
 * Created by dmt on 18.09.2015.
 */
public class MongoLoadHistory implements Runnable {
    private final int INTERVAL_RECOVERY = 3000;

    private MongoController context;
    private MongoLoadHistoryParameter data;

    public MongoLoadHistory(MongoController context, MongoLoadHistoryParameter data) {
        this.context = context;
        this.data = data;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void run() {
        boolean success = false;
        while (!success) {
            try {
                final ArrayList<ChatMessage> chatMessages = new ArrayList<>();

                Block<Document> buildChatMessages = new Block<Document>() {
                    @Override
                    public void apply(Document document) {
                        MongoMessage mongoMessage = new MongoMessage(document);
                        ChatMessage chatMessage = mongoMessage.toChatMessage();
                        chatMessages.add(chatMessage);
                    }
                };

                context.getHistoryCollection()
                        .find(and(
                                eq("quad", data.getQuadIndex()),
                                gt("date", data.getDate()),
                                lt("date", new Date()),
                                or(
                                        exists("receiver", false),
                                        or(
                                                eq("receiver.key", data.getUser().getKey()),
                                                eq("sender.key", data.getUser().getKey())))))
                        .forEach(buildChatMessages);

                Message msg = context.getLoadHistoryHandler().obtainMessage();
                Bundle bundle = new Bundle();

                bundle.putParcelableArrayList("msg", chatMessages);
                msg.setData(bundle);
                context.getLoadHistoryHandler().sendMessage(msg);

                success = true;
            }
            catch (Exception e) {
                try {
                    Thread.sleep(INTERVAL_RECOVERY); //sleep and then try again
                } catch (InterruptedException e1) {
                    break;
                }
            }
        }
    }
}
