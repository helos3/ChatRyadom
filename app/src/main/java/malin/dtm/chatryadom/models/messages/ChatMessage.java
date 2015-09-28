package malin.dtm.chatryadom.models.messages;

import android.os.Parcel;
import android.os.Parcelable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import malin.dtm.chatryadom.models.User;

/**
 * Created by dmt on 11.09.2015.
 */
public class ChatMessage extends ViewMessage implements Parcelable{
    protected User sender;
    protected User receiver;

    public ChatMessage(String message, User sender) {
        super(message);
        this.sender = sender;
    }

    public ChatMessage(String message, User sender, User receiver) {
        super(message);
        this.sender = sender;
        this.receiver = receiver;
    }

    public ChatMessage(String message, Date date, User sender, User receiver) {
        super(message, date);
        this.sender = sender;
        this.receiver = receiver;
    }

    public User getSender() {
        return sender;
    }

    public User getReceiver(){
        return receiver;
    }

    public String buildHeader(User me) {
        SimpleDateFormat ft = new SimpleDateFormat("HH:mm", Locale.getDefault());
        StringBuilder sb = new StringBuilder();
        String keyMe = me.getKey();
        //если отправитель НЕ пользователь
        if(!sender.getKey().equals(keyMe)) {
            sb.append(sender.getName());
            sb.append(", ");
        } else if (receiver != null) {
            //если пользователь отправил приватное сообщение
            sb.append("-> ");
            sb.append(receiver.getName());
            sb.append(", ");
        }
        sb.append(ft.format(date));
        return sb.toString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(message);
        dest.writeSerializable(date);
        dest.writeParcelable(sender, flags);
        dest.writeParcelable(receiver, flags);

    }

    public static final Parcelable.Creator<ChatMessage> CREATOR
            = new Parcelable.Creator<ChatMessage>() {
        public ChatMessage createFromParcel(Parcel in) {
            return new ChatMessage(in);
        }

        public ChatMessage[] newArray(int size) {
            return new ChatMessage[size];
        }
    };

    private ChatMessage(Parcel in) {
        super(in.readString(), (Date)in.readSerializable());
        sender = in.readParcelable(User.class.getClassLoader());
        receiver = in.readParcelable(User.class.getClassLoader());
    }
}
