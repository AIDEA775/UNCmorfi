package com.uncmorfi.balance.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.uncmorfi.R;
import com.uncmorfi.balance.backend.BalanceBackend;

import static com.uncmorfi.balance.dialogs.UserOptionsDialog.ARG_BACKEND;
import static com.uncmorfi.balance.dialogs.UserOptionsDialog.ARG_POS;
import static com.uncmorfi.balance.dialogs.UserOptionsDialog.ARG_USER;


public class SetNameDialog extends DialogFragment {

    public static SetNameDialog newInstance(int userId, int position, BalanceBackend backend) {
        Bundle args = new Bundle();

        args.putInt(ARG_USER, userId);
        args.putInt(ARG_POS, position);
        args.putSerializable(ARG_BACKEND, backend);

        SetNameDialog fragment = new SetNameDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        View v = View.inflate(getContext(), R.layout.dialog_set_name, null);
        builder.setView(v);

        final int userId = getArguments().getInt(ARG_USER);
        final int position = getArguments().getInt(ARG_POS);
        final BalanceBackend backend = (BalanceBackend) getArguments().getSerializable(ARG_BACKEND);

        final EditText input = (EditText) v.findViewById(R.id.set_name_input);
        Button save = (Button) v.findViewById(R.id.set_name_save);
        Button cancel = (Button) v.findViewById(R.id.set_name_cancel);

        if (backend != null) {
            input.append(backend.getUserById(userId).getName());

            save.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    backend.updateNameOfUser(userId, position, input.getText().toString());
                    dismiss();
                }
            });

            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });
        }

        return showKeyboard(builder.create());
    }

    private AlertDialog showKeyboard(AlertDialog dialog) {
        Window window = dialog.getWindow();
        if (window != null)
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        return dialog;
    }
}