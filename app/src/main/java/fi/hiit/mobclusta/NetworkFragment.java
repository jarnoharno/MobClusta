package fi.hiit.mobclusta;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
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

    private LinearLayout availableList;
    private LinearLayout availableScanning;

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
                            availableScanning.setVisibility(View.VISIBLE);
                            mScanning = true;
                        }

                        @Override
                        public void onFailure(int reason) {
                            Log.d("failed to start scanning");
                            item.setEnabled(true);
                            item.setTitle(R.string.action_scan);
                            availableScanning.setVisibility(View.INVISIBLE);
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
                            availableScanning.setVisibility(View.INVISIBLE);
                            mScanning = false;
                        }

                        @Override
                        public void onFailure(int reason) {
                            Log.d("failed to stop scanning");
                            item.setEnabled(true);
                            item.setTitle(R.string.action_stop_scan);
                            availableScanning.setVisibility(View.VISIBLE);
                            mScanning = true;
                        }
                    });
                }
                return true;
            }
        });
        wifiP2pEnabled(provider.wifiP2pEnabled());
        availableList = (LinearLayout) view.findViewById(R.id.available_list);
        availableScanning = (LinearLayout) view.findViewById(R.id.available_scanning);
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

    @Override
    public void setPeerList(WifiP2pDeviceList peers) {
        availableList.removeAllViews();
        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        for (WifiP2pDevice device : peers.getDeviceList()) {
            View view = layoutInflater.inflate(android.R.layout.simple_list_item_2, availableList,
                    false);
            TextView text1 = (TextView) view.findViewById(android.R.id.text1);
            TextView text2 = (TextView) view.findViewById(android.R.id.text2);
            text1.setText(device.deviceName);
            text2.setText(device.deviceAddress);
            availableList.addView(view);
        }
    }

}
