package lien.ching.maptracker;

import android.os.Environment;

/**
 * Created by lienching on 11/27/15.
 */
public class Constant {
    public static final String PATH_MAPSFORGE = Environment.getExternalStorageDirectory().toString() + "/mapsforge/maps/";
    public static final String PATH_WORLDMAP = PATH_MAPSFORGE + "world/world-lowres-0-7.map";
    public static final String PATH_TAIWANMAP = PATH_MAPSFORGE + "asia/taiwan.map";

}
