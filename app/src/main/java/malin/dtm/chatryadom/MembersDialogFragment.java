package malin.dtm.chatryadom;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import malin.dtm.chatryadom.adapters.MembersViewArrayAdapter;
import malin.dtm.chatryadom.models.User;
import malin.dtm.chatryadom.models.messages.MembersMessage;

/**
 * Created by dmt on 22.09.2015.
 */
public class MembersDialogFragment extends AppCompatDialogFragment {

    @Override
    public AppCompatDialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        Bundle bundle = getArguments();
        MembersMessage msg = bundle.getParcelable("msg");

        View view = getActivity().getLayoutInflater().inflate(R.layout.members_dialog, null);
        ListView listView = (ListView) view.findViewById(R.id.membersList);

        MembersViewArrayAdapter adapter = new MembersViewArrayAdapter(getActivity(), msg);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MembersViewArrayAdapter adapter = (MembersViewArrayAdapter) parent.getAdapter();
                User user = adapter.getItem(position);
                ((ChatActivity)getActivity()).setTo(user);
                dismiss();
            }
        });

        builder.setView(view)
                .setTitle(R.string.count)
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dismiss();
                    }
                });

        return builder.create();
    }








}
