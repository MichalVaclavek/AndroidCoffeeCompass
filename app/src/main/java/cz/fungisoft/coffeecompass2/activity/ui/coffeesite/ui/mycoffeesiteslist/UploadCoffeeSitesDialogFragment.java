package cz.fungisoft.coffeecompass2.activity.ui.coffeesite.ui.mycoffeesiteslist;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import cz.fungisoft.coffeecompass2.R;

/**
 * Dialog to get confirmation to upload CoffeeSites from user.
 */
public class UploadCoffeeSitesDialogFragment extends DialogFragment {

    private int numOfNewOrUpdatedCoffeeSites = 0;

    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface UploadCoffeeSitesDialogListener {
        void onDialogPositiveClick(DialogFragment dialog);
        void onDialogNegativeClick(DialogFragment dialog);
    }

    // Use this instance of the interface to deliver action events
    UploadCoffeeSitesDialogListener listener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Verify that the host activity implements the callback interface
        try {
            listener = (UploadCoffeeSitesDialogListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(listener.toString()
                    + " must implement UploadCoffeeSitesDialogListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.numOfNewOrUpdatedCoffeeSites = getArguments().getInt("numOfNewOrUpdatedCoffeeSites");

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = requireActivity().getLayoutInflater();
        final View dialogView;
        final TextView numberOfNewCoffeeSitesTextView;

        dialogView = inflater.inflate(R.layout.confirm_upload_coffeesites_dialog, null);
        numberOfNewCoffeeSitesTextView = dialogView.findViewById(R.id.upload_dialog_number_of_new_and_updated_coffeesites_textview);

        if (this.numOfNewOrUpdatedCoffeeSites != 0) {
            String numOfCoffeeSitesText = String.format(getString(R.string.upload_dialog_number_of_new_coffeesites), String.valueOf(this.numOfNewOrUpdatedCoffeeSites));
            numberOfNewCoffeeSitesTextView.setText(numOfCoffeeSitesText);
        }

        builder.setView(dialogView)
               .setTitle(R.string.upload_coffeesites_dialog_title)
               //.setMessage(R.string.insert_author_comment_dialog_message)
               .setPositiveButton(R.string.insert_author_comment_dialog_confirm, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        listener.onDialogPositiveClick(UploadCoffeeSitesDialogFragment.this);
                    }
                })
               .setNegativeButton(R.string.insert_author_comment_dialog_cancelation, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Send the negative button event back to the host activity
                        listener.onDialogNegativeClick(UploadCoffeeSitesDialogFragment.this);
                    }
               });
        // Create the AlertDialog object and return it
        return builder.create();
    }

}
