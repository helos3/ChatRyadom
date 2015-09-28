package malin.dtm.chatryadom.adapters;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import malin.dtm.chatryadom.R;
import malin.dtm.chatryadom.models.messages.ChatMessage;
import malin.dtm.chatryadom.models.User;
import malin.dtm.chatryadom.models.messages.MembersMessage;
import malin.dtm.chatryadom.models.messages.ServiceMessage;
import malin.dtm.chatryadom.models.messages.ViewMessage;
import malin.dtm.chatryadom.utils.CommonUtil;

/**
 * Created by dmt on 12.09.2015.
 */
public class ChatViewArrayAdapter extends ArrayAdapter<ViewMessage> {
    private List<ViewMessage> viewMessageList = new ArrayList<>();
    private Context context;
    private User me;

    // View lookup cache
    private static class ViewHolder {
        TextView header;
        TextView message;
    }

    @Override
    public void add(ViewMessage object) {
        viewMessageList.add(object);
        super.add(object);
    }

    public ChatViewArrayAdapter(Context context, User me) {
        super(context, R.layout.message);
        this.context = context;
        this.me = me;
    }

    public Date getLastDate() {
        ChatMessage last = null;
        for (ViewMessage viewMessage : viewMessageList) {
            if (viewMessage instanceof ChatMessage) {
                last = (ChatMessage) viewMessage;
            }
        }
        return  last != null ? last.getDate() : CommonUtil.getOnlyDate();
    }

    @Override
    public int getCount() {
        return this.viewMessageList.size();
    }

    @Override
    public ViewMessage getItem(int index) {
        return this.viewMessageList.get(index);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //findViewById() - тяжелая операция, поэтому используем объект-хранилище ViewHolder
        ViewHolder viewHolder; // view lookup cache stored in tag
        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.message, parent, false);
            viewHolder.message = (TextView) convertView.findViewById(R.id.msgr);
            viewHolder.header = (TextView) convertView.findViewById(R.id.header);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        //set
        ViewMessage msgObject = getItem(position);

        String textMessage = msgObject.getMessage();
        viewHolder.message.setText(textMessage);

        LayoutParams gravity = null;
        //если наследник msgObject ChatMessage собираем объект для ListView
        if (msgObject instanceof ChatMessage) {
            ChatMessage chatMessage = (ChatMessage)msgObject;
            gravity = getLayoutGravityBySender(chatMessage.getSender());
            if(chatMessage.getReceiver() != null)
                viewHolder.message.setBackgroundResource(R.drawable.text_view_private);
            else
                viewHolder.message.setBackgroundResource(R.drawable.text_view);

            String textHeader = chatMessage.buildHeader(me);
            viewHolder.header.setText(textHeader);
        } else if (msgObject instanceof ServiceMessage ||
                msgObject instanceof MembersMessage) {
            //сервисное сообщение
            gravity = getLeftGravity();
            viewHolder.message.setBackgroundResource(R.drawable.text_view_service);
            String header = msgObject.buildDefaultHeader();
            viewHolder.header.setText(header);
        }
        //выравниваем
        viewHolder.message.setLayoutParams(gravity);
        viewHolder.header.setLayoutParams(gravity);
        return convertView;
    }

    private LayoutParams getLeftGravity() {
        LayoutParams p = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        p.gravity = Gravity.START;
        return p;
    }

    //метод задает сторону отображения
    private LayoutParams getLayoutGravityBySender(User sender) {
        String senderKey = sender.getKey();
        LayoutParams p = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        p.gravity = me.getKey().equals(senderKey) ? Gravity.END : Gravity.START;
        return p;
    }
}
