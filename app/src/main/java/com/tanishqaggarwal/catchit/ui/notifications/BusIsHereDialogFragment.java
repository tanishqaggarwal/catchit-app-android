package com.tanishqaggarwal.catchit.ui.notifications;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;

public class BusIsHereDialogFragment extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Looks like the bus may be here soon. Press 'OK' once you're on board. If you're not taking the bus, press 'Cancel'.")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        boolean locationServicesEnabled = getActivity().getSharedPreferences("CatchItPreferences", 0).getBoolean("locationServicesEnabled", false);
                        if (locationServicesEnabled) {
                            //Activate the location tracker service, and have it send to the server every 30 s or so
                        }
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        return builder.create();
    }
}
