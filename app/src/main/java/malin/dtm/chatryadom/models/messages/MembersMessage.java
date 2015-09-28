package malin.dtm.chatryadom.models.messages;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

import malin.dtm.chatryadom.models.User;

/**
 * Created by dmt on 22.09.2015.
 */
public class MembersMessage extends ViewMessage implements Parcelable{
    private ArrayList<User> users = new ArrayList<>();

    public MembersMessage(int count) {
        super("Участников чата: " + count + " (нажмите для просмотра)");
    }

    public void addUser(User user) {
        users.add(user);
    }

    public ArrayList<User> getUsers() {
        return users;
    }

    public static final Parcelable.Creator<MembersMessage> CREATOR
            = new Parcelable.Creator<MembersMessage>() {
        public MembersMessage createFromParcel(Parcel in) {
            return new MembersMessage(in);
        }

        public MembersMessage[] newArray(int size) {
            return new MembersMessage[size];
        }
    };

    @SuppressWarnings("unchecked")
    private MembersMessage(Parcel in) {
        super(in.readString());
        users = in.readArrayList(User.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(message);
        dest.writeList(users);
    }
}
