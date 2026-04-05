package cz.fungisoft.coffeecompass2.services.interfaces;

import cz.fungisoft.coffeecompass2.entity.CoffeeSite;

/**
 * An interface to be implemented by Activities, which needs
 * to react to results of actions performed by CoffeeSiteImageService
 * i.e. results of Save and Delete of CoffeeSite's image
 */
public interface CoffeeSiteImageServiceCallResultListener {

    /**
     *
     * @param cs CoffeeSite who's image was saved successfully
     * @param imageSaveResult
     */
    void onImageSaveSuccess(CoffeeSite cs, String imageSaveResult);
    void onImageSaveFailure(CoffeeSite cs, String imageSaveResult);

    /**
     *
     * @param cs CoffeeSite who's image was deleted successfully
     * @param imageDeleteResult
     */
    default void onImageDeleteSuccess(CoffeeSite cs, String imageDeleteResult) {}
    default void onImageDeleteFailure(CoffeeSite cs, String imageDeleteResult) {}
}
