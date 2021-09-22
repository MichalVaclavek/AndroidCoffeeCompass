package cz.fungisoft.coffeecompass2.activity.ui.notification;

import android.content.Context;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cz.fungisoft.coffeecompass2.R;
import cz.fungisoft.coffeecompass2.activity.data.Result;
import cz.fungisoft.coffeecompass2.activity.data.model.rest.places.Candidate;
import cz.fungisoft.coffeecompass2.activity.data.model.rest.places.CuzkCandidates;
import cz.fungisoft.coffeecompass2.asynctask.places.GetPlacesCandidatesCUZKTask;
import cz.fungisoft.coffeecompass2.services.interfaces.PlacesCandidatesCUZKRESTResultListener;

/**
 * String Adapter needed for AutoCompleteTextView of the {@link NewsSubscriptionActivity}
 * to show list of towns matching characters entered by user dynamically according
 * responses retrieved from CUZK Places API.
 * <p>
 * Based on stackoverflow.com advice.
 */
public class TownNamesArrayAdapter extends ArrayAdapter<String> implements Filterable,
                                                                           PlacesCandidatesCUZKRESTResultListener {

    private static final String TAG = "TownNamesArrayAdapter";

    private final List<String> resultList = new ArrayList<>();

    // characters valid in czech town names (plus standard a-z and A-Z)
    private final String allowedCheckChars = "áčďéěíňóřšťúůýžÁČĎÉĚÍŇÓŘŠŤÚŮÝŽ- ";

    public TownNamesArrayAdapter(android.content.Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    @Override
    public int getCount() {
        return resultList.size();
    }

    @Override
    public String getItem(int index) {
        return resultList.get(index);
    }

    @Override
    public Filter getFilter() {

        Filter filter = new Filter() {

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if (constraint != null && constraint.length() > 0) {
                    // Retrieve the autocomplete results
                    // ignore special characters, like dots '.' and so on
                    char endChar = constraint.charAt(constraint.length() - 1);
                    if ((endChar >= 'a' && endChar <= 'z') || (endChar >= 'A' && endChar <= 'Z')
                           || allowedCheckChars.contains(String.valueOf(endChar))) {
                        townNameStartChanged(constraint.toString());
                        // Assign the data to the FilterResults
                        filterResults.values = resultList;
                        filterResults.count = resultList.size();
                    }
                } else {
                    refresh(null); // to clear the drop down list?
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }
        };
        return filter;
    }

    /**
     * Founds all town names matching entered characters {@code townNameStart} using
     * CUZK places API.
     *
     * @param townNameStart - beginning chars of the town name to be searched for
     */
    public void townNameStartChanged(String townNameStart) {
        startSearchingPlacesAsyncTask(townNameStart, 9);
    }

    private void startSearchingPlacesAsyncTask(String placeName, int maxPlaces) {
        new GetPlacesCandidatesCUZKTask(placeName, maxPlaces, this).execute();
    }


    @Override
    public void onCandidatesReturned(Result<CuzkCandidates> result) {
        Set<String> matchingTowns = new HashSet<>();
        if (result instanceof Result.Success) {
            CuzkCandidates candidates = ((Result.Success<CuzkCandidates>) result).getData();
            for (Candidate candidate : candidates.getCandidates()) {
                matchingTowns.add(candidate.getAddress());
            }
            refresh(matchingTowns); // update the drop down list
        }
    }

    /**
     * Refresh the list of items of the drop down AutoCompleteTextView, which
     * this Adapter belongs to.
     *
     * @param matchingTowns - list of strings/towns to be shown in drop down list
     */
    private void refresh(Set<String> matchingTowns) {
        resultList.clear();
        if (matchingTowns != null) {
            resultList.addAll(matchingTowns);
        }
        this.notifyDataSetChanged();
    }

    /**
     * Founds all town names matching entered characters {@code townNameStart} using
     * Google places API.
     * <p>
     * NOT USED currently.
     *
     * @param context - required by Places API
     * @param townNameStart - beginning chars of the town name to be searched for
     */
    public void townNameStartChanged(Context context, String townNameStart) {

        Set<String> matchingTowns = new HashSet<>();

        Places.initialize(context, context.getResources().getString(R.string.google_maps_key));

        // Create a new PlacesClient instance
        PlacesClient placesClient = Places.createClient(context);

        // Create a new token for the autocomplete session. Pass this to FindAutocompletePredictionsRequest,
        // and once again when the user makes a selection (for example when calling fetchPlace()).
        AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();

        // Create a RectangularBounds object, bounds of the Czech Republic
        RectangularBounds bounds = RectangularBounds.newInstance(
                new LatLng(48.5, 12.0),
                new LatLng(51.1,  18.9));
        // Use the builder to create a FindAutocompletePredictionsRequest.
        FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                // Call either setLocationBias() OR setLocationRestriction().
                .setLocationRestriction(bounds)
                .setCountries("CZ")
                .setTypeFilter(TypeFilter.CITIES)
                .setSessionToken(token)
                .setQuery(townNameStart)
                .build();

        placesClient.findAutocompletePredictions(request).addOnSuccessListener((response) -> {
            for (AutocompletePrediction prediction : response.getAutocompletePredictions()) {
                matchingTowns.add(prediction.getPrimaryText(null).toString());
            }
            refresh(matchingTowns); // update the drop down list
        }).addOnFailureListener((exception) -> {
            if (exception instanceof ApiException) {
                ApiException apiException = (ApiException) exception;
                Log.e(TAG, "Place not found: " + apiException.getStatusCode());
            }
        });
    }
}
