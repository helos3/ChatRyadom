package malin.dtm.chatryadom;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;

import malin.dtm.chatryadom.models.User;

/**
 * Created by dmt on 15.09.2015.
 */
public class MainActivity extends AppCompatActivity {
    private static final String ME_TAG = "Me";
    private final int MIN_LENGTH = 4;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        User me  = readMeFromSettings();
        if (me != null) {
            start(me);
        }
        else {
            Button mSign = (Button) findViewById(R.id.sign);
            mSign.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EditText mUsername = (EditText) findViewById(R.id.username);
                    String username = mUsername.getText().toString();
                    if (username.length() >= MIN_LENGTH) {
                        User user = buildAndSaveUser(username);
                        start(user);
                    } else {
                        Context context = getApplicationContext();
                        int duration = Toast.LENGTH_SHORT;
                        String text = getString(R.string.username_warning);
                        Toast.makeText(context, text, duration).show();
                    }

                }
            });

            WelcomeDialogFragment dialog = new WelcomeDialogFragment();
            dialog.show(getSupportFragmentManager(), "WelcomeDialog");
        }
    }

    private void start(User me) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra(ME_TAG, me);
        startActivity(intent);
    }


    private User readMeFromSettings() {
        //Читаем из хранилища json-объект me, если нет вызываем диалог
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        String userJson = sharedPref.getString(ME_TAG, "");
        return new Gson().fromJson(userJson, User.class);
    }

    private User buildAndSaveUser(String username) {
        if (username.isEmpty()) {
            username = Build.MODEL;
        }
        //создаем пользователя
        User user = new User(username);
        //сериализуем
        String userJson = new Gson().toJson(user);
        //сохраняем в хранилище
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(ME_TAG, userJson);
        editor.apply();
        return user;
    }
}
