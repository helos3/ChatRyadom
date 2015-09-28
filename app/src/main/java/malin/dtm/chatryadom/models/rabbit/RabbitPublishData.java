package malin.dtm.chatryadom.models.rabbit;

import malin.dtm.chatryadom.models.messages.ChatMessage;

/**
 * Created by dmt on 14.09.2015.
 */
public class RabbitPublishData {
    private ChatMessage chatMessage;
    private String exchange;
    private String routingKey;

    public RabbitPublishData(ChatMessage chatMessage) {
        this.chatMessage = chatMessage;
    }

    public ChatMessage getChatMessage() {
        return chatMessage;
    }

    public String getExchange() {
        return exchange;
    }

    public String getRoutingKey() {
        return routingKey;
    }

    /**
     * метод создает topic вида user_from.user_to
     */
    public void buildPrivateData() {
        this.exchange = "amq.topic";
        StringBuilder sb = new StringBuilder();
        sb.append("user_");
        sb.append(chatMessage.getSender().getKey());
        sb.append(".");
        sb.append("user_");
        sb.append(chatMessage.getReceiver().getKey());
        this.routingKey = sb.toString();
    }

    public void buildPublicData(String routingKey) {
        this.exchange = "amq.direct";
        this.routingKey = "quad_" + routingKey;
    }
}
