package com.uncmorfi.balance.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.uncmorfi.R;
import com.uncmorfi.balance.backend.BalanceBackend;
import com.uncmorfi.balance.model.User;

import static com.uncmorfi.balance.dialogs.UserOptionsDialog.ARG_BACKEND;
import static com.uncmorfi.balance.dialogs.UserOptionsDialog.ARG_USER;


public class DeleteUserDialog extends DialogFragment {

    public static DeleteUserDialog newInstance(User user, BalanceBackend backend) {
        Bundle args = new Bundle();

        args.putSerializable(ARG_USER, user);
        args.putSerializable(ARG_BACKEND, backend);

        DeleteUserDialog fragment = new DeleteUserDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        final User user = (User) getArguments().getSerializable(ARG_USER);
        final BalanceBackend backend = (BalanceBackend) getArguments().getSerializable(ARG_BACKEND);

        if (backend != null && user != null) {
            DialogInterface.OnClickListener positiveListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    backend.deleteUser(user);
                    dismiss();
                }
            };

            DialogInterface.OnClickListener negativeListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dismiss();
                }
            };

            builder.setMessage(String.format(getString(R.string.balance_delete_user_title),
                        backend.getUserById(user.getId()).getName()))
                    .setPositiveButton(getString(R.string.balance_delete_user_positive),
                            positiveListener)
                    .setNegativeButton(getString(android.R.string.cancel), negativeListener);
        }
        return builder.create();
    }
}
