package cz.fungisoft.coffeecompass2.services.interfaces;

import cz.fungisoft.coffeecompass2.activity.data.Result;

/**
 * An observable interface to indicate new result
 * of the REST operations obtaining size of data to be downloaded.
 * <br>
 * Usually called by AsyncTasks with Retrofit call with Integer
 * as return value of REST call.
 */
public interface DataDownloadSizeRESTResultListener {

    void onSizeOfAllDataToDownload(Result<Integer> result);

    void onSizeOfAllDataWithoutImagesToDownload(Result<Integer> result);

    void onSizeOfDataToDownloadError(Result.Error error);
}
