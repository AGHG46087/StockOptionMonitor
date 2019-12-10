// StockMarketUtils.java
package com.stockmarket.data; 
 
import com.hgutil.HGCalendar; 
import com.hgutil.ParseData; 
import com.hgutil.data.*; 
import java.util.*; 
/** 
 * Class Object to facilatate some of the calculations required in Stocks 
 * @author: Hans-Jurgen Greiner 
 */ 
public class StockMarketUtils implements StockMarketTypes 
{ 
 
  static final public int TRADINGDAYSINYEAR = 252; 
  static final public int STRATEGY_BULL_CALL_SPREAD = 1; 
  static final public int STRATEGY_BEAR_CALL_SPREAD = 2; 
  static final public int STRATEGY_BULL_PUT_SPREAD  = 3; 
  static final public int STRATEGY_BEAR_PUT_SPREAD  = 4; 
   
 
  /** 
   * StockMarketUtils constructor comment. 
   */ 
  public StockMarketUtils() 
  { 
    super(); 
  } 
  /** 
   * Method to get the expiration date of a Stock Option 
   * Stock Options terminate on the 3rd Friday of each month 
   * Calculate the Expiration Day of the current running Month. 
   * This method caluclates a Strict Version of the Exiration Days 
   * Meaning excluding Weekends and also a Loose Version where 
   * weekend days are included 
   * @param month The Ordinal month, for which an Option is being played. 
   * @param strict boolean value, If Weekends are desired to be expcluded select true,  
   *        false will include weekends 
   * @return int the number of days 
   */ 
  public static int daysUntilExpiration(int month, boolean strict) 
  { 
    // Calculate the Expiration Day of the deisred calendar running Month 
    GregorianCalendar expireMonth = getExpirationDateForMonth(month); 
    int days = daysUntilExpiration(expireMonth,strict); 
    return (days); 
  } 
  /** 
   * Method to get the expiration date of a Stock Option 
   * Stock Options terminate on the 3rd Friday of each month 
   * Calculate the Expiration Day of the current running Month. 
   * This method caluclates a Strict Version of the Exiration Days 
   * Meaning excluding Weekends and also a Loose Version where 
   * weekend days are included 
   * @param calendar The Calendar date month, for which an Option is being played. 
   * @param strict boolean value, If Weekends are desired to be expcluded select true,  
   *        false will include weekends 
   * @return int the number of days 
   */ 
  public static int daysUntilExpiration(Date calendar, boolean strict) 
  { 
    // Calculate the Expiration Day of the deisred calendar running Month 
    GregorianCalendar expireMonth = new GregorianCalendar(); 
    expireMonth.setTime(calendar); 
    int days = daysUntilExpiration(expireMonth,strict); 
 
    return (days); 
  } 
  /** 
   * Method daysUntilExpiration. Method to get the expiration date of a Stock Option 
   * Stock Options terminate on the 3rd Friday of each month 
   * Calculate the Expiration Day of the current running Month. 
   * This method caluclates a Strict Version of the Exiration Days 
   * Meaning excluding Weekends and also a Loose Version where 
   * weekend days are included 
   * @param expireMonth GregorianCalendar - The date of epiration 
   * @param strict boolean value, if weekends are desired to be excluded selectect true 
   *        false will include weekends 
   * @return int the number of days 
   */ 
  public static int daysUntilExpiration(GregorianCalendar expireMonth, boolean strict) 
  { 
    int days = HGCalendar.daysBetween(expireMonth,strict); 
 
    return (days); 
  } 
  /** 
   * This method calculates a Best Guess of  the Expected,  
   * one standard deviation, movement of a stock based on  
   * "X" number of days until a Stock expiration has passed. 
   * Number of days until expiration are considered are Strict 
   * FORMULA: EM = Stock Price * Historic Volatility (HV) * sqrt(Number of Days) / sqrt(Trading days in year)  
   * @param sd The StockData Object containg a stock where the expected mocement, can be calculated 
   * @return java.lang.double 
   */ 
  public static double getExpectedMovement(StockData sd) 
  { 
 
    GregorianCalendar now = new GregorianCalendar(); 
    int days = daysUntilExpiration(now.getTime(), true); 
 
    double doubleEM = getExpectedMovement(sd, days); 
    return doubleEM; 
 
  } 
  /** 
   * This method calculate the Expected, one standard deviation, 
   * movement of a stock based on "X" number of days until 
   * a Stock expiration has passed. 
   * FORMULA: EM = Stock Price * Historic Volatility (HV) * sqrt(Number of Days) / sqrt(Trading days in year)  
   * @param sd The StockData Object containg a stock where the expected mocement, can be calculated 
   * @param  numberOfDays int The Number of days until Expiration Day ( The third Friday of the running Month ) 
   * @return java.lang.double 
   */ 
  public static double getExpectedMovement(StockData sd, int numberOfDays) 
  { 
    double doubleEM = 0.0; 
    if (sd != null) 
    { 
      double stockPrice = sd.getLast().doubleValue(); 
      double hv = ((Fraction) sd.getVolatility().getData()).doubleValue(); 
      double sqrtDays = Math.sqrt(numberOfDays); 
      double sqrtTradeDaysInYear = Math.sqrt(TRADINGDAYSINYEAR); 
      double em = stockPrice * hv * sqrtDays / sqrtTradeDaysInYear; 
 
      doubleEM = em; 
    } 
    return doubleEM; 
 
  } 
 
  /** 
   * Method isCallOption. Returns true if the option is a Call Option 
   * The months parameter is in the set of ALL the CALL and PUT character SET  
   * represented months as defined the Option Exchange.  
   * @param symbol String of the Symbol 
   * @param months String of the Symbol Months allowed 
   * @return boolean 
   */ 
  public static boolean isOptionInMonthRange(String symbol, String months) 
  { 
    char symbolMonth = symbol.charAt(symbol.length() - 2); 
    boolean rc = (months.indexOf(symbolMonth) >= 0); 
    return rc; 
  } 
  /** 
   * Method isCallOption. Returns true if the option is in the SET of CALL Options 
   * @param symbol 
   * @return boolean 
   */ 
  public static boolean isCallOption(String symbol) 
  { 
    char symbolMonth = symbol.charAt(symbol.length() - 2); 
    boolean rc = false; 
    for (int i = 0; i < CALLMONTHARR.length && !rc; i++) 
    { 
      char month = CALLMONTHARR[i].name.charAt(0); 
      rc |= (symbolMonth == month); 
    } 
    return rc; 
  } 
  /** 
   * Method isPutOption. Returns True if the option is in the SET of PUT Options 
   * @param symbol 
   * @return boolean 
   */ 
  public static boolean isPutOption(String symbol) 
  { 
    char symbolMonth = symbol.charAt(symbol.length() - 2); 
    boolean rc = false; 
    for (int i = 0; i < PUTMONTHARR.length && !rc; i++) 
    { 
      char month = PUTMONTHARR[i].name.charAt(0); 
      rc |= (symbolMonth == month); 
    } 
    return rc; 
  } 
 
  /** 
   * Method daysUntilExpiration.  This method will calcualtate the number if 
   * Days until Expiration, based upn the Option symbol 
   * @param optionSymbol The otion being evaluated 
   * @param strict  true to exclude weekends 
   * @return int the number of days 
   */ 
  public static int daysUntilExpiration( String optionSymbol, boolean strict ) 
  { 
    int days = 0; 
    boolean done = false; 
    int ordMonth = 0; 
    char symbolMonth = optionSymbol.charAt(optionSymbol.length() - 2); 
    if ( isCallOption( optionSymbol ) ) 
    { 
      for (int i = 0; i < CALLMONTHARR.length && !done; i++) 
      { 
        char month = CALLMONTHARR[i].name.charAt(0); 
        if ( symbolMonth == month) 
        { 
          ordMonth = i; 
          done = true; 
        } 
      } 
    } 
    else 
    { 
      for (int i = 0; i < PUTMONTHARR.length && !done; i++) 
      { 
        char month = PUTMONTHARR[i].name.charAt(0); 
        if ( symbolMonth == month) 
        { 
          ordMonth = i; 
          done = true; 
        } 
      } 
    } 
     
    GregorianCalendar calendar = getExpirationDateForMonth(ordMonth); 
    days = daysUntilExpiration(calendar, strict); 
    return days; 
  } 
  /** 
   * Method getSetOfCallOptions.  This method returns Subset of the  
   * Vector of Option Data being passed in. 
   * @param list - Vector of OptionData objects 
   * @return Vector - A Sorted List based on the @see OptionDataVerticalComparator 
   */ 
  public static Vector getSetOfCallOptions(Vector list, int numberOfMonths) 
  { 
    // Create a new Vector 
    Vector callList = new Vector(); 
    // Did we get a list that is usable 
    if (list != null) 
    { 
      // Loop through the List 
      for (int i = 0; i < list.size(); i++) 
      { 
        // Grab the Object and see if it is a OptionData 
        Object obj = list.elementAt(i); 
        if (obj instanceof OptionData) 
        { 
          if (((OptionData) obj).getOptionTypeValue() == StockMarketTypes.CALL) 
          { 
            callList.addElement(obj); 
          } 
        } 
        else if (obj instanceof String) 
        { 
          // Get the Symbol Of the OptionData 
          String symbol = (String) obj; 
          if (isCallOption(symbol)) 
          { // Add the Option Data to our list 
            OptionData od = new OptionData(); 
            od.setSymbol(symbol); 
            callList.addElement(od); 
          } 
        } 
      } 
    } 
    return callList; 
  } 
  /** 
   * Method getSetOfCallOptions.  This method returns Subset of the  
   * Vector of Option Data being passed in. 
   * @param list - Vector of OptionData objects 
   * @return Vector - A Sorted List based on the @see OptionDataVerticalComparator 
   */ 
  public static Vector getSetOfPutOptions(Vector list, int numberOfMonths) 
  { 
    // Create a new Vector 
    Vector putList = new Vector(); 
    // Did we get a list that is usable 
    if (list != null) 
    { 
      // Loop through the List 
      for (int i = 0; i < list.size(); i++) 
      { 
        // Grab the Object and see if it is a OptionData 
        Object obj = list.elementAt(i); 
        if (obj instanceof OptionData) 
        { 
          if (((OptionData) obj).getOptionTypeValue() == StockMarketTypes.PUT) 
          { 
            putList.addElement(obj); 
          } 
        } 
        else if (obj instanceof String) 
        { 
          // Get the Symbol Of the OptionData 
          String symbol = (String) obj; 
          if (isPutOption(symbol)) 
          { // Add the Option Data to our list 
            OptionData od = new OptionData(); 
            od.setSymbol(symbol); 
            putList.addElement(od); 
          } 
        } 
      } 
    } 
    return putList; 
  } 
 
  /** 
   * Method to delete a particular File 
   * @param filename A String representation of the Watch List Name 
   */ 
  public static void expungeListsFile(String filename) 
  { 
    try 
    { 
      // Create a temp File and delete it - if it exists 
      java.io.File file = new java.io.File(filename); 
      if (file.exists() && file.canRead()) 
      { 
        Thread.currentThread().sleep(100); 
        file.delete(); 
      } 
    } 
    catch (InterruptedException exc) 
    { 
      // Do nothing 
    } 
    catch (Exception exc) 
    { 
      // Caught an Exception 
      String excName = exc.getClass().getName(); 
    } 
  } 
  /** 
   * Method to maintain Persistance of a Vector Model to a filename 
   * @param index integer representation of the vector we want to save 
   * @param fileName The File to which we are going to serialize the data 
   */ 
  public static synchronized void serializeData(Object data, String fileName) 
  { 
    // First see if we already have one then delete it 
    expungeListsFile(fileName); 
 
    java.io.ObjectOutputStream ostream = null; 
 
    try 
    { 
      // Open a new output stream to save the data 
      ostream = new java.io.ObjectOutputStream(new java.io.FileOutputStream(fileName)); 
 
      // Write the data to the file 
      ostream.writeObject(data); 
 
      // Close the data 
      ostream.close(); 
 
    } 
    catch (java.io.NotSerializableException exc) 
    { 
      System.err.println("serializeData() - NotSerializableException: Failed saving data to [" + fileName + "]"); 
      System.err.println("serializeData() - exc info ==> " + exc); 
    } 
    catch (java.io.IOException exc) 
    { 
      System.err.println("serializeData() - IOException: Failed saving data to [" + fileName + "]"); 
      System.err.println("serializeData() - exc info ==> " + exc); 
    } 
    // Enforce the idea of closing the output stream 
    finally 
    { 
      if (ostream != null) 
      { 
        try 
        { 
          ostream.close(); 
        } 
        catch (Exception exc) 
        {} 
      } 
    } 
  } 
  /**  
   * Creates a Historic Data conainer from a Serialized file on Disk 
   * that was a originally serialized from HistoricStockDataContainer  
   * @param fileName the Name of the File on Disk  
   * @return HistoricStockDataContainer  
   */ 
  public static HistoricStockDataContainer getHistoricDataFromDisk(String fileName) 
  { 
    HistoricStockDataContainer container = null; 
 
    try 
    { 
      // Open a new output stream to save the data 
      java.io.ObjectInputStream istream = new java.io.ObjectInputStream(new java.io.FileInputStream(fileName)); 
 
      // Write the data to the file 
      container = (HistoricStockDataContainer) istream.readObject(); 
 
      // Close the data 
      istream.close(); 
 
    } 
    catch (ClassNotFoundException exc) 
    { 
      System.err.println("createDataContainer() - ClassNotFoundException: Failed reading data to [" + fileName + "]"); 
      System.err.println("createDataContainer() - exc info ==> " + exc); 
    } 
    catch (java.io.NotSerializableException exc) 
    { 
      System.err.println("createDataContainer() - NotSerializableException: Failed reading data to [" + fileName + "]"); 
      System.err.println("createDataContainer() - exc info ==> " + exc); 
    } 
    catch (java.io.IOException exc) 
    { 
      System.err.println("createDataContainer() - IOException: Failed reading data to [" + fileName + "]"); 
      System.err.println("createDataContainer() - exc info ==> " + exc); 
    } 
    return (container); 
  } 
 
  /** 
   * Method getNextExpirationDate. Returns the Next Expiration Friday based upon todays date. 
   * @return GregorianCalendar 
   */ 
  public static GregorianCalendar getNextExpirationDate() 
  { 
    // Calculate the Expiration Day of the current running Month 
    GregorianCalendar expireMonth = new GregorianCalendar(TimeZone.getTimeZone("America/New_York"));; 
    boolean done = false; 
    do 
    { 
      // Get the Expiration Friday for the Month 
      expireMonth = getExpirationDateForMonth( expireMonth.get(Calendar.MONTH) ); 
      // Get Today and Time 
      GregorianCalendar now = new GregorianCalendar(); 
      // If we are greater than that we are to advance the month 
      if( now.getTimeInMillis() > expireMonth.getTimeInMillis()) 
      { 
        expireMonth.add(Calendar.MONTH, 1); 
      } 
      else 
      { // We are good to go. 
        done = true; 
      } 
    }while(!done); 
     
    return expireMonth; 
  } 
  /** 
   * Method getExpirationDateForMonth.  Calculates the expiration Friday Date 
   * For the specified Month 
   * @param month 
   * @return GregorianCalendar 
   */ 
  public static GregorianCalendar getExpirationDateForMonth(int month) 
  { 
    // Calculate the Expiration Day of the current running Month 
    GregorianCalendar expireMonth = new GregorianCalendar(); 
    boolean done = false; 
     
    if (Calendar.JANUARY <= month && month <= Calendar.DECEMBER) 
    { 
      // If we have a month in the Past.  then Add a year 
      if ( month < expireMonth.get(Calendar.MONTH) ) 
      { 
        expireMonth.add( Calendar.YEAR, 1 ); 
      } 
      // Set the Month, Day of the Week and Week of the Month 
      expireMonth.set(Calendar.MONTH, month); 
      expireMonth.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY); 
      expireMonth.set(Calendar.WEEK_OF_MONTH, 1 ); 
       
      // Get the Month running 
      int monthInYear = expireMonth.get(Calendar.MONTH); 
      int tmpMonth = monthInYear; 
       
      // If our Month has not changed then calculate  the day 
      // Other wise Friday on the 1st week of the month is  
      // really the last friday of the running month, and month  
      // will be different 
      if ( tmpMonth == month ) 
      { 
        do 
        { 
          expireMonth.add(Calendar.WEEK_OF_MONTH, -1); 
          tmpMonth = expireMonth.get(Calendar.MONTH); 
        }while( tmpMonth == monthInYear ); 
      } 
      // Add 3 weeks, to end on Expiration Friday 
      expireMonth.add(Calendar.WEEK_OF_MONTH, 3); 
    } 
    return expireMonth; 
  } 
  /** 
   * Method calculateSpreadDifference. Method calculates the Value of the Spread Difference. 
   * returning the Absolute value of the difference. 
   * Formula: 
   * sellBid - buyAsk 
   * sellAsk - buyBid 
   * spread = sellSpread - buySpread 
   * @param buyBid double The Option that is being bought, Bid Price 
   * @param buyAsk double The Option that is being bought, Ask Price 
   * @param sellBid double The Option that is being sold, Bid Price 
   * @param sellAsk double The Option that is being sold, Ask Price 
   * @return double The Spread Difference 
   */ 
  public static double calculateSpreadDifference(double buyBid, double buyAsk, double sellBid, double sellAsk) 
  { 
    double buySpread = Math.abs(sellBid - buyAsk); 
    double sellSpread = Math.abs(sellAsk - buyBid); 
    double spreadDiff = Math.abs(sellSpread - buySpread); 
    return spreadDiff; 
  } 
   
  /** 
   * Method calculateSpreadDifference. Method calculates the Value of the Spread Difference. 
   * returning the Absolute value of the difference. 
   * Formula: 
   * sellBid - buyAsk 
   * sellAsk - buyBid 
   * spread = sellSpread - buySpread 
   * @param buyOption The BuyOption 
   * @param sellOption The Sell Option 
   * @return double The Difference 
   */ 
  public static double calculateSpreadDifference(OptionData buyOption, OptionData sellOption) 
  { 
    double rc = 0.0; 
    if ( ( buyOption != null ) && 
         ( sellOption != null ) ) 
    { 
      rc = calculateSpreadDifference( buyOption.getBid().doubleValue(), buyOption.getAsk().doubleValue(), 
                                      sellOption.getBid().doubleValue(), sellOption.getAsk().doubleValue() );
    } 
    return rc; 
  } 
   
  /** 
   * Method getBreakEvenDelta. Calculates the Break Even Delta between and Option Spread that is to be sold. 
   * Using the Formula: BED = ((LOD - SOD) * ( BEP - SOS) / DBS ) +-SOD 
   * Where: BED = Break Even Delta 
   *        LOD = Long Option Delta 
   *        SOD = Short Option Delta 
   *        BEP = Break Even Point 
   *        SOS = Short Option Strike Price 
   *        DBS = Distance Between Strikes. 
   * NOTE: This method is only to be used where the Strategy includes SELLING a Spread 
   * @param longOption The Option that is to be bought 
   * @param shortOption the Option that is to be Sold 
   * @param breakEvenPoint The AMount of Money that will be Break Even 
   * @param spreadType The Type of Spread SPREAD2_50, SPREAD5_00, SPREAD10_00 
   * @return double The Return Type 
   */ 
  public static double getBreakEvenDelta(OptionData longOption, OptionData shortOption, double breakEvenPoint, int spreadType) 
  { 
    double rc = 0.0; 
     
    if ( ( longOption != null ) && 
         ( shortOption != null ) ) 
    { 
      final double distanceBetweenStrikes = getSpreadWidth(spreadType); // DBS 
      double longOptionDelta = Math.abs(longOption.getGreekDelta()); 
      double shortOptionDelta = Math.abs(shortOption.getGreekDelta()); 
      int sign = ( shortOption.getOptionTypeValue() == StockMarketUtils.PUT ) ? -1 : 1; 
      double shortOptionStrike = shortOption.getStrikePrice().doubleValue(); 
      // Formula: 
      // BED = ((LOD - SOD) * ( BEP - SOS) / DBS ) +-SOD 
      // 1st. Subtract the SOD from LOD 
      double lod_sod = longOptionDelta - shortOptionDelta; 
      // 2nd. Subtract the SOS from BEP 
      double bep_sos = breakEvenPoint - shortOptionStrike; 
      // 3rd. Multiple the two results 
      double mult = lod_sod * bep_sos; 
      // 4th. Divide the Multiple by the Distance between Strikes. 
      double div = mult / distanceBetweenStrikes; 
      // 5th. Add the Short Option Delta and sign to the previous result. 
      double bed = div + ( sign * shortOptionDelta ); 
      rc = bed; 
    } 
     
    return rc; 
  } 
  /** 
   * Method getSpreadWidth. Given the Spread Type this method returns the width of the Spread 
   * @param spreadType int Where spread type is SPREAD2_50, SPREAD5_00, or SPREAD10_0 
   * @return double 
   */ 
  public static double getSpreadWidth( int spreadType ) 
  { 
    double rc = 0.0; 
      switch( spreadType ) 
      { 
        case SPREAD_2_50: 
          rc = 2.50; 
          break; 
        case SPREAD_5_00: 
          rc = 5.00; 
          break; 
        case SPREAD_10_00: 
          rc = 10.00; 
          break; 
        default: 
          rc = 0.0; 
          break; 
      } 
    return rc; 
  } 
  public static void main( String[] args ) 
  { 
  } 
}
