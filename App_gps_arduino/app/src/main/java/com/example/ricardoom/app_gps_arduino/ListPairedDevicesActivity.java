package com.example.ricardoom.app_gps_arduino;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;

import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

public class ListPairedDevicesActivity extends ListActivity {
    ArrayAdapter<String> btArrayAdapter;
    ArrayList<BluetoothDevice> arrayListBluetoothDevices = null;
    BluetoothAdapter bluetoothAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        arrayListBluetoothDevices = new ArrayList<BluetoothDevice>();
        btArrayAdapter
                = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1);

        bluetoothAdapter
                = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices
                = bluetoothAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                String deviceBTName = device.getName();
                String deviceBTMajorClass
                        = getBTMajorDeviceClass(device
                        .getBluetoothClass()
                        .getMajorDeviceClass());
                btArrayAdapter.add(deviceBTName + "\n"
                        + deviceBTMajorClass);
                arrayListBluetoothDevices.add(device);
            }
        }
        setListAdapter(btArrayAdapter);
        IntentFilter filter = new IntentFilter();

        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        registerReceiver(mReceiver, filter);
        bluetoothAdapter.startDiscovery();
    }
    private void getPairedDevices() {
        btArrayAdapter= new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1);
        arrayListBluetoothDevices = new ArrayList<BluetoothDevice>();
        Set<BluetoothDevice> pairedDevices =bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                String deviceBTName = device.getName();
                String deviceBTMajorClass
                        = getBTMajorDeviceClass(device
                        .getBluetoothClass()
                        .getMajorDeviceClass());
                btArrayAdapter.add(deviceBTName + "\n"
                        + deviceBTMajorClass);
                arrayListBluetoothDevices.add(device);
            }
        }
        btArrayAdapter.notifyDataSetChanged();
    }

    private String getBTMajorDeviceClass(int major){
        switch(major){
            case BluetoothClass.Device.Major.AUDIO_VIDEO:
                return "AUDIO_VIDEO";
            case BluetoothClass.Device.Major.COMPUTER:
                return "COMPUTER";
            case BluetoothClass.Device.Major.HEALTH:
                return "HEALTH";
            case BluetoothClass.Device.Major.IMAGING:
                return "IMAGING";
            case BluetoothClass.Device.Major.MISC:
                return "MISC";
            case BluetoothClass.Device.Major.NETWORKING:
                return "NETWORKING";
            case BluetoothClass.Device.Major.PERIPHERAL:
                return "PERIPHERAL";
            case BluetoothClass.Device.Major.PHONE:
                return "PHONE";
            case BluetoothClass.Device.Major.TOY:
                return "TOY";
            case BluetoothClass.Device.Major.UNCATEGORIZED:
                return "UNCATEGORIZED-ARDUINO";
            case BluetoothClass.Device.Major.WEARABLE:
                return "AUDIO_VIDEO";

            default: return "DESCONOCIDO";
        }
    }

    BluetoothDevice bdDevice;

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        // TODO Auto-generated method stub
        super.onListItemClick(l, v, position, id);

        //Intent intent = new Intent();
        //setResult(RESULT_OK, intent);

        bdDevice = arrayListBluetoothDevices.get(position);
        getPairedDevices();

        Log.i("Log", "The dvice : "+bdDevice.toString());
        Log.e("Log", "The dvice : "+bdDevice.getName());
        //try {

        //    mHilo.openBT();
        //} catch (IOException e) {
        //    e.printStackTrace();
        // }
        if (bdDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
            unpairDevice(bdDevice);
        } else {
            showToast("Pairing...");
            //mHilo.beginListenForData();
            try {
                openBT();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //registerReceiver(mPairReceiver, new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED));

        //finish();
    }

    private void pairDevice(BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("createBond", (Class[]) null);
            method.invoke(device, (Object[]) null);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void openBT() throws IOException {
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); //Standard //SerialPortService ID
        //mmSocket = bdDevice.createRfcommSocketToServiceRecord(uuid);
        try {
            mmSocket = (BluetoothSocket) bdDevice.getClass().getMethod("createRfcommSocket", new Class[]{int.class}).invoke(bdDevice, 1);

            mmSocket.connect();
            Log.i("Log", "se conecto al socket ");
        }catch(Exception e2){}
        mmInputStream = mmSocket.getInputStream();
        mmOutputStream = mmSocket.getOutputStream();
        mmOutputStream.write("h".getBytes());
        Log.i("Log", "se conecto al socket "+mmInputStream.toString());
        //mmSocket.close();
        beginListenForData();
        //finish();
    }

    String session_id;

    void beginListenForData() {
        final Handler handler = new Handler();
        final byte delimiter = 10; //This is the ASCII code for a newline character

        stopWorker = false;
        readBufferPosition = 0;
        readBuffer = new byte[1024];
        workerThread = new Thread(new Runnable() {
            public void run() {
                while(!Thread.currentThread().isInterrupted() && !stopWorker) {


                    try {
                        int bytesAvailable = mmInputStream.available();
                        if(bytesAvailable > 0) {
                            byte[] packetBytes = new byte[bytesAvailable];
                            mmInputStream.read(packetBytes);
                            for(int i=0;i<bytesAvailable;i++) {
                                byte b = packetBytes[i];
                                if(b == delimiter) {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    final String data = new String(encodedBytes, "US-ASCII");
                                    readBufferPosition = 0;

                                    handler.post(new Runnable() {
                                        public void run() {

                                            Log.e("Log", "la data es //////////////////////////: "+data);

                                            Intent intent = new Intent();
                                            intent.putExtra("posicion0",data);
                                            setResult(RESULT_OK, intent);
                                            finish();
                                            //SharedPreferences myprefs= getSharedPreferences("user", MODE_WORLD_READABLE);
                                            //myprefs.edit().putString("session_id", data).commit();



                                        }
                                    });
                                }
                                else {
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                        }
                        //else{Log.i("Log", "no hay data q recibir");}
                    }
                    catch (IOException ex) {
                        Log.i("Log", "el socket dejo de funcionar");
                        stopWorker = true;
                    }
                    mandarDatos();
                }
            }
        });

        workerThread.start();
    }



    public void mandarDatos()
    {
        SharedPreferences myprefs= getSharedPreferences("user", MODE_WORLD_READABLE);
        session_id= myprefs.getString("session_id", null);

        if(!session_id.equals("espera")){
            Log.i("Log", "mi info a manda res "+ session_id);
            try{


                byte[] buffer1 = (session_id+"d").getBytes();
                mmOutputStream.write(buffer1);
                finish();
            }
            catch (IOException eee)
            {Log.i("Log", "no manda datos al arduino");}
        }

    }



    private void unpairDevice(BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("removeBond", (Class[]) null);
            method.invoke(device, (Object[]) null);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private final BroadcastReceiver mPairReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                final int state        = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                final int prevState    = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);

                if (state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING) {
                    showToast("Paired");
                    //Log.e("Log", "paired");
                } else if (state == BluetoothDevice.BOND_NONE && prevState == BluetoothDevice.BOND_BONDED){
                    showToast("Unpaired");
                    //Log.e("Log", "Unpaired");
                }

            }
        }
    };

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                //discovery starts, we can show progress dialog or perform other tasks
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                //discovery finishes, dismis progress dialog
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                //bluetooth device found
                BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                showToast("Found device " + device.getName());
            }
        }
    };

    private OutputStream outStream = null;
    private void sendData(String message) {
        byte[] msgBuffer = message.getBytes();

        Log.d("Log", "...Send data: " + message + "...");

        try {
            outStream.write(msgBuffer);
        } catch (IOException e) {
            String msg = "In onResume() and an exception occurred during write: " + e.getMessage();
            if (bdDevice.getAddress().equals("00:00:00:00:00:00"))
                msg = msg + ".\n\nUpdate your server address from 00:00:00:00:00:00 to the correct address on line 35 in the java code";
            //msg = msg +  ".\n\nCheck that the SPP UUID: " + MY_UUID.toString() + " exists on server.\n\n";

            //errorExit("Fatal Error", msg);
        }
    }


    @Override
    public void onDestroy() {
        unregisterReceiver(mReceiver);

        super.onDestroy();
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }


    BluetoothSocket mmSocket;
    OutputStream mmOutputStream;
    InputStream mmInputStream;
    Thread workerThread;
    byte[] readBuffer;
    int readBufferPosition;
    int counter;
    volatile boolean stopWorker;






}

