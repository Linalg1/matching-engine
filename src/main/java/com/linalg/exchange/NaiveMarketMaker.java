package com.linalg.exchange;

import java.util.ArrayList;
import java.util.List;

public final class NaiveMarketMaker implements Strategy {
    private final long halfSpread;
    private final long size;
    private long nextId = 1_000_000;

    private Long activeBidId;
    private Long activeAskId;

    public NaiveMarketMaker(long halfSpread, long size) {
        if (halfSpread <= 0) throw new IllegalArgumentException("halfSpread must be positive");
        if (size <= 0) throw new IllegalArgumentException("size must be positive");
        this.halfSpread = halfSpread;
        this.size = size;
    }

    @Override
    public String name() { return "NaiveMM"; }

    @Override
    public List<StrategyAction> onTick(BookView view, Position pos) {
        List<StrategyAction> actions = new ArrayList<>();

        // Cancel previous quotes before replacing them. If a quote was already filled,
        // cancellation simply returns false inside the simulation and is harmless.
        if (activeBidId != null) actions.add(new StrategyAction.Cancel(activeBidId));
        if (activeAskId != null) actions.add(new StrategyAction.Cancel(activeAskId));
        activeBidId = null;
        activeAskId = null;

        var mid = view.mid();
        if (mid.isEmpty()) return actions;

        long inventorySkew = -pos.inventory() / 10;
        long fair = mid.get() + inventorySkew;

        long bidPrice = fair - halfSpread;
        long askPrice = fair + halfSpread;
        if (bidPrice <= 0 || askPrice <= bidPrice) return actions;

        Order bid = new Order(nextId++, Side.BUY, bidPrice, size, view.tick());
        Order ask = new Order(nextId++, Side.SELL, askPrice, size, view.tick());
        activeBidId = bid.id();
        activeAskId = ask.id();

        actions.add(new StrategyAction.Submit(bid));
        actions.add(new StrategyAction.Submit(ask));
        return actions;
    }
}
