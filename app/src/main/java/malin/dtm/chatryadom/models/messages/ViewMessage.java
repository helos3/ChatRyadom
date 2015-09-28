package malin.dtm.chatryadom.models.messages;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by dmt on 18.09.2015.
 */
public class ViewMessage {
    protected String message;
    protected Date date;

    protected ViewMessage(String message) {
        this.message = message;
        this.date = new Date();
    }

    protected ViewMessage(String message, Date date) {
        this.message = message;
        this.date = date;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getDate() {
        return date;
    }

    public String buildDefaultHeader() {
        SimpleDateFormat ft = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return "ЧатРядом, " + ft.format(date);
    }
}
