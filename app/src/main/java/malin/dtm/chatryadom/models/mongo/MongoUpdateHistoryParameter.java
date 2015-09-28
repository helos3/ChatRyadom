package malin.dtm.chatryadom.models.mongo;

/**
 * Created by dmt on 18.09.2015.
 */
public class MongoUpdateHistoryParameter {
    private MongoMessage message;
    private String indexQuad;

    public MongoUpdateHistoryParameter(MongoMessage message, String indexQuad) {
        this.message = message;
        this.indexQuad = indexQuad;
    }

    public MongoMessage getMessage() {
        return message;
    }

    public String getIndexQuad() {
        return indexQuad;
    }
}
