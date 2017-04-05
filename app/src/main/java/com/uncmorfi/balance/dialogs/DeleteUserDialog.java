package com.uncmorfi.balance.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.uncmorfi.R;
import com.uncmorfi.balance.model.User;
import com.uncmorfi.balance.model.UserProvider;


public class DeleteUserDialog extends DialogFragment {
    public static final String ARG_USER = "user";

    public DeleteUserDialog() {}

    public static DeleteUserDialog newInstance(User user) {
        Bundle args = new Bundle();
        args.putSerializable(DeleteUserDialog.ARG_USER, user);

        DeleteUserDialog fragment = new DeleteUserDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        final User user = (User) getArguments().getSerializable(ARG_USER);

        if (user != null) {
            DialogInterface.OnClickListener positiveListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    saveChanges(user);
                    returnActivityResult();
                }
            };

            DialogInterface.OnClickListener negativeListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dismiss();
                }
            };

            builder.setMessage(String.format(getString(R.string.balance_delete_user_title), user.getName()))
                    .setPositiveButton(getString(R.string.balance_delete_user_positive), positiveListener)
                    .setNegativeButton(getString(R.string.balance_delete_user_negative), negativeListener);
        }
        return builder.create();
    }

    private void saveChanges(User user) {
        ContentResolver resolver = getActivity().getContentResolver();

        resolver.delete(
                ContentUris.withAppendedId(UserProvider.CONTENT_URI, user.getId()),
                null,
                null);
    }

    private void returnActivityResult() {
        Intent intent = new Intent();
        intent.putExtra("msg", R.string.balance_delete_user_msg);

        getTargetFragment().onActivityResult(
                getTargetRequestCode(),
                Activity.RESULT_OK,
                intent);
    }
}
