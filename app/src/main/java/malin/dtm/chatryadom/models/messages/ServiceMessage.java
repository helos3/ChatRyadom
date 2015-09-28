package malin.dtm.chatryadom.models.messages;

import android.os.Parcel;
import android.os.Parcelable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by dmt on 18.09.2015.
 */
public class ServiceMessage extends ViewMessage implements Parcelable {

    public ServiceMessage(String message) {
        super(message);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(message);
    }

    public static final Parcelable.Creator<ServiceMessage> CREATOR
            = new Parcelable.Creator<ServiceMessage>() {
        public ServiceMessage createFromParcel(Parcel in) {
            return new ServiceMessage(in);
        }

        public ServiceMessage[] newArray(int size) {
            return new ServiceMessage[size];
        }
    };

    private ServiceMessage(Parcel in) {
        super(in.readString());
    }


}
