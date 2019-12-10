// WelcomeModule.java
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
public class WelcomeModule extends HTMLModule
{

  /**
   * WelcomeModule constructor comment.
   */
  public WelcomeModule(StockMarketApp stockMarketApp)
  {
    // Set the title for this demo, and an icon used to represent this
    // demo inside the StockMarketApp .
    super(stockMarketApp, "WelcomeModule", "toolbar/DontPanic.gif", "/resources/Welcome.html");
  }
  /**
   * main method allows us to run as a standalone demo.
   */
  public static void main(String[] args)
  {
    StockMarketBaseModule demo = new WelcomeModule(null);
    demo.mainImpl();
  }
  /**
   * Method to startup any threads if neccesary
   */
  public void startup()
  {}
}
