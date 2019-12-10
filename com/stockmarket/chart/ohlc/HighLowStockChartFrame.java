// HighLowStockChartFrame.java
package com.stockmarket.chart.ohlc;

import java.awt.*;
import com.jrefinery.chart.*;
import java.awt.event.*;
import javax.swing.*;
import com.hgmenu.*;
/**
 * Helper Window Frame class, Essentially only holds and acts events 
 * for the Frame
 * @author Hans-Jurgen Greiner
 */
class HighLowStockChartFrame extends JFrame
{

  private JMenuBar menuBar = new JMenuBar();
  private ActionTrigger actionTrigger = new ActionTrigger();
  private boolean allowApplicationExit = false;
  private JFreeChartPanel content = null;
  ////////////////////////////////////////////////////////////
  // Inner Class ActionTrigger
  ////////////////////////////////////////////////////////////
  class ActionTrigger implements ActionListener
  {

    public void actionPerformed(ActionEvent evt)
    {

      String cmd = evt.getActionCommand();

      if ("Date Range".equals(cmd))
      {
        content.zoomAuto();
        popupWindow();
        content.zoomAuto();
      }
      else if ("Refresh Chart".equals(cmd))
      {
        content.zoomAuto();
      }
      else if ("Exit".equals(cmd))
      {
        HighLowStockChartFrame.this.closeFrame();
      }

    }
  }
  /**
   * Constructs a frame for a chart.
   * @param title The frame title.
   * @param chart The chart.
   */
  public HighLowStockChartFrame(String title, JFreeChart chart)
  {
    this(title, chart, false);
  }
  /**
   * Constructs a frame for a chart.
   * @param title The frame title.
   * @param chart The chart.
   */
  public HighLowStockChartFrame(String title, JFreeChart chart, boolean scrollPane)
  {
    super(title);

    createMenuBar();

    //    content = new JFreeChartPanel(chart);
    //    content.setToolTipGeneration(false);
    // GEEK, Here we are turning off Tool Tip Generation
    content =
      new JFreeChartPanel(
        chart,
        JFreeChartPanel.DEFAULT_WIDTH,
        JFreeChartPanel.DEFAULT_HEIGHT,
        JFreeChartPanel.WIDTH_SCALING_THRESHOLD,
        JFreeChartPanel.HEIGHT_SCALING_THRESHOLD,
        JFreeChartPanel.DEFAULT_BUFFER_USED,
        true,
      // properties
    true, // save
    true, // print
    true, // zoom
    true // tooltips
  );

    content.setTraceMouseMovement(true);

    JPanel labels = ChartLabels.createDefaultPanel();
    ChartLabels.setOpaque(false);

    JPanel theContentPanel = new JPanel(new BorderLayout());
    theContentPanel.add(labels, BorderLayout.NORTH);
    theContentPanel.add(content, BorderLayout.CENTER);

    if (scrollPane)
    {
      this.setContentPane(new JScrollPane(theContentPanel));
    }
    else
      this.setContentPane(theContentPanel);

    addWindowListener(new WindowAdapter()
    {
      public void windowClosing(WindowEvent evt)
      {
        HighLowStockChartFrame.this.closeFrame();
      }
    });

  }
  /**
   * Insert the method's description here.
   */
  public void createMenuBar()
  {

    JMenu fileMenu =
      HGMenuItem.makeMenu("File", 'F', new Object[] { "Date Range", "Refresh Chart", null, "Exit" }, actionTrigger);
    menuBar.add(fileMenu);

    setJMenuBar(menuBar);

  }
  /**
   * Insert the method's description here.
   */
  public void popupWindow()
  {
    JFreeChart chart = content.getChart();
    HistoricRequestPanel.showHistoricRequestDialog(this, chart);
  }
  /**
   * Insert the method's description here.
   * @param param boolean
   */
  public void setAllowApplicationExit(boolean param)
  {
    allowApplicationExit = param;
  }

  /** 
   * Method to close the frame and perform any cleanup necesarry
   */
  private void closeFrame()
  {
    ChartLabels.clearChartLabels();
    this.dispose();
    if (allowApplicationExit)
    {
      System.exit(0);
    }

  }
}
