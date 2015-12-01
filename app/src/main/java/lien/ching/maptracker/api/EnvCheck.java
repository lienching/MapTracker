package lien.ching.maptracker.api;

import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

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
    public EnvCheck(Context context){
        this.context = context;
    }

    //All in one method(Check&Download)
    public void CheckAndDownload(String continent, String sourcefile){
        String mapfile = continent+"/"+sourcefile;
        if(!this.isMapResourceExist(mapfile)){
            mapDownloadManager = new MapDownloadManager(context);
            mapDownloadManager.execute(mapfile,continent);
            try {
                while (!mapDownloadManager.get(5, TimeUnit.MINUTES)) ;
            }catch (Exception e){
                Log.e("mapDownloadManager",e.toString());
            }
        }
        else{
            mapUpdateManager = new MapUpdateManager(context);
            mapUpdateManager.execute(mapfile,continent);
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
