package malin.dtm.chatryadom.models.mongo;

/**
 * Created by dmt on 18.09.2015.
 */
public class MongoUpdateMemberStateParameter {
    private String quadIndex;
    private MongoMember mongoMember;

    public MongoUpdateMemberStateParameter(String quadIndex, MongoMember mongoMember) {
        this.quadIndex = quadIndex;
        this.mongoMember = mongoMember;
    }
    
    public String getQuadIndex() {
        return quadIndex;
    }

    public MongoMember getMongoMember() {
        return mongoMember;
    }
}
