// GetProxyAuthParameters.java
package com.stockmarket.parsers.httpclient;

import HTTPClient.*;
import java.net.*;
import java.io.*;
/**
 * Class to get the Proxy Authorization Parameters
 * @author: Hans-Jurgen Greiner
 */
public class GetProxyAuthParameters implements AuthorizationPrompter
{

  private String proxyScheme = null;
  private String proxyName = null;
  private String proxyPswd = null;
  private String proxyRealm = null;
  private String proxyPort = null;
  private String proxyHost = null;
  private boolean been_here = false;

  private boolean success = true;
  private java.lang.String message;
  /**
   * Method to get the information message 
   * @return java.lang.String
   */
  public java.lang.String getMessage()
  {
    return message;
  }
  /**
   * Method to get the proxy Host Domain
   * @return java.lang.String
   */
  public java.lang.String getProxyHost()
  {
    return proxyHost;
  }
  /**
   * Method to get the proxy User ID Name
   * @return java.lang.String
   */
  public java.lang.String getProxyName()
  {
    return proxyName;
  }
  /**
   * Method to get the proxy Port
   * @return int
   */
  public String getProxyPort()
  {
    return proxyPort;
  }
  /**
   * Method to get the proxy Password
   * @return java.lang.String
   */
  public java.lang.String getProxyPswd()
  {
    return proxyPswd;
  }
  /**
   * Method to get the proxy Realm
   * @return java.lang.String
   */
  public java.lang.String getProxyRealm()
  {
    return proxyRealm;
  }
  /**
   * Method to get the proxy Scheme
   * @return java.lang.String
   */
  public java.lang.String getProxyScheme()
  {
    return proxyScheme;
  }
  /**
   * Method to Handle a response when authorization fails - It calls back to
   * the main appliction and provides the details required to populate 
   * correct info to pass a URL
   * @return challenge The Authorization information
   * @param forProxy boolean value if we are attempting a proxy bypass
   */
  public NVPair getUsernamePassword(AuthorizationInfo challenge, boolean forProxy)
  {
    if (forProxy && proxyName != null)
    {
      if (been_here)
      {
        System.err.println();
        System.err.println("Proxy authorization failed");
        this.setSuccess(false);
        return null;
      }

      been_here = true;
      return new NVPair(proxyName, proxyPswd);
    }

    if (been_here)
    {
      System.err.println();
      System.err.println("Proxy authorization succeeded");
    }

    // print out all challenge info

    System.err.println();
    if (forProxy)
    {
      System.err.println("The proxy requires authorization");
    }
    else
    {
      System.err.println("The server requires authorization for this resource");
    }

    this.setSuccess(true);
    this.setProxyScheme(challenge.getScheme());
    this.setProxyRealm(challenge.getRealm());
    this.setProxyHost(challenge.getHost());
    this.setProxyPort(challenge.getPort());

    System.err.println();
    System.err.println("Proxy scheme is " + challenge.getScheme());
    System.err.println("Add the following line near the beginning of your application:");
    System.err.println();

    String solution = null;
    if (challenge.getScheme().equalsIgnoreCase("Basic"))
    {
      solution =
        "    AuthorizationInfo.addBasicAuthorization(\""
          + challenge.getHost()
          + "\", "
          + challenge.getPort()
          + ", \""
          + challenge.getRealm()
          + "\", "
          + "<username>, <password>);";
    }
    else if (challenge.getScheme().equalsIgnoreCase("Digest"))
    {
      solution =
        "    AuthorizationInfo.addDigestAuthorization(\""
          + challenge.getHost()
          + "\", "
          + challenge.getPort()
          + ", \""
          + challenge.getRealm()
          + "\", "
          + "<username>, <password>);";
    }
    else
    {
      solution =
        "    AuthorizationInfo.addAuthorization(\""
          + challenge.getHost()
          + "\", "
          + challenge.getPort()
          + ", \""
          + challenge.getScheme()
          + "\", \""
          + challenge.getRealm()
          + "\", "
          + "...);";
    }

    this.setMessage(solution);
    System.err.println(solution);
    System.err.println();

    return null;
  }
  /**
   * Method to ask if it was succeesful operation
   * @return boolean
   */
  public boolean isSuccess()
  {
    return success;
  }
  /**
   * Method to set the information Message
   * @param newMessage java.lang.String
   */
  protected void setMessage(java.lang.String newMessage)
  {
    if (message == null)
    {
      message = newMessage;
    }
    else
    {
      message += "\n" + newMessage;
    }
  }
  /**
   * Method to set the proxy Host domain
   * @param newProxyHost java.lang.String
   */
  public void setProxyHost(java.lang.String newProxyHost)
  {
    System.out.println("Host:  " + newProxyHost);
    proxyHost = newProxyHost;
  }
  /**
   * Method to set the proxy User ID Name
   * @param newProxyName java.lang.String
   */
  public void setProxyName(java.lang.String newProxyName)
  {
    System.out.println("Name:  " + newProxyName);
    proxyName = newProxyName;
  }
  /**
   * Method to set the proxy Port
   * @param newProxyPort int
   */
  public void setProxyPort(int newProxyPort)
  {
    System.out.println("Port:  " + newProxyPort);
    proxyPort = "" + newProxyPort;
  }
  /**
   * Method to set the proxy Port
   * @param newProxyPort String
   */
  public void setProxyPort(String newProxyPort)
  {
    proxyPort = newProxyPort;
  }
  /**
   * Method to set the proxy Password
   * @param newProxyPswd java.lang.String
   */
  public void setProxyPswd(java.lang.String newProxyPswd)
  {
    proxyPswd = newProxyPswd;
  }
  /**
   * Method to set the proxy realm
   * @param newProxyRealm java.lang.String
   */
  public void setProxyRealm(java.lang.String newProxyRealm)
  {
    System.out.println("Realm:  " + newProxyRealm);
    proxyRealm = newProxyRealm;
  }
  /**
   * Method to set the proxy Scheme
   * @param newProxyScheme java.lang.String
   */
  public void setProxyScheme(java.lang.String newProxyScheme)
  {
    System.out.println("Scheme: " + newProxyScheme);
    proxyScheme = newProxyScheme;
  }
  /**
   * Method to set the successfule operation
   * @param newSuccess boolean
   */
  protected void setSuccess(boolean newSuccess)
  {
    success = newSuccess;
  }
  /**
   * This method will test the URL http://www.msn.com
   * if it is success we will see the results in an output file.
   * if it is a failure we will also see the results and what is 
   * required to pass through the proxy.
   */
  public void testURL()
  {

    BufferedReader in = null;
    PrintStream holdErr = System.err;
    String errMsg = "";
    try
    {

      this.setSuccess(false);

      PrintStream fileout = new PrintStream(new FileOutputStream("testURLFile.html"));
      System.setErr(fileout);
      System.err.println("testURL() - entry");

      System.getProperties().put("https.proxyHost", "" + this.getProxyHost());
      System.getProperties().put("https.proxyPort", "" + this.getProxyPort());
      System.getProperties().put("http.proxyHost", "" + this.getProxyHost());
      System.getProperties().put("http.proxyPort", "" + this.getProxyPort());
      //      System.getProperties().put("java.protocol.handler.pkgs", "com.sun.net.ssl.internal.www.protocol");

      URL url = new URL("http://www.msn.com");

      CookieModule.setCookiePolicyHandler(null); // Accept all cookies
      // Set the Authorization Handler
      // This will let us know waht we are missing
      DefaultAuthHandler.setAuthorizationPrompter(this);
      HTTPConnection con = new HTTPConnection(url);
      con.setDefaultHeaders(new NVPair[] { new NVPair("User-Agent", "Mozilla/4.5")});
      con.setDefaultAllowUserInteraction(false);
      HTTPResponse headRsp = con.Head(url.getFile());
      HTTPResponse rsp = con.Get(url.getFile());

      int sts = headRsp.getStatusCode();
      if (sts < 300)
      {
        System.err.println("No authorization required to access " + url);
        this.setSuccess(true);
      }
      else if (sts >= 400 && sts != 401 && sts != 407)
      {
        System.err.println("Error trying to access " + url + ":\n" + headRsp);
      }
      else if (sts == 407)
      {
        System.err.println(
          "Error trying to access "
            + url
            + ":\n"
            + headRsp
            + "\n"
            + "<HTML><HEAD><TITLE>Proxy authorization required</TITLE></HEAD>");
        this.setMessage("Error trying to access " + url + ":\n" + headRsp + "\n" + "Proxy authorization required");
      }
      // Start reading input
      in = new BufferedReader(new InputStreamReader(rsp.getInputStream()));
      String inputLine;

      while ((inputLine = in.readLine()) != null)
      {
        System.err.println(inputLine);
      }
      // All Done - We were success

      in.close();

    }
    catch (ModuleException exc)
    {
      errMsg = "ModuleException caught: " + exc;
      errMsg += "\nVerify Host Domain address and Port";
      System.err.println(errMsg);
    }
    catch (ProtocolNotSuppException exc)
    {
      errMsg = "ProtocolNotSuppException caught: " + exc;
      errMsg += "\nVerify Host Domain address and Port";
      System.err.println(errMsg);
    }
    catch (MalformedURLException exc)
    {
      errMsg = "MalformedURLException caught: " + exc;
      errMsg += "\nVerify Host Domain address and Port";
      System.err.println(errMsg);
    }
    catch (FileNotFoundException exc)
    {
      errMsg = "FileNotFoundException caught: " + exc;
      errMsg += "\nVerify Host Domain address and Port";
      System.err.println(errMsg);
    }
    catch (IOException exc)
    {
      errMsg = "IOException caught: " + exc;
      errMsg += "\nVerify Host Domain address and Port";
      System.err.println(errMsg);
    }
    finally
    {
      System.err.println("testURL() - exit");
      System.setErr(holdErr);
      if (in != null)
      {
        try
        {
          in.close(); // ENSURE we are CLOSED
        }
        catch (Exception exc)
        {
          // Do Nothing, It will be throwing an exception 
          // if it cannot close the buffer
        }
      }
      this.setMessage(errMsg);
      System.gc();
    }

  }

  /**
   * GetProxyAuthParameters constructor comment.
   * @param host the host domain of a proxy
   * @param port the port of the domain for the proxy
   */
  public GetProxyAuthParameters(String host, int port)
  {
    super();
    clearData();
    setProxyHost(host);
    setProxyPort(port);
  }

  /**
   * GetProxyAuthParameters constructor comment.
   * @param host the host domain of a proxy
   * @param port the port of the domain for the proxy
   */
  public GetProxyAuthParameters(String host, String port)
  {
    super();
    clearData();
    setProxyHost(host);
    setProxyPort(port);
  }

  /**
   * Method to clear the proxy data
   */
  protected void clearData()
  {
    System.getProperties().put("https.proxyHost", "");
    System.getProperties().put("https.proxyPort", "");
    System.getProperties().put("http.proxyHost", "");
    System.getProperties().put("http.proxyPort", "");
    proxyScheme = null;
    proxyName = null;
    proxyPswd = null;
    proxyRealm = null;
    proxyPort = null;
    proxyHost = null;
    been_here = false;
    success = false;
    message = null;
  }
}
