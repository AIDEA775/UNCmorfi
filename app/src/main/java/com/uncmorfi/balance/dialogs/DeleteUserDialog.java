package com.uncmorfi.balance.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.uncmorfi.R;
import com.uncmorfi.balance.backend.BalanceBackend;

import static com.uncmorfi.balance.dialogs.UserOptionsDialog.ARG_BACKEND;
import static com.uncmorfi.balance.dialogs.UserOptionsDialog.ARG_POS;
import static com.uncmorfi.balance.dialogs.UserOptionsDialog.ARG_USER;


public class DeleteUserDialog extends DialogFragment {

    public static DeleteUserDialog newInstance(int userId, int position, BalanceBackend backend) {
        Bundle args = new Bundle();

        args.putInt(ARG_USER, userId);
        args.putInt(ARG_POS, position);
        args.putSerializable(ARG_BACKEND, backend);

        DeleteUserDialog fragment = new DeleteUserDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        final int userId = getArguments().getInt(ARG_USER);
        final int position = getArguments().getInt(ARG_POS);
        final BalanceBackend backend = (BalanceBackend) getArguments().getSerializable(ARG_BACKEND);

        if (backend != null) {
            DialogInterface.OnClickListener positiveListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    backend.deleteUser(userId, position);
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
                        backend.getUserById(userId).getName()))
                    .setPositiveButton(getString(R.string.balance_delete_user_positive),
                            positiveListener)
                    .setNegativeButton(getString(android.R.string.cancel), negativeListener);
        }
        return builder.create();
    }
}
