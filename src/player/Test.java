package player;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import org.json.*;

import classes.Kline;
import classes.Period;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import javax.mail.*;
import javax.mail.internet.*;

/**
 *
 * @author Reydar
 */
public class Test {
    
    public static String jsonTimestamp = "resources/lastTimestamp.json";
    
    public static DateFormat logFormat = new SimpleDateFormat("yyyy_MM_dd_HHmmss");
    public static DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    
    public static final Logger logger = Logger.getLogger("DebugLog");  
    
    public static double totalAmount = 0;
    
    public static DecimalFormat df = new DecimalFormat("0.00000000");

    
    /**
     * MAIN
     * 
     * @param args the command line arguments
     * @throws java.io.FileNotFoundException
     * @throws java.lang.InterruptedException
     */
    public static void main(String[] args) throws FileNotFoundException, IOException, InterruptedException 
    {   
        System.out.println("Binance Player started...");
        
        // Initialize the variables
        Period period_9 = new Period(9);
        Period period_21 = new Period(21);
        Period period_40 = new Period(40);
        
        initializeLog();
        
        String coinName = "DASHETH";
        long currentTimestamp = 1512082800000L;
        long startTime = 1512082800000L;
        long endTimestamp = 1514674800000L; //startTime 1512082800000
        
        while(currentTimestamp <= endTimestamp)
        {
            try {
                URL oracle = new URL("https://www.binance.com/api/v1/klines?symbol=" + coinName + "&interval=15m&startTime=" + startTime); // URL to Parse
                URLConnection yc = oracle.openConnection();
                BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));

                // Read all the input lines from the URL (in this case just 1)
                String inputLine;
                while ((inputLine = in.readLine()) != null) //just One 
                {
                    // Parse the full JSON
                    JSONArray j_rows = new JSONArray(inputLine);
                    for(int i = 0; i < j_rows.length(); i++) 
                    {
                        JSONArray j_rowData = new JSONArray(j_rows.get(i).toString());

                        Kline kline = new Kline(j_rowData.getLong(0), j_rowData.getString(1), j_rowData.getString(2), j_rowData.getString(3), j_rowData.getString(4), 
                                j_rowData.getString(5), j_rowData.getLong(6), j_rowData.getString(7), j_rowData.getInt(8), j_rowData.getString(9), j_rowData.getString(10));

                        //update Timestamp
                        currentTimestamp = kline.getEndTime();
                        
                        // Analyze the current Kline
                        if(currentTimestamp >= endTimestamp) {System.out.println("BREAK"); break;} //If we are out of endDate finish
                        analyzePeriods(coinName, kline, period_9, period_21, period_40, currentTimestamp);

                    } //forEveryRowFromKGraph
                }
                in.close();
                
                startTime = currentTimestamp;
                
            } catch(IOException | JSONException e) {
                System.err.println(e);
            }
            
        } //endWhile
        
        System.out.println("TOTAL = " + totalAmount);
    }

    /**
     * 
     * @throws IOException 
     */
    private static void initializeLog() throws IOException
    {
        FileHandler fh;  

        String logName = logFormat.format(System.currentTimeMillis());

        // This block configure the logger with handler and formatter  
        fh = new FileHandler("log/" + logName + ".log");  
        logger.addHandler(fh);
        SimpleFormatter formatter = new SimpleFormatter();  
        fh.setFormatter(formatter);  

        // the following statement is used to log any messages  
        logger.info("Initializing");  
    }
    
    /**
     * 
     * @param coinName
     * @param kline
     * @param period_9
     * @param period_21
     * @param period_40
     * @param currentTimestamp
     * @param lastKlineEndTimestamp
     * @throws IOException 
     */
    private static void analyzePeriods(String coinName, Kline kline, Period period_9, Period period_21, Period period_40, 
            long currentTimestamp) throws IOException
    {
        period_9.addKline(kline);
        period_21.addKline(kline);
        period_40.addKline(kline);

        // Where we have enough data...
        if(period_9.periodIsReady() && period_21.periodIsReady() && period_40.periodIsReady()) 
        {
            // Check period_40 is the lowest
            boolean period_40oldMeansmallest = (period_40.getOldMean() < period_21.getOldMean()) &&(period_40.getOldMean() < period_9.getOldMean());
            boolean period_40actualMeansmallest = (period_40.getActualMean()< period_21.getActualMean()) &&(period_40.getActualMean() < period_9.getActualMean());
            if(period_40oldMeansmallest && period_40actualMeansmallest)
            {
                // Eloi's case 1 - Sell
                boolean period_9old_over_period21_old = (period_9.getOldMean() > period_21.getOldMean());
                boolean period_21actual_over_period9_actual = (period_21.getActualMean() > period_9.getActualMean());
                if(period_9old_over_period21_old && period_21actual_over_period9_actual)
                {
                    //System.out.println("\t-order: SELL");
                    logger.log(Level.INFO, "[Coin - SELL] {0} {1} {2}", new Object[]{coinName, df.format(kline.getClosePrice()), dateFormat.format(kline.getStartTime())});
                    System.out.println(kline.getClosePrice());

                    totalAmount += kline.getClosePrice();
                }

                // Eloi's case 5 - Buy
                boolean period_21old_over_period9_old = (period_21.getOldMean() > period_9.getOldMean());
                boolean period_9actual_over_period21_actual = (period_9.getActualMean() > period_21.getActualMean());
                if(period_21old_over_period9_old && period_9actual_over_period21_actual)
                {
                    //System.out.println("\t-order: BUY");
                    logger.log(Level.INFO, "[Coin - BUY] {0} {1} {2}", new Object[]{coinName, df.format(kline.getClosePrice()), dateFormat.format(kline.getStartTime())});
                    System.out.println(kline.getClosePrice());

                    totalAmount -= kline.getClosePrice();
                }
            } 

            // Set the last timestamp
            JSONObject obj = new JSONObject();
            obj.put("Timestamp", kline.getEndTime());
            try (FileWriter file = new FileWriter(jsonTimestamp)) {
                file.write(obj.toString());
            } 

        } //endIfPeriodsReady
    }
    
}
