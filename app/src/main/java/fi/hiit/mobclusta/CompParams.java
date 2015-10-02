package fi.hiit.mobclusta;

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

    public int imageBytes() {
        return width*height*8;
    }

    public long totalIterations() {
        return ((long) width)*height*subsamples*maxiterations;
    }

}
