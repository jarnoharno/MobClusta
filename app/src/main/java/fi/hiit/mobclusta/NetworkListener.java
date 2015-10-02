package fi.hiit.mobclusta;

import android.net.wifi.p2p.WifiP2pDeviceList;

public interface NetworkListener {
    void wifiP2pEnabled(boolean enabled);
    void setPeerList(WifiP2pDeviceList peers);
    void discoveryStopped();
    void enableDiscovery(boolean enable);
    boolean isMasterSlave();
}
