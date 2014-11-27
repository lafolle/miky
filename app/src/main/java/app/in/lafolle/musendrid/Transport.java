package app.in.lafolle.musendrid;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Created by lafolle on 4/11/14.
 */
public class Transport {

    public Transport() {
        super();
    }

    private Context context;

    // TODO: rem mac address
    private final String BLUETOOTH_SERVER_MAC = "F4:B7:E2:4C:53:9E";
    private BluetoothDevice serverDevice;
    private BluetoothSocket clientBluetoothSocket;
    private Communicate communicate;
    private android.os.Handler hostMessagehandler;
//    private BluetoothAdapter bluetoothAdapter;

    /*
     * Initializes the Bluetooth connection with
     * server by starting an async task.
     */
    public void InitConn(Context ctx, android.os.Handler handler, BluetoothAdapter bluetoothAdapter) {
        context = ctx;
        hostMessagehandler = handler;
        new InitiateBluetooth().execute(bluetoothAdapter);
    }

    public void Reconnect() {

    }

    /*
     * Close the transport connection
     * Releases the client bluetooth socket
     */
    public void Close() {
        Map map = new HashMap();
        map.put("mtype", "CLOSE_CONN");
        communicate.write(new JSONObject(map).toString().getBytes());
        communicate.cancel();
        communicate.interrupt();
        communicate = null;
    }

    /*
     * Writes data to client socket using
     * communicate instance
     */
    public void Write(byte[] bytes) {
        communicate.write(bytes);
    }

    /*
     * Main class for Bluetooth communication
     */
    private class Communicate extends Thread {

        private final String DEBUG_TAG = InitiateBluetooth.class.getSimpleName();
        private final InputStream btInputStream;
        private final OutputStream btOutputStream;
        private BluetoothSocket socket;

        public Communicate(BluetoothSocket clientsocket) {

            InputStream ins = null;
            OutputStream ots = null;

            socket = clientsocket;

            try {
                ins = socket.getInputStream();
                ots = socket.getOutputStream();
            } catch (IOException e) {
                Log.d(DEBUG_TAG, "can't get input/output stream");
            }

            btInputStream = ins;
            btOutputStream = ots;

        }

        @Override
        public void run() {

            byte[] buffer = new byte[1024];
            int bytes;

            // keep listen on inputstream
            while (true) {
                try {
                    bytes = btInputStream.read(buffer);
                    JSONObject json = new JSONObject(buffer.toString());
                    if (json.getString("mtype").equals("INIT_CONN")) {
                        // host is ready to receive events
                        Map map = new HashMap();
                        map.put("mtype", "INIT_CONN");
                        map.put("screen_width", json.getString("screen_width"));
                        map.put("screen_height", json.getString("screen_height"));
                        Message message = new Message();
                        message.obj = map;
                        hostMessagehandler.sendMessage(message);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }

        public void write(byte[] bytes) {
            try {
                String data = new String(bytes);
                Log.d("Transport.Communcate.write", "Data writing : " + data);
                btOutputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // call this from main activity to close conn
        public void cancel() {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class InitiateBluetooth extends AsyncTask<BluetoothAdapter, Void, Boolean> {

        private final String DEBUG_TAG = InitiateBluetooth.class.getSimpleName();
        private final String ERROR_TAG = InitiateBluetooth.class.getSimpleName();
        private final UUID APP_BLUETOOTH_UUID = UUID.fromString("00000000-0000-0000-0000-000023000000");

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            String text;
            if (aBoolean) {
                text = "Connection established";
            } else {
                text = "Connecton failed";
            }
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show();

            /* start the read write thread */
            communicate = new Communicate(clientBluetoothSocket);
            communicate.start();

            // send to host INIT_CONN message
            Map map = new HashMap();
            map.put("mtype", "INIT_CONN");
            JSONObject json = new JSONObject(map);
            communicate.write(json.toString().getBytes());

            super.onPostExecute(aBoolean);
        }

        @Override
        protected Boolean doInBackground(BluetoothAdapter... btas) {

            BluetoothAdapter bluetoothAdapter = btas[0];
            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
            if (pairedDevices.size() == 0) {
                Log.d(DEBUG_TAG, "no paired devices found");
            } else {
                Log.d(DEBUG_TAG, "no of paired devices are " + Integer.toString(pairedDevices.size()));
            }

            for (BluetoothDevice device : pairedDevices) {
                Log.d(DEBUG_TAG, "paired device: " + device.getAddress() + ", " + BLUETOOTH_SERVER_MAC);
                if (device.getAddress().equals(BLUETOOTH_SERVER_MAC)) {
                    // we found the device, break
                    Log.d(DEBUG_TAG, "found device");
                    serverDevice = device;
                    break;
                }
            }

            if (serverDevice == null) {
                Log.d(DEBUG_TAG, "serverdevice is null");
                return false;
            }

            try {
                Log.d(DEBUG_TAG, "getting client bluetoothsocket");
                clientBluetoothSocket = serverDevice.createRfcommSocketToServiceRecord(APP_BLUETOOTH_UUID);
            } catch (IOException e) {
                Log.e(ERROR_TAG, "exception occurred when creating clientBluetoothSocket");
            }

            try {
                Log.d(DEBUG_TAG, "connecting to bluetooth");
                clientBluetoothSocket.connect();

            } catch (IOException e) {
                Log.d(DEBUG_TAG, "connecting to bluetoothserver failed: " + e.getMessage());
                Log.d(DEBUG_TAG, "Trying fallback");

                try {
                    clientBluetoothSocket = (BluetoothSocket) serverDevice.getClass().getMethod("createRfcommSocket", new Class[]{int.class}).invoke(serverDevice, 1);
                    clientBluetoothSocket.connect();
                } catch (IllegalAccessException e1) {
                    e1.printStackTrace();
                } catch (InvocationTargetException e1) {
                    e1.printStackTrace();
                } catch (NoSuchMethodException e1) {
                    e1.printStackTrace();
                } catch (IOException e1) {
                    Log.d(DEBUG_TAG, "fallback failed");
                }
            }

            return true;
        }

    }


}
