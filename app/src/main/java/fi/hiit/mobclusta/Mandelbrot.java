package fi.hiit.mobclusta;

import java.util.Random;

public class Mandelbrot {

    public static double xmin = -2.5;
    public static double xmax =  1.0;
    public static double ymin = -1.0;
    public static double ymax =  1.0;

    public static double pixel(int w, int h, double px, double py) {
        return pixel(w, h, px, py, 1000);
    }

    public static long pixel(int w, int h, double px, double py, long max_iteration) {
        double x0 = px*(xmax-xmin)/w + xmin;
        double y0 = py*(ymax-ymin)/h + ymin;
        double x = 0.0;
        double y = 0.0;
        long iteration = 0;
        while (x*x + y*y < 2*2 && iteration < max_iteration) {
            double xtemp = x*x - y*y + x0;
            y = 2*x*y + y0;
            x = xtemp;
            iteration++;
        }
        return iteration;
    }

    public static double[][] strip(int w, int h, int h0, int striph, int subsamples, long max_iteration) {
        Random random = new Random(0);
        double[][] img = new double[striph][w];
        for (int y = h0; y < h0+striph; ++y) {
            for (int x = 0; x < w; ++x) {
                long total = 0;
                for (int i = 0; i < subsamples; ++i) {
                    double ex = random.nextDouble();
                    double ey = random.nextDouble();
                    total += pixel(w, h, x + ex, y + ey, max_iteration);
                }
                img[y][x] = ((double) total) / subsamples;
            }
        }
        return img;
    }

    public static double[][] image(int w, int h, int subsamples, long max_iterations) {
        return strip(w, h, 0, h, subsamples, max_iterations);
    }
}
