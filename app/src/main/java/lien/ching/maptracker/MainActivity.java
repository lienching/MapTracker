package lien.ching.maptracker;

import android.app.Activity;
import android.os.Bundle;

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
        AndroidGraphicFactory.createInstance(this.getApplication());
        mapView = new MapView(this);
        mapView.setClickable(true);
        mapView.getMapScaleBar().setVisible(true);
        mapView.setBuiltInZoomControls(true);
        mapView.getMapZoomControls().setZoomLevelMin((byte) 5);
        mapView.getMapZoomControls().setZoomLevelMax((byte) 20);
        setContentView(mapView);
        nowLocationLayout = new NowLocationLayout(this,mapView.getModel().mapViewPosition);

    }

    @Override
    protected void onStart() {
        super.onStart();
        envCheck = new EnvCheck(this);
        envCheck.CheckAndDownload("world","world-lowres-0-7.map");
        envCheck.CheckAndDownload("asia","taiwan.map");
        worldMap = new MapFile(new File(Constant.PATH_WORLDMAP));
        taiwanMap = new MapFile(new File(Constant.PATH_TAIWANMAP));
        multiMapDataStore = new MultiMapDataStore(MultiMapDataStore.DataPolicy.RETURN_ALL);

        tileCache = AndroidUtil.createTileCache(this, "mapcache", mapView.getModel().displayModel.getTileSize(), 1f, this.mapView.getModel().frameBufferModel.getOverdrawFactor());

        multiMapDataStore.addMapDataStore(worldMap,true,true);
        multiMapDataStore.addMapDataStore(taiwanMap,false,false);

        tileRendererLayer = new TileRendererLayer(tileCache,multiMapDataStore,mapView.getModel().mapViewPosition,false,true, AndroidGraphicFactory.INSTANCE);
        tileRendererLayer.setXmlRenderTheme(InternalRenderTheme.OSMARENDER);

        mapView.getModel().mapViewPosition.setMapPosition(new MapPosition(new LatLong(23.6, 121), (byte) 7));


        //Adding Layout(first add display layout and then add control layout)
        mapView.getLayerManager().getLayers().add(tileRendererLayer);
        mapView.getLayerManager().getLayers().add(nowLocationLayout);
        nowLocationLayout.enableMyLocation(true);
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
