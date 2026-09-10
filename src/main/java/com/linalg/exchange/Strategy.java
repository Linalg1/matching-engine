package com.linalg.exchange;

import java.util.List;

public interface Strategy {
    String name();
    List<StrategyAction> onTick(BookView view, Position position);
}
