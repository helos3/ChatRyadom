package malin.dtm.chatryadom.controllers.mongo;

import org.bson.Document;

import java.util.concurrent.BlockingDeque;

import malin.dtm.chatryadom.models.mongo.MongoUpdateHistoryParameter;


/**
 * Created by dmt on 17.09.2015.
 */
public class MongoUpdateHistory implements Runnable{

    private final int INTERVAL_RECOVERY = 3000;

    private MongoController context;

    public MongoUpdateHistory (MongoController context) {
        this.context = context;
    }

    @Override
    public void run() {
        while (true) {
            try {
                BlockingDeque<MongoUpdateHistoryParameter> queue = context.getUpdateHistoryQueue();

                MongoUpdateHistoryParameter data =  queue.takeFirst();
                try {
                    Document message = data.getMessage().getDocument();
                    context.getHistoryCollection().insertOne(
                            message.append("quad", data.getIndexQuad()));

                }
                catch (Exception e) {
                    queue.putFirst(data);
                    throw e;
                }
            }
            catch (InterruptedException e) {
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
