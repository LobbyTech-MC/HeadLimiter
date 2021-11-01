package dev.j3fftw.headlimiter;

import javax.annotation.Nonnull;
import java.util.Map;

public class CountResult {

    private final int total;
    private final Map<String, Integer> counts;

    protected CountResult(int total, Map<String, Integer> counts) {
        this.total = total;
        this.counts = counts;
    }

    public int getTotal() {
        return total;
    }

    public int getCount(@Nonnull String id) {
        return counts.getOrDefault(id, 0);
    }

    @Nonnull
    public Map<String, Integer> getCounts() {
        return counts;
    }
}
