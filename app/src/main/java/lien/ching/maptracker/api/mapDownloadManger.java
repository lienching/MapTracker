package lien.ching.maptracker.api;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.util.AndroidUtil;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.datastore.MapDataStore;
import org.mapsforge.map.layer.Layer;
import org.mapsforge.map.layer.LayerManager;
import org.mapsforge.map.layer.Layers;
import org.mapsforge.map.layer.cache.TileCache;
import org.mapsforge.map.layer.renderer.TileRendererLayer;
import org.mapsforge.map.reader.MapFile;
import org.mapsforge.map.rendertheme.InternalRenderTheme;

import java.io.File;
import java.net.URL;

import lien.ching.maptracker.Constant;


/**
 * Created by lienching on 12/21/15.
 */
public class mapDownloadManger implements  Runnable{


    private DownloadManager downloadManager;
    private Context context;
    private Long enqueue;
    public MapView mapview;
    public String target;
    private BroadcastReceiver receiver;
    public mapDownloadManger(Context context, final MapView mapView, final String target){
        super();
        this.context = context;
        this.mapview = mapView;
        this.target = target;
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                    long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
                    if(downloadId == enqueue){
                        MapFile targetFile = new MapFile(new File(Constant.PATH_MAPSFORGE+target));
                        TileCache tileCache = AndroidUtil.createTileCache(context, "mapcache", mapDownloadManger.this.mapview.getModel().displayModel.getTileSize(), 1f, mapDownloadManger.this.mapview.getModel().frameBufferModel.getOverdrawFactor());
                        TileRendererLayer tileRendererLayer = new TileRendererLayer(tileCache,targetFile,mapDownloadManger.this.mapview.getModel().mapViewPosition,false,true, AndroidGraphicFactory.INSTANCE);
                        tileRendererLayer.setXmlRenderTheme(InternalRenderTheme.OSMARENDER);
                        mapView.getLayerManager().getLayers().add(tileRendererLayer);
                    }
                }
            }
        };
    }
    @Override
    public void run() {
        downloadManager = (DownloadManager) context.getSystemService(context.DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse("http://download.mapsforge.org/maps/" + target));
        enqueue = downloadManager.enqueue(request);
    }

    public void addLayout(){


        mapview.repaint();
    }
}
