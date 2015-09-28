package malin.dtm.chatryadom.models.mongo;

import org.bson.Document;

import malin.dtm.chatryadom.models.User;
import malin.dtm.chatryadom.models.messages.MembersMessage;

/**
 * Created by dmt on 22.09.2015.
 */
public class MongoMember {
    private String userKey;
    private Document document;

    public MongoMember(Document document) {
        this.userKey = "";
        this.document = document;
    }

    public MongoMember(User user) {
        userKey = user.getKey();
        document = new Document("member",
                new Document("key", user.getKey())
                        .append("name", user.getName()));

    }

    public Document getDocument() {
        return document;
    }

    public String getUserKey() {
        return userKey;
    }

    public void addToMembersMessage(MembersMessage membersMessage) {
        Document documentUser = (Document)document.get("member");
        User user = new User(documentUser.getString("name"), documentUser.getString("key"));
        membersMessage.addUser(user);
    }
}
