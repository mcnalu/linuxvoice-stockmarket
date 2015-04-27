/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.mcnalu.stockmarket;

import java.io.BufferedReader;
import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;

/**
 * This class holds information on a stock time series, handling loading data,
 * as well as buying and selling shares.
 * @author nalu
 */
public class StockTimeSeries {
    
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    static DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
    String name;
    private ArrayList<Datum> series;
    private int index;
    private int sharesHeld;
    private double priceAtLastPurchase;
    

    public class Datum {

        Date date;
        double price;

        public String toString() {
            return dateFormat.format(date) + "," + price;
        }
    }

    public StockTimeSeries() {
        series = new ArrayList();
        reset();
    }

    public StockTimeSeries(File file){
        this(file,1);
    }
    
    public StockTimeSeries(File file, int priceColumn) {
        this();
        load(file,priceColumn);
    }

    public void reset() {
        index = 0;
        sharesHeld = 0;
        priceAtLastPurchase = 0.0;
    }

    public void load(File file) {
        load(file, 1);
    }

    public void load(File file, int priceColumn) {
        name=file.getName().substring(0,file.getName().indexOf(".csv"));
        series.clear();
        BufferedReader br = StockMarket.getBufferedReader(file);
        try {
            br.readLine(); //skip header
            String line = br.readLine();
            while (line != null) {
                line = line.trim();
                if (line.length() > 0 && line.charAt(0) != '#') {
                    String[] ss = line.split(",");
                    series.add(0, getDatum(ss, priceColumn));//series in reverse time order
                }
                line = br.readLine();
            }
            br.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public boolean next() {
        index++;
        if (index >= series.size()) {
            return false;
        } else {
            return true;
        }
    }

    public double purchaseShares(double moneyToSpend) {
        int nShares = (int) (moneyToSpend / getPrice());
        purchaseShares(nShares);
        return nShares * getPrice();
    }

    public void purchaseShares(int numberOfShares) {
        sharesHeld += numberOfShares;
        priceAtLastPurchase = getPrice();
    }

    public double sellShares(double fraction) {
        int sharesToSell = (int) (fraction * sharesHeld);
        if(sharesToSell<10){//Sell all if few are left
            sharesToSell=sharesHeld;
        }
        return sellShares(sharesToSell);
    }

    public double sellShares(int numberOfShares) {
        if (numberOfShares > sharesHeld) {
            numberOfShares = sharesHeld;
        }
        sharesHeld -= numberOfShares;
        double moneyFromSale = numberOfShares * getPrice();
        return moneyFromSale;
    }

    public int getSharesHeld() {
        return sharesHeld;
    }

    public boolean hasShares(){
        return sharesHeld>0;
    }
    
    public double getPrice() {
        return series.get(index).price;
    }

    public double getValue() {
        return sharesHeld * getPrice();
    }

    public double getPriceAtLastPurchase() {
        return priceAtLastPurchase;
    }

    public double getFinalPrice() {
        return series.get(series.size() - 1).price;
    }

    public double getFinalValue() {
        return sharesHeld * getFinalPrice();
    }

    public Date getFirstDate() {
        return series.get(0).date;
    }

    public Date getFinalDate() {
        return series.get(series.size() - 1).date;
    }

    public Datum getDatum() {
        return series.get(index);
    }

    public double getDelta(int ago) {
        if (index - ago < 0) {
            return Double.NaN;
        } else {
            return getPrice() - series.get(index - ago).price;
        }
    }

    public boolean hasDecreased(int ago) {
        double delta = getDelta(ago);
        if (Double.isNaN(delta) || delta >= 0.) {
            return false;
        } else {
            return true;
        }
    }

    private Datum getDatum(String[] ss, int priceColumn) {
        Datum d = new Datum();
        try {
            d.date = dateFormat.parse(ss[0]);
        } catch (ParseException ex) {
            System.err.println("Failed to parse date: " + ss[0]);
        }
        d.price = Double.parseDouble(ss[priceColumn]) / 1000; //Just Open for now, revise

        return d;
    }

    public boolean findDatum(Date date) {
        //try current and next index first for speed's sake?
        if (date.before(getFirstDate()) || date.after(getFinalDate())) {
            return false;
        } else {
            for (int i = 0; i < series.size(); i++) {
                Datum d = series.get(i);
                if (d.date.equals(date)) {
                    index = i;
                    return true;
                }
            }
        }

        return false;
    }

    public String toString() {
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        String s = "";
        for (Datum d : series) {
            s += d.toString() + "\n";
            if (d.price < min) {
                min = d.price;
            }
            if (d.price > max) {
                max = d.price;
            }
        }
        s += "min=" + min + ", max=" + max;
        return s;
    }
}
