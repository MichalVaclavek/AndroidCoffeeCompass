package cz.fungisoft.coffeecompass2.activity.ui.comments;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import cz.fungisoft.coffeecompass2.R;
import cz.fungisoft.coffeecompass2.activity.data.model.rest.comments.CommentAndStars;

/**
 * Dialog to enter new Comment for CoffeeSite and the rating (stars) of the CoffeeSite
 * or for updating existing Comment text and/or Stars.
 */
public class EnterCommentAndRatingDialogFragment extends DialogFragment {

    private final CommentAndStars commentAndStars = new CommentAndStars();

    private int numOfStarsForSiteAndUser = 0;

    private String currentCommentText = "";

    public CommentAndStars getCommentAndStars() {
        return commentAndStars;
    }

    private final ImageView[] ratingCupsViews = new ImageView[5];

    private int selectedRatingNumber = 0;

    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface CommentAndRatingDialogListener {

        void onSaveUpdateCommentDialogPositiveClick(EnterCommentAndRatingDialogFragment dialog);
        default void onSaveUpdateCommentDialogNegativeClick(EnterCommentAndRatingDialogFragment dialog) {}
    }

    // Use this instance of the interface to deliver action events
    CommentAndRatingDialogListener listener;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.numOfStarsForSiteAndUser = getArguments().getInt("numOfStars");
        this.currentCommentText = getArguments().getString("commentText");
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
            throw new ClassCastException(listener.toString() + " must implement CommentAndRatingDialogListener");
        }
    }


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

        /*
        Setup rating cups ImageView. Tags equals number of stars
         */
        ratingCupsViews[0] = ((ImageView) dialogView.findViewById(R.id.dialog_rating_image_cup1));
        ratingCupsViews[0].setOnClickListener(createOnClickListenerForRatingCupsImageView());
        ratingCupsViews[0].setTag(1);
        ratingCupsViews[1] = ((ImageView) dialogView.findViewById(R.id.dialog_rating_image_cup2));
        ratingCupsViews[1].setOnClickListener(createOnClickListenerForRatingCupsImageView());
        ratingCupsViews[1].setTag(2);
        ratingCupsViews[2] = ((ImageView) dialogView.findViewById(R.id.dialog_rating_image_cup3));
        ratingCupsViews[2].setOnClickListener(createOnClickListenerForRatingCupsImageView());
        ratingCupsViews[2].setTag(3);
        ratingCupsViews[3] = ((ImageView) dialogView.findViewById(R.id.dialog_rating_image_cup4));
        ratingCupsViews[3].setOnClickListener(createOnClickListenerForRatingCupsImageView());
        ratingCupsViews[3].setTag(4);
        ratingCupsViews[4] = ((ImageView) dialogView.findViewById(R.id.dialog_rating_image_cup5));
        ratingCupsViews[4].setOnClickListener(createOnClickListenerForRatingCupsImageView());
        ratingCupsViews[4].setTag(5);

        setCurrentRatingCupsImageViews(ratingCupsViews, numOfStarsForSiteAndUser);

        commentInput = dialogView.findViewById(R.id.commentTextInput);

        // If current Comment is to be modified
        if (!currentCommentText.isEmpty()) {
            commentInput.setText(currentCommentText);
        }

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(dialogView)
               .setTitle(R.string.comment_dialog_title)
               .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Create new Comment and star object and call listener to pass it further as return value of this dia;log
                        commentAndStars.setStars(new CommentAndStars.Stars(selectedRatingNumber));
                        commentAndStars.setComment(commentInput.getText().toString());
                        listener.onSaveUpdateCommentDialogPositiveClick(EnterCommentAndRatingDialogFragment.this);
                    }
                })
               .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Send the negative button event back to the host activity
                        listener.onSaveUpdateCommentDialogNegativeClick(EnterCommentAndRatingDialogFragment.this);
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }


    /**
     * OnClick listener to handle click on ratinf cups ImageViews
     *
     * @return
     */
    private View.OnClickListener createOnClickListenerForRatingCupsImageView() {
        View.OnClickListener retVal;

        retVal = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get selected cup image
                if (view instanceof ImageView) {
                    selectedRatingNumber = (Integer) view.getTag();
                    setCurrentRatingCupsImageViews(ratingCupsViews, selectedRatingNumber);
                }
            }
        };
        return retVal;
    }

    private void setCurrentRatingCupsImageViews(ImageView[] ratingCupsViews, int currentRating) {
        // Set all cups empty first
        for (ImageView cupImageView : ratingCupsViews) {
            cupImageView.setImageResource(R.drawable.cup_rating_empty_4);
        }
        // All cups till selected make full
        for (int i = 0; i < currentRating; i++) {
            ratingCupsViews[i].setImageResource(R.drawable.cup_rating_grey_full_5);
        }
    }

}
