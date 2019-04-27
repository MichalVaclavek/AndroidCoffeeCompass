package cz.fungisoft.coffeecompass2.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;


public class CoffeeSitesInRangeUpdateService extends Service {




    /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    public class LocalBinder extends Binder {
        CoffeeSitesInRangeUpdateService getService() {
            return CoffeeSitesInRangeUpdateService.this;
        }
    }

    // This is the object that receives interactions from clients.  See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new CoffeeSitesInRangeUpdateService.LocalBinder();

    @Override
    public IBinder onBind(Intent intent) {
//        location = posledniPozice(LAST_PRESNOST, MAX_STARI_DAT);
        return mBinder;
    }


    @Override
    public void onCreate() {

    }


}
