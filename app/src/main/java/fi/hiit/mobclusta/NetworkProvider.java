package fi.hiit.mobclusta;

import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;

public interface NetworkProvider {
    void setOwnerIntent(boolean intent);
    boolean wifiP2pEnabled();
    void discoverPeers(WifiP2pManager.ActionListener listener);
    void stopPeerDiscovery(WifiP2pManager.ActionListener listener);
    void connect(WifiP2pDevice device, WifiP2pManager.ActionListener listener);
    void addListener(NetworkListener listener);
    void removeListener(NetworkListener listener);
    void removeGroup(WifiP2pManager.ActionListener actionListener);
}
