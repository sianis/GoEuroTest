package sianis.org.goeurotest;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnItemClick;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import sianis.org.goeurotest.network.model.Place;

public class PlacePickerActivity extends ActionBarActivity implements GoogleApiClient.ConnectionCallbacks, LocationListener {

    public static final String EXTRA_SELECTED_PLACE = "EXTRA_SELECTED_PLACE";
    public static final String EXTRA_SELECTED_DIRECTION = "EXTRA_SELECTED_DIRECTION";
    public static final int EXTRA_DIRECTION_FROM = 0;
    public static final int EXTRA_DIRECTION_TO = 1;

    private GoogleApiClient mGoogleApiClient;

    @InjectView(R.id.place)
    EditText place;

    @InjectView(R.id.list)
    ListView listView;

    @InjectView(R.id.clear)
    View clear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_picker);
        ButterKnife.inject(this);

        if (getIntent() != null && getIntent().hasExtra(EXTRA_SELECTED_DIRECTION)) {
            if (getIntent().getIntExtra(EXTRA_SELECTED_DIRECTION, EXTRA_DIRECTION_FROM) == EXTRA_DIRECTION_FROM) {
                setTitle(getString(R.string.choose_departure));
            } else {
                setTitle(getString(R.string.choose_arrival));
            }
        }

        place.addTextChangedListener(customTextWatcher);
        if (getIntent() != null && getIntent().hasExtra(EXTRA_SELECTED_PLACE)) {
            place.setText(getIntent().getStringExtra(EXTRA_SELECTED_PLACE));
        }

        buildGoogleApiClient();
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();
    }

    @OnItemClick(R.id.list)
    void itemClicked(int position) {
        select(listView.getAdapter().getItem(position).toString());
    }


    @OnClick(R.id.clear)
    void clear() {
        place.getText().clear();
        listView.setAdapter(null);
    }

    private void select(String selectedPlace) {
        Intent data = new Intent();
        data.putExtra(EXTRA_SELECTED_PLACE, selectedPlace);
        setResult(Activity.RESULT_OK, data);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(place.getWindowToken(), 0);
        finish();
    }

    //Location part
    @Override
    protected void onResume() {
        super.onResume();
        if (!mGoogleApiClient.isConnected() && !mGoogleApiClient.isConnecting()) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected() || mGoogleApiClient.isConnecting()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null && isLocationFresh(mLastLocation)) {
            //Save it and use it
            GoEuroApplication.setLastKnownLocation(mLastLocation);
            mGoogleApiClient.disconnect();
        } else {
            //Request location update
            LocationRequest locationRequest = new LocationRequest().setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        //Ignore
    }

    @Override
    public void onLocationChanged(Location location) {
        if (isLocationFresh(location)) {
            GoEuroApplication.setLastKnownLocation(location);
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }

    private boolean isLocationFresh(Location location) {
        return location != null && Calendar.getInstance().getTimeInMillis() - location.getTime() < DateUtils.DAY_IN_MILLIS;
    }

    //TextWatcher
    TextWatcher customTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            //Not necessary
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            //Not necessary
        }

        @Override
        public void afterTextChanged(Editable s) {
            clear.setVisibility(s.length() == 0 ? View.INVISIBLE : View.VISIBLE);
            if (s.length() >= 2) {
                final String term = s.toString().trim();
                GoEuroApplication.getService().listPlaces(Locale.getDefault().getLanguage(), term, new Callback<List<Place>>() {
                    @Override
                    public void success(List<Place> places, Response response) {
                        //Check answer for current text
                        if (term.equals(place.getText().toString().trim())) {
                            if (GoEuroApplication.getLastKnownLocation() != null) {
                                //Order list by distance
                                Collections.sort(places, new Comparator<Place>() {
                                    @Override
                                    public int compare(Place lhs, Place rhs) {
                                        Location lhsLocation = new Location("");
                                        lhsLocation.setLatitude(lhs.geo_position.latitude);
                                        lhsLocation.setLongitude(lhs.geo_position.longitude);
                                        Location rhsLocation = new Location("");
                                        rhsLocation.setLatitude(rhs.geo_position.latitude);
                                        rhsLocation.setLongitude(rhs.geo_position.longitude);
                                        return Float.compare(GoEuroApplication.getLastKnownLocation().distanceTo(lhsLocation), GoEuroApplication.getLastKnownLocation().distanceTo(rhsLocation));
                                    }
                                });
                            }
                            ArrayAdapter<Place> placesAdapter = new ArrayAdapter<Place>(place.getContext(), android.R.layout.simple_dropdown_item_1line, places);
                            listView.setAdapter(placesAdapter);
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        //We load nothing
                    }
                });
            }
        }
    };
}