package org.ecmkit.service.impl;

import org.alfresco.repo.site.SiteModel;
import org.ecmkit.service.CreateEntityService;
import org.ecmkit.service.NetworkRequestService;

import junit.framework.TestCase;

public class CreateEntityServiceImplTest extends TestCase {
	
	NetworkRequestServiceImpl networkRequestService = null;
	CreateEntityServiceImpl createEntityService = null;
	private static final String createSiteUrl = "share/service/modules/create-site";
	private static final String personUrl = "alfresco/service/api/people";
	private static final String addMemberUrl = "alfresco/service/api/sites";
	private static final String deleteMemberUrl = "alfresco/service/api/sites";
	private static final String powerUser = "admin";
	private static final String powerPasswd = "admin";
	private static final String shareLoginUrl = "share/page/dologin";
	private static final String hostUrl = "http://localhost:8080/";
	private static final String ticketUrl ="alfresco/service/api/login";
	
	private static final String userEmail = "liming.zhu@alcatel-sbell.com.cn";
	private static final String siteTitle = "Personal Site";
	private static final String siteDesc = siteTitle + "'s Home";
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		networkRequestService = new NetworkRequestServiceImpl();
		networkRequestService.setHostUrl(hostUrl);
		networkRequestService.setTicketUrl(ticketUrl);
		
		createEntityService = new CreateEntityServiceImpl();
		createEntityService.setAddMemberUrl(addMemberUrl);
		createEntityService.setCreateSiteUrl(createSiteUrl);
		createEntityService.setDeleteMemberUrl(deleteMemberUrl);
		createEntityService.setNetworkRequestService(networkRequestService);
		createEntityService.setPersonUrl(personUrl);
		createEntityService.setPowerPasswd(powerPasswd);
		createEntityService.setPowerUser(powerUser);
		createEntityService.setShareLoginUrl(shareLoginUrl);
	}
	
    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
    }
    
//	public void testCreateSite() throws Exception {
//		createEntityService.createSite(userEmail, siteTitle, siteDesc, false);
//	}
	
//	public void testAddMember() throws Exception {
//		createEntityService.addMember(userEmail, userEmail, SiteModel.SITE_MANAGER);
//	}
	
	public void testRemoveMember() throws Exception {
		createEntityService.removeMember(userEmail, "admin");
	}

}
