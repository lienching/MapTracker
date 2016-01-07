package lien.ching.maptracker.api;

import android.app.Activity;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import org.mapsforge.map.android.view.MapView;

import java.io.File;
import java.util.concurrent.TimeUnit;

import lien.ching.maptracker.Constant;
import lien.ching.maptracker.R;

/**
 * Created by lienching on 11/27/15.
 * This class is use for checking if the device have the required map file or not.
 */
public class EnvCheck {
    private Context context;
    private String externalpath = Environment.getExternalStorageDirectory().getPath();
    private MapDownloadManager mapDownloadManager;
    private MapUpdateManager mapUpdateManager;
    private mapDownloadManger downloadManger;
    private Activity activity;
    private MapView mapView;
    public EnvCheck(MapView mapView,Context context,Activity activity){
        this.activity = activity;
        this.context = context;
        this.mapView = mapView;
    }
    public EnvCheck(Context context,Activity activity){
        this.activity = activity;
        this.context = context;
        this.mapView = null;
    }

    public void CheckAnddownload(String continent, String sourcefile) {
        downloadManger = new mapDownloadManger(mapView,context,continent+"/"+sourcefile);
        Thread thread = new Thread(downloadManger);
        thread.run();
    }
    //All in one method(Check&Download)
    public void CheckAndDownload(String continent, String sourcefile){
        String mapfile = continent+"/"+sourcefile;
        if(!this.isMapResourceExist(mapfile)){
            mapDownloadManager = new MapDownloadManager(context,activity);
            mapDownloadManager.execute(mapfile,continent);
            try {
                while (!mapDownloadManager.get(10, TimeUnit.MINUTES)) ;
            }catch (Exception e){
                Log.e("mapDownloadManager",e.toString());
            }
        }
        else{
            mapUpdateManager = new MapUpdateManager(context,activity);
            mapUpdateManager.execute(mapfile,continent);
            try {
                while (!mapDownloadManager.get(10, TimeUnit.MINUTES)) ;
            }catch (Exception e){
                Log.e("mapDownloadManager",e.toString());
            }
        }
    }

    public boolean isMapResourceDirExist(final String continent){
        File dir = new File(Constant.PATH_MAPSFORGE+continent);
        if(!dir.exists()) {
            if (!dir.mkdirs()) {
                Log.e("EnvCheck","Dir create failed");
                return false;
            }
            Log.d("EnvCheck","Dir Create Sucess!");
        }
        return true;
    }
    public boolean isMapResourceExist(final String mapfile){
        if(!(new File(externalpath+"/mapsforge").exists()))
            createDirectory();
        File searchFile = new File(externalpath+"/mapsforge/maps/"+mapfile);
        if(searchFile.exists())
            return true;
        return false;
    }

    public void createDirectory(){
        File target = new File(externalpath+"/mapsforge/maps");
        target.mkdirs();
        if(target.exists()&&target.isDirectory()){
            Toast.makeText(context, R.string.toast_dir_success, Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(context, R.string.toast_dir_fail_retry,Toast.LENGTH_SHORT).show();
            target.mkdir();
            if(target.exists()&&target.isDirectory()){
                Toast.makeText(context, R.string.toast_dir_success, Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(context, R.string.toast_dir_fail, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
