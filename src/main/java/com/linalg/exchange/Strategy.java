package com.linalg.exchange;

import java.util.List;

public interface Strategy {
    String name();
    List<Order> onTick(BookView view, Position position);

} 