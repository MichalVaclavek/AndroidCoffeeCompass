package cz.fungisoft.coffeecompass2.services.interfaces;

/**
 * An interface to be implemented by Activities, which needs
 * to react to results of actions performed by CoffeeSiteImageService
 * i.e. results of Save and Delete of CoffeeSite's image
 */
public interface CoffeeSiteImageServiceCallResultListener {

    void onImageSaveSuccess(String imageSaveResult);
    void onImageSaveFailure(String imageSaveResult);

    void onImageDeleteSuccess(String imageDeleteResult);
    void onImageDeleteFailure(String imageDeleteResult);
}
