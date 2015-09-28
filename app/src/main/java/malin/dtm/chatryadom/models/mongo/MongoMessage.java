package malin.dtm.chatryadom.models.mongo;

import org.bson.Document;

import java.util.Date;

import malin.dtm.chatryadom.models.messages.ChatMessage;
import malin.dtm.chatryadom.models.User;

/**
 * Created by dmt on 17.09.2015.
 */
public class MongoMessage {
    private Document document;

    public MongoMessage(Document document) {
        this.document = document;
    }

    public MongoMessage(ChatMessage msg) {
        document = new Document("message", msg.getMessage())
                .append("date", msg.getDate());
        if(msg.getSender() != null) {
            document.append("sender",
                    new Document("name", msg.getSender().getName())
                            .append("key", msg.getSender().getKey()));
        }
        if(msg.getReceiver() != null) {
            document.append("receiver",
                    new Document("name", msg.getReceiver().getName())
                            .append("key", msg.getReceiver().getKey()));
        }
    }

    public ChatMessage toChatMessage() {
        String message = document.getString("message");
        Date date = document.getDate("date");

        User sender = null;
        if (document.containsKey("sender")) {
            Document documentSender =  (Document)document.get("sender");
            sender = new User(documentSender.getString("name"), documentSender.getString("key"));
        }
        User receiver = null;
        if (document.containsKey("receiver")) {
            Document documentSender =  (Document)document.get("receiver");
            receiver = new User(documentSender.getString("name"), documentSender.getString("key"));
        }
        return new ChatMessage(message, date, sender, receiver);
    }

    public Document getDocument() {
        return document;
    }


}
