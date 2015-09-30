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
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import fi.hiit.mobclusta.common.view.LogInterface;

public class MainActivity extends AppCompatActivity implements NetworkProvider {

    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private BroadcastReceiver mReceiver;
    private IntentFilter mIntentFilter;
    private boolean mWifiP2pEnabled = false;
    private boolean mOwnerIntent = false;
    private TextView indicatorText;
    private View indicatorLight;
    private ExecutorService pool;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pool = Executors.newCachedThreadPool();
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

    @Override
    protected void onDestroy() {
        pool.shutdown();
        super.onDestroy();
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
        int connectedTotal = 0;
        for (WifiP2pDevice device : peers.getDeviceList()) {
            if (device.status == WifiP2pDevice.CONNECTED) {
                ++connectedTotal;
            }
        }
        mConnected = connectedTotal;
        if (state == State.Owner) {
            setOwnerIndicator();
        }
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

    public static int PORT = 8888;

    // client

    private Socket clientSocket;
    private InetAddress groupOwnerAddress;
    private Future<?> clientFuture;

    public void startClient() {
        clientSocket = new Socket();
        try {
            clientSocket.bind(null);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        clientFuture = pool.submit(clientLoop);
    }

    private final Runnable clientLoop = new Runnable() {

        @Override
        public void run() {
            try {
                clientSocket.connect(new InetSocketAddress(groupOwnerAddress, PORT));
                InputStream in = clientSocket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in, Charset.defaultCharset()));
                Log.d("socket mConnected");
                try {
                    for (;;) {
                        String line = reader.readLine();
                        Log.d(line);
                    }
                } catch (IOException e) {
                    Log.d("socket closing");
                    return;
                }
            } catch (IOException e) {
                e.printStackTrace();
                return;
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    void stopClient() {
        try {
            clientSocket.close();
            try {
                clientFuture.get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        clientFuture = null;
        clientSocket = null;
        Log.d("socket closed");
    }

    // server

    private ServerSocket serverSocket;
    private Future<?> serverFuture;

    public void startServer() {
        try {
            serverSocket = new ServerSocket(PORT);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        serverFuture = pool.submit(serverLoop);
    }

    // only synchronized access allowed
    private Set<Socket> sockets = new HashSet<>();

    public synchronized void addSocket(Socket client) {
        sockets.add(client);
    }

    public synchronized void closeAllSockets() {
        for (Socket socket : sockets) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        sockets.clear();
    }

    private void log(String format, Object... args) {
        Log.d(format, args);
        if (logInterface == null) return;
        logInterface.d(format, args);
    }

    private final Runnable serverLoop = new Runnable() {
        @Override
        public void run() {
            log("server started");
            try {
                for (;;) {
                    Socket client = serverSocket.accept();
                    Log.d("server socket connected");
                    log("client connected");
                    addSocket(client);
                }
            } catch (IOException e) {
                Log.d("server stopping");
            }
        }
    };

    public void stopServer() {
        try {
            serverSocket.close();
            try {
                serverFuture.get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        closeAllSockets();
        Log.d("server stopped");
    }

    private LogInterface logInterface = null;

    public void setLog(LogInterface log) {
        Log.d("log interface set");
        this.logInterface = log;
        this.logInterface.d("Initialized");
    }

    public enum State {
        Owner,
        Client,
        Disconnected
    }

    private State state = State.Disconnected;

    private int mConnected = 0;

    private void setOwnerIndicator() {
        state = State.Owner;
        indicatorText.setText("Owner (" + mConnected + ")");
        indicatorLight.setBackground(getResources().getDrawable(R.drawable.circle_owner));
    }

    public void connectionChanged(WifiP2pInfo p2pInfo, NetworkInfo networkInfo) {
        if (p2pInfo.groupFormed) {
            if (p2pInfo.isGroupOwner) {
                if (state == State.Disconnected) {
                    startServer();
                }
                setOwnerIndicator();
            } else {
                if (state == State.Disconnected) {
                    startClient();
                }
                state = State.Client;
                indicatorText.setText("Client");
                indicatorLight.setBackground(getResources().getDrawable(R.drawable.circle_client));
                groupOwnerAddress = p2pInfo.groupOwnerAddress;
            }
        } else {
            if (state == State.Owner) {
                stopServer();
            } else if (state == State.Client) {
                stopClient();
            }
            state = State.Disconnected;
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
