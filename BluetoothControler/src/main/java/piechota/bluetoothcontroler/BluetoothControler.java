package piechota.bluetoothcontroler;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Created by Konrad on 2014-09-06.
 */
public class BluetoothControler {
    private BluetoothAdapter _bluetoothAdapter;

    private InputStream _inputStream;
    private OutputStream _outputStream;

    private HashSet<Device> _testDevice;
    private ArrayList<Device> _pairedDevices;
    private ArrayList<Device> _foundDevices;

    private ConnectAsClient _connectAsClient;

    private UUID _uuid;

    public void setUUDI(String uuid){_uuid = UUID.fromString(uuid);}

    public void setBroadcastReceiver(Context context){
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        context.registerReceiver(_receiver, filter);
    }

    public void  cleanUp(Context context){
        context.unregisterReceiver(_receiver);

        if(_connectAsClient != null) _connectAsClient.cancel();
    }

    public BluetoothAdapter getBluetoothAdapter() {return  _bluetoothAdapter;}

    public ArrayList<Device> getPairedDevices(){return  _pairedDevices;}

    public  ArrayList<Device> getFoundDevices(){return  _foundDevices;}

    public void turnOnBluetooth(){
        if(_bluetoothAdapter != null){
            if(!_bluetoothAdapter.isEnabled())
                _bluetoothAdapter.enable();
            else {
                for(BluetoothDevice dev : _bluetoothAdapter.getBondedDevices())
                    _pairedDevices.add(new Device(dev));
            }
        }
    }

    public void turnOffBluetooth(){
        if(_bluetoothAdapter != null){
            if(_bluetoothAdapter.isEnabled())
                _bluetoothAdapter.disable();
        }
    }

    public void tryConnectAsClient(BluetoothDevice device){
        _connectAsClient = new ConnectAsClient(device, _uuid);
        _connectAsClient.start();
    }

    public void startDiscoveryDevices(){_bluetoothAdapter.startDiscovery();}
    public void endDiscoveryDevices(){_bluetoothAdapter.cancelDiscovery();}

    private static BluetoothControler ourInstance = new BluetoothControler();

    public static BluetoothControler getInstance() {
        return ourInstance;
    }

    private BluetoothControler() {
        _bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        _testDevice = new HashSet<Device>();
        _pairedDevices = new ArrayList<Device>();
        _foundDevices = new ArrayList<Device>();
    }

    final BroadcastReceiver _receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)){
                Device tmp = new Device((BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE));
                if(_testDevice.add(tmp))
                    _foundDevices.add(tmp);
            }
            if(BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)&&
               intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) == BluetoothAdapter.STATE_ON){
                for(BluetoothDevice dev : _bluetoothAdapter.getBondedDevices())
                    _pairedDevices.add(new Device(dev));
            }
        }
    };

    public class Device{
        BluetoothDevice _device;

        public  Device(BluetoothDevice device){ _device = device;}

        @Override
        public String toString(){
            return _device.getName();
        }
        @Override
        public boolean equals(Object other){
            if(other == null) return false;

            Device tmp = (Device)other;
            return tmp._device.equals(_device);
        }
        @Override
        public int hashCode() {return _device.hashCode();}
    }

    private final class ConnectAsClient extends Thread{
        private final BluetoothSocket _socket;

        private ConnectAsClient(BluetoothDevice device, UUID uuid){
            BluetoothSocket tmp = null;
            try{tmp = device.createRfcommSocketToServiceRecord(uuid);}catch (IOException e){}
            _socket = tmp;
        }
        public void run(){
            try{
                _socket.connect();
            } catch (IOException connectException){
                try{
                    _socket.close();
                } catch (IOException e){}
                return;
            }
        }

        private void cancel(){
            if(_socket == null) return;
            try{_socket.close();}catch (IOException e){}
        }
    }
}
