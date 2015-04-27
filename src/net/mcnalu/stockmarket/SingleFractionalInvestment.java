/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.mcnalu.stockmarket;

import java.util.LinkedList;

/**
 * Simple investment strategy for one time series of share prices. This differs
 * from SimpleInvestment in that only a fraction of shares are bought are sold
 * each time.
 *
 * Sell some shares if they are sellThreshold times the amount they were bought
 * for. Spend some available money buying shares if the last five months saw a
 * fractional drop in value of more than (negative of) buyThreshold.
 *
 * Raw profit=8654 Adjusted profit=3031
 *
 * @author nalu
 */
public class SingleFractionalInvestment extends Investment {

    private static double sellThreshold = 2;
    private static double buyThreshold = -0.1;
    private static double sellFraction = 0.5;
    private static double buyFraction = 0.5;
    private StockTimeSeries timeSeries;

    public SingleFractionalInvestment(StockTimeSeries ts) {
        timeSeries = ts;
    }

    public void invest(double investment) {
        super.invest(investment);
        money = investment - timeSeries.purchaseShares(investment);
        int initialShares = timeSeries.getSharesHeld();
        do {
            String event = "";
            if (sellShares()) {
                event = "SELL";
            }
            if (buyShares()) {
                event = "BUY";
            }
            StockTimeSeries.Datum d = timeSeries.getDatum();
            printProgress(StockTimeSeries.dateFormat.format(d.date)
                    + "," + Math.round(initialShares * d.price)
                    + "," + Math.round(timeSeries.getValue())
                    + "," + Math.round(money)
                    + " " + event);
        } while (timeSeries.next());

        double finalValue = money + timeSeries.getFinalValue();
        double rawProfit = finalValue - investment;
        double adjustedProfit = finalValue - initialShares * timeSeries.getFinalPrice();
        System.err.println("Raw profit=" + Math.round(rawProfit));
        System.err.println("Adjusted profit=" + Math.round(adjustedProfit));
    }

    protected boolean sellShares() {
        if (timeSeries.getPrice() > sellThreshold * timeSeries.getPriceAtLastPurchase()) {
            money += timeSeries.sellShares(sellFraction);
            return true;
        }
        return false;
    }

    protected boolean buyShares() {
        double delta = timeSeries.getDelta(5);

        if (delta < buyThreshold * timeSeries.getPrice()) {
            money -= timeSeries.purchaseShares(buyFraction * money);
            return true;
        }

        return false;
    }
}
