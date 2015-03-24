package sianis.org.goeurotest.network.model;

public class Place {
    public String fullName;
    public Location geo_position;

    public class Location {
        public double latitude;
        public double longitude;
    }

    @Override
    public String toString() {
        return fullName;
    }
}
