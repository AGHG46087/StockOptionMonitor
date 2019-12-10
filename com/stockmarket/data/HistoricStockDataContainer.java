// HistoricStockDataContainer.java
package com.stockmarket.data;

import java.io.Serializable;
import com.hgutil.*;
import java.util.*;
/**
 * Class object that maintains a list of HistoricStockData
 * In addition it will also calcaulate the Monthly Movement
 * @author: Hans-Jurgen Greiner
 */
public class HistoricStockDataContainer implements Serializable
{
  protected Vector vectorData = null;
  protected Vector moveVector = null;

  private String stockSymbol = "";
  /**
   * HistoricalStockDataContainer constructor comment.
   */
  public HistoricStockDataContainer()
  {
    super();
  }
  /**
   * HistoricalStockDataContainer constructor comment.
   */
  public HistoricStockDataContainer(Vector list)
  {
    super();
    setVectorData(list);
  }
  /**
   * HistoricalStockDataContainer constructor comment.
   */
  public HistoricStockDataContainer(StockData sd, Vector list)
  {
    super();
    setVectorData(list);
    setStockSymbol(sd.getSymbol().toString());
  }

  /**
   * HistoricalStockDataContainer constructor comment.
   */
  public HistoricStockDataContainer(String stockSymbol, Vector list)
  {
    super();
    setVectorData(list);
    setStockSymbol(stockSymbol);
  }
  
  /**
   * Insert the method's description here.
   * @param histStockData HistoricStockData to retrieve data and information
   *        that will enable the Monthly Movement to be calculated.
   */
  protected void addMonthMovement(HistoricStockData histStockData)
  {

    if (moveVector == null)
    {
      moveVector = new Vector();
    }

    GregorianCalendar tempCal = histStockData.getGregorianCalendar();

    // Create a new Monthly Movement Object, We may be adding it to the
    // Collection
    MonthlyMovement mm = new MonthlyMovement();
    mm.setCalendar(tempCal);
    mm.setHigh(histStockData.getHigh().doubleValue());
    mm.setLow(histStockData.getLow().doubleValue());

    // Find the Index of the a MonthlyMovement Object that we may have already
    int index = moveVector.indexOf(mm);

    if (index != -1)
    {
      // The Monthly Movement already existed. We will use this one,
      MonthlyMovement temp = (MonthlyMovement) moveVector.get(index);
      temp.setHigh(temp.getHigh(), mm.getHigh());
      temp.setLow(temp.getLow(), mm.getLow());
      histStockData.setMonthlyMovement(temp);
    }
    else
    {
      moveVector.add(mm);
      Collections.sort(moveVector);
      histStockData.setMonthlyMovement(mm);
    }

  }
  /**
   * Method to allow an object to be added to the list,
   * it also enforces the object to be an instanceof HistoricStockData
   * @param obj Object The Data object being passed to add to the list
   */
  public void addObject(Object obj)
  {

    if (obj instanceof HistoricStockData)
    {
      HistoricStockData infoObj = (HistoricStockData) obj;

      addMonthMovement(infoObj);

      if (vectorData == null)
      {
        vectorData = new Vector();
      }
      vectorData.add(infoObj);

    }

  }
  /**
   * Method to allow an object to be retrieved from the list,
   * it also enforces the object to be an instanceof HistoricStockData
   * @param index the index of the item requested.
   * @return HistoricStockData Object The Data object being reeturned
   */
  public HistoricStockData getObject(int index)
  {
    HistoricStockData infoObj = null;
    if ( 0 <= index && index < size() )
    {
      infoObj = (HistoricStockData) vectorData.elementAt(index);
    }
    
    return infoObj;

  }
  /**
   * Method to retrieve the Monthly Movement of a particular index
   * @param int The Index of the Vector
   * @return double the Value of the MonthlyMovement
   */
  public double getMonthlyMovement(int index)
  {
    double value = 0.0;
    if (0 <= index && index < moveVector.size())
    {
      value = ((MonthlyMovement) moveVector.elementAt(index)).getMovement();
    }
    return value;
  }
  /**
   * Method to retrieve the Monthly Movement of a particular year and month
   * @param  int The Year
   * @param  int The Month
   * @return double the Value of the MonthlyMovement
   */
  public double getMonthlyMovement(int year, int month)
  {
    double value = 0.0;
    if (moveVector != null)
    {
      GregorianCalendar tempCal = new HGCalendar(year, month, 1);
      MonthlyMovement mm = new MonthlyMovement();
      mm.setCalendar(tempCal);
      value = getMonthlyMovement(mm.getCalendar());
    }
    return value;
  }
  /**
   * Method to retrieve the Monthly Movement via a Calendar
   * @param  calendar A GregorianCalendar Object
   * @return double the Value of the MonthlyMovement
   */
  public double getMonthlyMovement(GregorianCalendar calendar)
  {
    double value = 0.0;
    if (moveVector != null && calendar != null)
    {
      MonthlyMovement mm = new MonthlyMovement();
      mm.setCalendar(calendar);
      mm.setHigh(0.0);
      mm.setLow(0.0);
      int index = moveVector.indexOf(mm);
      value = getMonthlyMovement(index);
    }
    return value;
  }
  /**
   * Method to retrieve the Monthly Movement Vector Data in sorted order by date
   * NOTE:  It is possible to have not Monthly Movement reported, If the Underlying
   * Stock or Index is not carried as historical Data.
   * @return java.util.Vector
   */
  public Vector getMonthlyMovementVector()
  {
    if ( moveVector == null )
    {
      moveVector = new Vector();
    }
    Collections.sort(moveVector);
    return moveVector;
  }
  /**
   * Method To Retrieve the Stock Symbol
   * @return java.lang.String
   */
  public java.lang.String getStockSymbol()
  {
    return stockSymbol;
  }
  /**
   * Method sortVectorData. Sorts the Main Vector data according to the specified
   * request.
   * @param sortCol  The Column field to sort on
   * @param sortAscending Sort Ascending or descending
   */
  public void sortVectorData( int sortCol, boolean sortAscending ) 
  {
    if ( vectorData == null )
    {
      vectorData = new Vector();
    }
    Collections.sort(vectorData, new HistoricStockDataComparator(sortCol, sortAscending));
  }
  /**
   * Method to retrieve the Vector Data in sorted order by date
   * NOTE:  It is possible to have not Historical data reported, If the Underlying
   * Stock or Index is not carried as historical Data.
   * @return java.util.Vector
   */
  public java.util.Vector getVectorData()
  {
    int sortCol = 0;
    boolean sortAsc = true;
    this.sortVectorData(sortCol, sortAsc);
    return vectorData;
  }
  /**
   * Method to set the Stock Symbol
   * @param newStockSymbol java.lang.String
   */
  public void setStockSymbol(String newStockSymbol)
  {
    stockSymbol = (newStockSymbol != null) ? newStockSymbol : "";
  }
  /**
   * Method to set the Data via a vector. This Method will
   * not permit data that is not of type <B>HistoricStockData</B>
   * @param newVectorData The Vector of the Data
   */
  public void setVectorData(Vector newVectorData)
  {

    // If we have not yet added any data and our vector is null
    // Create a new instance
    if (vectorData == null)
    {
      vectorData = new Vector();
    }
    // If the Vector of objects that had passed as a partameter
    // is not null and we have at least one, then add it.
    // Add Object will reject the object if it not of the correct type
    if (newVectorData != null)
    { 
      vectorData.clear();
      for (int i = 0; i < newVectorData.size(); i++)
      {
        addObject(newVectorData.elementAt(i));
      }
    }
  }
  /**
   * Method to return the size of the data being contained
   * @return int
   */
  public int size()
  {
    int size = -1;
    if (vectorData != null)
    {
      size = vectorData.size();
    }
    return size;
  }
  
  /**
   * Method getHistoricVolatitlity. Returns thge Historic Volatility of
   * Stock for the entirety of the Data it contains.
   * @return double
   */
  public double getHistoricVolatitlity()
  {
    Vector data = this.getVectorData();
    double[] closeValues = new double[ data.size() ];
    
    for ( int i = 0; i < data.size(); i++ )
    {
      closeValues[i] = ((HistoricStockData)data.elementAt(i)).getClose().doubleValue();
    }
    
    return Financials.getHistoricVolatility(closeValues);
  }
}
