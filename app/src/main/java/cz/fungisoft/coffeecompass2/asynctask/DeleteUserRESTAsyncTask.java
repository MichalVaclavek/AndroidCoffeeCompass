package cz.fungisoft.coffeecompass2.asynctask;

import android.os.AsyncTask;

import cz.fungisoft.coffeecompass2.activity.data.UserAccountRepository;

/**
 * Async task for delete user account
 */
public class DeleteUserRESTAsyncTask extends AsyncTask<Void, Void, Void> {

    //private String userName;

    private UserAccountRepository registerRepository;

//    public DeleteUserRESTAsyncTask(String username, UserAccountRepository registerRepository) {
//        super();
//        //this.userName = username;
//        this.registerRepository = registerRepository;
//    }

    public DeleteUserRESTAsyncTask(UserAccountRepository registerRepository) {
        super();
        this.registerRepository = registerRepository;
        //this.userName = this.registerRepository.getLoggedInUser().getUserName();
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
