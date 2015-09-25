package fi.hiit.mobclusta;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import fi.hiit.mobclusta.common.view.SlidingTabLayout;

public class MainActivity extends AppCompatActivity implements NetworkProvider {

    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private BroadcastReceiver mReceiver;
    private IntentFilter mIntentFilter;
    private boolean mWifiP2pEnabled = false;
    private boolean mOwnerIntent = false;
    private TextView indicatorText;
    private View indicatorLight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        indicatorText = (TextView) findViewById(R.id.indicator_text);
        indicatorLight = findViewById(R.id.indicator_light);

        if (savedInstanceState == null) {
            Log.d("savedinstance IS NULL");
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            SlidingTabsBasicFragment fragment = new SlidingTabsBasicFragment();
            transaction.replace(R.id.sample_content_fragment, fragment);
            transaction.commit();
        }

        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), new WifiP2pManager.ChannelListener() {
            @Override
            public void onChannelDisconnected() {
                Log.d("Channel disconnected");
            }
        });
        mReceiver = new WiFiDirectBroadcastReceiver(mManager, mChannel, this);
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
    }

    /* register the broadcast receiver with the intent values to be matched */
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver, mIntentFilter);
    }
    /* unregister the broadcast receiver */
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    public void alert(String title, String msg) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(msg)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // continue with delete
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private Set<NetworkListener> networkListeners = new HashSet<>();

    public void wifiEnabled(boolean enabled) {
        if (enabled) {
            indicatorText.setText("Disconnected");
            indicatorLight.setBackground(getResources().getDrawable(R.drawable.circle));
        } else {
            indicatorText.setText("Unavailable");
            indicatorLight.setBackground(getResources().getDrawable(R.drawable.circle));
        }
        mWifiP2pEnabled = enabled;
        Iterator<NetworkListener> iterator = networkListeners.iterator();
        while (iterator.hasNext()) {
            NetworkListener listener = iterator.next();
            if (listener == null) {
                iterator.remove();
            } else {
                listener.wifiP2pEnabled(enabled);
            }
        }
    }

    @Override
    public void setOwnerIntent(boolean intent) {
        mOwnerIntent = intent;
    }

    @Override
    public boolean wifiP2pEnabled() {
        return mWifiP2pEnabled;
    }
    @Override
    public void addListener(NetworkListener listener) {
        networkListeners.add(listener);
    }

    @Override
    public void removeListener(NetworkListener listener) {
        networkListeners.remove(listener);
    }

    @Override
    public void removeGroup(final WifiP2pManager.ActionListener actionListener) {
        mManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                actionListener.onSuccess();
            }

            @Override
            public void onFailure(int reason) {
                actionListener.onFailure(reason);
            }
        });
    }

    @Override
    public void discoverPeers(final WifiP2pManager.ActionListener listener) {
        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                listener.onSuccess();
            }

            @Override
            public void onFailure(int reason) {
                listener.onFailure(reason);
            }
        });
    }

    @Override
    public void stopPeerDiscovery(final WifiP2pManager.ActionListener listener) {
        mManager.stopPeerDiscovery(mChannel, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                listener.onSuccess();
            }

            @Override
            public void onFailure(int reason) {
                listener.onFailure(reason);
            }
        });
    }

    @Override
    public void connect(WifiP2pDevice device, final WifiP2pManager.ActionListener listener) {
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        config.groupOwnerIntent = mOwnerIntent ? 15 : 0;
        mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                listener.onSuccess();
            }

            @Override
            public void onFailure(int reason) {
                listener.onFailure(reason);
            }
        });
    }

    public void setPeers(WifiP2pDeviceList peers) {
        Log.d("peers=%s", peers);
        Iterator<NetworkListener> iterator = networkListeners.iterator();
        while (iterator.hasNext()) {
            NetworkListener listener = iterator.next();
            if (listener == null) {
                iterator.remove();
            } else {
                listener.setPeerList(peers);
            }
        }
    }

    public void connectionChanged(WifiP2pInfo p2pInfo, NetworkInfo networkInfo) {
        if (p2pInfo.groupFormed) {
            if (p2pInfo.isGroupOwner) {
                indicatorText.setText("Owner");
                indicatorLight.setBackground(getResources().getDrawable(R.drawable.circle_owner));
            } else {
                indicatorText.setText("Client");
                indicatorLight.setBackground(getResources().getDrawable(R.drawable.circle_client));
            }
        } else {
            indicatorText.setText("Disconnected");
            indicatorLight.setBackground(getResources().getDrawable(R.drawable.circle));
        }
        Iterator<NetworkListener> iterator = networkListeners.iterator();
        while (iterator.hasNext()) {
            NetworkListener listener = iterator.next();
            if (listener == null) {
                iterator.remove();
            } else {
                listener.discoveryStopped();
                listener.wifiP2pEnabled(!p2pInfo.groupFormed);
            }
        }
    }
}
