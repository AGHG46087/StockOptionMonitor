// HistoricStockDataComparator.java
package com.stockmarket.data;

import java.util.Date;
import com.hgutil.datarenderer.*;

/**
 * Class is used by the StockTableModelData to provide a generic Sort
 * mechanism for the StockData Class
 * @author: Hans-Jurgen Greiner
 */
public class HistoricStockDataComparator implements java.util.Comparator
{
  protected int sortCol = 0;
  protected boolean sortAsc = true;

  /**
   * StockDataComparator constructor comment.
   */
  public HistoricStockDataComparator()
  {
    super();
  }
  /**
   * Constructor
   * @param sortCol int
   * @param sortAsc boolean
   */
  public HistoricStockDataComparator(int sortCol, boolean sortAsc)
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
    if (!(o1 instanceof HistoricStockData) || !(o2 instanceof HistoricStockData))
    {
      return 0;
    }
    HistoricStockData s1 = (HistoricStockData) o1;
    HistoricStockData s2 = (HistoricStockData) o2;
    int result = 0;
    double d1, d2;
    switch (sortCol)
    {
      case 0 : // Date
        Date date1 = (Date) s1.getDate();
        Date date2 = (Date) s2.getDate();
        result = date1.compareTo(date2);
        break;
      case 1 : // Open
        d1 = s1.getOpen().doubleValue();
        d2 = s2.getOpen().doubleValue();
        result = d1 < d2 ? -1 : (d1 > d2 ? 1 : 0);
        break;
      case 2 : // High
        d1 = s1.getHigh().doubleValue();
        d2 = s2.getHigh().doubleValue();
        result = d1 < d2 ? -1 : (d1 > d2 ? 1 : 0);
        break;
      case 3 : // Low
        d1 = s1.getLow().doubleValue();
        d2 = s2.getLow().doubleValue();
        result = d1 < d2 ? -1 : (d1 > d2 ? 1 : 0);
        break;
      case 4 : // Close
        d1 = s1.getClose().doubleValue();
        d2 = s2.getClose().doubleValue();
        result = d1 < d2 ? -1 : (d1 > d2 ? 1 : 0);
        break;
      case 5 : // volume
        long l1 = s1.getVolume().longValue();
        long l2 = s2.getVolume().longValue();
        result = l1 < l2 ? -1 : (l1 > l2 ? 1 : 0);
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
