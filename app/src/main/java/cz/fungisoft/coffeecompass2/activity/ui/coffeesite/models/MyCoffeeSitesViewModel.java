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
import io.reactivex.Single;

public class MyCoffeeSitesViewModel extends AndroidViewModel {

    private CoffeeSiteRepository coffeeSiteRepository;

    private final MutableLiveData<LoggedInUser> userInput = new MutableLiveData<>();

    private void setInput(LoggedInUser loggedInUser) {
        userInput.setValue(loggedInUser);
    }

    /**
     * List of all CoffeeSites created by current user found in DB (i.e. downloaded or created/modified Offline)
     */
    private final LiveData<List<CoffeeSite>> usersCoffeeSitesInDB = Transformations.switchMap(userInput, (ui) -> coffeeSiteRepository.getCoffeeSitesByAuthorUserName(ui.getUserName()));

    public LiveData<List<CoffeeSite>> getAllUsersCoffeeSitesInDB(LoggedInUser loggedInUser) {
        setInput(loggedInUser);
        return  usersCoffeeSitesInDB;
    }

    /**
     * List of all CoffeeSites created by current user not saved on server, yet. Includes completelly new sites
     * or sites already saved on server, but modified when Offline.
     */
    public LiveData<List<CoffeeSite>> getCoffeeSitesNotSavedOnServer() {
        return  coffeeSiteRepository.getCoffeeSitesNotSavedOnServer();
    }

    public LiveData<Integer> getNumOfCoffeeSitesNotSavedOnServer() {
        return coffeeSiteRepository.getNumOfCoffeeSitesNotSavedOnServer();
    }

    /**
     * List of all CoffeeSites created by current and saved on server, i.e. sites already downloaded
     * and not modified nor newly created.
     */
    private final LiveData<List<CoffeeSite>> usersCoffeeSitesInDBNotModified = Transformations.switchMap(userInput, (ui) -> coffeeSiteRepository.getCoffeeSitesFromUserSavedOnServer(ui.getUserName()));

    /**
     * List of all CoffeeSites created by current and saved on server, i.e. sites already downloaded
     * and not modified (or newly created)
     */
    public LiveData<List<CoffeeSite>> getUsersCoffeeSitesInDBNotModified(LoggedInUser loggedInUser) {
        setInput(loggedInUser);
        return  usersCoffeeSitesInDBNotModified;
    }

    public MyCoffeeSitesViewModel(@NonNull Application application) {
        super(application);
        coffeeSiteRepository = new CoffeeSiteRepository(CoffeeSiteDatabase.getDatabase(application.getApplicationContext()));
    }

}
