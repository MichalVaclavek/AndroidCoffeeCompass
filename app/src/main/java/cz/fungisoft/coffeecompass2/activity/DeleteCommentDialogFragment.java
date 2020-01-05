package cz.fungisoft.coffeecompass2.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import cz.fungisoft.coffeecompass2.R;

public class DeleteCommentDialogFragment extends DialogFragment {

    public interface DeleteCommentDialogListener {

        public void onDeleteCommentDialogPositiveClick(DeleteCommentDialogFragment dialog);
        public void onDeleteCommentDialogNegativeClick(DeleteCommentDialogFragment dialog);
    }

    // Use this instance of the interface to deliver action events
    DeleteCommentDialogFragment.DeleteCommentDialogListener listener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the DeleteUserAccountDialogListener so we can send events to the host
            listener = (DeleteCommentDialogFragment.DeleteCommentDialogListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(listener.toString()
                    + " must implement DeleteCommentDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setMessage(R.string.delete_comment_question)
                .setPositiveButton(R.string.delete_comment, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        listener.onDeleteCommentDialogPositiveClick(DeleteCommentDialogFragment.this);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Send the negative button event back to the host activity
                        listener.onDeleteCommentDialogNegativeClick(DeleteCommentDialogFragment.this);
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }

}
