package cz.fungisoft.coffeecompass2.services.interfaces;

import cz.fungisoft.coffeecompass2.activity.data.Result;
import cz.fungisoft.coffeecompass2.activity.data.model.rest.places.CuzkCandidates;

/**
 * An observable interface to indicate result of the REST operation, to CUZK Places API.
 * Used by {@link cz.fungisoft.coffeecompass2.asynctask.places.GetPlacesCandidatesCUZKTask}
 * <p>
 * Usually called by AsyncTasks with Retrofit call with {@code List<CuzkCandidates>}
 * as return value of REST call.
 */
public interface PlacesCandidatesCUZKRESTResultListener {

    /**
     * @param result - success or error result of the operation. If success, then {@link CuzkCandidates} is returned in result = new Result.Success<>(candidates);
     */
    void onCandidatesReturned(Result<CuzkCandidates> result);
}
