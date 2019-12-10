// HistoricStockData.java
package com.stockmarket.data;

import java.io.Serializable;
import java.util.*;
import javax.swing.*;
import com.hgtable.*;
import com.hgutil.*;
import com.hgutil.data.*;

/**
 * Class Object to maintain Historical Stock Info
 * @author Hans-Jurgen Greiner
 */
public class HistoricStockData implements ColumnDataCellValue, Comparable, Serializable
{
  protected HGCalendar date = null;
  protected String origDateSz = null;
  protected Fraction openPrice = null;
  protected Fraction highPrice = null;
  protected Fraction lowPrice = null;
  protected Fraction closePrice = null;
  protected LongValue volume = null;
  protected MonthlyMovement monthlyMovement = null;

  private java.awt.Color lineColor = java.awt.Color.red;

  static final public transient String DATETEXT = "Date";
  static final public transient ColumnHeaderData columns[] =
    {
      new ColumnHeaderData(DATETEXT, 100, JLabel.LEFT),
      new ColumnHeaderData("Open", 160, JLabel.LEFT),
      new ColumnHeaderData("High", 100, JLabel.RIGHT),
      new ColumnHeaderData("Low", 100, JLabel.RIGHT),
      new ColumnHeaderData("Close", 100, JLabel.RIGHT),
      new ColumnHeaderData("Volume", 100, JLabel.RIGHT)};

  
  static public transient boolean DEBUG = ParseData.parseBool(System.getProperties().getProperty("HGDEBUG"), false);
  /**
   * HistoricStockInfo constructor comment.
   */
  public HistoricStockData()
  {
    super();
    setDate("");
    setOpenPrice("");
    setHighPrice("");
    setLowPrice("");
    setClosePrice("");
    setVolume("");
  }
  /**
   * HistoricStockInfo constructor comment.
   */
  public HistoricStockData(String[] titles, String[] data)
  {
    super();
    loadDataByTitles(titles, data);
  }
  /**
   * HistoricStockInfo constructor comment.
   */
  public HistoricStockData(String[] titles, String dataLine)
  {
    super();
    String[] data = ParseData.parseString(dataLine, ",");
    loadDataByTitles(titles, data);
  }
  /**
   * HistoricStockInfo constructor comment.
   */
  public HistoricStockData(HistoricStockData obj)
  {
    super();
    setDate(obj.getDateSz());
    setOpenPrice(obj.getOpenPrice());
    setHighPrice(obj.getHighPrice());
    setLowPrice(obj.getLowPrice());
    setClosePrice(obj.getClosePrice());
    setVolume(obj.getVolume());
    setLineColor(obj.getOpen(), obj.getClose());
  }
  /**
   * HistoricStockInfo constructor comment.
   */
  public HistoricStockData(
    String dateField,
    String openQuote,
    String highQuote,
    String lowQuote,
    String closeQuote,
    String volumeQuote)
  {
    super();

    setOpenPrice(openQuote);
    setHighPrice(highQuote);
    setLowPrice(lowQuote);
    setClosePrice(closeQuote);
    setVolume(volumeQuote);
    setDate(dateField);
    setLineColor(openPrice, closePrice);
  }
  /**
   * HistoricStockInfo constructor comment.
   */
  public HistoricStockData(
    Date dateField,
    String openQuote,
    String highQuote,
    String lowQuote,
    String closeQuote,
    String volumeQuote)
  {
    super();
    setDate(dateField);
    setOpenPrice(openQuote);
    setHighPrice(highQuote);
    setLowPrice(lowQuote);
    setClosePrice(closeQuote);
    setVolume(volumeQuote);
    setLineColor(openPrice, closePrice);
  }
  /**
   * Comparison compare to method
   * @param obj Object
   * @return int
   */
  public int compareTo(Object obj)
  {
    int rc = -1;

    Date tmpDate = null;
    if (obj instanceof HistoricStockData)
    {
      HistoricStockData tmpStockData = (HistoricStockData) obj;
      tmpDate = tmpStockData.getDate();
    }
    else if (obj instanceof String)
    {
      tmpDate = (Date) ParseData.parseDate((String) obj);
    }
    else if (obj == null)
    {
      return rc;
    }
    rc = date.getTime().compareTo(tmpDate);
    return (rc);
  }
  /**
   * Utility Method to create a Date Object from three values
   * This method does not retain any data
   * @param year int representing the year
   * @param month int representing the Month of the Year
   * @param day int representing the day of the Month
   * @return Date The Date Object it had created
   */
  private Date createDate(int year, int month, int day)
  {
    GregorianCalendar calendar = new GregorianCalendar(year, month, day, 0, 1, 0);
    return calendar.getTime();
  }
  /**
   * Comparison compare to method
   * @param obj Object
   * @return int
   */
  public boolean equals(Object obj)
  {
    boolean rc = false;

    Date tmpDate = null;
    if (obj instanceof HistoricStockData)
    {
      HistoricStockData tmpStockData = (HistoricStockData) obj;
      tmpDate = tmpStockData.getDate();
    }
    else if (obj instanceof String)
    {
      tmpDate = (Date) ParseData.parseDate((String) obj);
    }
    else if (obj == null)
    {
      return rc;
    }
    rc = date.getTime().equals(tmpDate);
    return (rc);
  }
  /**
   * Method returns the Close Price Value of the Stock
   * @return Fraction
   */
  public Fraction getClose()
  {
    return closePrice;
  }
  /**
   * Method returns the Close Price Value of the Stock as a String
   * @return java.lang.String The Close Price
   */
  public String getClosePrice()
  {
    return closePrice.toString();
  }
  /**
   * Return Object to be displyed in the Column
   * @param col int
   * @return Object
   */
  public Object getColumnDataCell(int col)
  {

    Object obj = "";
    switch (col)
    {
      case 0 :
        obj = getGregorianCalendar();
        break;
      case 1 :
        obj = getOpen();
        break;
      case 2 :
        obj = getHigh();
        break;
      case 3 :
        obj = getLow();
        break;
      case 4 :
        obj = getClose();
        break;
      case 5 :
        obj = getVolume();
        break;
      default :
        obj = "";
        break;
    }
    return obj;
  }
  /**
   * Returns a Date object of the Date Object being stored
   * @return java.lang.String
   */
  public Date getDate()
  {
    return date.getTime();
  }
  /**
   * Insert the method's description here.
   * @return java.lang.String
   */
  public String getDateSz()
  {
    return this.origDateSz;
  }
  /**
   * Returns a Calendar of the Date Object being stored
   * @return java.util.GregorianCalendar
   */
  public GregorianCalendar getGregorianCalendar()
  {
    return date;
  }
  /**
   * Method returns the High Price Value of the Stock
   * @return double
   */
  public Fraction getHigh()
  {
    return highPrice;
  }
  /**
   * Method returns the High Price Value of the Stock as a String
   * @return String The High Price
   */
  public java.lang.String getHighPrice()
  {
    return highPrice.toString();
  }
  /**
   * Method to return the Line Color of the Object
   * @return java.awt.Color
   */
  public java.awt.Color getLineColor()
  {
    return lineColor;
  }
  /**
   * Method returns the Low Price Value of the Stock
   * @return Fraction
   */
  public Fraction getLow()
  {
    return lowPrice;
  }
  /**
   * Method returns the Low Price Value of the Stock as a String
   * @return String The Low Price
   */
  public java.lang.String getLowPrice()
  {
    return lowPrice.toString();
  }
  /**
   * Method to get the Monthly Movement
   * @return com.hgtable.data.MonthlyMovement
   */
  public MonthlyMovement getMonthlyMovement()
  {
    return monthlyMovement;
  }
  /**
   * Method returns the Open Price Value of the Stock
   * @return Fraction
   */
  public Fraction getOpen()
  {
    return openPrice;
  }
  /**
   * Method returns the Open Price Value of the Stock as a String
   * @return String The Open Price
   */
  public java.lang.String getOpenPrice()
  {
    return openPrice.toString();
  }
  /**
   * Method returns the Volume of the Stock
   * @return longValue
   */
  public LongValue getVolume()
  {
    return volume;
  }
  /**
   * Method to set the All the data if the order is dictated by the titles.
   * @param titles String array of the titles for each piece of data
   * @param data   String array of each piece of data
   */
  public void loadDataByTitles(String[] titles, String[] data)
  {
    try
    {
      for (int i = 0; i < titles.length; i++)
      {
        if ("DATE".startsWith(titles[i].toUpperCase()))
        {
          setDate(data[i]);
        }
        else if ("OPEN".startsWith(titles[i].toUpperCase()))
        {
          setOpenPrice(data[i]);
        }
        else if ("HIGH".startsWith(titles[i].toUpperCase()))
        {
          setHighPrice(data[i]);
        }
        else if ("LOW".startsWith(titles[i].toUpperCase()))
        {
          setLowPrice(data[i]);
        }
        else if ("CLOSE".startsWith(titles[i].toUpperCase()))
        {
          setClosePrice(data[i]);
        }
        else if ("VOLUME".startsWith(titles[i].toUpperCase()))
        {
          setVolume(data[i]);
        }
        else
        {
          // Unknown Title
        }
      }
      setLineColor(openPrice, closePrice);
    }
    catch (Exception exc)
    {
      System.out.println("Exception caught loading data ");
    }
  }
  /**
   * Method to set the Close Price Value of the Stock as a String
   * @param newClosePrice java.lang.String
   */
  public void setClosePrice(String newClosePrice)
  {
    double temp = ParseData.parseNum(newClosePrice, 0.0);
    closePrice = new Fraction(temp);
  }
  /**
   * Method to Set the date of the Stock Data being recorded.
   * @param newDate java.lang.String
   */
  public void setDate(String newDate)
  {
    origDateSz = new String(newDate);
    date = new HGCalendar(newDate);
  }
  /**
   * Method to Set the date of the Stock Data being recorded.
   * @param newDate java.lang.String
   */
  public void setDate(Date newDate)
  {
    date = new HGCalendar(newDate);
  }
  /**
   * Method to set the High Price Value of the Stock as a String
   * @param newHighPrice java.lang.String
   */
  public void setHighPrice(String newHighPrice)
  {
    double temp = ParseData.parseNum(newHighPrice, 0.0);
    highPrice = new Fraction(temp);
  }
  /**
   * Method to Set the Line Color if this Object should be charted.
   * @param openVal the Open value
   * @param closeVal the Close Value
   * @param newLineColor The Color of the Line to be drawn
   */
  private void setLineColor(double openVal, double closeVal)
  {
    setLineColor((openVal <= closeVal) ? java.awt.Color.green : java.awt.Color.red);
  }
  /**
   * Method to Set the Line Color if this Object should be charted.
   * @param openVal The Open Value as Fraction Object
   * @param closeVal The Close Value as Fraction Object
   */
  private void setLineColor(Fraction openVal, Fraction closeVal)
  {

    setLineColor(openVal.doubleValue(), closeVal.doubleValue());
  }
  /**
   * Method to Set the Line Color if this Object should be charted.
   * @param newLineColor The Color of the Line to be drawn
   */
  private void setLineColor(java.awt.Color newLineColor)
  {
    lineColor = newLineColor;
  }
  /**
   * Method to set the Low Price Value of the Stock as a String
   * @param newLowPrice java.lang.String
   */
  public void setLowPrice(String newLowPrice)
  {
    double temp = ParseData.parseNum(newLowPrice, 0.0);
    lowPrice = new Fraction(temp);
  }
  /**
   * Method To set the Monthly Movement, Note Every HistoricStockData
   * should have a reference to the Month and Year of which there was movement
   * this reference will tell how much movement was in the associated month
   * that this particular HistoricStockData item was representing.
   * @param newMonthlyMovement com.hgtable.data.MonthlyMovement
   */
  public void setMonthlyMovement(MonthlyMovement newMonthlyMovement)
  {
    monthlyMovement = newMonthlyMovement;
  }
  /**
   * Method to set the Open Price Value of the Stock as a String
   * @param newOpenPrice java.lang.String
   */
  public void setOpenPrice(String newOpenPrice)
  {
    double temp = ParseData.parseNum(newOpenPrice, 0.0);
    openPrice = new Fraction(temp);
  }
  /**
   * Method to set the Volume of the Stock as a String
   * @param newVolume LongValue object
   */
  public void setVolume(LongValue newVolume)
  {
    long temp = newVolume.longValue();
    volume = new LongValue(temp);
  }
  /**
   * Method to set the Volume of the Stock as a String
   * @param newVolume java.lang.String
   */
  public void setVolume(String newVolume)
  {
    long temp = ParseData.parseNum(newVolume, 0);
    volume = new LongValue(temp);
  }
  /**
   * Returns a String reprsentation of this class object
   * @return java.lang.String
   */
  public String toString()
  {
    String temp = date + "," + openPrice + "," + highPrice + "," + lowPrice + "," + closePrice + "," + volume;
    return temp;
  }
  
}
