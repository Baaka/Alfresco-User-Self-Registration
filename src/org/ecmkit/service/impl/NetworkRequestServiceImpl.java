package org.ecmkit.service.impl;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.alfresco.repo.webservice.authentication.AuthenticationFault;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ecmkit.service.NetworkRequestService;
import org.json.simple.parser.JSONParser;

public class NetworkRequestServiceImpl implements NetworkRequestService {

	private static final String HEADER_CONTENT_TYPE = "Content-Type";
	private static final String CONTENT_TYPE_JSON = "application/json";
	private static final String UTF_8 = "UTF-8";
	private static final String CONTENT_TYPE_TEXT_PLAIN = "text/plain";
	private static final Log logger = LogFactory.getLog(NetworkRequestServiceImpl.class);
	private String hostUrl;
	private String ticketUrl;
	
	@Override
	public void makePostCall(HttpClient httpClient, String url, String ticket,
			String data, String dataType, String callName, int expectedStatus) {
		PostMethod createSiteMethod = null;
		url = hostUrl + url;
		if(ticket != null) {
			url = url + "?alf_ticket=" + ticket;
		}
		try {
			// Create post method
			//createSiteMethod = createPostMethod(url + "/s/api/sites?alf_ticket=" + ticket, createSiteBody, CONTENT_TYPE_JSON);
			createSiteMethod = createPostMethod(url, data, dataType);
			
			if (logger.isDebugEnabled())
				logger.debug("Trying to make call with name: " + callName
						+ ". URL: " + createSiteMethod.getURI());

			// Execute the post method
			int createSiteStatus = httpClient.executeMethod(createSiteMethod);

			createSiteMethod.getResponseBody();

			if (logger.isDebugEnabled())
				logger.debug("Create site method returned status: "
						+ createSiteStatus);


		} catch (HttpException he) {
			if (logger.isDebugEnabled())
				logger.debug("Fail to execute call with name: " + callName
						+ ". Message: " + he.getMessage());
			// throw new RuntimeException(he);
		} catch (AuthenticationFault ae) {
//			if (logger.isDebugEnabled())
//				logger.debug("Fail to create site with name: " + shortName
//						+ ". Message: " + ae.getMessage());
			// throw new RuntimeException(ae);
		} catch (IOException ioe) {
//			if (logger.isDebugEnabled())
//				logger.debug("Fail to create site with name: " + shortName
//						+ ". Message: " + ioe.getMessage());

			// throw new RuntimeException(ioe);
		} finally {
			createSiteMethod.releaseConnection();
		}
		
	}

	@Override
	public void makeDeleteCall(HttpClient httpClient, String url,
			String callName, int expectedStatus) {
		DeleteMethod deleteMethod = null;
		url = hostUrl + url;
		try {
			deleteMethod = new DeleteMethod(url);;

			if (logger.isDebugEnabled())
				logger.debug("Trying to make call with name: " + callName
						+ ". URL: " + deleteMethod.getURI());

			// Execute the post method
			int createSiteStatus = httpClient.executeMethod(deleteMethod);

			deleteMethod.getResponseBody();

			if (logger.isDebugEnabled())
				logger.debug("Create site method returned status: "
						+ createSiteStatus);


		} catch (HttpException he) {
			if (logger.isDebugEnabled())
				logger.debug("Fail to execute call with name: " + callName
						+ ". Message: " + he.getMessage());
		} catch (AuthenticationFault ae) {

		} catch (IOException ioe) {

		} finally {
			deleteMethod.releaseConnection();
		}
		
	}
	
	
	/**
	 * 
	 * Creates POST method
	 * 
	 * 
	 * 
	 * @param url
	 *            URL for request
	 * 
	 * @param body
	 *            body of request
	 * 
	 * @param contentType
	 *            content type of request
	 * 
	 * @return POST method
	 * 
	 * @throws UnsupportedEncodingException
	 */

	private PostMethod createPostMethod(String url, String body,
			String contentType) throws UnsupportedEncodingException {
		PostMethod postMethod = new PostMethod(url);
		postMethod.setRequestHeader(HEADER_CONTENT_TYPE, contentType);
		postMethod.setRequestEntity(new StringRequestEntity(body, CONTENT_TYPE_TEXT_PLAIN, UTF_8));

		return postMethod;
	}

	@Override
	public String getSessionTicket(String useName, String password) {
		 String _ticket = "";
		  URL url;

		  HttpURLConnection connection = null;

		  try {
		     String urlParameters = "{ \"username\" : \"" + useName +"\", \"password\" : \"" + password +"\" }";

		     // Create connection
		     url = new URL(hostUrl + ticketUrl);
		     connection = (HttpURLConnection) url.openConnection();
		     connection.setRequestMethod("POST");
		     connection.setRequestProperty("Content-Type", "application/json");
		     connection.setRequestProperty("Content-Length", "" + Integer.toString(urlParameters.getBytes().length));
		     connection.setRequestProperty("Content-Language", "en-US");
		     connection.setUseCaches(false);
		     connection.setDoInput(true);
		     connection.setDoOutput(true);

		     // Send request

		     DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
		     wr.writeBytes(urlParameters);
		     wr.flush();
		     wr.close();

		     // Get Response
		     InputStream is = connection.getInputStream();
		     BufferedReader rd = new BufferedReader(new InputStreamReader(is));

		     String line;
		     StringBuffer response = new StringBuffer();
		     while ((line = rd.readLine()) != null) {
		        response.append(line);
		        response.append('\r');
		       }

		     rd.close();

		     String _jsonResponse = response.toString();

		     org.json.simple.JSONObject _jsonResponseObject = (org.json.simple.JSONObject) new JSONParser().parse(_jsonResponse);
		     org.json.simple.JSONObject jsonDataObject = (org.json.simple.JSONObject)new JSONParser().parse(_jsonResponseObject.get("data").toString());

		      _ticket = jsonDataObject.get("ticket").toString();
		  } catch (Exception e) {
		     e.printStackTrace();
		     return null;
		  } finally {
		     if (connection != null) {
		        connection.disconnect();
		     }
		  }
		  return _ticket;
	}
	
//	private boolean doLogin(HttpClient httpClient, String url, String data, String dataType) {	
//		PostMethod postMethod = null;
//
//        try {
//            postMethod = createPostMethod(url, data, dataType);
//            httpClient.executeMethod(postMethod);
//        } catch (HttpException he) {
////            logger.error("Failed to " + callName, he);
//        } catch (AuthenticationFault ae) {
////            logger.error("Failed to " + callName, ae);
//        } catch (IOException ioe) {
////            logger.error("Failed to " + callName, ioe);
//        } finally {
//            postMethod.releaseConnection();
//        }
//        return true;
//    }

	public String getHostUrl() {
		return hostUrl;
	}

	public void setHostUrl(String hostUrl) {
		this.hostUrl = hostUrl;
	}

	public String getTicketUrl() {
		return ticketUrl;
	}

	public void setTicketUrl(String ticketUrl) {
		this.ticketUrl = ticketUrl;
	}

}
