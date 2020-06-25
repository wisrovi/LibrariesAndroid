package com.example.arduino_usb_otg;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.util.Log;

import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;


/*
 * lIBRERIA CREADA POR: https://github.com/wisrovi, https://wisrovirodriguez.wixsite.com/wisrovi
 * BASADA DE: usbserial.jar y physicaloidlibrary.jar
 * */



/***
 *  AndroidManifest.xml
    <uses-feature android:name="android.hardware.usb.host"/>
 *
 *
 *
    OTG_USB usb_otg;  //Variable para control automatico y manual del OTG
 *          .
 *          .
 *          .
 *
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
                .
                .
                .
        usb_otg = new OTG_USB(this);
     }
 *
 *
 *          .
 *          .
 *          .
     private class OTG_USB extends OTG_LIBRARY {
         public OTG_USB(Context context) {
            super(context);
         }

         @Override
         public void NuevoDispositivoConectado() {
             super.NuevoDispositivoConectado();
             //Proceso ejecutar cuando se conecte el OTG
         }

         @Override
         public void DispositivoDesconectado() {
             super.DispositivoDesconectado();
             //Proceso ejecutar cuando se desconecte el OTG
         }

         @Override
         public void DatosRecibidos(String data) {
             super.DatosRecibidos(data);
             //Cuando se reciben datos por el OTG, estos se entregan en la variable: data
             //por ejemplo se puede usar un switch para procesar el 'data'
         }

         @Override
         public void BotonStart() {
             super.BotonStart();
             //Proceso cuando el dispositivo esta conectado, y tiene algúna desconexión leve y se vuelve a conectar
             //por ejemplo: un cable fallando que desconecta y conecta el OTG
         }
     }
 *
 *
 *
      // Tambien se tienen algunas funciones que se pueden usar cuando se requiera:
         usb_otg.Iniciar_OTG_manualmente(); //El puerto se inicia automaticamente cuando se conecta el OTG,pero también se puede iniciar de forma manual
         usb_otg.EnviarPorSerial(string + "\n"); //Para enviar datos, estos se compilan en una variable 'String' y debe terminar con el caracter '\n', para que sea recibido corerctamente y procesado por el dispositivo
         usb_otg.CerrarPuerto(); //El puerto se cierra automaticamente cuando se desconecta el OTG, pero si se desea, este se puede cerrar manualmente
 *
 *
 * ***/


public class OTG_LIBRARY {
    private Context context;
    private final String ACTION_USB_PERMISSION = "com.hariharan.arduinousb.USB_PERMISSION";
    private final int ARDUINO_UNO = 0x2341;
    private final int ESP32 = 4292;
    private final int BAUDIOS = 115200;
    private UsbDeviceConnection connection;
    private UsbManager usbManager;
    private UsbSerialDevice serialPort;
    private UsbDevice device;

    public OTG_LIBRARY(Context context) {
        this.context = context;
        IniciarOTG(context);
    }

    private void IniciarOTG(Context context) {
        usbManager = (UsbManager) context.getSystemService(context.USB_SERVICE);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        context.registerReceiver(broadcastReceiver, filter);
    }

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() { //Broadcast Receiver to automatically start and stop the Serial connection.
        @Override
        public void onReceive(Context context, Intent intent) {
            String ACTION_USB_PERMISSION = "com.hariharan.arduinousb.USB_PERMISSION";
            if (intent.getAction().equals(ACTION_USB_PERMISSION)) {
                boolean granted = intent.getExtras().getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED);
                if (granted) {
                    connection = usbManager.openDevice(device);
                    serialPort = UsbSerialDevice.createUsbSerialDevice(device, connection);
                    if (serialPort != null) {
                        if (serialPort.open()) { //Set Serial Connection Parameters.
                            NuevoDispositivoConectado();
                            serialPort.setBaudRate(BAUDIOS);
                            serialPort.setDataBits(UsbSerialInterface.DATA_BITS_8);
                            serialPort.setStopBits(UsbSerialInterface.STOP_BITS_1);
                            serialPort.setParity(UsbSerialInterface.PARITY_NONE);
                            serialPort.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
                            serialPort.read(mCallback);
                        } else {
                            Log.d("SERIAL", "PORT NOT OPEN");
                        }
                    } else {
                        Log.d("SERIAL", "PORT IS NULL");
                    }
                } else {
                    Log.d("SERIAL", "PERM NOT GRANTED");
                }
            } else if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {
                BotonStart();

            } else if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
                DispositivoDesconectado();
            }
        }
    };

    UsbSerialInterface.UsbReadCallback mCallback = new UsbSerialInterface.UsbReadCallback() { //Defining a Callback which triggers whenever data is read.
        @Override
        public void onReceivedData(byte[] arg0) {
            String data = null;
            try {
                data = new String(arg0, "UTF-8");
                DatosRecibidos(data);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    };


    public void BotonStart() {
        /**
         *
         *  Proceso cuando el dispositivo esta conectado, y tiene algúna desconexión leve y se vuelve a conectar
         *  por ejemplo: un cable fallando que desconecta y conecta el OTG
         *
         * */
    }

    public void DatosRecibidos(String data) {
        /**
         *
         *   Cuando se reciben datos por el OTG, estos se entregan en la variable: data
         *   por ejemplo se puede usar un switch para procesar el 'data'
         *
         * */
    }

    public void NuevoDispositivoConectado() {
        /**
         *
         *   Proceso ejecutar cuando se conecte el OTG
         *
         * */
    }

    public void DispositivoDesconectado() {
        /**
         *
         *   Proceso ejecutar cuando se desconecte el OTG
         *
         * */
    }



    /**
     *
     *      Funciones disponibles para ejecutar de forma manual desde cualquier parte del código
     *
     * */
    public void CerrarPuerto() {
        /**
         *
         * El puerto se cierra automaticamente cuando se desconecta el OTG, pero si se desea, este se puede cerrar manualmente
         *
         * */
        serialPort.close();
    }

    public void EnviarPorSerial(String string) {
        /**
        * Para enviar datos, estos se compilan en una variable 'String' y debe terminar con el caracter '\n', para que sea recibido corerctamente y procesado por el dispositivo
        * */
        serialPort.write(string.getBytes());
    }

    public void Iniciar_OTG_manualmente() {
        /**
         *
         * El puerto se inicia automaticamente cuando se conecta el OTG,pero también se puede iniciar de forma manual
         *
         * */

        HashMap<String, UsbDevice> usbDevices = usbManager.getDeviceList();
        if (!usbDevices.isEmpty()) {
            boolean keep = true;
            for (Map.Entry<String, UsbDevice> entry : usbDevices.entrySet()) {
                device = entry.getValue();
                int deviceVID = device.getVendorId();
                if (deviceVID == ESP32) {
                    PendingIntent pi = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), 0);
                    usbManager.requestPermission(device, pi);
                    keep = false;
                } else {
                    connection = null;
                    device = null;
                }
                if (!keep)
                    break;
            }
        }
    }

}

