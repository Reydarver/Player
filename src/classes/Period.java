package classes;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Victor
 */
public class Period {
    
    int iterations;
    List<Kline> values = new ArrayList<>();
    
    double oldMean = 0.0;
    double actualMean = 0.0;
    
    public Period(int iterations)
    {
        this.iterations = iterations;
    }
    
    // Private
    private double getMean()
    {
        double total;
        double sum = 0.0;
        
        for (Kline kline : this.values) {
            sum += kline.getClosePrice();
        }
        
        total = sum/(double)this.iterations;
        
        return total;
    }
    
    // Public
    public void addKline(Kline kline)
    {
        // Make sure not to add more than "iteration" values
        if(this.values.size() == this.iterations) this.values.remove(0);
        this.values.add(kline);
        
        // Update the means
        this.oldMean = this.actualMean;
        this.actualMean = this.getMean();
    }
    
    public boolean periodIsReady()
    {
        return this.values.size() == this.iterations;
    }
    
    public double getActualMean()
    {
        return this.actualMean;
    }
    
    public double getOldMean()
    {
        return this.oldMean;
    }
}
