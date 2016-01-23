package lien.ching.maptracker.api;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import lien.ching.maptracker.Constant;

/**
 * Created by lienching on 12/1/15.
 * In this class, I had no idea how to update a file by using DownloadManager
 * so I just try to use standard HTTP Download as the method
 */
public class MapUpdateManager extends AsyncTask<String,Void,Boolean> {

    private ProgressDialog dialog;
    private PowerManager.WakeLock wakeLock;
    private Context context;
    private Activity activity;
    public MapUpdateManager(Context context, Activity activity){
        super();
        this.activity = activity;
        this.context = context;
    }
    @Override
    protected Boolean doInBackground(String... params) {
        //For Details, please check http://stackoverflow.com/a/3028660
        String continent = params[1];
        InputStream input = null;
        OutputStream output = null;
        HttpURLConnection connection = null;
        File outputFile = new File(Constant.PATH_MAPSFORGE+params[0]);
        try{
            URL url = new URL("http://download.mapsforge.org/maps/"+params[0]);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            if(connection.getResponseCode() != HttpURLConnection.HTTP_OK){
                Toast.makeText(context,"Connection Fail..."+connection.getResponseCode(),Toast.LENGTH_SHORT).show();
                return false;
            }
            input = connection.getInputStream();


            if (outputFile.lastModified()==connection.getLastModified()){ //if the date is same as the latest file then close the download process
                input.close();
                output.close();
                return true;
            }
            dialog.setMessage("Map Source Updating");
            outputFile.delete();
            outputFile.createNewFile();
            output = new FileOutputStream(outputFile);
            byte data[] = new byte[8192];
            int count;
            while((count = input.read(data)) != -1){
                if(isCancelled()){
                    input.close();
                    return false;
                }

                output.write(data,0,count);
            }
        }catch (Exception e){
            Log.e("MapDownloadManager","Writing Data:"+e.toString());
            return false;
        }finally {
            try {
                if (output != null)
                    output.close();
                if (input != null)
                    input.close();

            } catch (IOException ignored) {
            }
            outputFile.setLastModified(connection.getLastModified());
            if (connection != null)
                connection.disconnect();
        }
        return false;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        PowerManager pm = (PowerManager) activity.getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, activity.getClass().getName());
        wakeLock.acquire();
        dialog = new ProgressDialog(activity);
        dialog.setMessage("Map Source Checking");
        dialog.setCancelable(false);
        dialog.setIndeterminate(true);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.show();
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);
        if(dialog.isShowing()){
            dialog.dismiss();
        }
        if(wakeLock.isHeld())
            wakeLock.release();
        try {
            if (!aBoolean)
                Toast.makeText(context, "Download error", Toast.LENGTH_LONG).show();
            else
                Toast.makeText(context, "File downloaded", Toast.LENGTH_SHORT).show();
        }
        catch (Exception e){
            Log.d("MapUpdateManager", e.toString());
        }
    }
}
