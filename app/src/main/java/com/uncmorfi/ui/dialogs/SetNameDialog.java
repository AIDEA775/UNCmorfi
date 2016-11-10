package com.uncmorfi.ui.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.uncmorfi.R;

public class SetNameDialog extends DialogFragment {
    public interface OnSetNameListener {
        void setName(String card, String name);
    }

    OnSetNameListener listener;

    public SetNameDialog() {
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return createNewCardDialog();
    }

    public AlertDialog createNewCardDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        View v = View.inflate(getContext(), R.layout.dialog_set_name, null);

        builder.setView(v);

        Button agree = (Button) v.findViewById(R.id.save_new_name_button);
        final EditText input = (EditText) v.findViewById(R.id.new_name_text);
        input.setText(getArguments().getString("oldName"));

        agree.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Añadir tarjeta
                        listener.setName(getArguments().getString("card"),
                                input.getText().toString());
                        dismiss();
                    }
                }
        );
        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (OnSetNameListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(
                    context.toString() +
                            " no implementó OnSetNameListener");
        }
    }

}