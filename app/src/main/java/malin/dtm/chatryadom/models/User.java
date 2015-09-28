package malin.dtm.chatryadom.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

import malin.dtm.chatryadom.utils.HashWithSalt;

/**
 * Created by dmt on 11.09.2015.
 */
public class User implements Parcelable{
    private String name;
    private String key;

    public User(String name, String key) {
        this.name = name;
        this.key = key;
    }

    public User (String name) {
        this.name = name;
        buildKey();
    }

    public String getName() {
        return name;
    }

    public String getKey() {
        return key;
    }

    private void buildKey() {
        Long unixTime = System.currentTimeMillis();
        HashWithSalt hashWithSalt = new HashWithSalt(this.name, unixTime.toString());
        key = hashWithSalt.getHash();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(key);
    }

    public static final Parcelable.Creator<User> CREATOR
            = new Parcelable.Creator<User>() {
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        public User[] newArray(int size) {
            return new User[size];
        }
    };

    private User(Parcel in) {
        name = in.readString();
        key = in.readString();
    }


}
