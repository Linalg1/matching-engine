package com.linalg.exchange;

import java.util.*;

public final class NaiveMarketMaker implements Strategy {
    private final long halfSpread;
    private final long size;
    private long nextId = 1_000_000;

    public NaiveMarketMaker(long halfSpread, long size) {
        this.halfSpread = halfSpread;
        this.size = size;
    }

    @Override
    public String name() { return "NaiveMM"; }

    @Override
    public List<Order> onTick(BookView view, Position pos) {
        var mid = view.mid();
        long skew = -pos.inventory() / 10;
        if (mid.isEmpty()) return List.of();

        long m = mid.get();
        return List.of(
            new Order(nextId++, Side.BUY,  m + skew - halfSpread, size, view.tick()),
            new Order(nextId++, Side.SELL, m + skew + halfSpread, size, view.tick())
        );
    }
}
