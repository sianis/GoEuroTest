package sianis.org.goeurotest.util;

import android.content.Context;
import android.widget.ArrayAdapter;

import java.util.List;

import sianis.org.goeurotest.network.model.Place;

public class SearchTermAdapter extends ArrayAdapter<Place> {

    public SearchTermAdapter(Context context, int resource, List<Place> objects) {
        super(context, resource, objects);
    }
}
