package piechota.bluetoothcontroller;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.UUID;

/**
 * Created by Konrad on 2014-09-06.
 */
public class BluetoothController {
    //singleton
    private static BluetoothController ourInstance = new BluetoothController();
    public static BluetoothController getInstance() {return ourInstance; }

    private final BluetoothAdapter _bluetoothAdapter;     //bluetooth adapter of the device
    private BluetoothSocket _socket;

    private InputStream _inputStream;       //stream form data will be reading
    private OutputStream _outputStream;     //stream to which data will be send

    private HashSet<Device> _testDevice;        //HashSet for test that we add device to the _foudDevices
    private ArrayList<Device> _pairedDevices;   //list of paired devices
    private ArrayList<Device> _foundDevices;    //list of founded devices

    private UUID _uuid;     //uuid for program
    private Context _context; //activity context

    private byte[] buffer;

    /*SETTERS*/
    public void setUUDI(String uuid){_uuid = UUID.fromString(uuid);} //settign uuid for program
    /*SETTERS*/

    /* GETTERS */
    public BluetoothAdapter getBluetoothAdapter() {return  _bluetoothAdapter;}
    public ArrayList<Device> getPairedDevices(){return  _pairedDevices;}
    public ArrayList<Device> getFoundDevices(){return  _foundDevices;}
    /* GETTERS */

    /*METHODS*/
    public void startDiscoveryDevices(){_bluetoothAdapter.startDiscovery();}
    public void endDiscoveryDevices(){_bluetoothAdapter.cancelDiscovery();}
    public void setBroadcastReceiver(){      //setting broadcast receiver (need context)
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND); //this message is send when we find device
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED); //this message is send when bluetooth change state (e.g. turn on)
        _context.registerReceiver(_receiver, filter);
    }
    public void cleanUp(){  //clean up method
        _context.unregisterReceiver(_receiver);
        if(_socket != null) {
            try{_socket.close();}catch (IOException e){/*some magic*/}
        }
    }
    public void turnOnBluetooth(Context context, int bufferSize){  //guess what
        if(_bluetoothAdapter != null){  //work only if we already not have adapter
            if(!_bluetoothAdapter.isEnabled())  //if adapter is turned off turn it on
                _bluetoothAdapter.enable();     //(in really ugly way)
            else {  //if adapater is already on get paired devices
                for(BluetoothDevice dev : _bluetoothAdapter.getBondedDevices())
                    _pairedDevices.add(new Device(dev));
            }

            setBroadcastReceiver();
            buffer = new byte[bufferSize];
        }
    }
    public void turnOffBluetooth(){ //it's simple
        if(_bluetoothAdapter != null){
            cleanUp();
            if(_bluetoothAdapter.isEnabled())
                _bluetoothAdapter.disable();
        }
    }
    public void tryConnectAsClient(BluetoothDevice device){ //method that try connect as a client with given device
       new ConnectAsClient(device); //create object of class ConnectAsClient
    }
    public void tryConnectAsServer(int timeForTry){ //method that try connect as a server
       new ConnectAsServer(timeForTry); //create object of class ConnectAsServer
    }
    public byte[] readBuffor() {
        byte[] returned = new byte[buffer.length];
        for(int i = 0; i < buffer.length; i++)
            returned[i] = buffer[i];

        return  returned;
    }
    public void writeBuffer(final byte[] bytes){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    _outputStream.write(bytes);
                } catch (IOException e) {/*next magic*/}
            }
        });
    }
    //private
    private void getStreams(){
        if(_socket == null)
            return;

        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        try{
            tmpIn = _socket.getInputStream();
            tmpOut = _socket.getOutputStream();
        } catch (IOException e){/*some magic here*/}

        _inputStream = tmpIn;
        _outputStream = tmpOut;

        if(_inputStream != null){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while(true){
                        try{
                            _inputStream.read(buffer);
                        } catch (IOException e) {break;}
                    }
                }
            });
        }
    }
    //constructors
    private BluetoothController() {  //constructor
        _bluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); //get adapter
        _testDevice = new HashSet<Device>();    //and make some lists
        _pairedDevices = new ArrayList<Device>();
        _foundDevices = new ArrayList<Device>();
    }
    /*METHODS*/

    final BroadcastReceiver _receiver = new BroadcastReceiver() {   //broadcast receiver
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)){    //if device is found
                Device tmp = new Device((BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE));
                if(_testDevice.add(tmp))
                    _foundDevices.add(tmp);
            }
            if(BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)&&   //if adapter state is changed and
               intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) == BluetoothAdapter.STATE_ON){  //is equal to STATE_ON
                for(BluetoothDevice dev : _bluetoothAdapter.getBondedDevices())
                    _pairedDevices.add(new Device(dev));
            }
        }
    };




    /*INNER CLASS*/
    public class Device{ //class that represent device
        BluetoothDevice _device;    //device for this object

        public  Device(BluetoothDevice device){ _device = device;} //constructor

        @Override
        public String toString(){
            return _device.getName();
        }
        ///method for HashSet
        @Override
        public boolean equals(Object other){
            if(other == null) return false;

            Device tmp = (Device)other;
            return tmp._device.equals(_device);
        }
        @Override
        public int hashCode() {return _device.hashCode();}
    }

    private final class ConnectAsClient extends Thread{ //class for connecting as client
        private ConnectAsClient(BluetoothDevice device){ //constructor that try to get socket
            BluetoothSocket tmp = null;
            try{tmp = device.createRfcommSocketToServiceRecord(_uuid);}catch (IOException e){/*we need some magic here*/}
            _socket = tmp;

            this.start();
        }
        public void run(){  //method that try connect with socket
            try{
                _socket.connect();
            } catch (IOException connectException){
                try{
                    _socket.close();    //if fail close socket
                } catch (IOException e){/*we need some magic here*/}
                return;
            }

            getStreams();
        }
    }

    private final class ConnectAsServer extends Thread{
        private  final BluetoothServerSocket _serverSocket;
        private int _timeForTry;

        private ConnectAsServer(int timeForTry){
            BluetoothServerSocket tmp = null;
            try{tmp = _bluetoothAdapter.listenUsingRfcommWithServiceRecord("SMS via bluetooth", _uuid);}
            catch (IOException e){/*let's make some magic*/}
            _serverSocket = tmp;
            _timeForTry = timeForTry + Calendar.getInstance().get(Calendar.SECOND);

            this.start();
        }
        public void run(){
            while(true){
                try{_socket = _serverSocket.accept(); }
                catch (IOException e){break;}

                if(_socket != null){
                    getStreams();
                    try{_serverSocket.close();}
                    catch (IOException e){/*magic again*/}
                    break;
                }

                if(Calendar.getInstance().get(Calendar.SECOND) >= _timeForTry){
                    try{_serverSocket.close();}
                    catch (IOException e){/*magic again*/}
                    break;
                }
            }
        }
    }
}
