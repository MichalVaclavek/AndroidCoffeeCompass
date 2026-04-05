package cz.fungisoft.coffeecompass2.activity.interfaces.datadownload;

import cz.fungisoft.coffeecompass2.BuildConfig;
import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Retrofit calls interface for REST calls obtaining sizes of data to download
 */
public interface DataDownloadSizesRESTInterface {

    String GET_DATA_DOWNLOAD_SIZE_URL = BuildConfig.DOWNLOAD_SIZE_API_PUBLIC_URL;

    /**
     * REST call for obtaining size of ALL data to be downloaded, when user requests Offline mode.
     *
     * @return
     */
    @GET("all/sizeKB")
    Call<Integer> getSizeOfAllToDownload();

    /**
     * REST call for obtaining size of all data, without Images (i.e. CoffeeSites and Comments) to be downloaded,
     * when user requests Offline mode.
     *
     * @return
     */
    @GET("allExceptImages/sizeKB")
    Call<Integer> getSizeOfAllWithoutImageToDownload();

}
