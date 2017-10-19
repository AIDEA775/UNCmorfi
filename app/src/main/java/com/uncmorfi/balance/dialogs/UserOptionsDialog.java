package com.uncmorfi.balance.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.uncmorfi.R;
import com.uncmorfi.balance.backend.BalanceBackend;


public class UserOptionsDialog extends DialogFragment {
    public static final String ARG_USER = "user";
    public static final String ARG_CARD = "card";
    public static final String ARG_POS = "position";
    public static final String ARG_BACKEND = "backend";


    public static UserOptionsDialog newInstance(int userId, String userCard, int position,
                                                BalanceBackend backend) {
        Bundle args = new Bundle();

        args.putInt(ARG_USER, userId);
        args.putString(ARG_CARD, userCard);
        args.putInt(ARG_POS, position);
        args.putSerializable(ARG_BACKEND, backend);

        UserOptionsDialog fragment = new UserOptionsDialog();
        fragment.setArguments(args);

        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        final CharSequence[] items = new CharSequence[5];
        items[0] = getString(R.string.balance_user_options_update);
        items[1] = getString(R.string.balance_user_options_delete);
        items[2] = getString(R.string.balance_user_options_copy);
        items[3] = getString(R.string.balance_user_options_barcode);
        items[4] = getString(R.string.balance_user_options_set_name);

        final int userId = getArguments().getInt(ARG_USER);
        final String userCard =  getArguments().getString(ARG_CARD);
        final int position = getArguments().getInt(ARG_POS);
        final BalanceBackend backend = (BalanceBackend) getArguments().getSerializable(ARG_BACKEND);

        if (backend != null) {
            builder.setTitle(getString(R.string.balance_user_options_title))
                    .setItems(items, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case 0:
                                    backend.updateBalanceOfUser(userCard, new int[]{position});
                                    break;
                                case 1:
                                    DeleteUserDialog.newInstance(userId, userCard, position, backend)
                                            .show(getFragmentManager(), "DeleteUserDialog");
                                    break;
                                case 2:
                                    backend.copyCardToClipboard(userCard);
                                    break;
                                case 3:
                                    BarcodeDialog.newInstance(userCard, backend)
                                            .show(getFragmentManager(), "BarcodeDialog");
                                    break;
                                case 4:
                                    SetNameDialog.newInstance(userId, position, backend)
                                            .show(getFragmentManager(), "SetNameDialog");
                                    break;
                                default:
                                    break;
                            }
                        }
                    });
        }

        return builder.create();
    }
}
