// HTMLModule.java
package com.stockmarket.app;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.text.html.*;
import javax.swing.border.*;
import javax.swing.colorchooser.*;
import javax.swing.filechooser.*;
import javax.accessibility.*;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.util.*;
import java.io.*;
import java.applet.*;
import java.net.*;
/**
 * Insert the type's description here.
 * @author Hans-Jurgen Greiner
 */
public class HTMLModule extends StockMarketBaseModule
{

  JEditorPane html;

  /**
   * WelcomeModule constructor comment.
   */
  public HTMLModule(StockMarketApp stockMarketApp)
  {
    // Set the title for this demo, and an icon used to represent this
    // demo inside the StockMarketApp .
    this(stockMarketApp, "HTMLModule", "toolbar/NewsHTML.gif");
  }
  /**
   * WelcomeModule constructor comment.
   */
  public HTMLModule(StockMarketApp mainApp, String resourceName, String iconPath)
  {
    // Set the title for this demo, and an icon used to represent this
    // demo inside the StockMarketApp .
    this(mainApp, resourceName, iconPath, "/resources/index.html");

  }
  /**
   * WelcomeModule constructor comment.
   */
  public HTMLModule(StockMarketApp mainApp, String resourceName, String iconPath, String urlPath)
  {
    // Set the title for this demo, and an icon used to represent this
    // demo inside the StockMarketApp .
    super(mainApp, resourceName, iconPath);
    setPath(urlPath);
  }
  /**
   * Method createHyperLinkListener. Adds a Listener for any Hyperlinks on the 
   * HTML Page
   * @return HyperlinkListener
   */
  public HyperlinkListener createHyperLinkListener()
  {
    return new HyperlinkListener()
    {
      public void hyperlinkUpdate(HyperlinkEvent e)
      {
        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
        {
          if (e instanceof HTMLFrameHyperlinkEvent)
          {
            ((HTMLDocument) html.getDocument()).processHTMLFrameHyperlinkEvent((HTMLFrameHyperlinkEvent) e);
          }
          else
          {
            try
            {
              html.setPage(e.getURL());
            }
            catch (IOException ioe)
            {
              System.out.println("IOE: " + ioe);
            }
          }
        }
      }
    };
  }
  /**
   * main method allows us to run as a standalone demo.
   */
  public static void main(String[] args)
  {
    HTMLModule demo = new HTMLModule(null);
    demo.mainImpl();
  }
  /**
   * Method setPath. Sets the Path of the HTML File URL
   * @param path
   */
  public void setPath(String path)
  {
    try
    {
      URL url = null;
      // System.getProperty("user.dir") +
      // System.getProperty("file.separator");
      try
      {
        url = getClass().getResource(path);
      }
      catch (Exception e)
      {
        System.err.println("Failed to open " + path);
        url = null;
      }
      if (url != null)
      {
        html = new JEditorPane(url);
        html.setEditable(false);
        html.addHyperlinkListener(createHyperLinkListener());

        JScrollPane scroller = new JScrollPane();
        JViewport vp = scroller.getViewport();
        vp.add(html);
        getModulePanel().add(scroller, BorderLayout.CENTER);
      }
    }
    catch (MalformedURLException e)
    {
      System.out.println("Malformed URL: " + e);
    }
    catch (IOException e)
    {
      System.out.println("IOException: " + e);
    }
  }
  /**
   * Method to shutdown an application and perform any cleanup neccesarry
   */
  public void shutdown()
  {
    // Do Nothing
  }
  /**
   * Method to startup any threads if neccesary
   */
  public void start()
  {}
  /**
   * Method to stop any threads if neccesary
   */
  public void stop()
  {}
}
