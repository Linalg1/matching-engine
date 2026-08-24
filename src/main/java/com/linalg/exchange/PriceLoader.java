package com.linalg.exchange;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public final class PriceLoader {

    public static long[] loadCloses(String path, int closeIndex) throws IOException {
        List<String> lines = Files.readAllLines(Path.of(path));
        List<Long> out = new ArrayList<>();

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;

            String[] cols = line.split(",");
            if (cols.length <= closeIndex) continue;

            try {
                double close = Double.parseDouble(cols[closeIndex]);
                out.add(Math.round(close * 100));
            } catch (NumberFormatException e) {
                continue;
            }
        }

        long[] prices = new long[out.size()];
        for (int i = 0; i < prices.length; i++) prices[i] = out.get(i);
        return prices;
    }
}