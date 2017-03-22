package com.uncmorfi.balance.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.InputFilter;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.uncmorfi.R;


public class NewUserDialog extends DialogFragment {
    public static final String ARG_CARD = "card";

    public NewUserDialog() {}

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        View v = View.inflate(getContext(), R.layout.dialog_new_user, null);
        builder.setView(v);

        Button agree = (Button) v.findViewById(R.id.new_card_button);
        final EditText input = (EditText) v.findViewById(R.id.new_card_id);
        input.setFilters(new InputFilter[] {new InputFilter.AllCaps()});

        agree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                returnActivityResult(input);
                dismiss();
            }
        });

        return showKeyboard(builder.create());
    }

    private void returnActivityResult(EditText input) {
        Intent intent = new Intent();
        intent.putExtra(ARG_CARD, input.getText().toString());
        getTargetFragment().onActivityResult(
                getTargetRequestCode(),
                Activity.RESULT_OK,
                intent);
    }

    private AlertDialog showKeyboard(AlertDialog dialog) {
        Window window = dialog.getWindow();
        if (window != null)
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        return dialog;
    }

}