package app.in.lafolle.musendrid;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends ActionBarActivity {

    private final String DEBUG_TAG = MainActivity.class.getSimpleName();


    private Transport transport = null;
    private HostConfig hostconfig;

    private GestureDetectorCompat gestureDetectorCompat;

    private final String LOG_DEBUG = MainActivity.class.getSimpleName();

    class MusendridGestureListener extends GestureDetector.SimpleOnGestureListener {

        private static final String LOG_DEBUG = "gesture";

        @Override
        public void onLongPress(MotionEvent e) {
            // toggle actionbar's visibility

            android.support.v7.app.ActionBar actionBar = getSupportActionBar();
            if (actionBar.isShowing()) {
                actionBar.hide();
            } else {
                actionBar.show();
            }
            super.onLongPress(e);
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return super.onDown(e);
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return super.onSingleTapUp(e);
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            Log.d(LOG_DEBUG, "single tap");
            Map jsonmap = new HashMap();
            jsonmap.put("mtype", "1P_T");
            transport.Write(new JSONObject(jsonmap).toString().getBytes());
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            Log.d(LOG_DEBUG, "double tap");
            Map jsonmap = new HashMap();
            jsonmap.put("mtype", "1P_DT");
            transport.Write(new JSONObject(jsonmap).toString().getBytes());
            return true;
        }
    }

    public Handler hostMessageHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            Map map = (HashMap) msg.obj;

            if (map.get("mtype").equals("INIT_CONN")) {
                hostconfig.screen_height = Integer.parseInt((String) map.get("screen_height"));
                hostconfig.screen_width = Integer.parseInt((String) map.get("screen_width"));
                Log.d(LOG_DEBUG, "hostconfig is set: " + hostconfig.toString());
            }

        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        transport.Close();
    }

    public class HostConfig {

        public int screen_height;
        public int screen_width;

        @Override
        public String toString() {
            return "(" + Integer.toString(screen_width) + ", " + Integer.toString(screen_height) + ")";
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_DEBUG, "created");


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().hide();
        gestureDetectorCompat = new GestureDetectorCompat(this, new MusendridGestureListener());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        this.gestureDetectorCompat.onTouchEvent(event);

        int action = MotionEventCompat.getActionMasked(event);

        int pointerCount = event.getPointerCount();
        int x, y;

        switch (action) {
            case (MotionEvent.ACTION_DOWN): // primary pointer moves down
                Log.d(DEBUG_TAG, "Action was DOWN " + Integer.toString(pointerCount));
                x = (int) event.getX(pointerCount - 1);
                y = (int) event.getY(pointerCount - 1);

                Map map;
                map = new HashMap<String, String>();
                map.put("mtype", "1P_D");
                map.put("coord_x", x);
                map.put("coord_y", y);
                transport.Write(new JSONObject(map).toString().getBytes());
                return true;
            case (MotionEvent.ACTION_POINTER_DOWN): // secondry pointer moves up
                Log.d(DEBUG_TAG, "new pointer came: " + Integer.toString(pointerCount));
                return true;
            case (MotionEvent.ACTION_POINTER_UP): // secondry pointer moves up
                Log.d(DEBUG_TAG, "pointer up: " + Integer.toString(pointerCount));
                return true;
            case (MotionEvent.ACTION_MOVE):
                if (pointerCount == 1) {
                    int hs = event.getHistorySize();

                    x = (int) event.getX(0);
                    y = (int) event.getY(0);

                    Map mmap = new HashMap();
                    mmap.put("mtype", "1P_M");
                    mmap.put("coord_x", x);
                    mmap.put("coord_y", y);
                    transport.Write(new JSONObject(mmap).toString().getBytes());
//                    Log.d(LOG_DEBUG, new JSONObject(mmap).toString());
                }

                /*
                 * 2 finger scrolling
                 * Determine direction of motion and scroll
                 * horizontally or vertically
                 */
                if (pointerCount == 2) {

                    // all only primary pointer to fire events
                    if (event.getActionIndex() != 0) return true;
                    String mtype = null;
                    int hs = event.getHistorySize();

                    // edge condition checks
                    if (hs == 0) return true;
                    int lasty = (int) event.getHistoricalY(hs - 1);
                    y = (int) event.getY(0);

                    if (lasty > y) mtype = "2P_M_UP";
                    if (lasty < y) mtype = "2P_M_DOWN";

                    if (mtype == null) return true;

                    Map mmap = new HashMap();
                    mmap.put("mtype", mtype);
//                    mmap.put("coord_y", y);
                    transport.Write(new JSONObject(mmap).toString().getBytes());
                    Log.d(LOG_DEBUG, new JSONObject(mmap).toString());
                }

                /*
                 * 3 finger slide
                 * for workspace swtching
                 */
                if (pointerCount == 3) {

                }

                return true;
            case (MotionEvent.ACTION_UP): // primary pointer moves up
                Log.d(DEBUG_TAG, "Action was UP");
                x = (int) event.getX(pointerCount - 1);
                y = (int) event.getY(pointerCount - 1);

                Map umap;
                umap = new HashMap<String, String>();
                umap.put("mtype", "1P_U");
                umap.put("coord_x", x);
                umap.put("coord_y", y);
                transport.Write(new JSONObject(umap).toString().getBytes());
                return true;
            case (MotionEvent.ACTION_CANCEL):
                Log.d(DEBUG_TAG, "Action was CANCEL");
                return true;
            case (MotionEvent.ACTION_OUTSIDE):
                Log.d(DEBUG_TAG, "Movement occurred outside bounds " +
                        "of current screen element");
                return true;
            default:
                return super.onTouchEvent(event);
        }
    }

    @Override
    protected void onStart() {

        super.onStart();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onStop() {
        Log.d(LOG_DEBUG, "onStop");
        super.onStop();
    }

    @Override
    protected void onResume() {
        Log.d(LOG_DEBUG, "onResume");

        if (transport == null) {
            transport = new Transport();
        }
        transport.InitConn(getApplicationContext(),
                hostMessageHandler);

        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // close the transport
        transport.Close();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {

            Log.d(DEBUG_TAG, "Starting settingactvity");

            Intent intent = new Intent(this, SettingsActivtity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_reconnect) {

            /* Destroy earlier transport */
            if (transport != null) {
                transport.Reconnect();
            } else {
                transport = new Transport();
                transport.InitConn(getApplicationContext(),
                        hostMessageHandler);
            }
        }
        return super.onOptionsItemSelected(item);
    }

}
