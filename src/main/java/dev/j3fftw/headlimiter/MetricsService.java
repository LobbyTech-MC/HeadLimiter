package dev.j3fftw.headlimiter;

import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;

import java.text.DecimalFormat;

public class MetricsService {

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.00");

    // Jeff, do not fucking change this you dumbass!
    private static final int METRICS_ID = 9978;

    private final HeadLimiter plugin;

    protected MetricsService(final HeadLimiter plugin) {
        this.plugin = plugin;
    }

    protected void start() {
        final Metrics metrics = new Metrics(plugin, METRICS_ID);

        metrics.addCustomChart(new SimplePie("max_process_memory", () -> {
            final Runtime runtime = Runtime.getRuntime();
            final long maxMemory = runtime.maxMemory();
            return DECIMAL_FORMAT.format((maxMemory / Math.pow(1024, 3))) + " GB";
        }));
    }
}
