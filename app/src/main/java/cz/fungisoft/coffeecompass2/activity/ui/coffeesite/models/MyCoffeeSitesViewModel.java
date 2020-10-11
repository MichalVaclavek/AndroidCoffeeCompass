package cz.fungisoft.coffeecompass2.activity.ui.coffeesite.models;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import java.util.List;

import cz.fungisoft.coffeecompass2.activity.data.model.LoggedInUser;
import cz.fungisoft.coffeecompass2.entity.CoffeeSite;
import cz.fungisoft.coffeecompass2.entity.repository.CoffeeSiteDatabase;
import cz.fungisoft.coffeecompass2.entity.repository.CoffeeSiteRepository;

public class MyCoffeeSitesViewModel extends AndroidViewModel {

    private CoffeeSiteRepository coffeeSiteRepository;

    private final MutableLiveData<LoggedInUser> userInput = new MutableLiveData<>();

    private void setInput(LoggedInUser loggedInUser) {
        userInput.setValue(loggedInUser);
    }

    /**
     * Actual list of CoffeeSites in the search range from current position of the equipment as
     * found in DB.
     */
    private LiveData<List<CoffeeSite>> usersCoffeeSitesInDB = Transformations.switchMap(userInput, (ui) -> coffeeSiteRepository.getCoffeeSitesByAuthorUserName(ui.getUserName()));

    public LiveData<List<CoffeeSite>> getUsersCoffeeSites(LoggedInUser loggedInUser) {
        setInput(loggedInUser);
        return  usersCoffeeSitesInDB;
    }

    public MyCoffeeSitesViewModel(@NonNull Application application) {
        super(application);
        coffeeSiteRepository = new CoffeeSiteRepository(CoffeeSiteDatabase.getDatabase(application.getApplicationContext()));
    }

}
