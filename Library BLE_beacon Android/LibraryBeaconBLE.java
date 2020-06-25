package com.example.ble_beacon.BeaconBLE;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.BeaconTransmitter;
import org.altbeacon.beacon.powersave.BackgroundPowerSaver;

import java.util.Arrays;

public class LibraryBeaconBLE {

    /*
     * gradle:
     * implementation 'org.altbeacon:android-beacon-library:2.9.2'
     *
     * Manifiest:
     * <uses-permission android:name="android.permission.BLUETOOTH" />
     * <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
     *
     * BIBLIOGRAFIA:
     * https://github.com/jaisonfdo/BeaconTransmitter/blob/master/app/src/main/java/droidmentor/beacontransmitter/BeaconTransmitterActivity.java
     * https://altbeacon.github.io/android-beacon-library/samples.html
     * https://danielggarcia.wordpress.com/2013/10/19/bluetooth-i-activando-y-desactivando-el-bluetooth-en-android/
     */


    /*
     *      EXAMPLE:
     *
     *      LibraryBeaconBLE beacon;*
     *      .
     *      .
     *      .
     *     @Override
     *     protected void onCreate(Bundle savedInstanceState) {
     *         super.onCreate(savedInstanceState);
     *         setContentView(R.layout.activity_main);
     *         .
     *         .
     *         .
     *         String id = "D2717982125";
     *         beacon = new LibraryBeaconBLE(MainActivity.this);
     *         beacon.setId(id);
     *
     *         if(beacon.ValidarCompatibilidadBeacon()){
     *             beacon.IniciarBeacon();
     *         }
     *         .
     *         .
     *         .
     *    }
     *    .
     *    .
     *    .
     *    @Override
     *     protected void onDestroy() {
     *         super.onDestroy();
     *         beacon.finish();
     *     }
     * */


    private static int maxIdentifier = 100;
    private BeaconTransmitter beaconTransmitter;
    private Context context;
    private String id;

    public LibraryBeaconBLE(Context context) {
        IntentFilter filtro = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        context.registerReceiver(bReceiver, filtro);
        this.context = context;
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        IntentFilter filtroBLE = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        context.registerReceiver(bReceiver, filtroBLE);
    }

    public void setId(String id) {
        this.id = id;
    }

    public void IniciarBeacon() {
        if (id != null) {
            int minor = createMinor();
            Beacon beacon = new Beacon.Builder()
                    .setId1("2f234454-cf6d-4a0f-adf2-0" + id)
                    .setId2(Integer.toString(minor))
                    .setId3(createMajor(minor))
                    .setManufacturer(0x0118)
                    .setTxPower(-59)
                    .setDataFields(Arrays.asList(new Long[]{0l}))
                    .build();
            BeaconParser beaconParser = new BeaconParser()
                    .setBeaconLayout("m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25");
            beaconTransmitter = new BeaconTransmitter(context, beaconParser);
            beaconTransmitter.startAdvertising(beacon);


            BackgroundPowerSaver backgroundPowerSaver = new BackgroundPowerSaver(context);

            Toast.makeText(context, "Activando Beacon", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Not defined 'id'", Toast.LENGTH_SHORT).show();
        }
    }


    private int createMinor() {
        int minimo = 1;
        int menor = (int) Math.floor(Math.random() * (maxIdentifier - minimo + 1) + minimo);

        return menor;
    }

    private String createMajor(int minor) {
        int minimo = 1;
        int mayor = (int) Math.floor(Math.random() * (maxIdentifier - minimo + 1) + minimo);

        if (minor > mayor) {
            mayor = minor + 1;
        }
        return Integer.toString(mayor);
    }

    // Instanciamos un BroadcastReceiver que se encargara de detectar si el estado
    // del Bluetooth del dispositivo ha cambiado mediante su handler onReceive
    private final BroadcastReceiver bReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            // Filtramos por la accion. Nos interesa detectar BluetoothAdapter.ACTION_STATE_CHANGED
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                final int estado = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
                if (estado == BluetoothAdapter.STATE_OFF) {
                    if (beaconTransmitter != null) {
                        beaconTransmitter.stopAdvertising();
                    }
                }
                if (estado == BluetoothAdapter.STATE_ON) {
                    if (ValidarCompatibilidadBeacon()) {
                        IniciarBeacon();
                    }
                }
            }
        }
    };

    public boolean ValidarCompatibilidadBeacon() {
        final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter != null) {
            if (adapter.isEnabled()) {
                /**
                 * Se confirma que el Bluetooth este activado
                 * */
                if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
                    /**
                     * Se valida que el movil tenga BLE
                     * */
                    if (adapter.isMultipleAdvertisementSupported()) {
                        /**
                         * Se valida que el BLE pueda hacer publicidad (indispensable para el modo Beacon)
                         * */
                        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            /**
                             * Se valida que la versión del android sea superior a 6.0
                             * */
                            /**
                             * Build.VERSION_CODES.M = API 23 = Android 6.0
                             *
                             * Otra forma es: int versionSDK = android.os.Build.VERSION.SDK_INT;
                             * también: String version = Build.VERSION.RELEASE;
                             * */
                            return true;
                        } else {
                            Toast.makeText(context, "Unsupported android version", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(context, "Beacon Not Supported", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, "BLE Not Supported", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(context, "Bluetooth disable", Toast.LENGTH_SHORT).show();
                // Lanzamos el Intent que mostrara la interfaz de activacion del
                // Bluetooth. La respuesta de este Intent se manejara en el metodo
                // onActivityResult
                AlertDialog.Builder alert = new AlertDialog.Builder(context);
                alert.setCancelable(false);
                alert.setTitle("Es necesario activar el Bluetooth para el modo Beacon!");
                alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        adapter.enable();
                    }
                });
                alert.create().show();
            }
        } else {
            Toast.makeText(context, "Not Have Bluetooth", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    public void finish() {
        context.unregisterReceiver(bReceiver);
    }
}
