package cz.fungisoft.coffeecompass2.activity.interfaces.images;

import java.util.List;

import cz.fungisoft.coffeecompass2.activity.data.Result;

public interface CoffeeSiteImagesLoadListener {

    void onImageUrlsLoaded(List<String> imageUrls);

    void onImageUrlsLoadFailed(Result.Error error);
}
