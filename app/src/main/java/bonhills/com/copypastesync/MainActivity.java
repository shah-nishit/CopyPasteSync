package bonhills.com.copypastesync;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import java.io.*;
import java.net.*;

public class MainActivity extends ActionBarActivity {


    private ClipboardManager clipboardManager;
    private String textToPaste;
    Switch toggleSwitch;
    private String ServerIPAddress;
    private final int ServerPort = 6340;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            toggleSwitch = (Switch) findViewById(R.id.copyPasteSyncSwitch);

            WifiManager wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
            if (!wifiManager.isWifiEnabled()) {
                wifiManager.setWifiEnabled(true);
                toggleSwitch.setChecked(true);
                if (toggleSwitch.isChecked()) {
                    Toast.makeText(getApplicationContext(), "Copy Paste Sync is Running...!!!", Toast.LENGTH_SHORT).show();
                }

            }else
            {
                toggleSwitch.setChecked(true);
                Toast.makeText(getApplicationContext(), "Copy Paste Sync is Running...!!!", Toast.LENGTH_SHORT).show();
            }

            clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

            clipboardManager.addPrimaryClipChangedListener(new ClipboardManager.OnPrimaryClipChangedListener() {

                @Override
                public void onPrimaryClipChanged() {
                    try {
                        ClipData clipData = clipboardManager.getPrimaryClip();
                        ClipData.Item item = clipData.getItemAt(0);
                        textToPaste = item.getText().toString();
                        Toast.makeText(getApplicationContext(), textToPaste, Toast.LENGTH_SHORT).show();
                        SendMessage sendMessageTask = new SendMessage();
                        sendMessageTask.execute();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });

            toggleSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (!isChecked) {
                        Toast.makeText(getApplicationContext(), "Copy Paste Sync is Shutting Down...!!!", Toast.LENGTH_SHORT).show();
                        //finish();
                        //onDestroy();
                        //android.os.Process.killProcess(android.os.Process.myPid());
                    }
                }
            });
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class SendMessage extends AsyncTask<Void, Void, Void> {

        //@Override
        protected Void doInBackground(Void... params) {
            try {

                BufferedReader inFromUser = new BufferedReader(new InputStreamReader(
                        System.in));
                DatagramSocket datagramSocket = new DatagramSocket();
                InetAddress inetAddress = InetAddress.getLocalHost();
                // System.out.println(inetAddress); // name and IP address
                // System.out.println(inetAddress.getHostName()); // name
                // System.out.println(inetAddress.getHostAddress()); // IP address only
                String ipAddressSplit[] = inetAddress.getHostAddress().split("\\.");
                String ipAddress = ipAddressSplit[0] + "." + ipAddressSplit[1] + "."
                        + ipAddressSplit[2] + ".";

                for (int i = 1; i < 254; i++) {
                    try {
                        System.out.println(ipAddress + Integer.toString(i));
                        InetAddress IPAddress = InetAddress.getByName(ipAddress
                                + Integer.toString(i));

                        System.out.println("IP Address: " + IPAddress);
                        byte[] sendData = new byte[1024];
                        byte[] receiveData = new byte[1024];
                        String sentence = "Is this Server?";
                        sendData = sentence.getBytes();
                        DatagramPacket sendPacket = new DatagramPacket(sendData,
                                sendData.length, IPAddress, ServerPort);
                        datagramSocket.send(sendPacket);
                        DatagramPacket receivePacket = new DatagramPacket(receiveData,
                                receiveData.length);
                        datagramSocket.setSoTimeout(1000);
                        datagramSocket.receive(receivePacket);
                        if (datagramSocket.getReceiveBufferSize() > 0) {
                            ServerIPAddress = ipAddress + Integer.toString(i);
                            String modifiedSentence = new String(
                                    receivePacket.getData());
                            System.out.println("FROM SERVER:" + modifiedSentence);
                            break;
                        }

                    } catch (Exception e) {
                        System.out.println("Sorry, I'm not Server...!!!");
                        continue;
                    }
                }
                datagramSocket.close();
                //Socket clientSocket = new Socket(ServerIPAddress, ServerPort); // connect to the server
                Socket clientSocket = new Socket(ServerIPAddress, ServerPort); // connect to the server
                PrintWriter printWriter = new PrintWriter(clientSocket.getOutputStream(), true);
                printWriter.write(textToPaste); // write the message to output stream
                printWriter.flush();
                printWriter.close();
                clientSocket.close(); // closing the connection
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
