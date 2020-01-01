package cz.fungisoft.coffeecompass2.asynctask.user;

import android.os.AsyncTask;

import cz.fungisoft.coffeecompass2.activity.data.UserAccountRepository;

/**
 * Async task for delete user account
 */
public class DeleteUserRESTAsyncTask extends AsyncTask<Void, Void, Void> {

    private UserAccountRepository registerRepository;

    public DeleteUserRESTAsyncTask(UserAccountRepository registerRepository) {
        super();
        this.registerRepository = registerRepository;
    }


    @Override
    protected Void doInBackground(Void... voids) {
        registerRepository.delete();
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
    }

}
