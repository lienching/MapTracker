package lien.ching.maptracker.api;

import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;

import lien.ching.maptracker.MainActivity;

/**
 * Created by lienching on 11/27/15.
 * This class make download map source file much easier.
 */
public class MapDownloadManager extends AsyncTask<String,Void,Boolean>{
    private DownloadManager downloadManager;
    private DownloadManager.Request request;
    private String external_path = Environment.getExternalStorageDirectory().toString();
    private Context context;
    private ProgressDialog progressDialog;
    public MapDownloadManager(Context context){
        this.context = context;
        downloadManager = (DownloadManager)context.getSystemService(Context.DOWNLOAD_SERVICE);

    }

    @Override
    protected Boolean doInBackground(String... params) {
        Uri download_URI = Uri.parse("http://download.mapsforge.org/maps/"+params[0]);
        request = new DownloadManager.Request(download_URI);
        //Set the network type which is allow to process the download request
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
        //Set whether this download may proceed over a roaming connection.
        request.setAllowedOverRoaming(false);
        request.setTitle("Mapsforge Map Download");
        request.setDestinationInExternalPublicDir("/mapsforge/maps/", params[0]);
        downloadManager.enqueue(request);
        return true;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Map Source Downloading...");
        progressDialog.setCancelable(false);
        progressDialog.show();

    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);
        progressDialog.cancel();
    }


}
