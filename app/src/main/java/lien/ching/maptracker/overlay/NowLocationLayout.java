package lien.ching.maptracker.overlay;

import android.Manifest;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
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
import org.mapsforge.map.android.view.MapView;
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


//This class is using LocationManager to provided location information it may cause some issue, for more reliable location information use  Google Play services location APIs(http://developer.android.com/training/location/index.html)

public class NowLocationLayout extends Layer implements LocationListener,GpsStatus.Listener,ActivityCompat.OnRequestPermissionsResultCallback {


    private Activity activity;
    private MapViewPosition mapViewPosition;
    private MapView mapView;
    private LocationManager locationManager;
    private List<LatLong> history_path;

    private Boolean track;

    //For drawing purpose (http://mapsforge.org/docs/0.6.0/org/mapsforge/map/android/graphics/AndroidGraphicFactory.html)
    private static final GraphicFactory GRAPHIC_FACTORY = AndroidGraphicFactory.INSTANCE;
    private Circle accuray_circle;
    private Marker loc_marker;
    private Polyline usr_path;




    //Constructor
    public NowLocationLayout(Activity activity,MapViewPosition mapViewPosition,MapView mapView){
        this.activity = activity;
        this.mapViewPosition = mapViewPosition;
        this.mapView = mapView;
        this.locationManager = (LocationManager) activity.getSystemService(activity.LOCATION_SERVICE);

        track = false;

        //http://developer.android.com/reference/android/location/LocationManager.html#requestLocationUpdates%28java.lang.String,%20long,%20float,%20android.location.LocationListener%29
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L,0, this);
        //Check if location services available
        if(!locationManager.isProviderEnabled (LocationManager.GPS_PROVIDER)){
            activity.startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), 0);
        }

        locationManager.addGpsStatusListener(this);//To add satellite listener


        //setDisplayModel(this.displayModel) set circle, polyline, marker etc... in to mapview's display model, so DisplayModel.getTileSize() won't point to a null object.
        accuray_circle = new Circle(null,0,getDefaultCircleFill(),getDefaultCircleStroke());//Circle(LatLong,radius,color,stroke);
        accuray_circle.setDisplayModel(mapView.getModel().displayModel);

        usr_path = new Polyline(getDefaultPolylineStroke(),GRAPHIC_FACTORY);
        usr_path.setDisplayModel(mapView.getModel().displayModel);

        //AndroidGraphicFactory.convertToBitmap() for detail please check AndroidGraphicFactory
        Drawable drawable = activity.getResources().getDrawable(R.drawable.locationmarker);
        Bitmap bitmap = AndroidGraphicFactory.convertToBitmap(drawable);
        loc_marker = new Marker(null, bitmap, 1, 0);
        loc_marker.setDisplayModel(mapView.getModel().displayModel);

        history_path = new LinkedList<LatLong>();//record user path
    }

    //Transfer Function

    private LatLong LocationToLatLong(Location location){
        LatLong latlong = new LatLong(location.getLatitude(),location.getLongitude());
        return latlong;
    }

    //Getter
    protected Paint getPaint(int color, float strokeWidth, Style style){
        Paint paint = GRAPHIC_FACTORY.createPaint();
        paint.setColor(color);
        paint.setStrokeWidth(strokeWidth);
        paint.setStyle(style);
        return paint;
    }

    private Paint getDefaultCircleFill() {
        return this.getPaint(GRAPHIC_FACTORY.createColor(48, 0, 0, 255), 0, Style.FILL);
    }

    private Paint getDefaultCircleStroke() {
        return this.getPaint(GRAPHIC_FACTORY.createColor(160, 0, 0, 255), 2, Style.STROKE);
    }

    private Paint getDefaultPolylineStroke() {
        return this.getPaint(GRAPHIC_FACTORY.createColor(160, 0, 255, 0), 20, Style.STROKE);
    }


    //Enable Location Tracking
    public void startTrack(){
        this.track = true;
    }


    @Override
    public void draw(BoundingBox boundingBox, byte zoomLevel, Canvas canvas, Point topLeftPoint) {
        if(track) {
            loc_marker.draw(boundingBox, zoomLevel, canvas, topLeftPoint);
            accuray_circle.draw(boundingBox, zoomLevel, canvas, topLeftPoint);
        }
        List<LatLong> temp = usr_path.getLatLongs(); //To get all the LatLong that had been drew
        temp.removeAll(temp); //Remove all
        temp.addAll(history_path);//To re-add all path
        usr_path.draw(boundingBox, zoomLevel, canvas, topLeftPoint);
    }

    @Override
    public void onGpsStatusChanged(int event) {

    }

    @Override
    public void onLocationChanged(Location location) {

        LatLong latLong = this.LocationToLatLong(location);
        loc_marker.setLatLong(latLong);
        accuray_circle.setLatLong(latLong);
        history_path.add(latLong);

        //https://stackoverflow.com/a/13807786
        accuray_circle.setRadius(location.getAccuracy());

        mapViewPosition.setCenter(latLong);
        mapViewPosition.setZoomLevel((byte) 17);
        requestRedraw();

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}

    @Override
    public void onProviderEnabled(String provider) {
        locationManager.requestLocationUpdates(provider, 1000L, 0, this);
        if(locationManager.GPS_PROVIDER.equals(provider)) {
            track = true;
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        if(locationManager.GPS_PROVIDER.equals(provider)){
            track = false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {}

}
