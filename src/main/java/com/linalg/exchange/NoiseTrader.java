package com.linalg.exchange;

import java.util.List;
import java.util.Random;

public final class NoiseTrader implements Strategy {

    private final Random rng;
    private long nextId = 2_000_000;

    public NoiseTrader(long seed) {
        this.rng = new Random(seed);
    }

    @Override
    public String name() { return "Noise"; }

    @Override
    public List<Order> onTick(BookView view, Position pos) {
        if (rng.nextDouble() > 0.3) return List.of();

        long ref = view.mid().orElse(view.lastPrice());
        Side side = rng.nextBoolean() ? Side.BUY : Side.SELL;
        long offset = rng.nextInt(5) - 2;

        return List.of(new Order(nextId++, side, ref + offset, 10, view.tick()));
    }
}