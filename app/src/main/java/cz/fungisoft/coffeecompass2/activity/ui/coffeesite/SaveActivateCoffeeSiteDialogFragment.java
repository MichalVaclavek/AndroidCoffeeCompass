package cz.fungisoft.coffeecompass2.activity.ui.coffeesite;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import cz.fungisoft.coffeecompass2.R;

public class SaveActivateCoffeeSiteDialogFragment extends DialogFragment {

    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface SaveActivateCoffeeSiteDialogListener {

        void onDialogPositiveClick(DialogFragment dialog);
        void onDialogNeutralClick(DialogFragment dialog);
        void onDialogNegativeClick(DialogFragment dialog);
    }

    // Use this instance of the interface to deliver action events
    SaveActivateCoffeeSiteDialogListener listener;

    // Override the Fragment.onAttach() method to instantiate the DeleteUserAccountDialogListener
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the DeleteUserAccountDialogListener so we can send events to the host
            listener = (SaveActivateCoffeeSiteDialogListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(listener.toString()
                    + " must implement SaveActivateCoffeeSiteDialogListener");
        }
    }

    /**
     * Neutral and Negative buttons meaning is swapped here as we need to have second
     * option next to first one, not the last one neutral.
     *
     * @param savedInstanceState
     * @return
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.save_and_activate_dialog_title)
                .setMessage(R.string.save_or_saveactivate_coffeesite_dialog_question)
                .setPositiveButton(R.string.save_and_activate_coffeesite_confirm, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        listener.onDialogPositiveClick(SaveActivateCoffeeSiteDialogFragment.this);
                    }
                })
                .setNeutralButton(R.string.cancel_coffeesite_save, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Send the negative button event back to the host activity
                        listener.onDialogNeutralClick(SaveActivateCoffeeSiteDialogFragment.this);
                    }
                })
                .setNegativeButton(R.string.save_only_coffeesite_confirm, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Send the negative button event back to the host activity
                        listener.onDialogNegativeClick(SaveActivateCoffeeSiteDialogFragment.this);
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }

}
