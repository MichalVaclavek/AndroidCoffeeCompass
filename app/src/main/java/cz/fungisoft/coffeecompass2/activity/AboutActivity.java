package cz.fungisoft.coffeecompass2.activity;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.google.android.material.snackbar.Snackbar;

import cz.fungisoft.coffeecompass2.R;
import cz.fungisoft.coffeecompass2.activity.data.StatisticsPrefencesHelper;
import cz.fungisoft.coffeecompass2.activity.ui.notification.StaticCoffeeSitesListActivity;
import cz.fungisoft.coffeecompass2.asynctask.ReadStatsAsyncTask;
import cz.fungisoft.coffeecompass2.entity.Statistics;
import cz.fungisoft.coffeecompass2.utils.NetworkStateReceiver;
import cz.fungisoft.coffeecompass2.utils.Utils;

/**
 * Shows the About app. info, author, version, aim of the app.
 */
public class AboutActivity extends AppCompatActivity {

    private ProgressBar aboutActivityProgressBar;

    private CardView statisticsAndNewsCardView; // needed to change background color, when statistics shows new CoffeeSites last week

    // Saves Statistics
    private StatisticsPrefencesHelper statisticsPrefencesHelper;

    private LinearLayout statisticsLayout;

    private final int DAYS_BACK_FOR_LOAD_STATISTICS = 7;

    /**
     * To indicate if a user clicked on statistics View, to load latest
     * CoffeeSites after reading statistics.
     */
    private boolean statisticsCalledUponUsersClick = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        statisticsPrefencesHelper = new StatisticsPrefencesHelper(this);

        setContentView(R.layout.activity_about);

        // Setup main toolbar with back button arrow
        Toolbar mainToolbar = (Toolbar) findViewById(R.id.aboutl_toolbar);
        setSupportActionBar(mainToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (mainToolbar != null) {
            getSupportActionBar().setTitle(getString(R.string.about_toolbar_title));
        }

        aboutActivityProgressBar = findViewById(R.id.progress_about_activity);

        statisticsAndNewsCardView = findViewById(R.id.statistics_news_about_card_view);

        if (statisticsPrefencesHelper.getNumOfSitesLastWeekChanged()) {
            statisticsAndNewsCardView.setCardBackgroundColor(getResources().getColor(R.color.colorPrimary));
        }

        statisticsLayout = findViewById(R.id.statistics_layout);

        statisticsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onStatisticsClick(v);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Read statistics
        startReadStatistics();
        if (!Utils.isOnline(getApplicationContext()) && Utils.offlineDataAvailable(getApplicationContext())) {
            showAndSaveStatistics(statisticsPrefencesHelper.getSavedStatistics());
        }
    }

        /**
         * Starts AsyncTask to read app. statistics to be shown in MainActivity
         * if internet is available.<br>
         * Can also be read later, after internet becomes available, see {@link NetworkStateReceiver}
         */
    public synchronized void startReadStatistics() {
        if (Utils.isOnline(getApplicationContext())) {
            if (statisticsCalledUponUsersClick) {
                showProgressbar();
            }
            new ReadStatsAsyncTask(this).execute();
        }
    }

    /**
     * After click on Statistics View, load the latest statistics and if there are some new
     * CoffeeSites, load and show them.
     *
     * @param view
     */
    public void onStatisticsClick(View view) {
        statisticsCalledUponUsersClick = true;
        statisticsAndNewsCardView.setCardBackgroundColor(getResources().getColor(R.color.white_transparent));
        startReadStatistics();
    }


    /**
     * To show statistics, can be called from AsyncTask
     *
     * @param stats
     */
    public void showAndSaveStatistics(Statistics stats) {
        hideProgressbar();
        if (Integer.parseInt(statisticsPrefencesHelper.getNumOfSitesLastWeek()) < Integer.parseInt(stats.numOfSitesLastWeek)) {
            statisticsPrefencesHelper.putNumOfSitesLastWeekChanged(true);
        }
        if (Integer.parseInt(stats.numOfSitesLastWeek) == 0) {
            statisticsPrefencesHelper.putNumOfSitesLastWeekChanged(false);
        }
        statisticsPrefencesHelper.saveStatistics(stats);

        TextView sitesView = findViewById(R.id.all_sites_TextView);
        TextView sites7View = findViewById(R.id.AllSites7TextView);
        TextView sitesToday = findViewById(R.id.TodaySitesTextView);
        TextView usersView = findViewById(R.id.AllUsersTextView);

        sitesView.setText(stats.numOfSites);
        sitesToday.setText(stats.numOfSitesToday);
        sites7View.setText(stats.numOfSitesLastWeek);
        if (statisticsPrefencesHelper.getNumOfSitesLastWeekChanged()) {
            statisticsAndNewsCardView.setCardBackgroundColor(getResources().getColor(R.color.colorPrimary));
        }
        usersView.setText(stats.numOfUsers);

        // Where the statistics shown upon user's click on Statistics CardView ?
        if (statisticsCalledUponUsersClick && Integer.parseInt(stats.numOfSitesLastWeek) > 0) {
            statisticsCalledUponUsersClick = false;
            if (Utils.isOnline(getApplicationContext())) {
                Intent intent = new Intent(this, StaticCoffeeSitesListActivity.class);
                intent.putExtra("daysBack", DAYS_BACK_FOR_LOAD_STATISTICS);
                startActivity(intent);
                // user now checked the new CoffeeSites of the week, so background of the Statistics CardView can
                // changed back to it's original color
                statisticsAndNewsCardView.setCardBackgroundColor(getResources().getColor(R.color.white_transparent));
                statisticsPrefencesHelper.putNumOfSitesLastWeekChanged(false);
            } else {
                Snackbar mySnackbar = Snackbar.make(statisticsLayout, R.string.toast_no_internet_no_offline_data, Snackbar.LENGTH_LONG);
                mySnackbar.show();
            }
        }
    }

    public void showProgressbar() {
        aboutActivityProgressBar.setVisibility(VISIBLE);
    }

    /**
     * Helper method ...
     */
    public void hideProgressbar() {
        aboutActivityProgressBar.setVisibility(GONE);
    }
}
