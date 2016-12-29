package com.uncmorfi.balance.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.uncmorfi.R;
import com.uncmorfi.balance.model.User;
import com.uncmorfi.balance.model.UserProvider;
import com.uncmorfi.balance.model.UsersContract;


public class SetNameDialog extends DialogFragment {
    public static final String ARG_USER = "user";

    public SetNameDialog() {}

    public static SetNameDialog newInstance(User user) {

        Bundle args = new Bundle();
        args.putSerializable(SetNameDialog.ARG_USER, user);

        SetNameDialog fragment = new SetNameDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        View v = View.inflate(getContext(), R.layout.dialog_set_name, null);
        builder.setView(v);

        final User user = (User) getArguments().getSerializable(ARG_USER);

        final EditText input = (EditText) v.findViewById(R.id.new_name_text);
        Button agree = (Button) v.findViewById(R.id.save_new_name_button);

        if (user != null) {
            input.setText(user.getName());

            agree.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ContentResolver resolver = getActivity().getContentResolver();
                    ContentValues values = new ContentValues();
                    values.put(UsersContract.UserEntry.NAME, input.getText().toString());
                    resolver.update(
                            ContentUris.withAppendedId(UserProvider.CONTENT_URI, user.getId()),
                            values,
                            null,
                            null
                    );
                    getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, null);
                    dismiss();
                }
            });
        }
        return builder.create();
    }

}