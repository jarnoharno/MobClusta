package fi.hiit.mobclusta;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;

public class CompParams implements Serializable {

    public int width;
    public int height;
    public int tasks;
    public int subsamples;
    public int maxiterations;
    public int task;

    public CompParams() {}
    public CompParams(CompParams compParams) {
        this.width = compParams.width;
        this.height = compParams.height;
        this.tasks = compParams.tasks;
        this.subsamples = compParams.subsamples;
        this.maxiterations = compParams.maxiterations;
        this.task = compParams.task;
    }

    public void write(DataOutputStream out) throws IOException {
        out.write(width);
        out.write(height);
        out.write(tasks);
        out.write(subsamples);
        out.write(maxiterations);
        out.write(task);
    }

    public static CompParams read(DataInputStream in) throws IOException {
        CompParams params = new CompParams();
        params.width = in.readInt();
        params.height = in.readInt();
        params.tasks = in.readInt();
        params.subsamples = in.readInt();
        params.maxiterations = in.readInt();
        params.task = in.readInt();
        return params;
    }
}
