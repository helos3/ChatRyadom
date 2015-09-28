package malin.dtm.chatryadom.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by dmt on 11.09.2015.
 */
public class HashWithSalt {
    private String hash;

    public HashWithSalt(String toHash, String salt) {
        try {
            StringBuilder sb = new StringBuilder();
            if (!toHash.isEmpty()) {
                MessageDigest md = MessageDigest.getInstance("MD5");
                String original = toHash + salt;
                md.update(original.getBytes());
                byte[] digest = md.digest();
                for (byte b: digest) {
                    sb.append(String.format("%02x", b & 0xff));
                }
            }
            this.hash = sb.toString();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getHash() {
        return hash;
    }

    public boolean checkHash(String hash){
        return this.hash.equals(hash);
    }
}
