package malin.dtm.chatryadom.controllers.mongo;

import com.mongodb.client.model.UpdateOptions;

import org.bson.Document;

import java.util.concurrent.BlockingDeque;

import malin.dtm.chatryadom.models.mongo.MongoUpdateMemberStateParameter;

import static com.mongodb.client.model.Filters.eq;

/**
 * Created by dmt on 18.09.2015.
 */
public class MongoUpdateMemberState implements Runnable{

    private final int INTERVAL_RECOVERY = 3000;

    private MongoController context;

    public MongoUpdateMemberState(MongoController context) {
        this.context = context;
    }

    @Override
    public void run() {
        while (true) {
            try {
                BlockingDeque<MongoUpdateMemberStateParameter> queue = context.getUpdateMemberStateQueue();

                MongoUpdateMemberStateParameter data =  queue.takeFirst();
                try {
                    String userKey = data.getMongoMember().getUserKey();
                    Document document = data.getMongoMember().getDocument();
                    context.getMembersCollection().updateOne(
                            eq("member.key", userKey),
                            new Document("$set", document
                                    .append("quad", data.getQuadIndex())),
                            new UpdateOptions().upsert(true));
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
