package xiaoyu.recorder;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by Xiaoyu on 10/29/2016.
 */
public class GeoLocation {
    private Location location;
    private LocationManager locationManager;
    private String locationProvider;

    LocationListener locationListener =  new LocationListener() {

        @Override
        public void onStatusChanged(String provider, int status, Bundle arg2) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }

        @Override
        public void onLocationChanged(Location location) {
            //if location change, show again
            //showLocation(location);
        }
    };

    public Location getLocation(Context context) {
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        System.out.println("testestest");
        //requestGPS(locationManager);
        List<String> providers = locationManager.getProviders(true);
        if (providers.contains(LocationManager.NETWORK_PROVIDER)) {
            // Network provider
            locationProvider = LocationManager.NETWORK_PROVIDER;
        } else if (providers.contains(LocationManager.GPS_PROVIDER)) {
            // GPS provider
            locationProvider = LocationManager.GPS_PROVIDER;
        } else {
            //no location provider
            System.out.println("No location provider!!!!");
            return null;
        }
        location = locationManager.getLastKnownLocation(locationProvider);
        locationManager.requestLocationUpdates(locationProvider, 3000, 1,locationListener);

        return location;
    }


    public String getGeocoder(double longitude, double latitude, Context context)
    {
        String address ="";
        //getBaseContext()
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addressList = geocoder.getFromLocation(latitude,longitude , 1);
            if(addressList.size() >0)
            {
                for (int i=0; i < addressList.get(0).getMaxAddressLineIndex(); i++)
                {
                    address += addressList.get(0).getAddressLine(i)+" ";
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return address;
    }
}
