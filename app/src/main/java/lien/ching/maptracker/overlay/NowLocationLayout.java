package lien.ching.maptracker.overlay;

import android.Manifest;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.TextView;

import org.mapsforge.core.graphics.Bitmap;
import org.mapsforge.core.graphics.Canvas;
import org.mapsforge.core.graphics.GraphicFactory;
import org.mapsforge.core.graphics.Paint;
import org.mapsforge.core.graphics.Style;
import org.mapsforge.core.model.BoundingBox;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.Point;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.layer.Layer;
import org.mapsforge.map.layer.overlay.Circle;
import org.mapsforge.map.layer.overlay.Marker;
import org.mapsforge.map.layer.overlay.Polyline;
import org.mapsforge.map.model.MapViewPosition;
import org.mapsforge.map.android.util.AndroidSupportUtil;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import lien.ching.maptracker.R;

/**
 * Created by lienching on 12/2/15.
 */

//TODO AGPS判斷
public class NowLocationLayout extends Layer implements LocationListener,GpsStatus.Listener,ActivityCompat.OnRequestPermissionsResultCallback {

    private final byte PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 10;
    private static final GraphicFactory GRAPHIC_FACTORY = AndroidGraphicFactory.INSTANCE;
    private float minDistance = 0.0f;
    private long minTime = 0;
    private MapViewPosition mapViewPosition;
    private Activity activity;
    private boolean centerAtNextFix;
    private final Circle circle;
    private Location lastLocation;
    private final LocationManager locationManager;
    private final Marker marker;
    private final Polyline polyline;
    private List<LatLong> history;
    private boolean myLocationEnabled;
    private boolean snapToLocationEnabled;
    private Bitmap bitmap;
    private GpsStatus mGpsStatus;
    private int satellitenum;
    /**
     * @param location
     *            the location whose geographical coordinates should be converted.
     * @return a new LatLong with the geographical coordinates taken from the given location.
     */
    public static LatLong locationToLatLong(Location location) {
        return new LatLong(location.getLatitude(), location.getLongitude(), true);
    }

    private static Paint getDefaultCircleFill() {
        return getPaint(GRAPHIC_FACTORY.createColor(48, 0, 0, 255), 0, Style.FILL);
    }

    private static Paint getDefaultCircleStroke() {
        return getPaint(GRAPHIC_FACTORY.createColor(160, 0, 0, 255), 2, Style.STROKE);
    }

    private static Paint getDefaultPolylineStroke() {
        return getPaint(GRAPHIC_FACTORY.createColor(160, 0, 255,0), 20, Style.STROKE);
    }

    private static Paint getPaint(int color, int strokeWidth, Style style) {
        Paint paint = GRAPHIC_FACTORY.createPaint();
        paint.setColor(color);
        paint.setStrokeWidth(strokeWidth);
        paint.setStyle(style);
        return paint;
    }

    private int getSatelliteInUseNum(){
        return this.satellitenum;
    }

    public NowLocationLayout(Activity activity, MapViewPosition mapViewPosition) {
        super();
        this.activity = activity;
        this.mapViewPosition = mapViewPosition;
        this.locationManager = (LocationManager) activity.getSystemService(activity.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L,1.0f, this);
        if(!locationManager.isProviderEnabled (LocationManager.GPS_PROVIDER)){
            activity.startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), 0);
        }

        Drawable drawable = activity.getResources().getDrawable(R.drawable.locationmarker);
        bitmap = AndroidGraphicFactory.convertToBitmap(drawable);
        this.marker = new Marker(null, bitmap, 1, 0);
        this.circle = new Circle(null, 0, getDefaultCircleFill(), getDefaultCircleStroke());
        this.polyline = new Polyline(getDefaultPolylineStroke(),GRAPHIC_FACTORY);
        history = new LinkedList<LatLong>();
        locationManager.addGpsStatusListener(this);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        GpsStatus gpsStatus = locationManager.getGpsStatus(null);
        if(gpsStatus != null) {
            Iterable<GpsSatellite>satellites = gpsStatus.getSatellites();
            Iterator<GpsSatellite> sat = satellites.iterator();
            String lSatellites = null;
            int i = 0;
            while (sat.hasNext()) {
                GpsSatellite satellite = sat.next();
                lSatellites = "Satellite" + (i++) + ": "
                        + satellite.getPrn() + ","
                        + satellite.usedInFix() + ","
                        + satellite.getSnr() + ","
                        + satellite.getAzimuth() + ","
                        + satellite.getElevation()+ "\n\n";

                Log.d("SATELLITE",lSatellites);
            }
            this.satellitenum = i;
        }
    }

    @Override
    public void onProviderEnabled(String provider) {
        this.enableBestAvailableProvider();
    }

    @Override
    public void onProviderDisabled(String provider) {
        this.enableBestAvailableProvider();
    }

    /**
     * Stops the receiving of location . Has no effect if location updates are already disabled.
     */
    public synchronized void disableMyLocation() {
        if (this.myLocationEnabled) {
            this.myLocationEnabled = false;
            try {
                this.locationManager.removeUpdates(this);
            } catch (Exception e) {
                // do we need to catch security exceptions for this call on Android 6?
            }
        }
    }

    @Override
    public synchronized void draw(BoundingBox boundingBox, byte zoomLevel, Canvas canvas, Point topLeftPoint) {
        if (!this.myLocationEnabled || this.getSatelliteInUseNum() < 6 || lastLocation.getAccuracy()>Criteria.ACCURACY_HIGH) {
            return;
        }
        this.circle.draw(boundingBox, zoomLevel, canvas, topLeftPoint);
        this.marker.draw(boundingBox, zoomLevel, canvas, topLeftPoint);
        List<LatLong> coordinateList = polyline.getLatLongs();
        coordinateList.removeAll(coordinateList);
        coordinateList.addAll(history);
        polyline.draw(boundingBox, zoomLevel, canvas, topLeftPoint);
    }


    public synchronized void enableMyLocation(boolean centerAtFirstFix) {
        enableBestAvailableProvider();
        this.centerAtNextFix = centerAtFirstFix;
    }

    /**
     * @return the most-recently received location fix (might be null).
     */
    public synchronized Location getLastLocation() {
        return this.lastLocation;
    }

    /**
     * @return true if the map will be centered at the next received location fix, false otherwise.
     */
    public synchronized boolean isCenterAtNextFix() {
        return this.centerAtNextFix;
    }

    /**
     * @return true if the receiving of location updates is currently enabled, false otherwise.
     */
    public synchronized boolean isMyLocationEnabled() {
        return this.myLocationEnabled;
    }

    /**
     * @return true if the snap-to-location mode is enabled, false otherwise.
     */
    public synchronized boolean isSnapToLocationEnabled() {
        return this.snapToLocationEnabled;
    }

    @Override
    public void onDestroy() {
        this.marker.onDestroy();
    }

    @Override
    public void onLocationChanged(Location location) {
        if(this.getSatelliteInUseNum()<6)return;
        synchronized (this) {
            this.lastLocation = location;

            LatLong latLong = locationToLatLong(location);
            this.marker.setLatLong(latLong);
            this.circle.setLatLong(latLong);
            history.add(latLong);
            if (location.getAccuracy() <= Criteria.ACCURACY_HIGH) {
                this.circle.setRadius(location.getAccuracy());
            } else {
                this.circle.setRadius(40);
            }

            this.mapViewPosition.setCenter(latLong);
            this.mapViewPosition.setZoomLevel((byte) 17);
            requestRedraw();
        }
    }




    public void setMinDistance(float minDistance) {
        this.minDistance = minDistance;
    }


    public void setMinTime(long minTime) {
        this.minTime = minTime;
    }

    /*
    // @param snapToLocationEnabled
   //           whether the map should be centered at each received location fix.
    public synchronized void setSnapToLocationEnabled(boolean snapToLocationEnabled) {
        this.snapToLocationEnabled = snapToLocationEnabled;
    }
    */

    private synchronized void enableBestAvailableProvider() {
        if (!AndroidSupportUtil.runtimePermissionRequiredForAccessFineLocation(activity)) {
            enableBestAvailableProviderPermissionGranted();
        } else {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    private void enableBestAvailableProviderPermissionGranted() {
        disableMyLocation();

        this.circle.setDisplayModel(this.displayModel);
        this.marker.setDisplayModel(this.displayModel);
        this.polyline.setDisplayModel(this.displayModel);

        boolean result = false;
        for (String provider : this.locationManager.getProviders(true)) {
            if (LocationManager.GPS_PROVIDER.equals(provider)
                    || LocationManager.NETWORK_PROVIDER.equals(provider)) {
                result = true;
                this.locationManager.requestLocationUpdates(provider, minTime, minDistance, this);
            }
        }
        this.myLocationEnabled = result;
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION == requestCode && AndroidSupportUtil.verifyPermissions(grantResults)) {
            enableBestAvailableProviderPermissionGranted();
        }

    }

    @Override
    public void onGpsStatusChanged(int event) {
        satellitenum = 0;
        mGpsStatus = locationManager.getGpsStatus(mGpsStatus);
        Iterable<GpsSatellite> satellites = mGpsStatus.getSatellites();
        if (satellites != null) {
            for (GpsSatellite gpsSatellite : satellites) {
                if (gpsSatellite.usedInFix()) {
                    satellitenum++;
                }
            }
        }
    }
}
