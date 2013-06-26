package com.home.dvr;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;


public class MyLoc implements LocationListener

{
	private static final String TAG = "LOC";
	public double latitude;
	public double longitud;
	@Override
	public void onLocationChanged(Location loc)

	{
		latitude = loc.getLatitude();
		longitud = loc.getLongitude();
	}

	@Override
	public void onProviderDisabled(String provider)

	{

		Log.v(TAG,"gpsDisable");

	}

	@Override
	public void onProviderEnabled(String provider)

	{

		Log.v(TAG,"gpsEnable");

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras)

	{

	}

}
