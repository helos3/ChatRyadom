package malin.dtm.chatryadom.controllers.mongo;

import android.os.Handler;


import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;


import org.bson.Document;

import java.util.Date;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import malin.dtm.chatryadom.models.User;
import malin.dtm.chatryadom.models.messages.ChatMessage;
import malin.dtm.chatryadom.models.mongo.MongoLoadHistoryParameter;
import malin.dtm.chatryadom.models.mongo.MongoMember;
import malin.dtm.chatryadom.models.mongo.MongoMessage;
import malin.dtm.chatryadom.models.mongo.MongoUpdateMemberStateParameter;
import malin.dtm.chatryadom.models.mongo.MongoUpdateHistoryParameter;

/**
 * Created by dmt on 16.09.2015.
 */
public class MongoController {
    private final String dbName = "chat";

    private MongoClient mongoClient;

    private BlockingDeque<MongoUpdateHistoryParameter> updateHistoryQueue = new LinkedBlockingDeque<>();
    private Thread updateHistoryThread = null;

    private BlockingDeque<MongoUpdateMemberStateParameter> updateMemberStateQueue = new LinkedBlockingDeque<>();
    private Thread updateMemberStateThread = null;

    private Handler loadHistoryHandler;
    private Thread loadHistoryThread = null;

    private Handler loadMembersHandler;
    private Thread loadMembersThread = null;

    public MongoController(Handler loadHistoryHandler, Handler loadMembersHandler) {
        connectionInit();
        this.loadHistoryHandler = loadHistoryHandler;
        this.loadMembersHandler = loadMembersHandler;
    }

    private void connectionInit() {
        String uri = "mongodb://188.166.37.228:27017";
        MongoClientURI connectionString = new MongoClientURI(uri);
        mongoClient = new MongoClient(connectionString);
    }

    //#region история
    public MongoCollection<Document> getHistoryCollection() {
        MongoDatabase database = mongoClient.getDatabase(dbName);
        return database.getCollection("history");
    }

    public Handler getLoadHistoryHandler() {
        return loadHistoryHandler;
    }

    public BlockingDeque<MongoUpdateHistoryParameter> getUpdateHistoryQueue() {
        return updateHistoryQueue;
    }

    public void loadHistory(User user, Date fromDate, String quadIndex) {
        MongoLoadHistoryParameter data = new MongoLoadHistoryParameter(user, fromDate, quadIndex);
        loadHistoryThread = new Thread(new MongoLoadHistory(this, data));
        loadHistoryThread.start();
    }

    private void buildThreadUpdateHistory() {
        buildThread(updateHistoryThread, new MongoUpdateHistory(this));
    }

    public void updateHistory(ChatMessage message, String indexQuad) {
        MongoMessage mongoMessage = new MongoMessage(message);
        MongoUpdateHistoryParameter data = new MongoUpdateHistoryParameter(mongoMessage, indexQuad);
        buildThreadUpdateHistory();
        //Adds a message to internal blocking queue
        try {
            updateHistoryQueue.putLast(data);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    //#endregion

    //#region пользователи
    public MongoCollection<Document> getMembersCollection() {
        MongoDatabase database = mongoClient.getDatabase(dbName);
        return database.getCollection("members");
    }

    public Handler getLoadMembersHandler() {
        return loadMembersHandler;
    }

    public BlockingDeque<MongoUpdateMemberStateParameter> getUpdateMemberStateQueue() {
        return updateMemberStateQueue;
    }

    public void loadMembers(String quadIndex) {
        loadMembersThread = new Thread(new MongoLoadMembers(this, quadIndex));
        loadMembersThread.start();
    }

    private void buildThreadUpdateMemberState() {
        buildThread(updateMemberStateThread, new MongoUpdateMemberState(this));
    }

    public void updateMemberState(String quadIndex, User user) {
        buildThreadUpdateMemberState();
        MongoMember member = new MongoMember(user);
        MongoUpdateMemberStateParameter data = new MongoUpdateMemberStateParameter(quadIndex, member);
        try {
            updateMemberStateQueue.putLast(data);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    //#endregion

    private void buildThread(Thread thread, Runnable runnable) {
        if (thread == null || !thread.isAlive()) {
            thread = new Thread(runnable);
            thread.start();
        }
    }

    public void close() {
        closeThread(loadHistoryThread);
        closeThread(loadMembersThread);
        closeThread(updateHistoryThread);
        closeThread(updateMemberStateThread);
    }

    private void closeThread(Thread thread) {
        if (thread != null && thread.isAlive()) {
            thread.interrupt();
        }
    }
}