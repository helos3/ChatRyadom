package malin.dtm.chatryadom.models.rabbit;

/**
 * Created by dmt on 14.09.2015.
 */
public class RabbitSubscribeData {
    private String exchange;
    private String routingKey;

    /**
     * метод подписывается на топис вида #.member.# (принимать в обе стороны)
     * @param routingKey
     */
    public void buildPrivateData(String routingKey) {
        this.exchange = "amq.topic";
        this.routingKey = "#.user_" + routingKey + ".#";
    }

    public void buildPublicData(String routingKey) {
        this.exchange = "amq.direct";
        this.routingKey = "quad_" + routingKey;
    }

    public String getExchange() {
        return exchange;
    }

    public String getRoutingKey() {
        return routingKey;
    }
}
