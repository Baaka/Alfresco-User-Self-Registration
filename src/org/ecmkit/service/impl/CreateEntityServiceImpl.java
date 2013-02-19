package org.ecmkit.service.impl;

import org.alfresco.repo.site.script.ScriptSiteService;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.ecmkit.service.CreateEntityService;
import org.ecmkit.service.NetworkRequestService;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.surf.util.URLEncoder;

public class CreateEntityServiceImpl implements CreateEntityService {
	
	private NetworkRequestService networkRequestService;
	private static final String CONTENT_TYPE_JSON = "application/json";
	public static final String FORM_CONTENT_TYPE = "application/x-www-form-urlencoded";
	private String createSiteUrl;
	private String deleteMemberUrl;
	private String addMemberUrl;
	private String personUrl;
	private String powerUser;
	private String powerPasswd;
	private String shareLoginUrl;

	@Override
	public boolean createSite(String shortName, String title,
			String description, boolean isPublic) {
		
		boolean siteCreated = false;
//		String loginData = "username=" + URLEncoder.encode(shortName) + "&password=" + URLEncoder.encode(password);
//		String loginData = "username=admin&password=admin";
//		doLogin(httpClient, baseUrl + loginUrl, loginData, FORM_CONTENT_TYPE);
		// String createSiteBody =
		// "{\"isPublic\"smiley"" + isPublic + "\",\"title\"smiley"" + title + "\",\"shortName\"smiley"" + shortName + "\","
		// + "\"description\"smiley"" + description

		// + "\",\"sitePreset\"smiley"" + sitePreset + "\"" + (isPublic ?
		// ",\"alfresco-createSite-instance-isPublic-checkbox\"smiley"on\"}" :
		// "}"smiley;
		String createSiteBody = null;
		try {
			JSONObject site = new JSONObject();
			site.put("sitePreset", "site-dashboard");
			site.put("shortName", shortName);
			site.put("title", title);
			site.put("description", description);
			site.put("visibility", ScriptSiteService.PRIVATE_SITE);

			createSiteBody = site.toString();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//login share first
		HttpClient httpClient = new HttpClient();
		String loginData = "username=" + powerUser + "&password=" + powerPasswd;
		networkRequestService.makePostCall(httpClient, shareLoginUrl, null, loginData, FORM_CONTENT_TYPE, "login share", HttpStatus.SC_OK);
		//create site
		networkRequestService.makePostCall(httpClient, createSiteUrl, null, createSiteBody, CONTENT_TYPE_JSON, "Create Site", HttpStatus.SC_OK);
		return false;
	}

	@Override
	public boolean removeMember(String shortName,
			String authority) {
		HttpClient httpClient = new HttpClient();
		String ticket = networkRequestService.getSessionTicket(powerUser, powerPasswd);
		networkRequestService.makeDeleteCall(httpClient, deleteMemberUrl + "/" + URLEncoder.encode(shortName) + "/memberships/" + URLEncoder.encode(authority) + "?alf_ticket=" + ticket, "Delete member", HttpStatus.SC_OK);
		return false;
	}

	@Override
	public boolean addMember(String shortName,
			String authority, String role) {
		JSONObject membership = new JSONObject();
        try {
			membership.put("role", role);
			JSONObject person = new JSONObject();
	        person.put("userName", authority);
	        membership.put("person", person);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
       HttpClient httpClient = new HttpClient();
       String ticket = networkRequestService.getSessionTicket("admin", "admin");
		networkRequestService.makePostCall(httpClient, addMemberUrl + "/" + shortName + "/memberships", ticket, membership.toString(), CONTENT_TYPE_JSON, "Add Site Member", HttpStatus.SC_OK);
		return false;
	}

	@Override
	public boolean addPerson(String email, String firstName,
			String lastName, String password) {
		JSONObject newPerson = new JSONObject();
		try {
			newPerson.put("userName", email);
			newPerson.put("firstName", firstName);
			newPerson.put("lastName", lastName);
			newPerson.put("email", email);
			newPerson.put("password", password);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	        
	        
	        //String url = baseUrl + deleteMemberUrl + "/" + shortName + "/memberships";
//	        String url = baseUrl + "alfresco/service/api/people";
//	        String url = baseUrl + personUrl;
//	        String ticket = getAlfTicket(baseUrl, "admin", "admin");
//	        String ticket = null;
//			ticket = callLoginWebScript("http://localhost:8080/alfresco/service", "admin", "admin");
//			String response = callInOutWebScript(url, "POST", ticket, newPerson.toString());
	       HttpClient httpClient = new HttpClient();
			String ticket = networkRequestService.getSessionTicket("admin", "admin");
			networkRequestService.makePostCall(httpClient, personUrl, ticket, newPerson.toString(), CONTENT_TYPE_JSON, "Create Site", HttpStatus.SC_OK);
//				response = null;


			return true;
	}

	public NetworkRequestService getNetworkRequestService() {
		return networkRequestService;
	}

	public void setNetworkRequestService(NetworkRequestService networkRequestService) {
		this.networkRequestService = networkRequestService;
	}

	public String getCreateSiteUrl() {
		return createSiteUrl;
	}

	public void setCreateSiteUrl(String createSiteUrl) {
		this.createSiteUrl = createSiteUrl;
	}

	public String getDeleteMemberUrl() {
		return deleteMemberUrl;
	}

	public void setDeleteMemberUrl(String deleteMemberUrl) {
		this.deleteMemberUrl = deleteMemberUrl;
	}

	public String getAddMemberUrl() {
		return addMemberUrl;
	}

	public void setAddMemberUrl(String addMemberUrl) {
		this.addMemberUrl = addMemberUrl;
	}

	public String getPersonUrl() {
		return personUrl;
	}

	public void setPersonUrl(String personUrl) {
		this.personUrl = personUrl;
	}

	public String getPowerUser() {
		return powerUser;
	}

	public void setPowerUser(String powerUser) {
		this.powerUser = powerUser;
	}

	public String getPowerPasswd() {
		return powerPasswd;
	}

	public void setPowerPasswd(String powerPasswd) {
		this.powerPasswd = powerPasswd;
	}

	public String getShareLoginUrl() {
		return shareLoginUrl;
	}

	public void setShareLoginUrl(String shareLoginUrl) {
		this.shareLoginUrl = shareLoginUrl;
	}
}
