package lien.ching.maptracker.overlay;

import android.Manifest;
import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;

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
import org.mapsforge.map.model.MapViewPosition;
import org.mapsforge.map.android.util.AndroidSupportUtil;

import java.util.LinkedList;
import java.util.List;

import lien.ching.maptracker.R;

/**
 * Created by lienching on 12/2/15.
 */
public class NowLocationLayout extends Layer implements LocationListener,ActivityCompat.OnRequestPermissionsResultCallback {

    private final byte PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 10;
    private static final GraphicFactory GRAPHIC_FACTORY = AndroidGraphicFactory.INSTANCE;
    private float minDistance = 0.0f;
    private long minTime = 0;
    private MapViewPosition mapViewPosition;
    private Activity activity;

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

    private static Paint getPaint(int color, int strokeWidth, Style style) {
        Paint paint = GRAPHIC_FACTORY.createPaint();
        paint.setColor(color);
        paint.setStrokeWidth(strokeWidth);
        paint.setStyle(style);
        return paint;
    }

    private boolean centerAtNextFix;
    private final Circle circle;
    private Location lastLocation;
    private final LocationManager locationManager;
    private final Marker marker;
    private List<Marker> history;
    private boolean myLocationEnabled;
    private boolean snapToLocationEnabled;
    private Bitmap bitmap;

    public NowLocationLayout(Activity activity, MapViewPosition mapViewPosition) {
        super();
        this.activity = activity;
        this.mapViewPosition = mapViewPosition;
        this.locationManager = (LocationManager) activity.getSystemService(activity.LOCATION_SERVICE);
        Drawable drawable = activity.getResources().getDrawable(R.drawable.locationmarker);
        bitmap = AndroidGraphicFactory.convertToBitmap(drawable);
        this.marker = new Marker(null, bitmap, 0, 0);
        this.circle = new Circle(null, 0, getDefaultCircleFill(), getDefaultCircleStroke());
        history = new LinkedList<Marker>();

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

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
        if (!this.myLocationEnabled) {
            return;
        }

        this.circle.draw(boundingBox, zoomLevel, canvas, topLeftPoint);
        for(Marker marker:history){
            marker.draw(boundingBox, zoomLevel, canvas, topLeftPoint);
        }
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

        synchronized (this) {
            this.lastLocation = location;

            LatLong latLong = locationToLatLong(location);
            this.marker.setLatLong(latLong);
            this.circle.setLatLong(latLong);
            Marker tmp = new Marker(latLong,bitmap,0,0);
            tmp.setDisplayModel(this.displayModel);
            history.add(tmp);
            if (location.getAccuracy() != 0) {
                this.circle.setRadius(location.getAccuracy());
            } else {
                // on the emulator we do not get an accuracy
                this.circle.setRadius(40);
            }

            this.mapViewPosition.setCenter(latLong);

            requestRedraw();
        }
    }




    public void setMinDistance(float minDistance) {
        this.minDistance = minDistance;
    }


    public void setMinTime(long minTime) {
        this.minTime = minTime;
    }

    /**
     * @param snapToLocationEnabled
     *            whether the map should be centered at each received location fix.
     */
    public synchronized void setSnapToLocationEnabled(boolean snapToLocationEnabled) {
        this.snapToLocationEnabled = snapToLocationEnabled;
    }

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

}
