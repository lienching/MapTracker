package lien.ching.maptracker.api;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import org.mapsforge.map.layer.labels.LabelLayer;
import org.mapsforge.map.layer.renderer.TileRendererLayer;
import org.mapsforge.map.reader.MapFile;
import org.mapsforge.map.rendertheme.InternalRenderTheme;

import java.io.File;
import java.net.URL;

import lien.ching.maptracker.Constant;
import lien.ching.maptracker.overlay.NowLocationLayout;


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
    private NowLocationLayout locationLayout;
    public mapDownloadManger(MapView mapView,NowLocationLayout locationLayout,Context context, final String target){
        super();
        this.context = context;
        this.target = target;
        this.mapview = mapView;
        this.locationLayout = locationLayout;
    }
    @Override
    public void run() {
        downloadManager = (DownloadManager) context.getSystemService(context.DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse("http://download.mapsforge.org/maps/" + target));
        request.setDestinationInExternalPublicDir("mapsforge/maps/",target);
        enqueue = downloadManager.enqueue(request);
        receiver = new LayerAdder(mapview,locationLayout,target,enqueue);
        context.registerReceiver(receiver,new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }


}
