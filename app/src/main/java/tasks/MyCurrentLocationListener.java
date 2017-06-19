package tasks;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

import activities.TentActivity;

/**
 * MyCurrentLocationListener holds and manages the doctor's location
 */
public class MyCurrentLocationListener implements LocationListener {

    private Location myLocation = null;
    private double latitude = 0;
    private double longitude = 0;

    private static final int TWO_MINUTES = 1000 * 60 * 2;

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    /**
     * on location change, update the current location if it is more accurate
     * @param location the new location data
     */
    public void onLocationChanged(Location location) {
        if (myLocation == null)
            myLocation = location;
        else if (isBetterLocation(location, myLocation)){
            myLocation = location;
        }
        if (latitude != myLocation.getLatitude() || longitude != myLocation.getLongitude()){
            latitude = myLocation.getLatitude();
            longitude = myLocation.getLongitude();
            Double[] coordinates = {latitude, longitude};
            new LocationTask().execute(coordinates);
            TentActivity.lock.lock();
            try {
                TentActivity.updateToWeb = true;
            } finally {
                TentActivity.lock.unlock();
            }
        }

    }

    public void onStatusChanged(String s, int i, Bundle bundle) {}

    public void onProviderEnabled(String s) {}

    public void onProviderDisabled(String s) {}

    /**
     *
     * @param location
     * @param currentBestLocation
     * @return
     */
    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /** Checks whether two providers are the same */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }
}
