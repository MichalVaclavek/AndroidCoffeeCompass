package cz.fungisoft.coffeecompass2.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;

import cz.fungisoft.coffeecompass2.R;
import cz.fungisoft.coffeecompass2.activity.ui.login.DeleteUserAccountDialogFragment;

/**
 * Dialog to enter Comment for CoffeeSite and the rating (stars) of the CoffeeSite
 */
public class EnterCommentAndRatingDialogFragment extends DialogFragment {

    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface CommentAndRatingDialogListener {

        public void onDialogPositiveClick(DialogFragment dialog);
        public void onDialogNegativeClick(DialogFragment dialog);
    }

    // Use this instance of the interface to deliver action events
    CommentAndRatingDialogListener listener;

    // Override the Fragment.onAttach() method to instantiate the CommentAndRatingDialogListener
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the DeleteUserAccountDialogListener so we can send events to the host
            listener = (CommentAndRatingDialogListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(listener.toString()
                    + " must implement CommentAndRatingDialogListene");
        }
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(inflater.inflate(R.layout.comment_and_rating_dialog, null))
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        listener.onDialogPositiveClick(EnterCommentAndRatingDialogFragment.this);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Send the negative button event back to the host activity
                        listener.onDialogNegativeClick(EnterCommentAndRatingDialogFragment.this);
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
