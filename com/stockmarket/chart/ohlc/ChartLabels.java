// ChartLabels.java
package com.stockmarket.chart.ohlc;

import com.stockmarket.data.*;
import com.hgutil.*;
import com.hgutil.swing.*;
import java.awt.*;
import javax.swing.*;
/**
 * Insert the type's description here.
 * @author Hans-Jurgen Greiner
 */
class ChartLabels
{
  private static JLabel lblDate = new JLabel("01/01/2001");
  private static JLabel lblOpen = new JLabel("$0.00");
  private static JLabel lblHigh = new JLabel("$0.00");
  private static JLabel lblLow = new JLabel("$0.00");
  private static JLabel lblClose = new JLabel("$0.00");
  private static JLabel lblVolume = new JLabel("0");
  private static JLabel lblMinMove = new JLabel("$0.00");

  private static JLabel titleDate = new JLabel("Date");
  private static JLabel titleOpen = new JLabel("Open");
  private static JLabel titleHigh = new JLabel("High");
  private static JLabel titleLow = new JLabel("Low");
  private static JLabel titleClose = new JLabel("Close");
  private static JLabel titleVolume = new JLabel("Volume");
  private static JLabel titleMinMove = new JLabel("Min. Movement");

  /**
   * Method to add a field to the Panel
   * @param title javax.swing.JLabel
   * @param field javax.swing.JLabel
   * @param panel javax.swing.JPanel
   */
  public static void addToPanel(JLabel title, JLabel field, JPanel panel)
  {
    panel.add(title);
    panel.add(field);
    panel.setOpaque(false);
  }
  /**
   * Method to create a default panel. This method creates a Flow layout of ColumnLayout
   */
  public static JPanel createDefaultPanel()
  {

    JPanel cDate = new JPanel(new ColumnLayout());
    JPanel cOpen = new JPanel(new ColumnLayout());
    JPanel cHigh = new JPanel(new ColumnLayout());
    JPanel cLow = new JPanel(new ColumnLayout());
    JPanel cClose = new JPanel(new ColumnLayout());
    JPanel cVolume = new JPanel(new ColumnLayout());
    JPanel cMinMove = new JPanel(new ColumnLayout());

    addToPanel(titleDate, lblDate, cDate);
    addToPanel(titleOpen, lblOpen, cOpen);
    addToPanel(titleHigh, lblHigh, cHigh);
    addToPanel(titleLow, lblLow, cLow);
    addToPanel(titleClose, lblClose, cClose);
    addToPanel(titleVolume, lblVolume, cVolume);
    addToPanel(titleMinMove, lblMinMove, cMinMove);

    JPanel mainPanel = new JPanel(new GridLayout(1, 7));
    mainPanel.add(cDate);
    mainPanel.add(cOpen);
    mainPanel.add(cHigh);
    mainPanel.add(cLow);
    mainPanel.add(cClose);
    mainPanel.add(cVolume);
    mainPanel.add(cMinMove);
    mainPanel.setBackground(Color.white);
    setForeground(Color.black);
    setOpaque(false);

    return mainPanel;
  }
  /**
   * Method to refresh the painting of all the text fields
   */
  public static void refreshFields()
  {
    lblDate.repaint();
    lblOpen.repaint();
    lblHigh.repaint();
    lblLow.repaint();
    lblClose.repaint();
    lblVolume.repaint();
    lblMinMove.repaint();
  }
  /**
   * Method to the Foreground Color of all the Text Fields
   * @param param A Color Object for which to set the Foreground Color
   */
  public static void setForeground(Color param)
  {
    lblDate.setForeground(param);
    lblOpen.setForeground(param);
    lblHigh.setForeground(param);
    lblLow.setForeground(param);
    lblClose.setForeground(param);
    lblVolume.setForeground(param);
    lblMinMove.setForeground(param);
    titleDate.setForeground(param);
    titleOpen.setForeground(param);
    titleHigh.setForeground(param);
    titleLow.setForeground(param);
    titleClose.setForeground(param);
    titleVolume.setForeground(param);
    titleMinMove.setForeground(param);
  }
  /**
   * Method to set the Background to opaque or not
   * @param param boolean value for opaque
   */
  public static void setOpaque(boolean param)
  {
    lblDate.setOpaque(param);
    lblOpen.setOpaque(param);
    lblHigh.setOpaque(param);
    lblLow.setOpaque(param);
    lblClose.setOpaque(param);
    lblVolume.setOpaque(param);
    lblMinMove.setOpaque(param);
    titleDate.setOpaque(param);
    titleOpen.setOpaque(param);
    titleHigh.setOpaque(param);
    titleLow.setOpaque(param);
    titleClose.setOpaque(param);
    titleVolume.setOpaque(param);
    titleMinMove.setOpaque(param);
  }
  /**
   * Method to set the labels to the Values
   * @param info A HistoricStockData Object
   */
  public static synchronized void setStockInfoPanel(HistoricStockData info)
  {

    try
    {
      lblDate.setText(ParseData.format(info.getDate(), "MM/dd/yyyy"));
      lblOpen.setText(ParseData.format(info.getOpen().doubleValue(), "$#,##0.00"));
      lblHigh.setText(ParseData.format(info.getHigh().doubleValue(), "$#,##0.00"));
      lblLow.setText(ParseData.format(info.getLow().doubleValue(), "$#,##0.00"));
      lblClose.setText(ParseData.format(info.getClose().doubleValue(), "$#,##0.00"));
      lblVolume.setText(ParseData.format(info.getVolume().longValue(), "#,##0"));
      lblMinMove.setText(ParseData.format(info.getMonthlyMovement().doubleValue(), "$#,##0.00"));
      refreshFields();
    }
    catch (Exception exc)
    {
      System.out.println("Exception caught setting text: " + exc.getClass().getName() + "\n" + exc.getMessage());
    }

  }
  /**
   * Method to clear the data variables being presented.
   */
  public static synchronized void clearChartLabels()
  {

    try
    {
      lblDate.setText("01/01/2000");
      lblOpen.setText("$0.00");
      lblHigh.setText("$0.00");
      lblLow.setText("$0.00");
      lblClose.setText("$0.00");
      lblVolume.setText("0");
      lblMinMove.setText("$0.00");
      refreshFields();
    }
    catch (Exception exc)
    {
      System.out.println("Exception caught setting text: " + exc.getClass().getName() + "\n" + exc.getMessage());
    }

  }
}
