package lien.ching.maptracker.api;

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
 */
public class MapUpdateManager extends AsyncTask<String,Void,Boolean> {

    private ProgressDialog dialog;
    private PowerManager.WakeLock wakeLock;
    private Context context;
    public MapUpdateManager(Context context){
        super();
        this.context = context;
    }
    @Override
    protected Boolean doInBackground(String... params) {
        String continent = params[1];
        InputStream input = null;
        OutputStream output = null;
        HttpURLConnection connection = null;
        try{
            URL url = new URL("http://download.mapsforge.org/maps/"+params[0]);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            if(connection.getResponseCode() != HttpURLConnection.HTTP_OK){
                Toast.makeText(context,"Connection Fail..."+connection.getResponseCode(),Toast.LENGTH_SHORT).show();
                return false;
            }
            input = connection.getInputStream();
            File outputFile = new File(Constant.PATH_MAPSFORGE+params[0]);

            if(connection.getContentLength()==outputFile.length())return true;

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

            if (connection != null)
                connection.disconnect();
        }
        return true;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, context.getClass().getName());
        wakeLock.acquire();
        dialog = new ProgressDialog(context);
        dialog.setMessage("Map Source Updating");
        dialog.setCancelable(false);
        dialog.setIndeterminate(true);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.show();
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);
        if(dialog.isShowing()){
            dialog.cancel();
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
