package com.linalg.exchange;

import java.util.Optional;

public record BookView(
    Optional<Long> bestBid,
    Optional<Long> bestAsk,
    long lastPrice,
    long tick
) {
    public Optional<Long> spread() {
        if (bestBid.isEmpty() || bestAsk.isEmpty()) return Optional.empty();
        return Optional.of(bestAsk.get() - bestBid.get());
    }

    public Optional<Long> mid() {
        if (bestBid.isEmpty() || bestAsk.isEmpty()) return Optional.empty();
        return Optional.of((bestAsk.get() + bestBid.get()) / 2);
    }
}
