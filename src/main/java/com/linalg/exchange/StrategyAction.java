package com.linalg.exchange;

public sealed interface StrategyAction permits StrategyAction.Submit, StrategyAction.Cancel {
    record Submit(Order order) implements StrategyAction {}
    record Cancel(long orderId) implements StrategyAction {}
}
