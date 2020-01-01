package cz.fungisoft.coffeecompass2.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import cz.fungisoft.coffeecompass2.R;
import cz.fungisoft.coffeecompass2.activity.data.model.rest.CommentAndStarsToSave;

/**
 * Dialog to enter Comment for CoffeeSite and the rating (stars) of the CoffeeSite
 */
public class EnterCommentAndRatingDialogFragment extends DialogFragment {

    private final CommentAndStarsToSave commentAndStars = new CommentAndStarsToSave();

    private int numOfStarsForSiteAndUser = 0;

    public CommentAndStarsToSave getCommentAndStars() {
        return commentAndStars;
    }

    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface CommentAndRatingDialogListener {

        public void onSaveCommentDialogPositiveClick(EnterCommentAndRatingDialogFragment dialog);
        public void onSaveCommentDialogNegativeClick(EnterCommentAndRatingDialogFragment dialog);
    }

    // Use this instance of the interface to deliver action events
    CommentAndRatingDialogListener listener;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.numOfStarsForSiteAndUser = getArguments().getInt("numOfStars");
    }

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
                    + " must implement CommentAndRatingDialogListener");
        }
    }

//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        // R.layout.my_layout - that's the layout where your textview is placed
//        View view = inflater.inflate(R.layout.comment_and_rating_dialog, container, false);
//
//        return view;
//    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Get the layout inflater
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        final View dialogView;
        final RadioGroup starsRadioGroup;
        final EditText commentInput;

        dialogView = inflater.inflate(R.layout.comment_and_rating_dialog, null);

        starsRadioGroup = dialogView.findViewById(R.id.stars_radiogroup);
        if (numOfStarsForSiteAndUser != 0) {
            switch (numOfStarsForSiteAndUser)
            {
                case 1: starsRadioGroup.check(R.id.radioButton1);
                    break;
                case 2: starsRadioGroup.check(R.id.radioButton2);
                    break;
                case 3: starsRadioGroup.check(R.id.radioButton3);
                    break;
                case 4: starsRadioGroup.check(R.id.radioButton4);
                    break;
                case 5: starsRadioGroup.check(R.id.radioButton5);
                    break;
                default: break;
            }
        } else {
            starsRadioGroup.check(R.id.radioButton3);
        }
        commentInput = dialogView.findViewById(R.id.commentTextInput);

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(dialogView)
               .setTitle(R.string.comment_dialog_title)
               .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        int selectedId = starsRadioGroup.getCheckedRadioButtonId();

                        // find the radiobutton by returned id
                        RadioButton starsButton = (RadioButton) dialogView.findViewById(selectedId);
                        int stars = Integer.parseInt(starsButton.getText().toString());
                        commentAndStars.setStars(new CommentAndStarsToSave.Stars(stars));
                        commentAndStars.setComment(commentInput.getText().toString());
                        //CommentAndStarsToSave commentAndStars = new CommentAndStarsToSave(new CommentAndStarsToSave.Stars(stars), commentInput.getText().toString());
                        listener.onSaveCommentDialogPositiveClick(EnterCommentAndRatingDialogFragment.this);
                    }
                })
               .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Send the negative button event back to the host activity
                        listener.onSaveCommentDialogNegativeClick(EnterCommentAndRatingDialogFragment.this);
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
