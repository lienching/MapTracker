package lien.ching.maptracker;

import android.app.Activity;
import android.os.Bundle;
import android.view.WindowManager;

import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.MapPosition;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.util.AndroidUtil;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.datastore.MapDataStore;
import org.mapsforge.map.datastore.MultiMapDataStore;
import org.mapsforge.map.layer.cache.TileCache;
import org.mapsforge.map.layer.renderer.TileRendererLayer;
import org.mapsforge.map.reader.MapFile;
import org.mapsforge.map.rendertheme.InternalRenderTheme;

import java.io.File;

import lien.ching.maptracker.api.EnvCheck;
import lien.ching.maptracker.overlay.NowLocationLayout;

/**
 * Created by lienching on 11/27/15.
 */
public class MainActivity extends Activity {
    private MapView mapView;
    private MapDataStore taiwanMap,worldMap;
    private MultiMapDataStore multiMapDataStore;
    private TileCache tileCache;
    private TileRendererLayer tileRendererLayer;
    private EnvCheck envCheck;
    private NowLocationLayout nowLocationLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidGraphicFactory.createInstance(this.getApplication()); //Initiation Mapsforge Library
        mapView = new MapView(this);
        mapView.setClickable(true);
        mapView.getMapScaleBar().setVisible(true);
        mapView.setBuiltInZoomControls(true);
        mapView.getMapZoomControls().setZoomLevelMin((byte) 5);
        mapView.getMapZoomControls().setZoomLevelMax((byte) 20);
        mapView.getModel().mapViewPosition.setMapPosition(new MapPosition(new LatLong(23.6, 121), (byte) 7));
        setContentView(mapView);
        nowLocationLayout = new NowLocationLayout(this,mapView.getModel().mapViewPosition,mapView);
        envCheck = new EnvCheck(mapView,nowLocationLayout,this.getApplicationContext(),this);
        //To keep screen on(http://developer.android.com/training/scheduling/wakelock.html)
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    }

    @Override
    protected void onStart() {
        super.onStart();

        envCheck.CheckAndDownload("world", "world-lowres-0-7.map"); //Check if the map source exist
        envCheck.CheckAndDownload("asia", "taiwan.map");
        mapView.getLayerManager().getLayers().add(nowLocationLayout);//Add Location Layer
        nowLocationLayout.startTrack();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        this.mapView.destroyAll();
    }

}
