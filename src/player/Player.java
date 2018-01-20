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
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Reydar
 */
public class Player {

    /**
     * @param args the command line arguments
     * @throws java.io.FileNotFoundException
     */
    public static void main(String[] args) throws FileNotFoundException, IOException, InterruptedException 
    {     
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Period period_9 = new Period(9);
        Period period_21 = new Period(21);
        Period period_40 = new Period(40);
        
        String jsonTimestamp = "resources/lastTimestamp.json";
        List<String> coinsList = readJSONCoinsConfig("resources/coinsConf.json");
        
        while(true)
        {
            long currentTimestamp = System.currentTimeMillis();
            long lastKlineEndTimestamp = readJSONLastTimestamp(jsonTimestamp);
            
            for(int c = 0; c < coinsList.size(); c++) 
            {
            
                String coinName = coinsList.get(c);
                
                try {
                    System.out.println("Checking " + coinName);
                    URL oracle = new URL("https://www.binance.com/api/v1/klines?symbol=" + coinName + "&interval=15m"); // URL to Parse
                    URLConnection yc = oracle.openConnection();
                    BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));

                    // Read all the input lines from the URL (in this case just 1)
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {

                        // Parse the full JSON
                        JSONArray j_rows = new JSONArray(inputLine);
                        for(int i = 0; i < j_rows.length(); i++) 
                        {
                            JSONArray j_rowData = new JSONArray(j_rows.get(i).toString());

                            Kline kline = new Kline(j_rowData.getLong(0), j_rowData.getString(1), j_rowData.getString(2), j_rowData.getString(3), j_rowData.getString(4), 
                                    j_rowData.getString(5), j_rowData.getLong(6), j_rowData.getString(7), j_rowData.getInt(8), j_rowData.getString(9), j_rowData.getString(10));

                            period_9.addKline(kline);
                            period_21.addKline(kline);
                            period_40.addKline(kline);

                            // Where we have enough data...
                            if(period_9.periodIsReady() && period_21.periodIsReady() && period_40.periodIsReady()) 
                            {
        //                        System.out.println(period_9.getActualMean());
        //                        System.out.println(period_21.getActualMean());
        //                        System.out.println(period_40.getActualMean());

                                // Check if the actual range needs to be procesed
                                if( (lastKlineEndTimestamp < kline.getStartTime()) && 
                                    (currentTimestamp > kline.getStartTime() && currentTimestamp < kline.getEndTime()))
                                {
                                    System.out.println(" - checking " + dateFormat.format(kline.getStartTime()));

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
                                            System.out.println(dateFormat.format(kline.getStartTime()));
                                            System.out.println(" -order: SELL");
                                        }

                                        // Eloi's case 5 - Sell
                                        boolean period_21old_over_period9_old = (period_21.getOldMean() > period_9.getOldMean());
                                        boolean period_9actual_over_period21_actual = (period_9.getActualMean() > period_21.getActualMean());
                                        if(period_21old_over_period9_old && period_9actual_over_period21_actual)
                                        {
                                            System.out.println(dateFormat.format(kline.getStartTime()));
                                            System.out.println(" -order: BUY");
                                        }
                                    } 

                                    // Set the last timestamp
                                    JSONObject obj = new JSONObject();
                                    obj.put("Timestamp", kline.getEndTime());
                                    try (FileWriter file = new FileWriter(jsonTimestamp)) {
                                        file.write(obj.toString());
                                    }
                                } //endIfTimestamp     
                            }


                        }
                    }
                    in.close();

                } catch(IOException | JSONException e) {
                    System.err.println(e);
                }
                
            } //forEveryCoin
            
            Thread.sleep(1 * 60 * 1000); // milliseconds to a second
            
        } //endWhileTrue
        
    }

    /**
     * 
     * @param filename
     * @return
     * @throws FileNotFoundException
     * @throws IOException 
     */
    private static long readJSONLastTimestamp(String filename) throws FileNotFoundException, IOException 
    {
        File f = new File(filename);
        
        if(!f.exists()) return 0;
        else 
        {
            BufferedReader br = new BufferedReader(new FileReader(filename));
            String line;
            if((line = br.readLine()) != null)
            {
                JSONObject json = new JSONObject(line);
                return json.getLong("Timestamp");
            }
            else 
            {
                return 0;
            }
        }
    }
    
    /**
     * 
     * @param filename
     * @return
     * @throws FileNotFoundException
     * @throws IOException 
     */
    private static List<String> readJSONCoinsConfig(String filename) throws FileNotFoundException, IOException 
    {
        File f = new File(filename);
        List<String> coinNames = new ArrayList<>();
        
        if(!f.exists()) throw new java.lang.RuntimeException("Coins config file not found.");
        else 
        {
            BufferedReader br = new BufferedReader(new FileReader(filename));
            String line;
            
            if((line = br.readLine()) != null)
            {
                JSONObject json = new JSONObject(line);
                JSONArray jsonArray = new JSONArray(json.get("coins").toString());
                
                for(int i = 0; i < jsonArray.length(); i++) coinNames.add(jsonArray.get(i).toString());
                
                return coinNames;
            }
            else 
            {
                throw new java.lang.RuntimeException("Error with coins config file.");
            }
        }
    }
    
}
