// GetPageInfo.java
package com.stockmarket.parsers.httpclient;

import java.net.*;
import java.io.*;
import java.util.ResourceBundle;
import java.util.MissingResourceException;

import HTTPClient.*;
import com.hgutil.*;
import com.hgutil.data.*;

/**
 * Class to facilitate the retrieval of data for the an HTTP connection
 * @author: Hans-Jurgen Greiner
 */
abstract public class GetPageInfo
{
  private ResourceBundle bundle = null;
  protected boolean proxyRequired = false;

  private String proxyName = null;
  private String proxyPswd = null;
  private String proxyHost = null;
  private String proxyPort = null;
  private String proxyRealm = null;
  protected static boolean basicAuthComplete = false;
  protected static AppProperties appProps =null;
  
  protected static final boolean DEBUG = ParseData.parseBool(System.getProperties().getProperty("HGDEBUG"), false);
  private static int debugCounter = 0;

  /**
   * Constructor 
   */
  public GetPageInfo()
  {
    super();

    if (!basicAuthComplete)
    {
      // Here is how it works, It first Attempts to get the data form the System Properties.
      // If that is not available, it then attempts to grab the default information the resource bundle
      proxyName = System.getProperties().getProperty("HTTPClient.proxy_name", getString("HTTPClient.proxy_name"));
      proxyPswd = System.getProperties().getProperty("HTTPClient.proxy_password", getString("HTTPClient.proxy_password"));
      proxyHost = System.getProperties().getProperty("HTTPClient.proxy_host", getString("HTTPClient.proxy_host"));
      proxyPort = System.getProperties().getProperty("HTTPClient.proxy_port", getString("HTTPClient.proxy_port"));
      proxyRealm = System.getProperties().getProperty("HTTPClient.proxy_realm", getString("HTTPClient.proxy_realm"));
      proxyRequired =
        ParseData.parseBool(
          System.getProperties().getProperty("HTTPClient.proxy_required"),
          getBoolean("HTTPClient.proxy_required"));

      System.getProperties().put("https.proxyHost", proxyHost);
      System.getProperties().put("https.proxyPort", proxyPort);
      System.getProperties().put("java.protocol.handler.pkgs", "com.sun.net.ssl.internal.www.protocol");

      if (proxyRequired)
      {
        if (DEBUG)
        {
          System.out.println(
            "PROXY REQUIRED IS ["
              + proxyRequired
              + "]: "
              + proxyHost
              + ":"
              + proxyPort
              + ", REALM="
              + proxyRealm
              + ", USERID="
              + proxyName);
        }
        int port = ParseData.parseNum(proxyPort, 0);
        // Add the Authorization for our Proxy Name Port, realm, userid, Pswd
        setBasicAuthorization(proxyHost, port, proxyRealm, proxyName, proxyPswd);
      }
      // We have Completed the Basic Authorization - Set the value to true
      basicAuthComplete = true;
    }

    CookieModule.setCookiePolicyHandler(null); // Accept all cookies

  }
  /**
   * If DEBUG is defined, prints debug information out to std ouput.
   * @param s String
   */
  public void debug(String s)
  {
    if (DEBUG)
    {
      System.out.println((debugCounter++) + ": " + s);
    }
  }
  /**
   * Method dumpProperties.
   */
  public void dumpProperties()
  {
    System.out.println("GetPageInfo::dumpProperties()");
    System.out.println("  ResourceBundle = [" + bundle + "]");
    System.out.println("   ProxyRequired = [" + proxyRequired + "]");
    System.out.println("       ProxyName = [" + proxyName + "]");
    System.out.println("       ProxyPswd = [" + proxyPswd + "]");
    System.out.println("       ProxyHost = [" + proxyHost + "]");
    System.out.println("       ProxyPort = [" + proxyPort + "]");
    System.out.println("      ProxyRealm = [" + proxyRealm + "]");
    System.out.println("           DEBUG = [" + DEBUG + "]");
    System.out.println("    DebugCounter = [" + debugCounter + "]");
  }
  /**
   * Method to retrieve Boolean Value from the resource bundle
   * @param key String 
   */
  public boolean getBoolean(String key)
  {
    debug("getBoolean(" + key + ") - retrieving Key...");
    String valueSz = null;
    try
    {
      valueSz = getResourceBundle().getString(key);
    }
    catch (MissingResourceException e)
    {
      System.out.println("java.util.MissingResourceException: " + "Couldn't find value for: " + key);
    }
    if (valueSz == null)
    {
      valueSz = "Could not find resource: " + key + "  ";
    }
    boolean value = new Boolean(valueSz).booleanValue();
    debug("getBoolean(" + key + ") - value ==> " + value);
    debug("getBoolean(" + key + ") - retrieving Key...Done");
    return value;
  }
  /**
   * Method to retireve a HTTPConnection
   * @param url URL
   * @throws ProtocolNotSuppException
   */
  protected HTTPConnection getConnectionToURL(URL url) throws ProtocolNotSuppException
  {

    HTTPConnection con = new HTTPConnection(url);
    con.setDefaultHeaders(new NVPair[] { new NVPair("User-Agent", "Mozilla/4.5")});
    con.setDefaultAllowUserInteraction(false);

    if (proxyRequired)
    {
      con.addBasicAuthorization(proxyRealm, proxyName, proxyPswd);
    }

    return (con);
  }
  /**
   * Method to retireve a PAGE Info, i.e. HTML 
   * @param urlLocation String
   * @throws RuntimeException
   */
  public void getInfoPage(String urlLocation) throws RuntimeException
  {

    try
    {
      URL url = new URL(urlLocation);

      getInfoPage(url);
    }
    catch (Exception exc)
    {
      String errorMsg = "GetPageInfo::getInfoPage(String) Caught: \n[" + exc + "]";
      if (DEBUG)
      {
        System.out.println(errorMsg);
      }
      throw new RuntimeException(errorMsg);
    }
  }
  /**
   * Method to retireve a PAGE Info, i.e. HTML 
   * @param urlLocation URL
   * @throws RuntimeException
   */
  public void getInfoPage(URL urlLocation) throws RuntimeException
  {
    BufferedReader in = null;

    try
    {
      URL url = urlLocation;

      HTTPConnection con = getConnectionToURL(url);

      HTTPResponse headRsp = con.Head(url.getFile());
      HTTPResponse rsp = con.Get(url.getFile());

      int status = headRsp.getStatusCode();

      if (status < 300 && DEBUG)
      {
        System.out.println("No authorization required to access " + url);
      }
      else if (status >= 400 && status != 401 && status != 407)
      {
        String errorMsg = "Error trying to access " + url + ":\n" + headRsp;
        if (DEBUG)
        {
          System.out.println(errorMsg);
        }
      }
      else
      { // Everything else
        String errorMsg = "Access to URL " + url + "returned status = " + status + ":\n" + headRsp;
        if (DEBUG)
        {
          System.out.println(errorMsg);
        }
      }
      in = new BufferedReader(new InputStreamReader(rsp.getInputStream()));
      String inputLine;

      while ((inputLine = in.readLine()) != null)
      {
        processHTMLLine(inputLine);
        if (DEBUG)
        {
          System.out.println(inputLine);
        }
      }

    }
    catch (Exception exc)
    {
      String errorMsg = "GetPageInfo::getInfoPage(URL) Caught: \n[" + exc + "]";
      if (DEBUG)
      {
        System.out.println(errorMsg);
      }
      throw new RuntimeException(errorMsg);
    }
  }
  /**
   * Returns the resource bundle associated with this demo. Used
   * to get accessable and internationalized strings.
   */
  public ResourceBundle getResourceBundle()
  {
    if (bundle == null)
    {
      String propfile = getResourceBundlePropName();
      bundle = ResourceBundle.getBundle(propfile);
    }
    return bundle;
  }
  /**
   * This method returns a string from the demo's resource bundle.
   */
  public String getString(String key)
  {
    debug("getString(" + key + ") - retrieving Key...");
    String value = null;
    try
    {
      value = getResourceBundle().getString(key);
    }
    catch (MissingResourceException e)
    {
      System.out.println("java.util.MissingResourceException: " + "Couldn't find value for: " + key);
    }
    if (value == null)
    {
      value = "Could not find resource: " + key + "  ";
    }
    debug("getString(" + key + ") - value ==> " + value);
    debug("getString(" + key + ") - retrieving Key...Done");
    return value;
  }
  /**
   * Static Method to set the ProxyInfo given the ini file name
   * @param iniFile A String providing the name of the ini file to load.
   */
  public static final void preloadProxyInfo(String iniFile)
  {
    if ( appProps == null )
    {
      appProps = new AppProperties(iniFile);
    }
    String AUTHUSERID = "HTTPClient.proxy_name";
    String AUTHUSERPSWD = "HTTPClient.proxy_password";
    String AUTHUSERHOST = "HTTPClient.proxy_host";
    String AUTHUSERPORT = "HTTPClient.proxy_port";
    String AUTHUSERREALM = "HTTPClient.proxy_realm";
    String AUTHUSERPROXY_REQ = "HTTPClient.proxy_required";

    System.getProperties().setProperty(AUTHUSERID, appProps.getProperty(AUTHUSERID, ""));
    System.getProperties().setProperty(AUTHUSERPSWD, appProps.getProperty(AUTHUSERPSWD, ""));
    System.getProperties().setProperty(AUTHUSERHOST, appProps.getProperty(AUTHUSERHOST, ""));
    System.getProperties().setProperty(AUTHUSERPORT, appProps.getProperty(AUTHUSERPORT, ""));
    System.getProperties().setProperty(AUTHUSERREALM, appProps.getProperty(AUTHUSERREALM, ""));
    System.getProperties().setProperty(AUTHUSERPROXY_REQ, appProps.getProperty(AUTHUSERPROXY_REQ, ""));

  }
  /**
   * Abstract Method - This method should be implemented by the extending class
   * it will pass in one line of data at a time retireved from the URL
   * @param line String
   */
  abstract protected void processHTMLLine(String line);
  /**
   * This method sets the basic authorization for a proxy
   * @param hostName String
   * @param hostPort int
   * @param realm String
   * @param userid String
   * @param pswd String
   */
  protected void setBasicAuthorization(String hostName, int hostPort, String realm, String userid, String pswd)
  {

    AuthorizationInfo.addBasicAuthorization(hostName, hostPort, realm, userid, pswd);
    // Inform the HTTPConnection that our Proxy Host,
    // Port is a proxyServer.
    HTTPConnection.setProxyServer(hostName, hostPort);
  }
  
  /**
   * Method getResourceBundlePropName. Method returns the string value of the property 
   * file used for a resouirce bundle
   * @return String
   */
  abstract protected String getResourceBundlePropName();
}
