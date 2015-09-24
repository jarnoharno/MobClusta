package fi.hiit.mobclusta;

import android.net.wifi.p2p.WifiP2pManager;

public interface NetworkProvider {
    boolean wifiP2pEnabled();
    void discoverPeers(WifiP2pManager.ActionListener listener);
    void stopPeerDiscovery(WifiP2pManager.ActionListener listener);
    void addListener(NetworkListener listener);
    void removeListener(NetworkListener listener);
}
