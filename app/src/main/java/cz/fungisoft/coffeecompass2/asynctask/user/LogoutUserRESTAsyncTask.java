package cz.fungisoft.coffeecompass2.asynctask.user;

import android.os.AsyncTask;

import cz.fungisoft.coffeecompass2.activity.data.UserAccountRepository;

/**
 * Async task for new user registering REST request call
 */
public class LogoutUserRESTAsyncTask extends AsyncTask<Void, Void, Void> {

    private UserAccountRepository registerRepository;

    public LogoutUserRESTAsyncTask(UserAccountRepository registerRepository) {
        super();
        this.registerRepository = registerRepository;
    }


    @Override
    protected Void doInBackground(Void... voids) {
        registerRepository.logout();
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
    }

}
