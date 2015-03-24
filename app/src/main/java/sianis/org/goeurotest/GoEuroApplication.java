package sianis.org.goeurotest;

import android.app.Application;
import android.location.Location;

import retrofit.RestAdapter;
import sianis.org.goeurotest.network.GoEuroService;

public class GoEuroApplication extends Application {

    private static GoEuroService service;
    private static Location lastKnownLocation;

    @Override
    public void onCreate() {
        //initialize rest service at start
        RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint("http://api.goeuro.com/api/v2").build();
        service = restAdapter.create(GoEuroService.class);
    }

    public static GoEuroService getService() {
        return service;
    }

    public static Location getLastKnownLocation() {
        return lastKnownLocation;
    }

    public static void setLastKnownLocation(Location lastKnownLocation) {
        GoEuroApplication.lastKnownLocation = lastKnownLocation;
    }
}
