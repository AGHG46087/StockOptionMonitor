// StockDataComparator.java
package com.stockmarket.data;

import com.hgutil.data.*;

/**
 * Class is used by the StockTableModelData to provide a generic Sort
 * mechanism for the StockData Class
 * @author: Hans-Jurgen Greiner
 */
public class StockDataComparator implements java.util.Comparator
{
  protected int sortCol;
  protected boolean sortAsc;

  /**
   * StockDataComparator constructor comment.
   */
  public StockDataComparator()
  {
    super();
  }
  /**
   * Constructor
   * @param sortCol int
   * @param sortAsc boolean
   */
  public StockDataComparator(int sortCol, boolean sortAsc)
  {
    this.sortCol = sortCol;
    this.sortAsc = sortAsc;
  }
  /**
   * Compares the selected Columns and specified Sort ascending or descending
   * @param o1 Object
   * @param o2 Object
   * @return int
   */
  public int compare(Object o1, Object o2)
  {
    if (!(o1 instanceof StockData) || !(o2 instanceof StockData))
    {
      return 0;
    }
    StockData s1 = (StockData) o1;
    StockData s2 = (StockData) o2;
    int result = 0;
    double d1, d2;
    switch (sortCol)
    {
      case 0 : // symbol
        String str1 = (String) s1.getSymbol().getData();
        String str2 = (String) s2.getSymbol().getData();
        result = str1.compareTo(str2);
        break;
      case 1 : // name
        result = s1.getName().compareTo(s2.getName());
        break;
      case 2 : // bid
        d1 = s1.getBid().doubleValue();
        d2 = s2.getBid().doubleValue();
        result = d1 < d2 ? -1 : (d1 > d2 ? 1 : 0);
        break;
      case 3 : // ask
        d1 = s1.getAsk().doubleValue();
        d2 = s2.getAsk().doubleValue();
        result = d1 < d2 ? -1 : (d1 > d2 ? 1 : 0);
        break;
      case 4 : // last
        d1 = s1.getLast().doubleValue();
        d2 = s2.getLast().doubleValue();
        result = d1 < d2 ? -1 : (d1 > d2 ? 1 : 0);
        break;
      case 5 : // open
        d1 = s1.getOpen().doubleValue();
        d2 = s2.getOpen().doubleValue();
        result = d1 < d2 ? -1 : (d1 > d2 ? 1 : 0);
        break;
      case 6 : // change
        d1 = ((Fraction) s1.getChange().getData()).doubleValue();
        d2 = ((Fraction) s2.getChange().getData()).doubleValue();
        result = d1 < d2 ? -1 : (d1 > d2 ? 1 : 0);
        break;
      case 7 : // change %
        d1 = ((Double) s1.getChangePr().getData()).doubleValue();
        d2 = ((Double) s2.getChangePr().getData()).doubleValue();
        result = d1 < d2 ? -1 : (d1 > d2 ? 1 : 0);
        break;
      case 8 : // volume
        long l1 = s1.getVolume().longValue();
        long l2 = s2.getVolume().longValue();
        result = l1 < l2 ? -1 : (l1 > l2 ? 1 : 0);
        break;
      case 9 : // Trend Tick
        break;
      case 10 : // 52 week Low
        d1 = s1.get52WeekLow().doubleValue();
        d2 = s2.get52WeekLow().doubleValue();
        result = d1 < d2 ? -1 : (d1 > d2 ? 1 : 0);
        break;
      case 11 : // 52 Week High
        d1 = s1.get52WeekHigh().doubleValue();
        d2 = s2.get52WeekHigh().doubleValue();
        result = d1 < d2 ? -1 : (d1 > d2 ? 1 : 0);
        break;
      case 12 : // Previous Close
        d1 = s1.getPreviousClose().doubleValue();
        d2 = s2.getPreviousClose().doubleValue();
        result = d1 < d2 ? -1 : (d1 > d2 ? 1 : 0);
        break;
      case 13 : // Volatility
        d1 = ((Fraction) s1.getVolatility().getData()).doubleValue();
        d2 = ((Fraction) s2.getVolatility().getData()).doubleValue();
        result = d1 < d2 ? -1 : (d1 > d2 ? 1 : 0);
        break;
      default :
        break;
    }

    if (!sortAsc)
    {
      result = -result;
    }
    return result;
  }
  /**
   * equals
   * @param obj Object
   * @return boolean
   */
  public boolean equals(Object obj)
  {
    if (obj instanceof StockDataComparator)
    {
      StockDataComparator compObj = (StockDataComparator) obj;
      return ((compObj.getSortCol() == sortCol) && (compObj.getSortAsc() == sortAsc));
    }
    return false;
  }
  /**
   * Returns if we are sorting in Ascending or Descending order
   * @return boolean
   */
  public boolean getSortAsc()
  {
    return (sortAsc);
  }
  /**
   * Returns the Sort Column
   * @return int
   */
  public int getSortCol()
  {
    return (sortCol);
  }
}
