package org.ecmkit.service;

import org.apache.commons.httpclient.HttpClient;

public interface NetworkRequestService {
	public void makePostCall(HttpClient httpClient, String url, String ticket, String data, String dataType,
            String callName, int expectedStatus);
	public void makeDeleteCall(HttpClient httpClient, String url, String callName, int expectedStatus);
	public String getSessionTicket(String useName, String password);
}
