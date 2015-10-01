package fi.hiit.mandelbrot;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

    final Params params;
    ExecutorService executorService;
    CompletionService<double[][]> completionService;

    Main(Params params) {
        this.params = params;
        executorService = Executors.newFixedThreadPool(params.threads);
        completionService = new ExecutorCompletionService<>(executorService);
    }

    void printParams() {
        printf("size=%dx%d", params.width, params.height);
        printf("tasks=%d", params.tasks);
        printf("subsamples=%d", params.subsamples);
        printf("maxIterations=%d", params.maxIterations);
        printf("threads=%d", params.threads);
        printf("outputBase=\"%s\"", params.outputBase == null ? "" : params.outputBase);
    }

    synchronized void printf(String format, Object... args) {
        System.out.printf(format + "\n", args);
    }

    void runTask(final int task) {
        completionService.submit(new Callable<double[][]>() {
            @Override
            public double[][] call() {
                printf("task (%d/%d) started", task + 1, params.tasks);
                try {
                    double[][] img = Mandelbrot.stripTask(params.width, params.height,
                            task, params.tasks, params.subsamples, params.maxIterations);
                    write(img);
                    printf("task (%d/%d) finished", task + 1, params.tasks);
                    return img;
                } catch (Exception e) {
                    printf(e.toString());
                    return new double[0][];
                }
            }

            void write(double[][] img) {
                if (params.outputBase == null) return;
                NumberFormat nf = NumberFormat.getInstance(Locale.US);
                int width = ((int) Math.log10(params.tasks)) + 1;
                String file = String.format("%s.%0" + width + "d.csv", params.outputBase, task);
                PrintWriter writer = null;
                try {
                    writer = new PrintWriter(file, "UTF-8");
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    return;
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    return;
                }
                printf("writing task (%d/%d) results to %s", task, params.tasks, file);
                for (int y = 0; y < img.length; ++y) {
                    for (int x = 0; x < img[y].length; ++x) {
                        writer.print(nf.format(img[y][x]));
                        writer.print(',');
                    }
                    writer.print('\n');
                }
                writer.close();
            }
        });
    }

    void run() {
        printParams();
        for (int i = 0; i < params.tasks; ++i) {
            runTask(i);
        }
        try {
            for (int i = 0; i < params.tasks; ++i) {
                completionService.take();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        executorService.shutdown();
    }

    public static void main(String[] args) {
        Params params = new Params();
        Options options = new Options();
        options.addOption("h","help",false,"show this help");
        options.addOption(Option
                .builder("d")
                .longOpt("dimensions")
                .hasArg()
                .argName("SIZE")
                .desc(String.format("image dimensions (default %dx%d)",params.width,params.height))
                .valueSeparator()
                .build());
        options.addOption(Option
                .builder("t")
                .longOpt("tasks")
                .hasArg()
                .argName("TASKS")
                .desc(String.format("tasks per image (default %d)",params.tasks))
                .valueSeparator()
                .build());
        options.addOption(Option
                .builder("s")
                .longOpt("subsamples")
                .hasArg()
                .argName("SUBSAMPLES")
                .desc(String.format("subsamples per pixel (default %d)", params.subsamples))
                .valueSeparator()
                .build());
        options.addOption(Option
                .builder("i")
                .longOpt("iterations")
                .hasArg()
                .argName("ITERATIONS")
                .desc(String.format("max iterations per sample (default %d)", params.maxIterations))
                .valueSeparator()
                .build());
        options.addOption(Option
                .builder("c")
                .longOpt("cores")
                .hasArg()
                .argName("CORES")
                .desc(String.format("threads used (default %d)", params.threads))
                .valueSeparator()
                .build());

        CommandLineParser parser = new DefaultParser();
        // parse the command line arguments
        CommandLine line = null;
        try {
            line = parser.parse(options, args);

            // minimal error checking
            if (line.hasOption("h")) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.setLongOptSeparator("=");
                formatter.printHelp("mandelbrot [OPTIONS...] [OUTPUT_BASE]",
                        "If OUTPUT_BASE is defined, the results for each task will be written to " +
                        "OUTPUT_BASE.TASK.csv. Otherwise the results will be discarded silently.",
                        options, "");
                return;
            }
            if (line.hasOption("d")) {
                String[] vals = line.getOptionValue("d").split("x");
                params.width = Integer.parseInt(vals[0]);
                params.height = Integer.parseInt(vals[1]);
            }
            if (line.hasOption("t")) {
                params.tasks = Integer.parseInt(line.getOptionValue("t"));
            }
            if (line.hasOption("s")) {
                params.subsamples = Integer.parseInt(line.getOptionValue("s"));
            }
            if (line.hasOption("i")) {
                params.maxIterations = Integer.parseInt(line.getOptionValue("i"));
            }
            if (line.hasOption("c")) {
                params.threads = Integer.parseInt(line.getOptionValue("c"));
            }
            if (line.getArgs().length > 0) {
                params.outputBase = line.getArgs()[0];
            }
            new Main(params).run();

        } catch (ParseException e) {
            System.out.println("Unexpected exception:" + e.getMessage());
        }
    }
}
