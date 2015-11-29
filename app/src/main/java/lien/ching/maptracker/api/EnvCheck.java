package lien.ching.maptracker.api;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;

import lien.ching.maptracker.R;

/**
 * Created by lienching on 11/27/15.
 * This class is use for checking if the device have the required map file or not.
 */
public class EnvCheck {
    private Context context;
    private String externalpath = Environment.getExternalStorageDirectory().getPath();
    private MapDownloadManager mapDownloadManager;
    public EnvCheck(Context context){
        this.context = context;
    }

    //All in one method(Check&Download)
    public String CheckAndDownload(final String mapfile){
        if(this.isMapResourceExist(mapfile)){
            return externalpath+"/mapsforge/maps/"+mapfile;
        }
        else{
            mapDownloadManager = new MapDownloadManager(context);
            String[] str = {mapfile};
            mapDownloadManager.execute(str);
        }
        while(true) {
            try {
                if (mapDownloadManager.get()) {
                    break;
                }
                wait(1000);
            } catch (Exception e) {
                Log.e("EnvCheck", e.toString());
            }
        }
        return externalpath+"/mapsforge/maps/"+mapfile;
    }

    public boolean isMapResourceExist(final String mapfile){
        if(!(new File(externalpath+"/mapsforge").exists()))
            createDirectory();
        File searchFile = new File(externalpath+"/mapforge/maps/"+mapfile);
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
