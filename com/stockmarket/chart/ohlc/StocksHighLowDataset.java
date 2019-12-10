// StocksHighLowDataset.java
package com.stockmarket.chart.ohlc;

import com.stockmarket.data.*;
import javax.swing.JComponent;
import java.awt.Paint;
import java.util.*;
import com.jrefinery.data.*;
import com.jrefinery.chart.*;
import com.jrefinery.chart.tooltips.XYToolTipGenerator;
import com.hgutil.*;
/**
 * A sample dataset for a high-low-open-close plot.
 * <P>
 * Note that the aim of this class is to create a self-contained 
 * dataset for demo purposes -
 * it is NOT intended to show how you should go about writing your 
 * own data sources.
 * @author Hans-Jurgen Greiner
 */
class StocksHighLowDataset  extends AbstractSeriesDataset
  implements HighLowDataset, MouseMovementListener, PlotPaintColor, XYToolTipGenerator
{

  private int numberOfItems = 0;
  private Object[] array = null;

  private HistoricStockDataContainer container = null;

  /**
   * Default constructor.
   */
  public StocksHighLowDataset()
  {}
  /**
   * Default constructor.
   */
  public StocksHighLowDataset(HistoricStockDataContainer cont)
  {
    initialiseData(cont);
  }
  /**
   * Returns a java.util.Date for the specified year, month and day.
   */
  private Date createDate(int year, int month, int day)
  {
    GregorianCalendar calendar = new GregorianCalendar(year, month, day);
    return calendar.getTime();
  }
  /**
   * Returns the close-value for the specified series and item.
   * Series are numbered 0, 1, ...
   * @param series The index (zero-based) of the series;
   * @param item The index (zero-based) of the required item;
   * @return The close-value for the specified series and item.
   */
  public Number getCloseValue(int series, int item)
  {
    if (series == 0)
    {
      if (array[item] instanceof HistoricStockData)
      {
        HistoricStockData info = (HistoricStockData) array[item];
        return (new Double(info.getClose().doubleValue()));
      }
    }
    return null;
  }
  /**
   * Returns the high-value for the specified series and item.
   * Series are numbered 0, 1, ...
   * @param series The index (zero-based) of the series;
   * @param item The index (zero-based) of the required item;
   * @return The high-value for the specified series and item.
   */
  public Number getHighValue(int series, int item)
  {
    if (series == 0)
    {
      if (array[item] instanceof HistoricStockData)
      {
        HistoricStockData info = (HistoricStockData) array[item];
        return (new Double(info.getHigh().doubleValue()));
      }
    }
    return null;
  }
  /**
   * Insert the method's description here.
   */
  public HistoricStockDataContainer getHistoricListContainer()
  {
    return (container);
  }
  /**
   * Returns the number of items in the specified series.
   * @param series The index (zero-based) of the series;
   * @return The number of items in the specified series.
   */
  public int getItemCount(int series)
  {
    return numberOfItems; // one series with n items in this sample
  }
  /**
   * Returns the low-value for the specified series and item.
   * Series are numbered 0, 1, ...
   * @param series The index (zero-based) of the series;
   * @param item The index (zero-based) of the required item;
   * @return The low-value for the specified series and item.
   */
  public Number getLowValue(int series, int item)
  {
    if (series == 0)
    {
      if (array[item] instanceof HistoricStockData)
      {
        HistoricStockData info = (HistoricStockData) array[item];
        return (new Double(info.getLow().doubleValue()));
      }
    }
    return null;
  }
  /**
   * Returns the open-value for the specified series and item.
   * Series are numbered 0, 1, ...
   * @param series The index (zero-based) of the series;
   * @param item The index (zero-based) of the required item;
   * @return The open-value for the specified series and item.
   */
  public Number getOpenValue(int series, int item)
  {
    if (series == 0)
    {
      if (array[item] instanceof HistoricStockData)
      {
        HistoricStockData info = (HistoricStockData) array[item];
        return (new Double(info.getOpen().doubleValue()));
      }
    }
    return null;
  }
  /**
   * Returns the volume for the specified series and item.
   * @param series The series (zero-based index).
   * @param item The item (zero-based index).
   * @return The volume-value for the specified series-item
   */
  public Number getVolumeValue(int series, int item)
  {
    if (series == 0)
    {
      if (array[item] instanceof HistoricStockData)
      {
        HistoricStockData info = (HistoricStockData) array[item];
        return (new Long(info.getVolume().longValue()));
      }
    }
    return null;
  }
  /**
   * Returns the number of series in the data source, ONE in this sample.
   * @return The number of series in the data source.
   */
  public int getSeriesCount()
  {
    return 1;
  }
  /**
   * Returns the name of the series.
   * @param series The index (zero-based) of the series;
   * @return The name of the series.
   */
  public String getSeriesName(int series)
  {
    if (series == 0)
    {
      if (container == null)
      {
        return "Unknown";
      }
      String sms = container.getStockSymbol();
      return (sms);
    }
    return "Error";
  }
  public Paint getSeriesPaint(int series, int item)
  {
    if (series == 0)
    {
      if (array[item] instanceof HistoricStockData)
      {
        HistoricStockData info = (HistoricStockData) array[item];
        return (info.getLineColor());
      }
    }
    return null;
  }
  /**
   * Returns the x-value for the specified series and item.
   * Series are numbered 0, 1, ...
   * @param series The index (zero-based) of the series;
   * @param item The index (zero-based) of the required item;
   * @return The x-value for the specified series and item.
   */
  public Number getXValue(int series, int item)
  {
    if (array[item] instanceof HistoricStockData)
    {
      HistoricStockData info = (HistoricStockData) array[item];
      Date theDate = info.getDate();

      return (new Long(theDate.getTime()));

    }
    return null;
  }
  /**
   * Returns the y-value for the specified series and item.
   * Series are numbered 0, 1, ...
   * @param series The index (zero-based) of the series;
   * @param item The index (zero-based) of the required item;
   * @return The y-value for the specified series and item.
   */
  public Number getYValue(int series, int item)
  {
    return (getCloseValue(series, item));
  }
  /**
   * Sets up the data for the sample data source.
   */
  private void initialiseData(HistoricStockDataContainer container)
  {
    this.container = container;
    array = container.getVectorData().toArray();

    numberOfItems = array.length;
  }

  /**
   * Callback method to the invoking appliction with the series,
   * The values on the X Axis with represented as a double, and
   * the value on the Y axis with respect to the data as double.
   * The values are not the (x,y) coordinates but rather the 
   * value of the data at the selected (x,y) coordinates. 
   * @param comp The JComponent for which this operation is being performed
   * @param series The series of the data
   * @param index The index of the item being traced
   */
  public void setCoordinateValues(JComponent comp, int series, int index)
  {
    HistoricStockData info = (HistoricStockData) array[index];
    ChartLabels.setStockInfoPanel(info);
    comp.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.CROSSHAIR_CURSOR));
  }
  /**
   * Callback method to the invoking appliction with the series,
   * The values on the X Axis with represented as a double, and
   * the value on the Y axis with respect to the data as double.
   * The values are not the (x,y) coordinates but rather the 
   * value of the data at the selected (x,y) coordinates. 
   * @param comp The JComponent for which this operation is being performed
   * @param series The series of the data
   * @param xValue The Actual value on the X axis represented as a double
   * @param yValue The Actual value on the Y axis represented as a double
   */
  public void setCoordinateValues(JComponent comp, int series, double xValue, double yValue)
  {

    if (series == 0)
    {

      GregorianCalendar xDate = new GregorianCalendar();
      xDate.setTime(new Date((long) xValue));
      for (int i = 0; i < numberOfItems; i++)
      {
        HistoricStockData info = (HistoricStockData) array[i];
        GregorianCalendar myDate = info.getGregorianCalendar();
        int year = myDate.get(Calendar.YEAR);
        int month = myDate.get(Calendar.MONTH);
        int day = myDate.get(Calendar.DAY_OF_MONTH);

        if (year == xDate.get(Calendar.YEAR) && month == xDate.get(Calendar.MONTH) && day == xDate.get(Calendar.DAY_OF_MONTH))
        {

          ChartLabels.setStockInfoPanel(info);
          comp.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.CROSSHAIR_CURSOR));
          //StringBuffer tips = new StringBuffer();
          //tips.append( "<html><font color=blue size=-2>" );
          //tips.append( info.getDateSz() + "<BR>" );
          //tips.append( info.getOpenPrice() + "<BR>" );
          //tips.append( info.getHighPrice() + "<BR>" );
          //tips.append( info.getLowPrice() + "<BR>" );
          //tips.append( info.getClosePrice() + "<BR>" );
          //tips.append( info.getVolume() );
          //tips.append( "</font></html>" );
          //comp.setToolTipText(tips.toString());
          return;
        }
      }
    }
  }

  /**
   * Generates a tooltip text item for a particular item within a series.
   * @param data The dataset.
   * @param series The series number (zero-based index).
   * @param item The item number (zero-based index).
   */
  public String generateToolTip(XYDataset data, int series, int item)
  {

    String result = null;
    if (!(data instanceof HighLowDataset))
    {
      return result;
    }
    HighLowDataset dataSet = (HighLowDataset) data;

    Number high = dataSet.getHighValue(series, item);
    Number low = dataSet.getLowValue(series, item);
    Number open = dataSet.getOpenValue(series, item);
    Number close = dataSet.getCloseValue(series, item);
    Number x = dataSet.getXValue(series, item);

    result = data.getSeriesName(series);
    HistoricStockData info = (HistoricStockData) array[item];
    StringBuffer tips = new StringBuffer();
    tips.append("<html><font color=blue size=-2>");
    tips.append("Date:  " + info.getDateSz() + "<BR>");
    tips.append("Open:  " + info.getOpenPrice() + "<BR>");
    tips.append("High:  " + info.getHighPrice() + "<BR>");
    tips.append("Low:   " + info.getLowPrice() + "<BR>");
    tips.append("Close: " + info.getClosePrice() + "<BR>");
    tips.append("Vol.:  " + info.getVolume());
    tips.append("</font></html>");
    result = tips.toString();

    return result;

  }

}
