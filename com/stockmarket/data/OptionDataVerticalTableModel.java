// OptionDataVerticalTableModel.java
package com.stockmarket.data;

import javax.swing.ImageIcon;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.table.JTableHeader;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import com.hgtable.ColumnDataCellValue;
import com.hgtable.ColumnHeaderData;
import com.hgtable.HGTableModel;
import com.hgutil.AppProperties;
import com.hgutil.ParseData;
import com.hgutil.data.ColoredData;

/**
 * Class Object to manage Option Data in a Table
 * This model does contain specifics in reference to the
 * Class Object <B>OptionData</B>
 * @author: Hans-Jurgen Greiner
 */
public class OptionDataVerticalTableModel extends HGTableModel
{

  protected ResourceBundle bundle = null;
  protected AppProperties appProps = null;
  protected int startMonth = new GregorianCalendar().get(Calendar.MONTH);
  protected String validMonthOptions = null;
  private static final int CALL = StockMarketUtils.CALL;
  private static final int PUT = StockMarketUtils.PUT;
  private static final int SPREAD_2_50 = StockMarketUtils.SPREAD_2_50;
  private static final int SPREAD_5_00 = StockMarketUtils.SPREAD_5_00;
  private static final int SPREAD_10_00 = StockMarketUtils.SPREAD_10_00;

  private final double DEFAULT_REWARD_FACTOR = ParseData.parseNum(getString("VerticalSpread.default_reward_factor"), 1.8);
  private final int MIN_OPEN_INTEREST_FACTOR =
    ParseData.parseNum(getString("VerticalSpread.minimum_open_interest_factor"), 100);
  private final int DEFAULT_MIN_CONTRACTS = ParseData.parseNum(getString("VerticalSpread.default_minimum_contracts"), 1);
  private final int MINIMUM_MOVEMENT_PERIODS =
    ParseData.parseNum(getString("VerticalSpread.minimum_movement_periods"), 3);
  private final boolean NEW_HIGH_VALIDATION_REQUIRED =
    ParseData.parseBool(getString("VerticalSpread.new_highs_validation_required"), false);
  private final boolean NEW_LOWS_VALIDATION_REQUIRED =
    ParseData.parseBool(getString("VerticalSpread.new_lows_validation_required"), false);
  private HistoricStockDataContainer historicData = null;

  /**
   * Inner class used to help with the double clicking on the headers.
   * (Workbench>Preferences>Java>Templates)
   */
  public class ColumnListener extends HGTableModel.MainModelColumnListener
  {

    /**
     * Class Contructor
     * @see MainModelColumnListener#MainModelColumnListener(JTable)
     */
    public ColumnListener(JTable table)
    {
      super(table);
    }

    /**
     * This method will sort the table again, based upon the StockMartketUtils 
     * sort method @see StockMartketUtils#sortVerticalOptionData
     * @see MouseListener#mouseClicked(MouseEvent)
     */
    public void mouseClicked(MouseEvent e)
    {
      sortVerticalOptionData();
      table.tableChanged(new TableModelEvent(OptionDataVerticalTableModel.this));
      table.repaint();
    }
  }

  /**
   * class OptionDataRulesTask. This inner class will process all the Vertical
   * CALLS and PUTS 
   * @author Hans-Jurgen Greiner
   * 
   */
  private class OptionDataRulesTask extends TimerTask
  {
    private Vector calls = null;
    private Vector puts = null;

    /**
     * Method OptionDataRulesTask. The Main Constructor, This Task Will process the rules
     * for every time it is invoked.  We require the Puts and Calls in two seperate 
     * vectors So we can have the ability process various rules independently
     * @param calls A Vector of CALL Options
     * @param puts  A Vector of PUT Options
     */
    public OptionDataRulesTask(Vector calls, Vector puts)
    {
      this.calls = calls;
      this.puts = puts;
    }
    /**
     * Performs the action run method which in turn refresh the option chain list data
     * Rules for each.
     * @see TimerTask#run(ActionEvent)
     */
    public void run()
    {
      // Method to set the Varient Paired Options on the CALLS i.e 
      // $2.50, $5.00 and $10.00 spread
      OptionDataVerticalTableModel.this.setCallSpreadValues(calls);
      OptionDataVerticalTableModel.this.processVerticalSpreadEntranceCriteria(calls, CALL);
      // Method to set the Varient Paired Options on the PUTS i.e 
      // $2.50, $5.00 and $10.00 spread
      OptionDataVerticalTableModel.this.setPutSpreadValues(puts);
      OptionDataVerticalTableModel.this.processVerticalSpreadEntranceCriteria(puts, PUT);
      // Fire the Data has changed.
      OptionDataVerticalTableModel.this.fireTableDataChanged();
      // We are done, Terminate the process.
      this.cancel();
    }
  }
  /**
   * OptionDataVerticalTableModel constructor comment.
   */
  public OptionDataVerticalTableModel(Vector headers, Vector data)
  {
    super(headers, data);
    OptionData.setRiskFreeRate(ParseData.parseNum(getString("RiskFreeInterestRate"), 0.0));
    sortVerticalOptionData();
  }
  /**
   * Returns the resource bundle associated with this demo. Used
   * to get accessable and internationalized strings.
   * @return ResourceBundle
   */
  protected ResourceBundle getResourceBundle()
  {
    if (bundle == null)
    {
      bundle = ResourceBundle.getBundle("resources.OptionDataVertical");
    }
    return bundle;
  }
  /**
   * Returns the appproperties associated with this demo. Used
   * to get accessable and internationalized strings.
   * @return ResourceBundle
   */
  protected AppProperties getAppProperties()
  {
    if (appProps == null)
    {
      appProps = new AppProperties("StockApp.ini");
    }
    return appProps;
  }
  /**
   * Method setMonthToView. Method to set the ordinal month to view, The ordinal number
   * is natural based meaning 0-11, where Calendar.January == 0
   * @param month in the The ordinal value
   */
  public void setMonthToView(int month)
  {
    if (Calendar.JANUARY <= month && month <= Calendar.DECEMBER)
    {
      this.startMonth = month;
      this.validMonthOptions = "";
      this.validMonthOptions += StockMarketUtils.CALLMONTHARR[this.startMonth].name;
      this.validMonthOptions += StockMarketUtils.PUTMONTHARR[this.startMonth].name;
    }
  }
  /**
   * Method setHistoricDataContainer. Method to set the Model Historic Data container
   * @param historicData
   */
  public void setHistoricDataContainer(HistoricStockDataContainer historicData)
  {
    this.historicData = historicData;
  }
  /**
   * This method returns a string from the model's resource bundle.
   * @param key The Key within the resource bundle
   */
  protected String getString(String key)
  {
    String value = null;
    try
    {
      value = getResourceBundle().getString(key);
    }
    catch (MissingResourceException e)
    {
      value = getAppProperties().getProperty(key, null);
    }
    if (value == null)
    {
      System.out.println("java.util.MissingResourceException: " + "Couldn't find value for: " + key);
      System.out.println(
        "The " + key + " key cannot be found in either\n" + "[OptionDataVertical.properties] or [StockApp.ini]");
      value = "Could not find resource: " + key + "  ";
    }
    return value;
  }
  /**
   * Adds any Mouse Listeners to Table
   * @param tableView JTable
   */
  public void addMouseListenersToTable(JTable tableView)
  {
    MouseListener mouseListener = new ColumnListener(tableView);

    JTableHeader header = tableView.getTableHeader();
    header.setUpdateTableInRealTime(true);
    header.addMouseListener(mouseListener);
    header.setReorderingAllowed(false);

    // Adding Mouse Events to Column Movement
    addColumnModelListener(tableView);
  }
  /**
   * Return an ImageIcon by the specified name
   * @param filename String
   * @return ImageIcon
   */
  public ImageIcon createImageIcon(String filename)
  {
    String path = "/resources/images/" + filename;
    System.out.println("Attempting loading of images: " + path);
    return new ImageIcon(getClass().getResource(path));
  }
  /**
   * Returns tThe Column Name, and signifies if the sort order
   * is ascending or descending via <B><I>»</I></B> or the
   * <B><I>«</I></B> symbols
   * @param column int
   * @return String
   */
  public String getColumnName(int column)
  {
    Object obj = headers.elementAt(column);
    String str = (obj != null) ? obj.toString() : "Unknown";
    if (obj instanceof ColumnHeaderData)
    {
      str = ((ColumnHeaderData) obj).getTitle();
    }
    return str;
  }
  /**
   * Returns the The Object located at Row, Column.
   * If the object implments the <B><I>ColumnDataCellValue</I></B> interface
   * a call to the method, <B>getColumnDataCell( nCol )</B> will be made
   * and return the value, otherwise if the data will attempt to pull 
   * the data as a Vector, If there still is no use it will attempt to
   * just return a empty String object.
   * @param nRow int
   * @param nCol int
   * @return Object
   */
  public Object getValueAt(int nRow, int nCol)
  {
    if (nRow < 0 || nRow >= getRowCount())
    {
      return "";
    }

    Object obj = data.elementAt(nRow);
    if (obj instanceof ColumnDataCellValue)
    {
      return (((ColumnDataCellValue) obj).getColumnDataCell(nCol));
    }
    Object cellData = "";
    if (obj instanceof Vector)
    {
      cellData = ((Vector) obj).elementAt(nCol);
    }
    return cellData;
  }
  /**
   * Returns false for all cells
   * @param nRow int
   * @param nCol int
   * @return boolean
   */
  public boolean isCellEditable(int nRow, int nCol)
  {
    boolean rc = false;
    //    if (nCol == 0)
    //    {
    //      OptionData obj = (OptionData) data.elementAt(nRow);
    //      String value = (String) obj.getSymbol().trim();
    //      rc = "".equals(value);
    //    }
    return rc;
  }
  /**
   * Method sets the Textual Value of the Data at Row, Col
   * If the object in in range of available Rows, and is the
   * Symbol section of the OptionData
   * @param Object The Data to set
   * @param nRow The Row in which we are currently at
   * @param nCol The Column, must be the Symbol column
   */
  public void setValueAt(Object value, int nRow, int nCol)
  {
    if ((0 <= nRow && nRow < getRowCount()) && (OptionData.SYMBOLTEXT.startsWith(OptionData.columns[nCol].getTitle())))
    {
      OptionData od = (OptionData) data.elementAt(nRow);
      String tmpValue = value.toString().toUpperCase();

      // Set the Symbol Requested
      od.setSymbol(tmpValue);
      // Now enforce the Entire Row, to be updated,
      // We only want to Update the row for Speed sake.
      for (nCol = 0; nCol < getColumnCount(); nCol++)
      {
        fireTableCellUpdated(nRow, nCol);
      }

    }
    return;
  }

  /**
   * Method excludeOutOfMonthOptions. This method excludes any options in the 
   * data vector that are not valid for the month being requested.
   */
  private void excludeOutOfMonthOptions()
  {
    if (data != null && data.size() > 1)
    {
      Vector temp = new Vector();
      for (int i = 0; i < data.size(); i++)
      {
        Object obj = data.elementAt(i);
        if (obj instanceof OptionData)
        {
          OptionData optionData = (OptionData) obj;
          if (StockMarketUtils.isOptionInMonthRange(optionData.getSymbol(), this.validMonthOptions))
          {
            temp.addElement(optionData);
          }
        }
      }

      data.removeAllElements();
      data.addAll(temp);
    }
  }
  /**
   * Method sortVerticalOptionData. This method breaks the options up into
   * the associated call and puts then merges the sorted lists 
   * Calls are first - puts are second
   * @see StockMarketUtils#getSetOfCallOptions
   * @see StockMarketUtils#getSetOfPutOptions
   * @param list A Vector of OptionData objects
   */
  public void sortVerticalOptionData()
  {
    if (data != null && data.size() > 1)
    {
      // Exlcude any Options that are not within our target Month
      excludeOutOfMonthOptions();
      // Split our options into Calls and Puts
      Vector calls = StockMarketUtils.getSetOfCallOptions(data, 1);
      Vector puts = StockMarketUtils.getSetOfPutOptions(data, 1);
      // Sort the Collection independently
      Collections.sort(calls, new OptionDataVerticalComparator());
      // Sort the Collection independently
      Collections.sort(puts, new OptionDataVerticalComparator());
      // Remove all the data that was previously in the data vector.
      // because we are going to add all the information back in a 
      // sorted list with CALLS on top, and PUTS on bottom
      data.removeAllElements();

      data.addAll(calls);
      data.addAll(puts);

      TimerTask tabPaneTask = new OptionDataRulesTask(calls, puts);
      Timer timer = new Timer(true); // Deamon
      timer.schedule(tabPaneTask, 100); // Short Time to start 1/10th of a second
    }
  }

  /**
   * Method setCallSpreadValues. This method will take the Vector of Put values
   * And iterate through them to to adjust there various bid ask prices
   * between vertical put spreads.
   * @param calls A Vector of Call OptionData
   */
  private void setCallSpreadValues(Vector calls)
  {
    // Loop through all the calls staarting with the AT THE MONEY CALL
    for (int i = calls.size() - 1; i > 0; i--)
    {
      // Get the element, if it is null or not a call continue to the next
      OptionData targetOption = (OptionData) calls.elementAt(i);
      if ((targetOption == null) || (targetOption.getOptionTypeValue() != StockMarketUtils.CALL))
      {
        continue;
      }
      // Reset Any Paired Values, it may have had previously, as they can change
      // with every iteration through the day.
      targetOption.resetPairedValues();

      // Get Ready to Ruuummmmmble, Decalre some optiondata variables as holders
      OptionData option1 = null;
      OptionData option2 = null;
      OptionData option3 = null;
      OptionData option4 = null;
      // Now we want to loop through all the remaining. and compare to the current.
      for (int j = i - 1, count = 0;(j >= 0) && (count < 4); j--, count++)
      {
        switch (count)
        {
          case 0 :
            option1 = (OptionData) calls.elementAt(j);
            break;
          case 1 :
            option2 = (OptionData) calls.elementAt(j);
            break;
          case 2 :
            option3 = (OptionData) calls.elementAt(j);
            break;
          case 3 :
            option4 = (OptionData) calls.elementAt(j);
            break;
          default :
            break;
        }
      }
      // Now we should have three values, lets process
      double ask1 = targetOption.getAsk().doubleValue();
      double bid1 = targetOption.getBid().doubleValue();
      double targetStrike = targetOption.getStrikePrice().doubleValue();
      double option1Diff = (option1 != null) ? option1.getStrikePrice().doubleValue() - targetStrike : 0;
      double option2Diff = (option2 != null) ? option2.getStrikePrice().doubleValue() - targetStrike : 0;
      double option3Diff = (option3 != null) ? option3.getStrikePrice().doubleValue() - targetStrike : 0;
      double option4Diff = (option4 != null) ? option4.getStrikePrice().doubleValue() - targetStrike : 0;

      // Option 1: 
      // $2.50 Strike Difference or
      if (option1Diff == 2.5)
      {
        targetOption.set2_50Pair(option1);
      }
      // $5.00 Strike Difference or
      else if (option1Diff == 5.0)
      {
        targetOption.set5_00Pair(option1);
      }
      // $10.00 Strike Difference
      else if (option1Diff == 10.0)
      {
        targetOption.set10_0Pair(option1);
      }

      // Option 2: 
      // $5.00 Strike Difference or
      if (option2Diff == 5)
      {
        targetOption.set5_00Pair(option2);
      }
      // $10.00 Strike Difference
      else if (option2Diff == 10.0)
      {
        targetOption.set10_0Pair(option2);
      }

      // Option 3: 
      // $10.00 Strike Difference only
      if (option3Diff == 10.0)
      {
        targetOption.set10_0Pair(option3);
      }
      // Option 4: 
      // $10.00 Strike Difference only
      if (option4Diff == 10.0)
      {
        targetOption.set10_0Pair(option4);
      }

    }
  }
  /**
   * Method setPutSpreadValues. This method will take the Vector of Put values
   * And iterate through them to to adjust there various bid ask prices
   * between vertical put spreads.
   * @param outs A Vector of Put OptionData
   */
  private void setPutSpreadValues(Vector puts)
  {
    // Loop through all the calls staarting with the AT THE MONEY CALL
    for (int i = 0; i < puts.size() - 1; i++)
    {
      // Get the element, if it is null or not a call continue to the next
      OptionData targetOption = (OptionData) puts.elementAt(i);
      if ((targetOption == null) || (targetOption.getOptionTypeValue() != StockMarketUtils.PUT))
      {
        continue;
      }
      // Reset Any Paired Values, it may have had previously, as they can change
      // with every iteration through the day.
      targetOption.resetPairedValues();

      // Get Ready to Ruuummmmmble, Decalre some optiondata variables as holders
      OptionData option1 = null;
      OptionData option2 = null;
      OptionData option3 = null;
      OptionData option4 = null;
      // Now we want to loop through all the remaining. and compare to the current.
      for (int j = i + 1, count = 0;(j < puts.size()) && (count < 4); j++, count++)
      {
        switch (count)
        {
          case 0 :
            option1 = (OptionData) puts.elementAt(j);
            break;
          case 1 :
            option2 = (OptionData) puts.elementAt(j);
            break;
          case 2 :
            option3 = (OptionData) puts.elementAt(j);
            break;
          case 3 :
            option4 = (OptionData) puts.elementAt(j);
            break;
          default :
            break;
        }
      }
      // Now we should have three values, lets process
      double ask1 = targetOption.getAsk().doubleValue();
      double bid1 = targetOption.getBid().doubleValue();
      double targetStrike = targetOption.getStrikePrice().doubleValue();
      double option1Diff = (option1 != null) ? targetStrike - option1.getStrikePrice().doubleValue() : 0;
      double option2Diff = (option2 != null) ? targetStrike - option2.getStrikePrice().doubleValue() : 0;
      double option3Diff = (option3 != null) ? targetStrike - option3.getStrikePrice().doubleValue() : 0;
      double option4Diff = (option4 != null) ? targetStrike - option4.getStrikePrice().doubleValue() : 0;

      // Compare the range for each however, only the first will be a 2.50 strike
      // the rest are $5 or $10 difference

      // Option 1: 
      // $2.50 Strike Difference or
      if (option1Diff == 2.5)
      {
        targetOption.set2_50Pair(option1);
      }
      // $5.00 Strike Difference or
      else if (option1Diff == 5.0)
      {
        targetOption.set5_00Pair(option1);
      }
      // $10.00 Strike Difference
      else if (option1Diff == 10.0)
      {
        targetOption.set10_0Pair(option1);
      }

      // Option 2: 
      // $5.00 Strike Difference or
      if (option2Diff == 5)
      {
        targetOption.set5_00Pair(option2);
      }
      // $10.00 Strike Difference
      else if (option2Diff == 10.0)
      {
        targetOption.set10_0Pair(option2);
      }

      // Option 3: 
      // $10.00 Strike Difference only
      if (option3Diff == 10.0)
      {
        targetOption.set10_0Pair(option3);
      }
      // Option 4: 
      // $10.00 Strike Difference only
      if (option4Diff == 10.0)
      {
        targetOption.set10_0Pair(option4);
      }

    }
  }

  /**
   * Method processVerticalSpreadEntranceCriteria. This method will process the data vector
   * for either call or put spreads.  It is absolutly neccesary to have the spreadData be
   * all CALLS or all PUTS, they cannot be mixed.
   * @param spreadData
   * @param type
   */
  private synchronized void processVerticalSpreadEntranceCriteria(Vector spreadData, int type)
  {
    // First lets elimate the calendar checks.  We want to ensure to ensure
    // we have suffient days until expiration Friday
    GregorianCalendar now = new GregorianCalendar();
    if (this.startMonth == now.get(Calendar.MONTH))
    { // Only if the same month do we need to check any other month will be in the future
      int days = StockMarketUtils.daysUntilExpiration(now.getTime(), true);
      int minDays = ParseData.parseNum(this.getString("VerticalSpread.minimum_trading_days"), 1);
      if (days < minDays)
      { // Minimum days are not met, Return and do nothing.
        return;
      }
    }
    // Now do we have valid types and valid vector data
    if (type == CALL && spreadData != null)
    {
      processVerticalBullCallSpreads(spreadData, SPREAD_2_50);
      processVerticalBullCallSpreads(spreadData, SPREAD_5_00);
      processVerticalBullCallSpreads(spreadData, SPREAD_10_00);
      
      processVerticalBearCallSpreads(spreadData, SPREAD_2_50);
      processVerticalBearCallSpreads(spreadData, SPREAD_5_00);
      processVerticalBearCallSpreads(spreadData, SPREAD_10_00);
    }
    else if (type == PUT && spreadData != null)
    {
      processVerticalBearPutSpreads(spreadData, SPREAD_2_50);
      processVerticalBearPutSpreads(spreadData, SPREAD_5_00);
      processVerticalBearPutSpreads(spreadData, SPREAD_10_00);
    }
  }
  /**
   * Method processVerticalBullCallSpreads. Method to process the BULL CALL spreads 
   * for each of the CALLS based upon the spreadType.  This is a Bullish approach
   * for stocks. Meaning we buy the call for the stock to go up.
   * @param spreadData
   * @param spreadType
   */
  private void processVerticalBullCallSpreads(Vector spreadData, int spreadType)
  {
    // If our Spread Data is null or empty  - Do Nothing.
    if ((spreadData == null) || (spreadData.size() < 1) || (historicData == null))
    {
      return;
    }
    StockData sd = ((OptionData) spreadData.elementAt(0)).getStockData();
    final int STRATEGY_TYPE = StockMarketUtils.STRATEGY_BULL_CALL_SPREAD;
    // 1. Collect Resource values 
    final double MAX_SPREAD_PRICE = getMaxSpreadPrice("BullCallSpread", spreadType);
    final double FAIR_MARKET_FACTOR = getFairMarketFactor("BullCallSpread", spreadType);
    final boolean EXCLUDE_IN_THE_MONEY = ParseData.parseBool(getString("BullCallSpread.exclude_in_the_money_options"), false);

    // Calculate the number of days and the expected movement of the stock
    boolean strictDayCount = ParseData.parseBool(getString("BullCallSpread.strict_day_count"), false);
    int days = StockMarketUtils.daysUntilExpiration(startMonth, strictDayCount);
    final double EXPECTED_MOVEMENT = StockMarketUtils.getExpectedMovement(sd, days);

    // Iterate through the List
    for (int i = 1; i < spreadData.size(); i++)
    {
      OptionData buyOption = (OptionData) spreadData.elementAt(i);
      OptionData sellOption = getSpreadPair(buyOption, spreadType);
      // 2. No pair captured then do not bother.
      //    If our sell Option pair is not avaible then simply continue the loop 
      //    to process the next in the list.
      if (sellOption == null)
      {
        continue; // We do not have a Sell Option Pair
      }
      // Do we need to exclude any options that are in the Money
      if (EXCLUDE_IN_THE_MONEY)
      { // Yes we do, check for range. Calls Are Bullish in nature
        // Therefore the lastPrice is greater than the Strike Price is 
        // do not process. 
        double lastPrice = sd.getLast().doubleValue();
        double strikePrice = buyOption.getStrikePrice().doubleValue();
        if (lastPrice > strikePrice)
        {
          continue; // Do Not Process Call Options in the Money
        }
      }
      // 3.a. Get the Option - One more out of the Money
      //      BuyOption.askPrice - tempOption.askPrice / 2 + FAIR_MARKET_FACTOR
      //      default our ask price
      double tempAsk = sellOption.getAsk().doubleValue() * 0.50;
      OptionData tempOption = getSpreadPair(sellOption, spreadType);
      if ( tempOption != null )
      {
        tempAsk = tempOption.getAsk().doubleValue();
      }
      // 3.b. Calcualte what the fair price would be for the trade
      double fairPrice = (((buyOption.getAsk().doubleValue() - tempAsk) / 2) + FAIR_MARKET_FACTOR);
      if( fairPrice > MAX_SPREAD_PRICE )
      {
        continue;
      }
      // 4. Calculate the openInteres differences
      //    First get the combined open interest of both options and add them together
      //    Then, divide the total open interest by the minimum open interest factor
      //    recommended is a factor of 40
      //    followed by checking if the quotient is less than the default number of contracts. i.e. 1 contract
      long combinedOpenInterest = buyOption.getOpenInterest().longValue() + sellOption.getOpenInterest().longValue();
      double minQuotient = combinedOpenInterest / MIN_OPEN_INTEREST_FACTOR;

      if (minQuotient < DEFAULT_MIN_CONTRACTS)
      {
        continue; // We are under contine processing on the next option, this fails here
      }

      // 5.a.  Calcualte the differences in Ask Prices betwwen the two options
      double askDiff = Math.abs(buyOption.getAsk().doubleValue() - sellOption.getAsk().doubleValue());
      // 5.b.  Get the next option further out of the money
      tempOption = getSpreadPair(sellOption, spreadType);
      double aboveDiff = askDiff * 0.50; // Default it to 1/2 of the current askDiff
      if (tempOption != null)
      {
        aboveDiff = Math.abs(tempOption.getAsk().doubleValue() - sellOption.getAsk().doubleValue());
      }
      // 5.c. Get the next option closer to the Money
      double belowDiff = askDiff * 2; // Default to 2 times the current askDiff
      if ((i + 1) < spreadData.size())
      {
        tempOption = (OptionData) spreadData.elementAt(i + 1);
        belowDiff = Math.abs(buyOption.getAsk().doubleValue() - tempOption.getAsk().doubleValue());
      }
      // 6. Validate our reward factor. Calculate gain and loss from step 5
      //    Followed by the calculating the Risk Reward Factor ( default 1.8 )  If the reward is less
      //    than the default then it should be ignored.
      double gain = askDiff - aboveDiff;
      double loss = askDiff - belowDiff;
      if (loss == 0)
      {
        continue; // Undefined.
      }
      double rewardFactor = gain / loss;

      if (rewardFactor < DEFAULT_REWARD_FACTOR)
      {
        continue; // Another Elimination
      }
      // 7.a. Min Movement validation - Can the stock HISOTRICALLY move the distance required?
      //      Has the stock moved in an amount equal to or greater than the amount needed for
      //      the spread to reach maximum value at expiration. 
      //      RequiredMovement = sellOptionStrikePrice - lastPrice
      double lastPrice = sd.getLast().doubleValue();
      double sellOptionStrike = sellOption.getStrikePrice().doubleValue();
      double requiredMovement = sellOptionStrike - lastPrice;
      Vector mmVector = this.historicData.getMonthlyMovementVector();
      int iterationCount = 0;
      boolean canMove = true; // Assume it can move, if it fails this flag will be false.
      for (int j = mmVector.size() - 1; j >= 0 && iterationCount < MINIMUM_MOVEMENT_PERIODS; j--)
      {
        double movement = ((MonthlyMovement) mmVector.elementAt(j)).getMovement();
        canMove &= (movement >= requiredMovement);
        iterationCount++;
      }
      // Did we pass all iterations - If we had any?
      if ((canMove == false) || (mmVector.size() < 1))
      {
        continue; // Guess not, Continue on to next candidate.
      }

      // 7.b. Expected Movement Validation. The expected Movement on the stock should be equal to
      //      or greater than the minimum required movement needed to reach the maximum profit 
      //      at expiration
      if (EXPECTED_MOVEMENT < requiredMovement)
      {
        continue;
      }

      // 7.c. New Highs validation, Does the Stock need to reach new highs to make the grade
      boolean newHighNeeded = (NEW_HIGH_VALIDATION_REQUIRED && (sellOptionStrike > sd.get52WeekHigh().doubleValue()));
      // 8. We have a winner
      buyOption.setVerticalSpreadStrategy(spreadType, STRATEGY_TYPE, newHighNeeded);
    }
  }
  /**
   * Method processVerticalBearPutSpreads. Method to process the BEAR PUT spreads 
   * for each of the PUTS based upon the spreadType.  This is a bearish approach to
   * stocks.  Meaning we buy the put for the stock to go down
   * @param spreadData
   * @param spreadType
   */
  private void processVerticalBearPutSpreads(Vector spreadData, int spreadType)
  {
    // If our Spread Data is null or empty  - Do Nothing.
    if ((spreadData == null) || (spreadData.size() < 1) || (historicData == null))
    {
      return;
    }
    StockData sd = ((OptionData) spreadData.elementAt(0)).getStockData();
    final int STRATEGY_TYPE = StockMarketUtils.STRATEGY_BEAR_PUT_SPREAD;
    // 1. Collect Resource values 
    final double MAX_SPREAD_PRICE = getMaxSpreadPrice("BearPutSpread", spreadType);
    final double FAIR_MARKET_FACTOR = getFairMarketFactor("BearPutSpread", spreadType);
    final boolean EXCLUDE_IN_THE_MONEY = ParseData.parseBool(getString("BearPutSpread.exclude_in_the_money_options"), false);

    // Calculate the number of days and the expected movement of the stock
    boolean strictDayCount = ParseData.parseBool(getString("BearPutSpread.strict_day_count"), false);
    int days = StockMarketUtils.daysUntilExpiration(startMonth, strictDayCount);
    final double EXPECTED_MOVEMENT = StockMarketUtils.getExpectedMovement(sd, days);

    // Iterate through the List
    for (int i = 0; i < spreadData.size() - 1; i++)
    {
      Object obj = spreadData.elementAt(i);
      OptionData buyOption = (OptionData) obj;
      OptionData sellOption = getSpreadPair(buyOption, spreadType);
      // 2. No pair captured then do not bother.
      //    If our sell Option pair is not avaible then simply continue the loop 
      //    to process the next in the list.
      if (sellOption == null)
      {
        continue; // We do not have a Sell Option Pair
      }
      // Do we need to exclude any options that are in the Money
      if (EXCLUDE_IN_THE_MONEY)
      { // Yes we do, check for range. Puts Are Bearish in nature
        // Therefore the lastPrice is less than the Strike Price is 
        // do not process. 
        double lastPrice = sd.getLast().doubleValue();
        double strikePrice = buyOption.getStrikePrice().doubleValue();
        if (lastPrice < strikePrice)
        {
          continue; // Do Not Process Call Options in the Money
        }
      }
      // 3.a. Get the Option - One more out of the Money
      //      BuyOption.askPrice - tempOption.askPrice / 2 + FAIR_MARKET_FACTOR
      //      default our ask price
      double tempAsk = sellOption.getAsk().doubleValue() * 0.50;
      OptionData tempOption = getSpreadPair(sellOption, spreadType);
      if ( tempOption != null )
      {
        tempAsk = tempOption.getAsk().doubleValue();
      }
      // 3.b. Calcualte what the fair price would be for the trade
      double fairPrice = (((buyOption.getAsk().doubleValue() - tempAsk) / 2) + FAIR_MARKET_FACTOR);
      if( fairPrice > MAX_SPREAD_PRICE )
      {
        continue; // too much money don't do it
      }
      // 4. Calculate the openInteres differences
      //    First get the combined open interest of both options and add them together
      //    Then, divide the total open interest by the minimum open interest factor
      //    recommended is a factor of 40
      //    followed by checking if the quotient is less than the default number of contracts. i.e. 1 contract
      long combinedOpenInterest = buyOption.getOpenInterest().longValue() + sellOption.getOpenInterest().longValue();
      double minQuotient = combinedOpenInterest / MIN_OPEN_INTEREST_FACTOR;

      if (minQuotient < DEFAULT_MIN_CONTRACTS)
      {
        continue; // We are under contine processing on the next option, this fails here
      }

      // 5.a.  Calcualte the differences in Ask Prices betwwen the two options
      double askDiff = Math.abs(buyOption.getAsk().doubleValue() - sellOption.getAsk().doubleValue());
      // 5.b.  Get the next option further out of the money
      tempOption = getSpreadPair(sellOption, spreadType);
      double aboveDiff = askDiff * 0.50; // Default it to 1/2 of the current askDiff
      if (tempOption != null)
      {
        aboveDiff = Math.abs(tempOption.getAsk().doubleValue() - sellOption.getAsk().doubleValue());
      }
      // 5.c. Get the next option closer to the Money
      double belowDiff = askDiff * 2; // Default to 2 times the current askDiff
      if ((i - 1) >= 0)
      {
        tempOption = (OptionData) spreadData.elementAt(i - 1);
        belowDiff = Math.abs(buyOption.getAsk().doubleValue() - tempOption.getAsk().doubleValue());
      }
      // 6. Validate our reward factor. Calculate gain and loss from step 5
      //    Followed by the calculating the Risk Reward Factor ( default 1.8 )  If the reward is less
      //    than the default then it should be ignored.
      double gain = askDiff - aboveDiff;
      double loss = askDiff - belowDiff;
      if (loss == 0)
      {
        continue; // Undefined.
      }
      double rewardFactor = gain / loss;

      if (rewardFactor < DEFAULT_REWARD_FACTOR)
      {
        continue; // Another Elimination
      }

      // 7.a. Min Movement validation - Can the stock HISOTRICALLY move the distance required?
      //      Has the stock moved in an amount equal to or greater than the amount needed for
      //      the spread to reach maximum value at expiration. 
      //      RequiredMovement = sellOptionStrikePrice - lastPrice
      double lastPrice = sd.getLast().doubleValue();
      double sellOptionStrike = sellOption.getStrikePrice().doubleValue();
      double requiredMovement = lastPrice - sellOptionStrike;
      Vector mmVector = this.historicData.getMonthlyMovementVector();
      int iterationCount = 0;
      boolean canMove = true; // Assume it can move, if it fails this flag will be false.
      for (int j = mmVector.size() - 1; j >= 0 && iterationCount < MINIMUM_MOVEMENT_PERIODS; j--)
      {
        double movement = ((MonthlyMovement) mmVector.elementAt(j)).getMovement();
        canMove &= (movement >= requiredMovement);
        iterationCount++;
      }
      // Did we pass all iterations - If we had any?
      if ((canMove == false) || ( mmVector.size() < 1))
      {
        continue; // Guess not, Continue on to next candidate.
      }

      // 7.b. Expected Movement Validation. The expected Movement on the stock should be equal to
      //      or greater than the minimum required movement needed to reach the maximum profit 
      //      at expiration
      if (EXPECTED_MOVEMENT < requiredMovement)
      {
        continue;
      }

      // 7.c. New Lows validation, Does the Stock need to reach new lows to make the grade
      boolean newLowsNeeded = (NEW_LOWS_VALIDATION_REQUIRED && (sellOptionStrike < sd.get52WeekLow().doubleValue()));
      // 8. We have a winner
      buyOption.setVerticalSpreadStrategy(spreadType, STRATEGY_TYPE, newLowsNeeded);
    }
  }
  /**
   * Method processVerticalBearCallSpreads. Method to process the BEAR CALL spreads 
   * for each of the CALLS based upon the spreadType.  This strategy is used for
   * Bearish on  stock. MEaning that we sell the spread for the stock to go down
   * @param spreadData
   * @param spreadType
   */
  private void processVerticalBearCallSpreads(Vector spreadData, int spreadType)
  {
    // If our Spread Data is null or empty  - Do Nothing.
    if ((spreadData == null) || (spreadData.size() < 1) || (historicData == null))
    {
      return;
    }
    StockData sd = ((OptionData) spreadData.elementAt(0)).getStockData();
    final int STRATEGY_TYPE = StockMarketUtils.STRATEGY_BEAR_CALL_SPREAD;
    // 1. Collect Resource values 
    final double SHAVE_PERCENT = ParseData.parseNum(getString("BearCallSpread.shave_percent"),0.0);
    final double MAX_SPREAD_PRICE = getMaxSpreadPrice("BearCallSpread", spreadType);
    final boolean EXCLUDE_IN_THE_MONEY = ParseData.parseBool(getString("BearCallSpread.exclude_in_the_money_options"), false);
    final boolean TEST_MAX_RISK = ParseData.parseBool(getString("BearCallSpread.test_max_risk_potential"), false);
    // Calculate the number of days and the expected movement of the stock
    boolean strictDayCount = ParseData.parseBool(getString("BearCallSpread.strict_day_count"), false);
    int days = StockMarketUtils.daysUntilExpiration(startMonth, strictDayCount);
    final double EXPECTED_MOVEMENT = StockMarketUtils.getExpectedMovement(sd, days);

    // Iterate through the List
    for (int i = 1; i < spreadData.size(); i++)
    {
      // Here we want to sell the target Option and the Pair is buy
      OptionData sellOption = (OptionData) spreadData.elementAt(i);
      OptionData buyOption = getSpreadPair(sellOption, spreadType);
      // 2. No pair captured then do not bother.
      //    If our sell Option pair is not avaible then simply continue the loop 
      //    to process the next in the list.
      if (buyOption == null)
      {
        continue; // We do not have a Sell Option Pair
      }
      // Do we need to exclude any options that are in the Money
      if (EXCLUDE_IN_THE_MONEY)
      { // Yes we do, check for range. Selling Calls Are Bearish in nature
        // Therefore the lastPrice is greater than the Strike Price is 
        // do not process. 
        double lastPrice = sd.getLast().doubleValue();
        double strikePrice = sellOption.getStrikePrice().doubleValue();
        if (lastPrice > strikePrice)
        {
          continue; // Do Not Process Call Options in the Money
        }
      }
      // 3. Calculate the openInteres differences
      //    First get the combined open interest of both options and add them together
      //    Then, divide the total open interest by the minimum open interest factor
      //    recommended is a factor of 40
      //    followed by checking if the quotient is less than the default number of contracts. i.e. 1 contract
      long combinedOpenInterest = buyOption.getOpenInterest().longValue() + sellOption.getOpenInterest().longValue();
      double minQuotient = combinedOpenInterest / MIN_OPEN_INTEREST_FACTOR;

      if (minQuotient < DEFAULT_MIN_CONTRACTS)
      {
        continue; // We are under contine processing on the next option, this fails here
      }
      // 4.a. Get the Difference in the Spread. 
      double spreadDiff = StockMarketUtils.calculateSpreadDifference( buyOption, sellOption );
      // 4.b. Add the shaving value to the bid price.
      double offerPrice = sellOption.getBid().doubleValue() + ( spreadDiff * SHAVE_PERCENT );
      // 5.a. Min Movement validation - Can the stock HISOTRICALLY move the distance required?
      //      Has the stock moved in an amount equal to or greater than the amount needed for
      //      the spread to reach maximum value at expiration. 
      //      RequiredMovement = (sellOptionStrikePrice + offerPrice) - lastPrice
      double lastPrice = sd.getLast().doubleValue();
      double sellOptionStrike = sellOption.getStrikePrice().doubleValue();
      double buyOptionStrike  = buyOption.getStrikePrice().doubleValue();
      
      final double requiredMovement;
      if ( TEST_MAX_RISK )
      {
        requiredMovement = buyOptionStrike - lastPrice;
      }
      else
      {
        requiredMovement = (sellOptionStrike + offerPrice) - lastPrice;
      }
      Vector mmVector = this.historicData.getMonthlyMovementVector();
      int iterationCount = 0;
      boolean canStay = true; // Assume it cannot move, if it fails this flag will be false.
      for (int j = mmVector.size() - 1; j >= 0 && iterationCount < MINIMUM_MOVEMENT_PERIODS; j--)
      {
        double movement = ((MonthlyMovement) mmVector.elementAt(j)).getMovement();
        canStay &= (movement < requiredMovement);
        iterationCount++;
      }
      // 5.b. Did we pass all iterations - If any exist
      if ((canStay == false ) || ( mmVector.size() < 1 ))
      {
        continue; // Guess not, Continue on to next candidate.
      }
      
      // 6. Calcualte the Break Even Point of the Selling the Spread.
      //    This is a Bear Call Spread so we add it to the Short Option
      double breakEvenPoint = sellOptionStrike + offerPrice;
      double breakEvenDelta = StockMarketUtils.getBreakEvenDelta(buyOption, sellOption, breakEvenPoint, spreadType);
      double BEP_spreadWidth = Math.abs(breakEvenDelta) * StockMarketUtils.getSpreadWidth(spreadType);
      
      if ( BEP_spreadWidth > offerPrice )
      {
        continue;  // Another Loser.  
      }
      
      // 7. New Highs validation, Does the Stock need to reach new highs to make the grade
      boolean newHighsNeeded = (NEW_HIGH_VALIDATION_REQUIRED && (sellOptionStrike > sd.get52WeekHigh().doubleValue()));
      sellOption.setVerticalSpreadStrategy(spreadType, STRATEGY_TYPE, newHighsNeeded);
    }
  }
  /**
   * Method getSpreadPair. Provided the base OptionData it will return the paired option based on
   * spread type
   * @param baseOption The option being evaluated
   * @param spreadType The spread Type, $2.50, $5.00 or $10.00
   * @return OptionData The Paired option, can be null
   */
  private OptionData getSpreadPair(OptionData baseOption, int spreadType)
  {
    OptionData optionPair = null;
    if (baseOption != null)
    {
      switch (spreadType)
      {
        case SPREAD_2_50 :
          optionPair = baseOption.get2_50Pair();
          break;
        case SPREAD_5_00 :
          optionPair = baseOption.get5_00Pair();
          break;
        case SPREAD_10_00 :
          optionPair = baseOption.get10_0Pair();
          break;
      }
    }
    return optionPair;
  }

  /**
   * Method getFairMarketFactor. Returns the Fair Market Factor Based on the SpreadType
   * @param stragedy String detailing the Stragedy
   * @param spreadType int The type of Spread, $2.50, $5.00, or $10.00
   * @return double The value 
   */
  private double getFairMarketFactor(String stragedy, int spreadType)
  {
    double fairMarketFactor = 10.0;
    switch (spreadType)
    {
      case SPREAD_2_50 :
        fairMarketFactor = ParseData.parseNum(getString(stragedy + ".2.50.fair_market_factor"), 10.0);
        break;
      case SPREAD_5_00 :
        fairMarketFactor = ParseData.parseNum(getString(stragedy + ".5.00.fair_market_factor"), 10.0);
        break;
      case SPREAD_10_00 :
        fairMarketFactor = ParseData.parseNum(getString(stragedy + ".10.00.fair_market_factor"), 10.0);
        break;
    }

    return fairMarketFactor;
  }
  /**
   * Method getMaxSpreadPrice. Returns the MAx Spread Price to pay Based on the SpreadType
   * @param stragedy String detailing the Stragedy
   * @param spreadType int The type of Spread, $2.50, $5.00, or $10.00
   * @return double The value 
   */
  private double getMaxSpreadPrice(String stragedy, int spreadType)
  {
    double maxSpreadPrice = 10.0;
    switch (spreadType)
    {
      case SPREAD_2_50 :
        maxSpreadPrice = ParseData.parseNum(getString(stragedy + ".2.50.max_price"), 10.0);
        break;
      case SPREAD_5_00 :
        maxSpreadPrice = ParseData.parseNum(getString(stragedy + ".5.00.max_price"), 10.0);
        break;
      case SPREAD_10_00 :
        maxSpreadPrice = ParseData.parseNum(getString(stragedy + ".10.00.max_price"), 10.0);
        break;
    }
    return maxSpreadPrice;
  }
}
