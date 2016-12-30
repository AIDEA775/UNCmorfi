package com.uncmorfi.balance.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.uncmorfi.R;
import com.uncmorfi.balance.BalanceFragment;
import com.uncmorfi.balance.UserCursorAdapter.UserViewHolder;
import com.uncmorfi.balance.model.User;


public class UserOptionsDialog extends DialogFragment {
    public static final String ARG_USER = "user";
    public static final String ARG_CARD = "card";
    public static final String ARG_HOLDER = "holder";


    public static UserOptionsDialog newInstance(User user, UserViewHolder holder) {
        Bundle args = new Bundle();

        args.putSerializable(ARG_USER, user);
        args.putSerializable(ARG_HOLDER, holder);

        UserOptionsDialog fragment = new UserOptionsDialog();
        fragment.setArguments(args);

        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        final CharSequence[] items = new CharSequence[3];
        items[0] = getString(R.string.balance_user_options_refresh);
        items[1] = getString(R.string.balance_user_options_delete);
        items[2] = getString(R.string.balance_user_options_set_name);

        final User user = (User) getArguments().getSerializable(ARG_USER);

        if (user != null) {
            builder.setTitle(getString(R.string.balance_user_options_title))
                    .setItems(items, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case 0:
                                    refreshUser(user.getCard());
                                    break;
                                case 1:
                                    showDeleteUserDialog(user);
                                    break;
                                case 2:
                                    showSetNameDialog(user);
                                    break;
                                default:
                                    break;
                            }
                        }
                    });
        } else {
            builder.setTitle("No existe la tarjeta");
        }
        return builder.create();
    }

    private void refreshUser(String card) {
        Intent intent = new Intent();
        intent.putExtra(ARG_CARD, card);
        intent.putExtra(ARG_HOLDER, getArguments().getSerializable(ARG_HOLDER));
        getTargetFragment().onActivityResult(
                getTargetRequestCode(),
                Activity.RESULT_OK,
                intent);
    }

    private void showDeleteUserDialog(User user) {
        DeleteUserDialog deleteDialog = DeleteUserDialog.newInstance(user);

        deleteDialog.setTargetFragment(getTargetFragment(),
                BalanceFragment.UPDATE_REQUEST_CODE);

        deleteDialog.show(getFragmentManager(), "DeleteUserDialog");
    }

    private void showSetNameDialog(User user) {
        SetNameDialog setNameDialog = SetNameDialog.newInstance(user);

        setNameDialog.setTargetFragment(getTargetFragment(),
                BalanceFragment.UPDATE_REQUEST_CODE);

        setNameDialog.show(getFragmentManager(), "SetNameDialog");
    }
}
