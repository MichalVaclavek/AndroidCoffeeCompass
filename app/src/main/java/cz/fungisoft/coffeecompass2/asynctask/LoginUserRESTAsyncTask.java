package cz.fungisoft.coffeecompass2.asynctask;

import android.os.AsyncTask;

import cz.fungisoft.coffeecompass2.activity.data.LoginRepository;

public class LoginUserRESTAsyncTask extends AsyncTask<Void, Void, Void> {

    private String userName;
    private String deviceID;
    private String password;

    private LoginRepository loginRepository;

    public LoginUserRESTAsyncTask(String username, String password, String deviceID, LoginRepository loginRepository) {
        super();
        this.deviceID = deviceID;
        this.userName = username;
        this.password = password;
        this.loginRepository = loginRepository;
    }


    @Override
    protected Void doInBackground(Void... voids) {
        loginRepository.login(userName, password, deviceID);
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
    }

}
