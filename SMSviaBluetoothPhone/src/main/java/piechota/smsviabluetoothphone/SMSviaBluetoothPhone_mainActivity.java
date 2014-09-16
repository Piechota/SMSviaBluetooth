package piechota.smsviabluetoothphone;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import org.w3c.dom.Text;

import piechota.bluetoothcontroller.BluetoothController;

public class SMSviaBluetoothPhone_mainActivity extends Activity {

    private ListView listDevaices;
    private Button buttonConnect;
    private Button buttonRead;
    private Button buttonWrite;
    private TextView textRead;
    private EditText textWrite;

    private BluetoothDevice selectedDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smsvia_bluetooth_phone_main);

        listDevaices = (ListView)findViewById(R.id.listDevice);
        buttonConnect = (Button)findViewById(R.id.buttonConnect);
        buttonRead = (Button)findViewById(R.id.buttonRead);
        buttonWrite = (Button)findViewById(R.id.buttonWrite);
        textRead = (TextView)findViewById(R.id.textRead);
        textWrite = (EditText)findViewById(R.id.textWrite);

        selectedDevice = null;

        BluetoothController.getInstance().setUUDI("878d94b0-3d23-11e4-916c-0800200c9a66");
        BluetoothController.getInstance().turnOnBluetooth(this, 2048);

        ArrayAdapter<BluetoothController.Device> devaicesAdapter =
                new ArrayAdapter<BluetoothController.Device>(this, R.layout.devaice_list, BluetoothController.getInstance().getPairedDevices());
        listDevaices.setAdapter(devaicesAdapter);


        listDevaices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                selectedDevice = ((BluetoothController.Device) listDevaices.getItemAtPosition(i)).getDevice();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.smsvia_bluetooth_phone_main, menu);
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
    }

    /*On Click Events*/
    public void buttonConnectEvent(View v){
        if(selectedDevice == null)
            return;
        BluetoothController.getInstance().tryConnectAsClient(selectedDevice);
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
}
