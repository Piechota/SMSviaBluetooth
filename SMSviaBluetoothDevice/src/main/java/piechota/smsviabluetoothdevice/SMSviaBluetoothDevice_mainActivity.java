package piechota.smsviabluetoothdevice;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import piechota.bluetoothcontroller.BluetoothController;

public class SMSviaBluetoothDevice_mainActivity extends Activity {

    private Button buttonConnect;
    private Button buttonRead;
    private Button buttonWrite;
    private TextView textConnected;
    private TextView textRead;
    private EditText textWrite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smsvia_bluetooth_device_main);

        buttonConnect = (Button)findViewById(R.id.buttonConnect);
        buttonRead = (Button)findViewById(R.id.buttonRead);
        buttonWrite = (Button)findViewById(R.id.buttonWrite);
        textConnected = (TextView)findViewById(R.id.textConnected);
        textRead = (TextView)findViewById(R.id.textRead);
        textWrite = (EditText)findViewById(R.id.textWrite);

        BluetoothController.getInstance().setUUDI("878d94b0-3d23-11e4-916c-0800200c9a66");
        BluetoothController.getInstance().turnOnBluetooth(this, 3);

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothController.CONNECTED);
        registerReceiver(_receiver, filter);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.smsvia_bluetooth_device_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BluetoothController.getInstance().cleanUp();
        unregisterReceiver(_receiver);
    }

    /*On Click Events*/
    public void buttonConnectEvent(View v){
        BluetoothController.getInstance().tryConnectAsServer(60);
    }
    public void buttonReadEvent(View v){
        textRead.setText(new String(BluetoothController.getInstance().readBuffer()));
    }
    public void buttonWriteEvent(View v){
        BluetoothController.getInstance().writeBuffer(textWrite.getText().toString().getBytes());
    }
    public void textClearEvent(View v){
        ((TextView)v).setText("");
    }
    /*On Click Events*/

    BroadcastReceiver _receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothController.CONNECTED.equals(action)){
                textConnected.setText("Connected!");
            }
        }
    };
}
