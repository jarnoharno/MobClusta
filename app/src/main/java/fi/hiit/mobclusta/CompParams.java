package fi.hiit.mobclusta;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class CompParams {

    public int width;
    public int height;
    public int tasks;
    public int subsamples;
    public int maxiterations;

    public CompParams() {}

    public void write(DataOutputStream out) throws IOException {
        out.write(width);
        out.write(height);
        out.write(tasks);
        out.write(subsamples);
        out.write(maxiterations);
    }

    public static CompParams read(DataInputStream in) throws IOException {
        CompParams params = new CompParams();
        params.width = in.readInt();
        params.height = in.readInt();
        params.tasks = in.readInt();
        params.subsamples = in.readInt();
        params.maxiterations = in.readInt();
        return params;
    }
}
