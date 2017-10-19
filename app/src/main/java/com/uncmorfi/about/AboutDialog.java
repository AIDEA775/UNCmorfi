package com.uncmorfi.about;

import android.app.Dialog;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.TextView;

import com.uncmorfi.R;

public class AboutDialog extends DialogFragment {

    public AboutDialog() {}

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return createNewCardDialog();
    }

    public AlertDialog createNewCardDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        View v = View.inflate(getContext(), R.layout.dialog_about, null);

        TextView versionText = (TextView) v.findViewById(R.id.version_name);
        versionText.setText(getVersionName());

        builder.setView(v);
        return builder.create();
    }

    private String getVersionName() {
        String versionName = "";
        try {
            PackageInfo packageInfo = getContext().getPackageManager()
                    .getPackageInfo(getContext().getPackageName(), 0);
            versionName = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionName;
    }
}
