package fi.hiit.mobclusta;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import fi.hiit.mobclusta.common.view.LogInterface;

public class ComputationFragment extends Fragment implements LogInterface, GroupListener {

    private Toolbar toolbar;
    private boolean computing = false;

    private MainActivity mainActivity;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.computation, container, false);
        log = (TextView) view.findViewById(R.id.log);
        for (String s : backlog) {
            log.append(s);
        }
        backlog.clear();
        toolbar = (Toolbar) view.findViewById(R.id.toolbar_computation);
        toolbar.setBackgroundColor(getResources().getColor(R.color.primary));
        toolbar.inflateMenu(R.menu.menu_computation);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(final MenuItem item) {
                // there should be only one item in the menu
                item.setEnabled(false);
                computing = !computing;
                if (computing) {
                    item.setEnabled(true);
                    item.setTitle(R.string.action_stop);
                    mainActivity.startComputation();
                } else {
                    item.setEnabled(true);
                    item.setTitle(R.string.action_start);
                    mainActivity.stopComputation();
                }
                return true;
            }
        });
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity) context;
        mainActivity.setLog(this);
        mainActivity.setGroupListener(this);
    }

    private TextView log;
    private List<String> backlog = new ArrayList<>();

    private SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    private String presentation(String s) {
        return "[" + dateFormat.format(new Date()) + "] " + s + "\n";
    }

    @Override
    public void d(String s) {
        String p = presentation(s);
        if (log == null) {
            backlog.add(p);
        } else {
            log.append(p);
        }
    }

    @Override
    public void setMasterAndConnected(boolean master) {
        if (toolbar == null) return;
        MenuItem item = toolbar.getMenu().getItem(0);
        item.setEnabled(master);
        item.setVisible(master);
    }

    @Override
    public int getWidth() {
        TextView view = (TextView) getView().findViewById(R.id.width);
        int value = Integer.parseInt(view.getText().toString());
        return value;
    }

    @Override
    public int getHeight() {
        TextView view = (TextView) getView().findViewById(R.id.height);
        int value = Integer.parseInt(view.getText().toString());
        return value;
    }

    @Override
    public int getTasks() {
        TextView view = (TextView) getView().findViewById(R.id.tasks);
        int value = Integer.parseInt(view.getText().toString());
        return value;
    }

    @Override
    public int getSubsamples() {
        TextView view = (TextView) getView().findViewById(R.id.subsamples);
        int value = Integer.parseInt(view.getText().toString());
        return value;
    }

    @Override
    public int getMaxIterations() {
        TextView view = (TextView) getView().findViewById(R.id.maxiterations);
        int value = Integer.parseInt(view.getText().toString());
        return value;
    }

    public CompParams getCompParams() {
        CompParams compParams = new CompParams();
        compParams.height = getHeight();
        compParams.width = getWidth();
        compParams.tasks = getTasks();
        compParams.subsamples = getSubsamples();
        compParams.maxiterations = getMaxIterations();
        return compParams;
    }

    @Override
    public void computationDone() {
        MenuItem item = toolbar.getMenu().getItem(0);
        item.setEnabled(true);
        item.setTitle(R.string.action_start);
    }
}
