package com.linalg.exchange;

import java.util.Optional;

public record BookView (
    Optional<Long> bestbid,
    Optional<Long> bestask,
    long lastPrice,
    long tick
){
    public Optional<Long> spread(){
        if (bestbid.isEmpty() || bestask.isEmpty()) return Optional.empty();
        return Optional.of(bestask.get() - bestbid.get());
    }

    public Optional<Long> mid() {
        if (bestbid.isEmpty() || bestask.isEmpty()) return Optional.empty();
        return Optional.of((bestask.get() + bestbid.get()) / 2);
    }
}
