package cz.fungisoft.coffeecompass2.asynctask;

import android.os.AsyncTask;

import cz.fungisoft.coffeecompass2.activity.data.UserAccountRepository;

/**
 * Async task for new user registering REST request call
 */
public class RegisterUserRESTAsyncTask extends AsyncTask<Void, Void, Void> {

    private String userName;
    private String deviceID;
    private String password;
    private String email;

    private UserAccountRepository registerRepository;

    public RegisterUserRESTAsyncTask(String username, String password, String email, String deviceID, UserAccountRepository registerRepository) {
        super();
        this.deviceID = deviceID;
        this.userName = username;
        this.password = password;
        this.email = email;
        this.registerRepository = registerRepository;
    }


    @Override
    protected Void doInBackground(Void... voids) {
        registerRepository.register(userName, password, email, deviceID);
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
    }

}
