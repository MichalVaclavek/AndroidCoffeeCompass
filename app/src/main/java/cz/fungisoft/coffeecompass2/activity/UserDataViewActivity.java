package cz.fungisoft.coffeecompass2.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import cz.fungisoft.coffeecompass2.R;
import cz.fungisoft.coffeecompass2.activity.data.model.LoggedInUser;

public class UserDataViewActivity extends AppCompatActivity {

    private LoggedInUser userProfileToShow;

    @BindView(R.id.userName) TextView usernameTextView;
    @BindView(R.id.userProfileEmail) TextView userEmailTextView;
    @BindView(R.id.userProfileNumOfCreatedSites) TextView numOfCreatedSitesTextView;

    @BindView(R.id.userProfileFirstNameLinearLayout) LinearLayout firstNameLinearLayout;
    @BindView(R.id.firstName) TextView firstNameTextView;

    @BindView(R.id.userProfileLastNameLinearLayout) LinearLayout lastNameLinearLayout;
    @BindView(R.id.lastName) TextView lastNameTextView;

    @BindView(R.id.userCreatedOn) TextView userCratedOnTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_data_view);

        ButterKnife.bind(this);

        userProfileToShow = (LoggedInUser) this.getIntent().getExtras().get("currentUserProfile");

        if (userProfileToShow != null) {
            usernameTextView.setText(userProfileToShow.getUserName());
            userEmailTextView.setText(userProfileToShow.getEmail());
            numOfCreatedSitesTextView.setText(String.valueOf(userProfileToShow.getNumOfCreatedSites()));

            if (userProfileToShow.getFirstName() != null && !userProfileToShow.getFirstName().isEmpty()) {
                firstNameTextView.setText(userProfileToShow.getFirstName());
            } else {
                firstNameLinearLayout.setEnabled(false);
            }
            if (userProfileToShow.getLastName() != null && !userProfileToShow.getLastName().isEmpty()) {
                lastNameTextView.setText(userProfileToShow.getLastName());
            } else {
                lastNameLinearLayout.setEnabled(false);
            }

            userCratedOnTextView.setText(userProfileToShow.getCreatedOnFormated());
        }

    }
}
