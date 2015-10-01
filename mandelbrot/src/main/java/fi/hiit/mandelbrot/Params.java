package fi.hiit.mandelbrot;

public class Params {

    // default values
    public int width = 350;
    public int height = 200;
    public int tasks = 1;
    public int subsamples = 4;
    public int maxIterations = 10;
    public int threads = 1;
    public String outputBase = null;

    public Params() {}

    public int imageBytes() {
        return width*height*8;
    }

    public long totalIterations() {
        return ((long) width)*height*subsamples*maxIterations;
    }
}
