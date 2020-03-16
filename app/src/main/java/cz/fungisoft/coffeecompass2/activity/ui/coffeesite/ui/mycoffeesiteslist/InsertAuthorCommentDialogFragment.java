package cz.fungisoft.coffeecompass2.activity.ui.coffeesite.ui.mycoffeesiteslist;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import cz.fungisoft.coffeecompass2.R;

/**
 * Dialog to insert CoffeeSite's author initial comment.
 */
public class InsertAuthorCommentDialogFragment extends DialogFragment {

    /**
     * Field to hold comment inserted by CoffeeSite's author
     */
    private String authorComment;

    public String getAuthorComment() {
        return authorComment;
    }

    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface InsertAuthorCommentDialogListener {
        void onDialogPositiveClick(DialogFragment dialog);
        void onDialogNegativeClick(DialogFragment dialog);
    }

    // Use this instance of the interface to deliver action events
    InsertAuthorCommentDialogListener listener;

    // Override the Fragment.onAttach() method to instantiate the InsertAuthorCommentDialogListener
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the InsertAuthorCommentDialogListener so we can send events to the host
            listener = (InsertAuthorCommentDialogListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(listener.toString()
                    + " must implement InsertAuthorCommentDialogListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.authorComment = getArguments().getString("authorComment");
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = requireActivity().getLayoutInflater();
        final View dialogView;
        final EditText authorCommentInput;

        dialogView = inflater.inflate(R.layout.author_comment_dialog, null);

        authorCommentInput = dialogView.findViewById(R.id.author_comment_TextInput);
        if (this.authorComment != null) {
            authorCommentInput.setText(this.authorComment);
        }

        builder.setView(dialogView)
               .setTitle(R.string.author_comment_dialog_title)
               .setMessage(R.string.insert_author_comment_dialog_message)
               .setPositiveButton(R.string.insert_author_comment_dialog_confirm, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        authorComment = authorCommentInput.getText().toString();
                        listener.onDialogPositiveClick(InsertAuthorCommentDialogFragment.this);
                    }
                })
               .setNegativeButton(R.string.insert_author_comment_dialog_cancelation, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Send the negative button event back to the host activity
                        listener.onDialogNegativeClick(InsertAuthorCommentDialogFragment.this);
                    }
               });
        // Create the AlertDialog object and return it
        return builder.create();
    }

}
