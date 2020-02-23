package cz.fungisoft.coffeecompass2.utils;

import android.content.Context;
import android.os.AsyncTask;


/**
 * Async task to check internet connection availability.
 *
 * Usage: new InternetCheckAsyncTask(internet -> { /* do something with boolean response  });
 *
 * from: https://stackoverflow.com/questions/1560788/how-to-check-internet-access-on-android-inetaddress-never-times-out
 * but modified strongly.
 */
public class InternetCheckAsyncTask extends AsyncTask<Void, Void, Boolean> {

    private Consumer mConsumer;

    public interface Consumer {
        void accept(Boolean internet);
        Context getContext();
    }

    public  InternetCheckAsyncTask(Consumer consumer) {
        mConsumer = consumer;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        return Utils.hasInternetAccess(mConsumer.getContext());
    }

    @Override
    protected void onPostExecute(Boolean internet) {
        mConsumer.accept(internet);
    }
}
