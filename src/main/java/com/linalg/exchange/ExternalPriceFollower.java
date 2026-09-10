package com.linalg.exchange;

import java.util.List;

public final class ExternalPriceFollower implements Strategy {
    private final long[] prices;
    private final long thresholdTicks;
    private int index = 0;
    private long nextId = 4_000_000;

    public ExternalPriceFollower(long[] prices) {
        this(prices, 2);
    }

    public ExternalPriceFollower(long[] prices, long thresholdTicks) {
        this.prices = prices.clone();
        this.thresholdTicks = thresholdTicks;
    }

    @Override
    public String name() { return "External"; }

    @Override
    public List<StrategyAction> onTick(BookView view, Position pos) {
        if (index >= prices.length) return List.of();

        long target = prices[index++];
        var mid = view.mid();
        if (mid.isEmpty()) return List.of();

        long m = mid.get();
        if (Math.abs(target - m) < thresholdTicks) return List.of();

        Side side = target > m ? Side.BUY : Side.SELL;
        return List.of(new StrategyAction.Submit(
            new Order(nextId++, side, target, 20, view.tick())
        ));
    }
}
