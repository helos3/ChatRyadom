package malin.dtm.chatryadom.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import malin.dtm.chatryadom.R;
import malin.dtm.chatryadom.models.User;
import malin.dtm.chatryadom.models.messages.MembersMessage;
import malin.dtm.chatryadom.utils.CommonUtil;

/**
 * Created by dmt on 22.09.2015.
 */
public class MembersViewArrayAdapter extends ArrayAdapter<User> {
    public MembersViewArrayAdapter(Context context, MembersMessage membersMessage) {
        super(context, android.R.layout.simple_list_item_1, membersMessage.getUsers());
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        User user = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.member, parent, false);
        }
        TextView userView = (TextView) convertView.findViewById(R.id.member_view);
        String firstUpper = CommonUtil.firstUpperCase(user.getName());
        userView.setText(firstUpper);
        // Return the completed view to render on screen
        return convertView;
    }

}
