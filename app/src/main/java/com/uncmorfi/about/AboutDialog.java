package com.uncmorfi.about;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;

import com.uncmorfi.R;


public class AboutDialog extends DialogFragment {

    public AboutDialog() {
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return createNewCardDialog();
    }

    public AlertDialog createNewCardDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        View v = View.inflate(getContext(), R.layout.dialog_about, null);

        builder.setView(v);

        return builder.create();
    }

}
