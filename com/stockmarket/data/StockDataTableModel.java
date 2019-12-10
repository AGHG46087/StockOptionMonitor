// StockDataTableModel.java
package com.stockmarket.data;

import javax.swing.*;
import java.util.*;
import javax.swing.JTable;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.TableModelEvent;

import com.hgtable.ColumnDataCellValue;
import com.hgtable.ColumnHeaderData;
import com.hgtable.HGTableModel;


/**
 * Class Object to manage Stock Data in a Table
 * This model does contain specifics in reference to the
 * Class Object <B>StockData</B>
 * @author: Hans-Jurgen Greiner
 */
public class StockDataTableModel extends HGTableModel
{

  /**
   * Inner class used to help with the double clicking on the headers.
   * (Workbench>Preferences>Java>Templates)
   */
  public class ColumnListener extends HGTableModel.MainModelColumnListener
  {

    ImageIcon sortAscIcon = null;
    ImageIcon sortDecIcon = null;
    ImageIcon blankIcon = null;

    /**
     * Class Contructor
     * @see MainModelColumnListener#MainModelColumnListener(JTable)
     */
    public ColumnListener(JTable table)
    {
      super(table);
      //sortAscIcon = createImageIcon( "table/SortAsc.gif" );
      //sortDecIcon = createImageIcon( "table/SortDec.gif" );
      //blankIcon = createImageIcon( "table/Blank.gif" );
      //((DefaultTableCellRenderer)table.getColumnModel().getColumn(0).getHeaderRenderer()).setIcon( blankIcon );
      //((DefaultTableCellRenderer)table.getColumnModel().getColumn(0).getHeaderRenderer()).setAlignmentX( JLabel.WEST );
    }

    /**
     * Method sorts any column within the table in either ascending or 
     * descending order
     * @see MouseListener#mouseClicked(MouseEvent)
     */
    public void mouseClicked(MouseEvent e)
    {
      TableColumnModel colModel = table.getColumnModel();
      int columnModelIndex = colModel.getColumnIndexAtX(e.getX());
      int modelIndex = colModel.getColumn(columnModelIndex).getModelIndex();

      // NOTE: sortCol and sortAsc, come from HGTableModel.this.sortCol and
      //       HGTableModel.this.sortAsc repectively.
      if (modelIndex < 0)
      {
        return;
      }
      if (sortCol == modelIndex)
      {
        sortAsc = !sortAsc;
      }
      else
      {
        sortCol = modelIndex;
      }

      //ImageIcon sortIcon = sortAsc ? sortAscIcon : sortDecIcon;

      for (int i = 0; i < StockDataTableModel.this.columnsCount; i++)
      {
        TableColumn column = colModel.getColumn(i);
        column.setHeaderValue(getColumnName(column.getModelIndex()));
        //((DefaultTableCellRenderer)column.getHeaderRenderer()).setAlignmentX( JLabel.RIGHT_ALIGNMENT );
        //((DefaultTableCellRenderer)column.getHeaderRenderer()).setIcon( blankIcon );
      }

      //((DefaultTableCellRenderer)colModel.getColumn(sortCol).getHeaderRenderer()).setAlignmentY( JLabel.RIGHT_ALIGNMENT );
      //((DefaultTableCellRenderer)colModel.getColumn(sortCol).getHeaderRenderer()).setIcon( sortIcon );
      table.getTableHeader().repaint();

      Collections.sort(data, new StockDataComparator(modelIndex, sortAsc));
      table.tableChanged(new TableModelEvent(StockDataTableModel.this));
      table.repaint();
    }
  }
  /**
   * StockTableData constructor comment.
   */
  public StockDataTableModel(Vector headers, Vector data)
  {
    super(headers, data);
    //this.headers = headers;
    //this.data = data;

    Collections.sort(data, new StockDataComparator(sortCol, sortAsc));
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
    header.setReorderingAllowed(true);

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
    if (column == sortCol)
    {
      str += sortAsc ? " »" : " «";
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
    if (nCol == 0)
    {
      StockData obj = (StockData) data.elementAt(nRow);
      String value = (String) obj.getSymbol().getDataSz().trim();
      rc = "".equals(value);
    }
    return rc;
  }
  /**
   * Method sets the Textual Value of the Data at Row, Col
   * If the object in in range of available Rows, and is the
   * Symbol section of the StockData
   * @param Object The Data to set
   * @param nRow The Row in which we are currently at
   * @param nCol The Column, must be the Symbol column
   */
  public void setValueAt(Object value, int nRow, int nCol)
  {
    if ((0 <= nRow && nRow < getRowCount()) && (StockData.SYMBOLTEXT.startsWith(StockData.columns[nCol].getTitle())))
    {
      StockData sd = (StockData) data.elementAt(nRow);
      String tmpValue = value.toString().toUpperCase();

      // Set the Symbol Requested
      sd.setSymbol(tmpValue, 0.00);
      // Set the Name as we are still Searching
      sd.setName("Search for " + tmpValue);
      // Now enforce the Entire Row, to be updated,
      // We only want to Update the row for Speed sake.
      for (nCol = 0; nCol < getColumnCount(); nCol++)
      {
        fireTableCellUpdated(nRow, nCol);
      }

    }
    return;
  }
}
