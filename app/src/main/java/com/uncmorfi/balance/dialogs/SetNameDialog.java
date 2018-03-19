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
import com.uncmorfi.balance.model.User;

import static com.uncmorfi.balance.dialogs.UserOptionsDialog.ARG_USER;

public class SetNameDialog extends DialogFragment {

    /**
     * @param user Puede no contener todos los datos del usuario, pero necesita:
     *             {@link User#getName()}
     */
    public static SetNameDialog newInstance(User user) {
        Bundle args = new Bundle();

        args.putSerializable(ARG_USER, user);

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

        final User user = (User) getArguments().getSerializable(ARG_USER);
        final BalanceBackend backend = BalanceBackend.getInstance(getContext());

        final EditText input = v.findViewById(R.id.set_name_input);
        Button save = v.findViewById(R.id.set_name_save);
        Button cancel = v.findViewById(R.id.set_name_cancel);

        if (backend != null && user != null) {
            input.append(user.getName());

            save.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    backend.updateNameOfUser(user, input.getText().toString());
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

    @Override
    public void onPause() {
        super.onPause();
        dismiss();
    }
}