/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.mcnalu.stockmarket;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * A simple-minded investment scheme that invests in several shares at a time.
 * If the currently held shares rise in value above the given threshold, those
 * shares will be sold and used to buy shares that currently decreasing in
 * value.
 *
 * @author nalu
 */
public class MultipleFractionalInvestment extends Investment {

    private static double sellThreshold = 1.5;
    private static double buyThreshold = -0.05;
    private static double sellFraction = 0.5;
    private static double buyFraction = 0.5;
    private static int NO_MORE_DATA = 0;
    private static int NO_NEW_DATA = 1;
    private static int NEW_DATA = 2;
    private ArrayList<StockTimeSeries> seriesList = new ArrayList();
    private ArrayList<StockTimeSeries> availableList = new ArrayList();
    Date dateToEnd = new Date();
    private String event;

    public void add(StockTimeSeries timeSeries) {
        //System.err.print("Adding " + timeSeries.name);
        if (seriesList.isEmpty() || timeSeries.getFirstDate().after(seriesList.get(0).getFirstDate())) {
            seriesList.add(timeSeries);
            //System.err.println("to end.");
        } else {
            seriesList.add(0, timeSeries);
            //System.err.println("to beginning.");
        }
    }

    public void invest(double investment) {
        super.invest(investment);
        money = investment;
        Calendar cal = Calendar.getInstance();
        cal.setTime(seriesList.get(0).getFirstDate());
        int status = NEW_DATA;
        status = updateAvailableList(cal.getTime());
        if (status != NEW_DATA) {
            System.err.println("Should never happen!");
            System.exit(999);
        }
        System.err.println(getHeaderString());
        do {
            event = "";
            boolean hasBought = buyShares();
            boolean hasSold = sellShares();
            if (cal.get(Calendar.DAY_OF_WEEK)==Calendar.MONDAY) {
                printProgress(getPortfolioStatus(cal.getTime()));
            }

            do {
                cal.add(Calendar.DAY_OF_MONTH, 1);
                status = updateAvailableList(cal.getTime());
            } while (status == NO_NEW_DATA);

        } while (status != NO_MORE_DATA);
        double total=money + getPortfolioValue();
        double rawProfit = total - investment;
        System.err.println(getPortfolioStatus(cal.getTime()));
        System.err.println("Final money=" + Math.round(money));
        System.err.println("Final value of held shares=" + Math.round(getPortfolioValue()));
        System.err.println("Raw profit=" + Math.round(rawProfit));
        StockTimeSeries timeSeries=seriesList.get(0);
        double diff = timeSeries.getFinalDate().getTime()-timeSeries.getFirstDate().getTime();
        diff/=1000*3600*24*365.25;//convert from milliseconds to years
        double interestRate=Math.pow(total/investment,1./diff)-1.;
        System.err.println("Annual equivalent interest rate="+interestRate);
    }

    private double getPortfolioValue() {
        double shareValue = 0.;
        for (StockTimeSeries timeSeries : seriesList) {
            shareValue += timeSeries.getValue();
        }
        return shareValue;
    }

    private String getPortfolioString() {
        String s = "";
        for (StockTimeSeries timeSeries : seriesList) {
            s += timeSeries.name + "," + Math.round(timeSeries.getValue()) + "\n";
        }
        return s;
    }

    private String getHeaderString(){
        String s = "date";
        for(StockTimeSeries timeSeries : seriesList){
            s+=","+timeSeries.name;
        }
        s += ",money,share value,total value,available shares,event";
        return s;        
    }
    
    private String getPortfolioStatus(Date date) {
        String s = StockTimeSeries.dateFormat.format(date);
        for(StockTimeSeries timeSeries : seriesList){
            s+=","+Math.round(timeSeries.getSharesHeld());
        }
        double pValue=getPortfolioValue();
        s += "," + Math.round(money)
                 + "," + Math.round(pValue)
                 + "," + Math.round(money+pValue)
                 + "," + availableList.size()
                 + "," + event;
        return s;
    }

    private int updateAvailableList(Date date) {
        if (date.after(dateToEnd)) {
            return NO_MORE_DATA;
        }
        int status = NO_NEW_DATA;
        availableList.clear();
        for (StockTimeSeries sts : seriesList) {
            if (sts.findDatum(date)) {
                availableList.add(sts);
                status = NEW_DATA;
            }
        }
        return status;
    }

    @Override
    protected boolean sellShares() {
        for (int i = 0; i < availableList.size(); i++) {
            StockTimeSeries timeSeries = availableList.get(i);
            if (timeSeries.hasShares() && timeSeries.getPrice() > sellThreshold * timeSeries.getPriceAtLastPurchase()) {
                money += timeSeries.sellShares(sellFraction);
                event += " SELL " + timeSeries.name;
                return true;
            }
        }
        return false;
    }

    @Override
    protected boolean buyShares() {
        if (money < 1.) {
            return false;
        }
        //Could be improved by finding shares with biggest drop, but this'll do
        for (int i = 0; i < availableList.size(); i++) {
            StockTimeSeries timeSeries = availableList.get(i);
            double delta = timeSeries.getDelta(5);

            if (delta < buyThreshold * timeSeries.getPrice()) {
                money -= timeSeries.purchaseShares(buyFraction * money);
                event += " BUY " + timeSeries.name;
                return true;
            }
        }
        return false;
    }
}
