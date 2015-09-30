package fi.hiit.mobclusta;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import fi.hiit.mobclusta.common.view.LogInterface;

public class ComputationFragment extends Fragment implements LogInterface {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.computation, container, false);
        log = (TextView) view.findViewById(R.id.log);
        for (String s : backlog) {
            log.append(s);
        }
        backlog.clear();
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        MainActivity main = (MainActivity) context;
        main.setLog(this);
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
}
