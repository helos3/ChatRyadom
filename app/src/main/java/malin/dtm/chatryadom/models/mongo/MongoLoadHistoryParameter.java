package malin.dtm.chatryadom.models.mongo;

import java.util.Date;

import malin.dtm.chatryadom.models.User;

/**
 * Created by dmt on 18.09.2015.
 */
public class MongoLoadHistoryParameter {
    private User user;
    private Date date;
    private String quadIndex;


    public MongoLoadHistoryParameter(User user, Date date, String quadIndex) {
        this.user = user;
        this.date = date;
        this.quadIndex = quadIndex;
    }

    public Date getDate() {
        return date;
    }

    public String getQuadIndex() {
        return quadIndex;
    }

    public User getUser() {
        return user;
    }

}
