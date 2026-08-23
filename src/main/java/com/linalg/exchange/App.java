package com.linalg.exchange;

import java.util.List;

public class App {
    public static void main(String[] args) {
        var sim = new Simulation(
            List.of(
                new NaiveMarketMaker(2, 10),
                new NoiseTrader(42)
                //new TrendFollower(20)
            ),
            10_000    // prix initial en ticks
        );

        sim.run(10_000);
        sim.printResults();
    }
}