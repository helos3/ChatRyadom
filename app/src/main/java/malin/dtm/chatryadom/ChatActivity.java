package malin.dtm.chatryadom;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Date;

import malin.dtm.chatryadom.adapters.ChatViewArrayAdapter;
import malin.dtm.chatryadom.controllers.mongo.MongoController;
import malin.dtm.chatryadom.controllers.QuadController;
import malin.dtm.chatryadom.controllers.rabbit.RabbitController;
import malin.dtm.chatryadom.models.messages.ChatMessage;
import malin.dtm.chatryadom.models.messages.MembersMessage;
import malin.dtm.chatryadom.models.messages.ServiceMessage;
import malin.dtm.chatryadom.models.messages.ViewMessage;
import malin.dtm.chatryadom.models.rabbit.RabbitPublishData;
import malin.dtm.chatryadom.models.rabbit.RabbitSubscribeData;
import malin.dtm.chatryadom.models.User;
import malin.dtm.chatryadom.utils.CommonUtil;

/**
 * Created by dmt on 11.09.2015.
 */
public class ChatActivity extends AppCompatActivity implements
        ConnectionCallbacks, OnConnectionFailedListener, LocationListener {
    private static final String ME_TAG = "Me";
    private static final int QUAD_TREE_DEPTH = 25;
    private static final int QUAD_TREE_ACCURACY = 7;
    private static final int INTERVAL_UPDATE = 10000;
    /**
     * Хост-пользователь
     */
    protected User me;
    /**
     * Пользователь, принимающий приватные сообщения
     */
    protected User to;
    /**
     * Адаптер для чата ViewMessage <-- ChatViewArrayAdapter -- > ListView
     */
    protected ChatViewArrayAdapter mChatViewArrayAdapter;
    /**
     * Контроллер предоставляет данные по квадрантам
     */
    protected QuadController mQuadController;
    /**
     * Контроллер MongoDB для записи и обновления истории чата
     */
    protected MongoController mMongoController;
    /**
     * Провайдер Google Play services.
     */
    protected GoogleApiClient mGoogleApiClient;
    /**
     *  Хранилище параметров для запросов к FusedLocationProviderApi.
     */
    protected LocationRequest mLocationRequest;
    /**
     * Предоставляет последние данные локации
     */
    protected Location mLastLocation;
    /**
     * Контроллер управляет логикой публикации/подписки AMQP
     */
    protected RabbitController mRabbitController;
    /**
     * Handler для обработки входящих сообщений от Rabbit
     */
    protected Handler mIncomingHandler;
    /**
     * Handler для прогрузки истории чата
     */
    protected Handler mLoadHistoryHandler;
    /**
     * Handler для прогрузки количества пользователей
     */
    protected Handler mLoadMembersHandler;
    /**
     * UI
     */
    private EditText mChatText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_activity);

        Intent intent = getIntent();
        me = intent.getParcelableExtra(ME_TAG);

        setupLoadHistoryHandler();
        setupIncomingHandler();
        setupLoadMembersHandler();

        mChatViewArrayAdapter = new ChatViewArrayAdapter(this, me);
        mQuadController = new QuadController(QUAD_TREE_DEPTH, QUAD_TREE_ACCURACY);
        mRabbitController = new RabbitController(mIncomingHandler);
        mMongoController = new MongoController(mLoadHistoryHandler, mLoadMembersHandler);

        setupChatText();
        setupChatView();
        setupButtonSend();
        setupButtonCount();

        buildGoogleApiClient();
    }

    /**
     * Создание клиента GoogleApi. Используется addApi() для запросов к LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();
    }

    /**
     * Создание хранилища запросов для обновления локации
     */
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL_UPDATE);
        mLocationRequest.setFastestInterval(INTERVAL_UPDATE / 2);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /**
     * Установка поведения UI EditText
     */
    private void setupChatText() {
        mChatText = (EditText) findViewById(R.id.msg);
        mChatText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    return sendChatMessage();
                }
                return false;
            }
        });
    }

    /**
     * Обработчик входящих сообщений от Rabbit. Json --> ChatMessage
     */
    private void setupIncomingHandler() {
        mIncomingHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                String message = msg.getData().getString("msg");
                ChatMessage chatMessage = new Gson().fromJson(message, ChatMessage.class);
                updateHistory(chatMessage);
                mChatViewArrayAdapter.add(chatMessage);
                return true;
            }
        });
    }

    /**
     * Обработчик истории от MongoDB
     */
    private void setupLoadHistoryHandler() {
        mLoadHistoryHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                ArrayList<ChatMessage> chatMessages = msg.getData().getParcelableArrayList("msg");
                for (ChatMessage chatMessage : chatMessages) {
                    mChatViewArrayAdapter.add(chatMessage);
                }
                return true;
            }
        });
    }
    /**
     * Обработчик сервисных сообщений (количество пользователей)
     */
    private void setupLoadMembersHandler() {
        mLoadMembersHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                MembersMessage membersMessage = msg.getData().getParcelable("msg");
                mChatViewArrayAdapter.add(membersMessage);
                return true;
            }
        });
    }

    /**
     * Метод загружает историю от MongoDB
     */
    private void loadChatHistory(Date fromDate) {
        String indexQuad = mQuadController.getIndexQuad();
        mMongoController.loadHistory(me, fromDate, indexQuad);
    }

    /**
     * Метод обновляет историю MongoDB
     * @param chatMessage - сообщение чата
     */
    private void updateHistory(ChatMessage chatMessage) {
        String senderKey = chatMessage.getSender().getKey();
        if (me.getKey().equals(senderKey)) {
            String indexQuad = mQuadController.getIndexQuad();
            mMongoController.updateHistory(chatMessage, indexQuad);
        }
    }

    /**
     * Метод устанавливает поведение для ListView. При выборе элемента пользователем устанавливается объект to
     */
    private void setupChatView(){
        ListView mChatView = (ListView) findViewById(R.id.msgview);
        mChatView.setAdapter(mChatViewArrayAdapter);
        mChatView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ChatViewArrayAdapter adapter = (ChatViewArrayAdapter) parent.getAdapter();
                ViewMessage viewMessage = adapter.getItem(position);
                if (viewMessage instanceof ChatMessage) {
                    ChatMessage chatMessage = (ChatMessage) viewMessage;
                    to = chatMessage.getSender();
                    String name = chatMessage.getSender().getName();
                    mChatText.setText(name);
                    mChatText.setSelection(name.length());
                } else if (viewMessage instanceof MembersMessage) {
                    MembersDialogFragment dialog = new MembersDialogFragment();
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("to", to);
                    bundle.putParcelable("msg", (MembersMessage) viewMessage);
                    dialog.setArguments(bundle);
                    dialog.show(getSupportFragmentManager(), "MembersDialog");
                }
            }
        });
    }

    /**
     * Обработчик кнопки отправки
     */
    private void setupButtonSend() {
        Button mButtonSend = (Button) findViewById(R.id.send);
        mButtonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                sendChatMessage();
            }
        });
    }
    /**
     * Обработчик кнопки получения количества пользователей
     */
    private void setupButtonCount() {
        Button mButtonCount = (Button) findViewById(R.id.count);
        mButtonCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String indexQuad = mQuadController.getIndexQuad();
                mMongoController.loadMembers(indexQuad);
            }
        });
    }

    /**
     * Метод отправляет сообщение в очередь Rabbit
     * @return true
     */
    private boolean sendChatMessage() {
        String message = mChatText.getText().toString();
        if (message.isEmpty())
            return false;
        ChatMessage chatMessage = buildChatMessage(message);
        if (isPrivateMessage(chatMessage)) {
            mRabbitController.privatePublish(chatMessage);
        }
        else {
            String quadIndex = mQuadController.getIndexQuad();
            mRabbitController.publicPublish(chatMessage, quadIndex);

        }
        mChatText.setText("");
        return true;
    }

    /**
     * Метод создает приватное или публичное сообщение
     * @param msg текстовое сообщение
     * @return ChatMessage
     */
    private ChatMessage buildChatMessage(String msg) {
        ChatMessage chatMessage;
        //если есть получатель и пользователь не отменил отправку (не стер)
        if (to != null && msg.contains(to.getName())) {
            String contain = to.getName();
            int subIndex = msg.indexOf(contain) + contain.length();
            msg = msg.substring(subIndex);
            chatMessage = new ChatMessage(msg, me, to);
        }
        else {
            chatMessage = new ChatMessage(msg, me);
        }
        return chatMessage;
    }

    private boolean isPrivateMessage(ChatMessage chatMessage) {
        return chatMessage.getReceiver() != null;
    }

    /**
     * Событие соединения с GoogleService.
     * @param bundle контейнер
     */
    @Override
    public void onConnected(Bundle bundle) {
        // Provides a simple way of getting a device's location and is well suited for
        // applications that do not require a fine-grained location and that do not need location
        // updates. Gets the best and most recent location currently available, which may be null
        // in rare cases when a location is not available.
        if(mLastLocation == null)
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        //Включаем запросы на обновление локаций
        startLocationUpdates();
        //Обновляем состояние чата
        updateStateChatAndReconnectDialog();

    }

    /**
     * Обновляем состояние чата и в случае ошибки вызываем диалог переподключения
     */
    private void updateStateChatAndReconnectDialog() {
        Integer warningId = updateChatState();
        if (warningId != null){
            Bundle bundle = new Bundle();
            bundle.putString("msg", getString(warningId));

            ReconnectDialogFragment dialogFragment = new ReconnectDialogFragment();
            dialogFragment.setArguments(bundle);
            dialogFragment.show(getSupportFragmentManager(), "ReconnectDialog");
        }
    }

    /**
     * Обновляем состояние чата если изменился квадрант
     */
    private Integer updateChatState() {
        Integer warningId = null;

        if(mLastLocation == null)
            return R.string.not_found_location;

        if(!isOnline())
            return R.string.not_internet_connection;

        //обновляем квадрант
        mQuadController.update(mLastLocation);
        //если квадрант изменен
        if (mQuadController.changed()) {
            addServiceMessage(me.getName() + getString(R.string.new_chat_title));
            //новый индекс квадранта
            String indexQuad = mQuadController.getIndexQuad();
            //обновляем Rabbit подписку
            updateRabbit(indexQuad);
            //покидаем квадрант и входим в новый
            mMongoController.updateMemberState(indexQuad, me);
            //загружаем историю для этого квадранта
            loadChatHistory(CommonUtil.getOnlyDate());
        }
        return warningId;
    }

    /**
     * Обновляем Rabbit публикации и подписки
     * @param quadRoutingKey - ключ квадранта
     */
    private void updateRabbit(String quadRoutingKey) {
        mRabbitController.destroyPublish();
        mRabbitController.destroySubscribe();
        mRabbitController.buildPublish();
        mRabbitController.buildSubscribe(me, quadRoutingKey);
    }

    @Override
    public void onConnectionSuspended(int i) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        mGoogleApiClient.connect();
    }

    /**
     * Событие изменения локации
     * @param location
     */
    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        updateStateChatAndReconnectDialog();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;
        String text = "Connection failed: ConnectionResult.getErrorCode() = " + connectionResult.getErrorCode();

        Toast.makeText(context, text, duration).show();

    }

    @Override
    protected void onStart() {
        super.onStart();
        //подключаем Google Api
        mGoogleApiClient.connect();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        //открываем последнюю подписку к Rabbit
        mRabbitController.open();
        //входим в квадрант
        String indexQuad = mQuadController.getIndexQuad();
        mMongoController.updateMemberState(indexQuad, me);
        //загружаем историю
        loadChatHistory(mChatViewArrayAdapter.getLastDate());
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopLocationUpdates();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        //покидаем квадрант
        mMongoController.updateMemberState("", me);
        //останавливаем Rabbit и Mongo
        mRabbitController.close();
        mMongoController.close();
    }

    /**
     * Requests location updates from the FusedLocationApi.
     */
    protected void startLocationUpdates() {
        // The final argument to {@code requestLocationUpdates()} is a LocationListener
        // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }
    /**
     * Removes location updates from the FusedLocationApi.
     */
    protected void stopLocationUpdates() {
        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.

        // The final argument to {@code requestLocationUpdates()} is a LocationListener
        // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    protected void addServiceMessage(String message) {
        ServiceMessage serviceMessage = new ServiceMessage(message);
        mChatViewArrayAdapter.add(serviceMessage);

    }

    /**
     * Публичный метод устанавливает получателя
     * @param user
     */
    public void setTo(User user) {
        to = user;
        mChatText.setText(user.getName());
    }

    /**
     * Публичный метод переподключает клиента
     */
    public void reconnect() {
        stopLocationUpdates();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        mGoogleApiClient.connect();
    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}
