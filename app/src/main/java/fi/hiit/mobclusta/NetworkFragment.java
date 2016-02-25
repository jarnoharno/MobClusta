package fi.hiit.mobclusta;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

public class NetworkFragment extends Fragment implements NetworkListener {

    private Toolbar toolbar;

    private NetworkProvider provider;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        provider = (NetworkProvider) context;
        provider.addListener(this);
    }

    private boolean mScanning = false;

    private LinearLayout connectedList;
    private LinearLayout availableList;
    private LinearLayout availableScanning;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.network, container, false);
        toolbar = (Toolbar) view.findViewById(R.id.toolbar_network);
        toolbar.setBackgroundColor(getResources().getColor(R.color.primary));
        toolbar.inflateMenu(R.menu.menu_network);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(final MenuItem item) {
                Log.d("scan button clicked: scanning=%b", mScanning);
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
                            clearList();
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
                            clearList();
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
        connectedList = (LinearLayout) view.findViewById(R.id.connected_list);
        availableList = (LinearLayout) view.findViewById(R.id.available_list);
        availableScanning = (LinearLayout) view.findViewById(R.id.available_scanning);
        CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkBox);
        provider.setOwnerIntent(checkBox.isChecked());
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                provider.setOwnerIntent(isChecked);
            }
        });
        CheckBox masterSlave = (CheckBox) view.findViewById(R.id.master_slave);
        provider.setMasterSlave(masterSlave.isChecked());
        masterSlave.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                provider.setMasterSlave(isChecked);
            }
        });
        Spinner spinner = (Spinner) view.findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.workers_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                provider.setWorkers(position+1);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        return view;
    }

    private int parseWorkers(String s) {
        return s.isEmpty() ? 1 : Math.max(Integer.parseInt(s), 1);
    }

    @Override
    public void wifiP2pEnabled(boolean enabled) {
        if (toolbar != null) {
            MenuItem item = toolbar.getMenu().getItem(0);
            item.setEnabled(enabled);
            item.setVisible(enabled);
        }
    }

    private Map<View, WifiP2pDevice> deviceMap = new HashMap<>();

    public void clearList() {
        connectedList.removeAllViews();
        availableList.removeAllViews();
        deviceMap.clear();
    }

    @Override
    public void setPeerList(WifiP2pDeviceList peers) {
        clearList();
        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        for (WifiP2pDevice device : peers.getDeviceList()) {
            View view = layoutInflater.inflate(android.R.layout.simple_list_item_2, availableList,
                    false);
            TextView text1 = (TextView) view.findViewById(android.R.id.text1);
            TextView text2 = (TextView) view.findViewById(android.R.id.text2);
            text1.setText(device.deviceName);
            text2.setText(device.deviceAddress + " (" + statusString(device.status) + ")");

            if (device.status == WifiP2pDevice.CONNECTED) {
                connectedList.addView(view);
                view.setOnClickListener(connectedListListener);
            } else {
                availableList.addView(view);
                view.setOnClickListener(deviceListListener);
            }
            deviceMap.put(view, device);
        }
    }

    final View.OnClickListener connectedListListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            final WifiP2pDevice device = deviceMap.get(v);
            final TextView text1 = (TextView) v.findViewById(android.R.id.text1);
            provider.removeGroup(new WifiP2pManager.ActionListener() {

                @Override
                public void onSuccess() {
                }

                @Override
                public void onFailure(int reason) {
                }
            });
            text1.setText(device.deviceName + " Disconnecting...");
        }
    };

    @Override
    public void discoveryStopped() {
        Log.d("discovery stopped");
        MenuItem item = toolbar.getMenu().getItem(0);
        item.setEnabled(true);
        item.setTitle(R.string.action_scan);
        availableScanning.setVisibility(View.INVISIBLE);
        mScanning = false;
    }

    public static String statusString(int status) {
        switch (status) {
            case WifiP2pDevice.AVAILABLE:
                return "available";
            case WifiP2pDevice.CONNECTED:
                return "connected";
            case WifiP2pDevice.FAILED:
                return "failed";
            case WifiP2pDevice.INVITED:
                return "invited";
            case WifiP2pDevice.UNAVAILABLE:
                return "unavailable";
        }
        return "";
    }

    final View.OnClickListener deviceListListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            final WifiP2pDevice device = deviceMap.get(v);
            final TextView text1 = (TextView) v.findViewById(android.R.id.text1);
            provider.connect(device, new WifiP2pManager.ActionListener() {

                @Override
                public void onSuccess() {
                }

                @Override
                public void onFailure(int reason) {
                }
            });
            text1.setText(device.deviceName + " Connecting...");
        }
    };

    @Override
    public void enableDiscovery(boolean enable) {
        MenuItem item = toolbar.getMenu().getItem(0);
        item.setEnabled(enable);
    }

    @Override
    public boolean isMasterSlave() {
        View view = getView();
        if (view == null) return false;
        CheckBox checkBox = (CheckBox) view.findViewById(R.id.master_slave);
        return checkBox.isChecked();
    }

}
