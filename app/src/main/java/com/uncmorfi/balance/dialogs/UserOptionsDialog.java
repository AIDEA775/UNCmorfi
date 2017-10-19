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


public class UserOptionsDialog extends DialogFragment {
    public static final String ARG_USER = "user";
    public static final String ARG_BACKEND = "backend";


    public static UserOptionsDialog newInstance(User user, BalanceBackend backend) {
        Bundle args = new Bundle();

        args.putSerializable(ARG_USER, user);
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

        final User user = (User) getArguments().getSerializable(ARG_USER);
        final BalanceBackend backend = (BalanceBackend) getArguments().getSerializable(ARG_BACKEND);

        if (backend != null && user != null) {
            builder.setTitle(getString(R.string.balance_user_options_title))
                    .setItems(items, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case 0:
                                    backend.updateBalanceOfUser(
                                            user.getCard(), new int[]{user.getPosition()});
                                    break;
                                case 1:
                                    DeleteUserDialog.newInstance(user, backend)
                                            .show(getFragmentManager(), "DeleteUserDialog");
                                    break;
                                case 2:
                                    backend.copyCardToClipboard(user.getCard());
                                    break;
                                case 3:
                                    BarcodeDialog.newInstance(user, backend)
                                            .show(getFragmentManager(), "BarcodeDialog");
                                    break;
                                case 4:
                                    SetNameDialog.newInstance(user, backend)
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
