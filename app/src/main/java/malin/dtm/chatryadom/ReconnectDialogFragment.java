package malin.dtm.chatryadom;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.app.AppCompatDialogFragment;

/**
 * Created by dmt on 22.09.2015.
 */
public class ReconnectDialogFragment extends AppCompatDialogFragment {
    @Override
    public AppCompatDialog onCreateDialog(Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        String msg = bundle.getString("msg");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(msg)
                .setMessage(R.string.reconnect)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ((ChatActivity)getActivity()).reconnect();
                        dismiss();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                       dismiss();
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }

}
