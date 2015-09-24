package fi.hiit.mobclusta;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toolbar;

public class NetworkFragment extends Fragment implements NetworkListener {

    Toolbar toolbar;

    private NetworkProvider provider;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        provider = (NetworkProvider) context;
        provider.addListener(this);
    }

    private boolean mScanning = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.network, container, false);
        toolbar = (Toolbar) view.findViewById(R.id.toolbar_network);
        toolbar.inflateMenu(R.menu.menu_network);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(final MenuItem item) {
                // there should be only one item in the menu
                item.setEnabled(false);
                if (!mScanning) {
                    provider.discoverPeers(new WifiP2pManager.ActionListener() {
                        @Override
                        public void onSuccess() {
                            Log.d("started scanning");
                            item.setEnabled(true);
                            item.setTitle(R.string.action_stop_scan);
                            mScanning = true;
                        }

                        @Override
                        public void onFailure(int reason) {
                            Log.d("failed to start scanning");
                            item.setEnabled(true);
                            item.setTitle(R.string.action_scan);
                            mScanning = false;
                        }
                    });
                } else {
                    provider.stopPeerDiscovery(new WifiP2pManager.ActionListener() {
                        @Override
                        public void onSuccess() {
                            Log.d("stopped scanning");
                            item.setEnabled(true);
                            item.setTitle(R.string.action_scan);
                            mScanning = false;
                        }
                        @Override
                        public void onFailure(int reason) {
                            Log.d("failed to stop scanning");
                            item.setEnabled(true);
                            item.setTitle(R.string.action_stop_scan);
                            mScanning = true;
                        }
                    });
                }
                return true;
            }
        });
        wifiP2pEnabled(provider.wifiP2pEnabled());
        return view;
    }

    @Override
    public void wifiP2pEnabled(boolean enabled) {
        if (toolbar != null) {
            MenuItem item = toolbar.getMenu().getItem(0);
            item.setEnabled(enabled);
            item.setVisible(enabled);
        }
    }

}
