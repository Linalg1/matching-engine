package com.linalg.exchange;

import java.util.*;

public final class ExternalPriceFollower implements Strategy {
    private final long[] prices;   // NASDAQ, en ticks
    private int index = 0;
    private long nextId = 4_000_000;

    public ExternalPriceFollower(long[] prices) {
        this.prices = prices;
    }

    @Override
    public String name() { return "External"; }

    @Override
    public List<Order> onTick(BookView view, Position pos) {
        if (index >= prices.length) return List.of();

        var mid = view.mid();
        if (mid.isEmpty()) return List.of();      // sortie AVANT de consommer

        long target = prices[index++];
        long m = mid.get();

        if (Math.abs(target - m) < 2) return List.of();

        Side side = (target > m) ? Side.BUY : Side.SELL;
        return List.of(new Order(nextId++, side, target, 20, view.tick()));
    }
}
