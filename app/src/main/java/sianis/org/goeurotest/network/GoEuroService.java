package sianis.org.goeurotest.network;

import java.util.List;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Path;
import sianis.org.goeurotest.network.model.Place;

public interface GoEuroService {
    @GET("/position/suggest/{locale}/{term}")
    void listPlaces(@Path("locale") String locale, @Path("term") String term, Callback<List<Place>> callback);
}
