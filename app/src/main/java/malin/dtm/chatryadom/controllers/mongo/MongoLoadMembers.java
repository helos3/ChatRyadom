package malin.dtm.chatryadom.controllers.mongo;

import android.os.Bundle;
import android.os.Message;

import com.mongodb.Block;

import org.bson.Document;

import java.util.ArrayList;

import malin.dtm.chatryadom.models.messages.MembersMessage;
import malin.dtm.chatryadom.models.mongo.MongoMember;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

/**
 * Created by dmt on 18.09.2015.
 */
public class MongoLoadMembers implements Runnable {
    private final int INTERVAL_RECOVERY = 3000;

    private MongoController context;
    private String quadIndex;

    public MongoLoadMembers(MongoController context, String quadIndex) {
        this.context = context;
        this.quadIndex = quadIndex;
    }

    @Override
    public void run() {
        boolean success = false;
        while (!success) {
            try {

                final ArrayList<MongoMember> mongoMemberList = new ArrayList<>();

                Block<Document> buildMongoMembersList = new Block<Document>() {
                    @Override
                    public void apply(Document document) {
                        MongoMember mongoMember = new MongoMember(document);
                        mongoMemberList.add(mongoMember);
                    }
                };

                context.getMembersCollection()
                        .find(eq("quad", quadIndex))
                        .forEach(buildMongoMembersList);

                //собираем в объект-список membersMessage
                MembersMessage membersMessage = new MembersMessage(mongoMemberList.size());

                for (MongoMember mongoMember : mongoMemberList) {
                    mongoMember.addToMembersMessage(membersMessage);
                }

                Message msg = context.getLoadMembersHandler().obtainMessage();
                Bundle bundle = new Bundle();

                bundle.putParcelable("msg", membersMessage);
                msg.setData(bundle);
                context.getLoadMembersHandler().sendMessage(msg);

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
