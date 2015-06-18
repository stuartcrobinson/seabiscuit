package utilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.io.IOUtils;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.DomSerializer;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.w3c.dom.Document;

/**
 * A utility that downloads a file from a URL.
 *
 * originally from  www.codejava.net
 *
 */
public class HttpDownloadUtility {

    private static final int BUFFER_SIZE = 4096;

    public static List<String> getFile(String urlStr) throws IOException, InterruptedException {

	List<String> lines = new ArrayList();

	try (BufferedReader br = new BufferedReader(new InputStreamReader(getWebInputStream(urlStr)))) {
	    String line;
	    while ((line = br.readLine()) != null)
		lines.add(line);
	}
	return lines;
    }

    public static String getFileSt(String fileURL) throws IOException, InterruptedException {

	List<String> lines = getFile(fileURL);

	StringBuilder sb = new StringBuilder();

	for (String line : lines)
	    sb.append(line).append(System.lineSeparator());

	return sb.toString();
    }

    /** is persistent by default */
    public static void downloadFile(String urlStr, File file) throws IOException, InterruptedException {
	InputStream is = getWebInputStream(urlStr, true);
	downloadFile(is, file);
    }

    public static void downloadFile(String urlStr, File file, Boolean be_persistent) throws IOException, InterruptedException {
	InputStream is = getWebInputStream(urlStr, be_persistent);
	downloadFile(is, file);
    }

    public static void downloadFile(InputStream is, File file) throws IOException, InterruptedException {

	try (FileOutputStream outputStream = new FileOutputStream(file)) {
	    int bytesRead;
	    byte[] buffer = new byte[BUFFER_SIZE];
	    while ((bytesRead = is.read(buffer)) != -1) {
		outputStream.write(buffer, 0, bytesRead);
	    }
	    System.out.print(" - " + file.getName() + " downloaded");
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    /** presistent by default */
    static InputStream getWebInputStream(String urlSt) throws IOException, PermissionDeniedWebException, InterruptedException {
	return getWebInputStream(urlSt, true);
    }

    /** ONLY use this for web access.  DO NOT access web in any other way! except selenium */
    static InputStream getWebInputStream(String urlSt, Boolean be_persistent) throws IOException, PermissionDeniedWebException, Bad_Gateway_Exception, InterruptedException {

	URL url;
	url = new URL(urlSt);
	InputStream is;

	try {
	    HttpURLConnection httpConn = (HttpURLConnection)url.openConnection();
	    httpConn.setConnectTimeout(40_000);
	    httpConn.setReadTimeout(40_000);
	    is = httpConn.getInputStream();
	} catch (IOException e) {
	    System.out.println("first exception for:\n" + urlSt);
	    if (e.toString().contains("code: 500 for URL"))
		throw new PermissionDeniedWebException(e.toString());
	    if (e.toString().contains("code: 502 for URL"))
		throw new Bad_Gateway_Exception(e.toString());
	    if (!be_persistent) {
		throw e;
	    } else {
		try {
		    System.out.println("failed to load page. try again in 1 seconds..." + urlSt);
		    Thread.sleep(1000);
		    HttpURLConnection httpConn = (HttpURLConnection)url.openConnection();
		    is = httpConn.getInputStream();
		    System.out.println("it worked the 2nd time! " + urlSt);
		} catch (IOException e2) {
		    System.out.println("failed 2nd time. try again in 4 seconds..." + urlSt);
		    Thread.sleep(4000);
		    HttpURLConnection httpConn = (HttpURLConnection)url.openConnection();
		    try {
			is = httpConn.getInputStream();
			System.out.println("it worked the 3rd time! " + urlSt);
		    } catch (Exception e3) {
			System.out.println("failed the 3rd time. giving up " + urlSt);
			throw e3;
		    }
		}
	    }
	}
	return is;
    }

    /** cascading try's cos once IOUtils threw a java.net.SocketTimeoutException */
    public static String getPageSource(String urlStr) throws InterruptedException, PermissionDeniedWebException, IOException {
	
	boolean persistance = true;
	
	try {
	    return IOUtils.toString(getWebInputStream(urlStr, persistance));
	} catch (PermissionDeniedWebException pdex) {
	    throw pdex;
	} catch (IOException ex) {
	    try {
		return IOUtils.toString(getWebInputStream(urlStr, persistance));
	    } catch (IOException ex1) {
		return IOUtils.toString(getWebInputStream(urlStr, persistance));
	    }
	}
    }
    /** cascading try's cos once IOUtils threw a java.net.SocketTimeoutException */
    public static String getPageSource(String urlStr, boolean persistance) throws InterruptedException, PermissionDeniedWebException, IOException {
	
	try {
	    return IOUtils.toString(getWebInputStream(urlStr, persistance));
	} catch (PermissionDeniedWebException pdex) {
	    throw pdex;
	} catch (IOException ex) {
	    try {
		return IOUtils.toString(getWebInputStream(urlStr, persistance));
	    } catch (IOException ex1) {
		return IOUtils.toString(getWebInputStream(urlStr, persistance));
	    }
	}
    }

    public static Document getWebpageDocument(String urlStr) throws InterruptedException, IOException {
	try {
	    HtmlCleaner cleaner = new HtmlCleaner();
	    CleanerProperties props = cleaner.getProperties();
	    props.setAllowHtmlInsideAttributes(true);
	    props.setAllowMultiWordAttributes(true);
	    props.setRecognizeUnicodeChars(true);
	    props.setOmitComments(true);

	    DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
	    DocumentBuilder builder = null;
	    try {
		builder = builderFactory.newDocumentBuilder();
	    } catch (ParserConfigurationException e) {
		e.printStackTrace();
	    }
	    String source = HttpDownloadUtility.getPageSource(urlStr);

	    TagNode tagNode = new HtmlCleaner().clean(source);

	    Document doc = new DomSerializer(new CleanerProperties()).createDOM(tagNode);

	    return doc;
	} catch (ParserConfigurationException ex) {
	    ex.printStackTrace();
	    return null;
	}
    }

    public static Document getWebpageDocument_fromSource(String source) throws InterruptedException, IOException {
	try {
	    HtmlCleaner cleaner = new HtmlCleaner();
	    CleanerProperties props = cleaner.getProperties();
	    props.setAllowHtmlInsideAttributes(true);
	    props.setAllowMultiWordAttributes(true);
	    props.setRecognizeUnicodeChars(true);
	    props.setOmitComments(true);

	    DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
	    DocumentBuilder builder = null;
	    try {
		builder = builderFactory.newDocumentBuilder();
	    } catch (ParserConfigurationException e) {
		e.printStackTrace();
	    }

	    TagNode tagNode = new HtmlCleaner().clean(source);

	    Document doc = new DomSerializer(new CleanerProperties()).createDOM(tagNode);

	    return doc;
	} catch (ParserConfigurationException ex) {
	    ex.printStackTrace();
	    return null;
	}
    }

    public static class PermissionDeniedWebException extends IOException {
	public PermissionDeniedWebException(String toString) {
	}
    }

    /** http error 502.   http://www.checkupdown.com/status/E502.html <br><br> This usually does not mean that the upstream server is down (no response to the gateway/proxy), but rather that the upstream server and the gateway/proxy do not agree on the protocol for exchanging data. Given that Internet protocols are quite clear, it often means that one or both machines have been incorrectly or incompletely programmed. */
    public static class Bad_Gateway_Exception extends IOException {
	public Bad_Gateway_Exception(String toString) {
	}
    }
}
