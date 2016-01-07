package lien.ching.maptracker.api;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.util.AndroidUtil;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.layer.cache.TileCache;
import org.mapsforge.map.layer.labels.LabelLayer;
import org.mapsforge.map.layer.renderer.TileRendererLayer;
import org.mapsforge.map.reader.MapFile;
import org.mapsforge.map.rendertheme.InternalRenderTheme;
import org.mapsforge.map.rendertheme.rule.RenderTheme;

import java.io.File;

import lien.ching.maptracker.Constant;
import lien.ching.maptracker.overlay.NowLocationLayout;

/**
 * Created by lienching on 1/7/16.
 */
public class LayerAdder extends BroadcastReceiver {
    private MapView mapView;
    private String target;
    private NowLocationLayout locationLayout;
    private Long enqueue;
    public LayerAdder(MapView mapView,NowLocationLayout locationLayout,String target,Long enqueue){
        super();
        this.mapView = mapView;
        this.target = target;
        this.enqueue = enqueue;
        this.locationLayout = locationLayout;
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
            long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
            if(downloadId == enqueue){
                MapFile targetFile = new MapFile(new File(Constant.PATH_MAPSFORGE+target));
                TileCache tileCache = AndroidUtil.createTileCache(context, "mapcache", mapView.getModel().displayModel.getTileSize(), 1f, mapView.getModel().frameBufferModel.getOverdrawFactor());
                TileRendererLayer tileRendererLayer = AndroidUtil.createTileRendererLayer(tileCache,mapView.getModel().mapViewPosition,targetFile, InternalRenderTheme.OSMARENDER,true,true);
                tileRendererLayer.setXmlRenderTheme(InternalRenderTheme.OSMARENDER);
                mapView.getLayerManager().getLayers().add(tileRendererLayer);
                mapView.getLayerManager().getLayers().remove(locationLayout);
                mapView.getLayerManager().getLayers().add(locationLayout);
                Log.d("LayerAdder","LayerAdd");
            }
        }
    }
}
