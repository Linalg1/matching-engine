# Java Matching Engine

A limit order book and multi-agent market simulation written from scratch in Java.

The project models price-time-priority matching, partial fills, order cancellation, multi-level execution, and a simple market simulation with a market maker, noise trader, and an externally informed trader. The goal is not to reproduce a production exchange, but to make the mechanics of matching and adverse selection explicit enough to test and reason about.

## Highlights

- Price-time-priority limit order book
- FIFO execution within each price level
- Partial fills and multi-level matching
- Order cancellation with an order-location index
- Market-maker cancel/requote behaviour
- Inventory-aware quote skew
- Randomized agent activation with reproducible seeds
- Simulation driven by historical QQQ prices
- JUnit tests for matching and cancellation invariants

## Architecture

The order book stores bids and asks separately:

```text
BUY  side: prices sorted descending
SELL side: prices sorted ascending
```

Each price level is represented by a `LinkedHashMap<Long, Order>`. This preserves insertion order for FIFO execution while also allowing direct removal of a known order from a price level.

A separate order-location index maps each resting order ID to its side and price level. This avoids scanning the entire book when cancelling an order.

Conceptually:

```text
Order ID -> (side, price)
                |
                v
        TreeMap<price, price level>
                         |
                         v
             LinkedHashMap<order ID, order>
```

## Matching rules

Incoming limit orders execute against the best available price on the opposite side while their limit price still crosses the book.

For example, a buy order at 101 can execute against resting asks at 99, 100, and 101, in that order. At each price level, older resting orders execute first.

Trades execute at the resting order's price.

If the incoming order is only partially filled, the unfilled quantity rests in the book at its limit price.

## Cancellation

Resting orders can be cancelled by ID.

The initial version of the project used an `ArrayDeque` at each price level. That made FIFO matching natural but made arbitrary cancellation expensive because the queue had to be searched.

The current implementation uses a `LinkedHashMap` per level plus an order-location index. This preserves FIFO ordering while making cancellation much more direct.

## Market simulation

The simulation contains three simple agent types.

### Naive market maker

The market maker posts a bid and ask around the current midpoint. Before placing new quotes, it cancels any previous quotes that are still resting in the book.

Its quote centre is shifted according to inventory:

```text
fair value = midpoint + inventory skew
bid        = fair value - half spread
ask        = fair value + half spread
```

The inventory skew pushes quotes lower when the market maker is long and higher when it is short.

### Noise trader

The noise trader submits randomly directed orders around the current market. It provides uninformed order flow and helps generate trading activity.

### External-price trader

The external-price trader observes a historical QQQ reference price. When the simulated market deviates sufficiently from that reference, it submits an order in the direction of the discrepancy.

This agent is intentionally better informed than the market maker and is used to study whether informed flow creates adverse-selection pressure.

## Why cancel/requote matters

An earlier version of the simulation allowed market-maker quotes to remain in the book indefinitely. That created stale orders which an informed trader could exploit mechanically.

The current version explicitly cancels and replaces previous quotes before submitting new ones. This makes any adverse-selection result more meaningful because losses are no longer automatically caused by abandoned historical quotes.

## Agent scheduling

Agents are activated in a randomized order at every simulation step.

The random generator is seeded, so experiments remain reproducible while avoiding a fixed sequencing advantage such as always allowing one trader to act last.

## Metrics

The simulation reports, for each strategy:

- final inventory
- cash balance
- mark-to-market PnL
- total filled quantity
- maximum absolute inventory

It also reports total trade count and the number of resting orders remaining in the book.

A single run should not be treated as evidence of a robust trading result. The next research step is to evaluate distributions across many random seeds and compare scenarios such as informed vs. uninformed order flow and inventory-skewed vs. symmetric market making.

## Tests

The JUnit suite covers core order-book behaviour, including:

- non-crossing orders resting in the book
- execution at the resting order's price
- partial fills
- FIFO priority at a shared price level
- walking multiple price levels
- cancelling a resting order
- cancelling the remainder after a partial fill
- preserving FIFO priority when another order at the same price is cancelled

Run the tests with:

```bash
mvn test
```

## Run the simulation

Requirements:

- Java 21+
- Maven

Then run:

```bash
mvn compile
mvn exec:java -Dexec.mainClass="com.linalg.exchange.App"
```

The repository includes historical QQQ data under `data/qqq.csv` for the simulation.

## Project structure

```text
src/main/java/com/linalg/exchange/
├── App.java
├── BookView.java
├── ExternalPriceFollower.java
├── NaiveMarketMaker.java
├── NoiseTrader.java
├── Order.java
├── OrderBook.java
├── Position.java
├── PriceLoader.java
├── Side.java
├── Simulation.java
├── Strategy.java
├── StrategyAction.java
└── Trade.java

src/test/java/com/linalg/exchange/
└── OrderBookTest.java
```

## Next steps

Planned extensions:

- amend/replace semantics with explicit priority rules
- experiment runner across many random seeds
- PnL and inventory time-series output
- comparison of market making with and without inventory skew
- sensitivity analysis for spread, order size, and informed-trader threshold
- transaction and fill-level logging for deeper microstructure analysis

## Scope

This is an educational matching engine and market simulation. It intentionally prioritizes transparent mechanics and testable assumptions over production-exchange concerns such as networking, persistence, concurrency, latency optimization, and full exchange protocol support.
