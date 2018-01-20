package classes;

/**
 *
 * @author Victor
 */
public class Kline {
    
    private long startTime;
    private double openPrice;
    private double highPrice;
    private double lowPrice;
    private double closePrice;
    private double volume;
    private long endTime;
    private double quoteVolume;
    private int nTrades;
    private double buyBaseVolume;
    private double quoteBaseVolume;
    
    public Kline(long startTime, String openPrice, String highPrice, String lowPrice, String closePrice,
            String volume, long endTime, String quoteVolume, int nTrades, String buyBaseVolume, String quoteBaseVolume )
    {
        this.startTime = startTime;
        this.openPrice = Double.parseDouble(openPrice);
        this.highPrice = Double.parseDouble(highPrice);
        this.lowPrice = Double.parseDouble(lowPrice);
        this.closePrice = Double.parseDouble(closePrice);
        this.volume = Double.parseDouble(volume);
        this.endTime = endTime;
        this.quoteVolume = Double.parseDouble(quoteVolume);
        this.nTrades = nTrades;
        this.buyBaseVolume = Double.parseDouble(buyBaseVolume);
        this.quoteBaseVolume = Double.parseDouble(quoteBaseVolume);
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public double getOpenPrice() {
        return openPrice;
    }

    public void setOpenPrice(double openPrice) {
        this.openPrice = openPrice;
    }

    public double getHighPrice() {
        return highPrice;
    }

    public void setHighPrice(double highPrice) {
        this.highPrice = highPrice;
    }

    public double getLowPrice() {
        return lowPrice;
    }

    public void setLowPrice(double lowPrice) {
        this.lowPrice = lowPrice;
    }

    public double getClosePrice() {
        return closePrice;
    }

    public void setClosePrice(double closePrice) {
        this.closePrice = closePrice;
    }

    public double getVolume() {
        return volume;
    }

    public void setVolume(double volume) {
        this.volume = volume;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public double getQuoteVolume() {
        return quoteVolume;
    }

    public void setQuoteVolume(double quoteVolume) {
        this.quoteVolume = quoteVolume;
    }

    public int getnTrades() {
        return nTrades;
    }

    public void setnTrades(int nTrades) {
        this.nTrades = nTrades;
    }

    public double getBuyBaseVolume() {
        return buyBaseVolume;
    }

    public void setBuyBaseVolume(double buyBaseVolume) {
        this.buyBaseVolume = buyBaseVolume;
    }

    public double getQuoteBaseVolume() {
        return quoteBaseVolume;
    }

    public void setQuoteBaseVolume(double quoteBaseVolume) {
        this.quoteBaseVolume = quoteBaseVolume;
    }
    
}
