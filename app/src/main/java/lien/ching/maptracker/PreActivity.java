package lien.ching.maptracker;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import lien.ching.maptracker.api.EnvCheck;

/**
 * Created by lienching on 12/10/15.
 */
public class PreActivity extends Activity{
    private EnvCheck envCheck;
    private Intent intent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.main);
        envCheck = new EnvCheck(this.getApplicationContext(),this);
        intent = new Intent(this,MainActivity.class);
    }

    @Override
    protected void onStart() {
        super.onStart();
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Map Source Downloading");
        dialog.setCancelable(false);
        dialog.setIndeterminate(true);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.show();
        envCheck.CheckAndDownload("world", "world-lowres-0-7.map");
        envCheck.CheckAndDownload("asia", "taiwan.map");
        dialog.cancel();
        startActivity(intent);
    }
}
