package org.ecmkit.registration;

import java.io.Serializable;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.action.executer.MailActionExecuter;
import org.alfresco.repo.model.Repository;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.admin.RepoAdminService;
import org.alfresco.service.cmr.invitation.InvitationException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.TemplateService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.util.ModelUtil;
import org.alfresco.util.ParameterCheck;

public class RegistrationSender {
	public static final String WF_INVITATION_ID = "wf_invitationId";
	public static final String WF_INSTANCE_ID = "wf_instanceId";
  public static final String WF_PACKAGE = "wf_package";
  
  
//workflow execution context variable names
  public static final String wfVarInviteeUserName = "regi_inviteeUserName";
  public static final String wfVarInviterUserName = "regi_inviterUserName";
  public static final String wfVarResourceName = "regi_resourceName";
  public static final String wfVarResourceTitle = "regi_resourceTitle";
  public static final String wfVarResourceDescription = "regi_resourceDescription";
  public static final String wfVarResourceType = "regi_resourceType";
  public static final String wfVarWorkflowInstanceId = "workflowinstanceid";
  public static final String wfVarRole = "regi_inviteeRole";
  public static final String wfVarInviteTicket = "regi_inviteTicket";
  public static final String wfVarServerPath = "regi_serverPath";
  public static final String wfVarAcceptUrl = "regi_acceptUrl";
  public static final String wfVarRejectUrl = "regi_rejectUrl";
  public static final String wfVarInviteeGenPassword = "regi_inviteeGenPassword";
  public static final String wfEmail = "regi_inviteeEmail";
  
  private ActionService actionService;
  private SearchService searchService;
  private Repository repository;
  private NamespaceService namespaceService;
  private FileFolderService fileFolderService;
  
  
  private RepoAdminService repoAdminService;
  
  private static final List<String> expectedProperties = Arrays.asList(
//		  wfVarInviteeUserName,//
//          wfVarResourceName,//
//          wfVarInviterUserName,//
//          wfVarInviteeUserName,//
//          wfVarRole,//
//          wfVarInviteeGenPassword,//
//          wfVarResourceName,//
          wfVarInviteTicket,//
          wfVarServerPath,//
          wfVarAcceptUrl,//
//          wfVarRejectUrl, 
          WF_INVITATION_ID,//
          WF_PACKAGE, wfEmail);
  
  public RegistrationSender(ServiceRegistry services, Repository repository) {
	  this.actionService = services.getActionService();
      this.searchService = services.getSearchService();
      this.fileFolderService = services.getFileFolderService();
//      this.sysAdminParams = services.getSysAdminParams();
      this.repoAdminService = services.getRepoAdminService();
      this.namespaceService = services.getNamespaceService();
      this.repository = repository;

  }
  

  
  public void sendMail(Map<String, String> properties)
  {
      checkProperties(properties);
      ParameterCheck.mandatory("Properties", properties);
//      NodeRef inviter = personService.getPerson(properties.get(wfVarInviterUserName));
//      String inviteeName = properties.get(wfVarInviteeUserName);
//      NodeRef invitee = personService.getPerson(inviteeName);
      Action mail = actionService.createAction(MailActionExecuter.NAME);
      mail.setParameterValue(MailActionExecuter.PARAM_FROM, "rock@ecmkit.com");
      mail.setParameterValue(MailActionExecuter.PARAM_TO, properties.get(wfEmail));
      mail.setParameterValue(MailActionExecuter.PARAM_SUBJECT, "Cloud Registration");
      mail.setParameterValue(MailActionExecuter.PARAM_TEMPLATE, getEmailTemplateNodeRef());
      mail.setParameterValue(MailActionExecuter.PARAM_TEMPLATE_MODEL, 
              (Serializable)buildMailTextModel(properties));
      mail.setParameterValue(MailActionExecuter.PARAM_IGNORE_SEND_FAILURE, true);
      actionService.executeAction(mail, getWorkflowPackage(properties));
  }
  
  /**
   * @param properties
   */
  private void checkProperties(Map<String, String> properties)
  {
      Set<String> keys = properties.keySet();
      if (!keys.containsAll(expectedProperties))
      {
          LinkedList<String> missingProperties = new LinkedList<String>(expectedProperties);
          missingProperties.removeAll(keys);
          throw new InvitationException("The following mandatory properties are missing:\n" + missingProperties);
      }
  }
  
  
  private NodeRef getEmailTemplateNodeRef()
  {
      List<NodeRef> nodeRefs = searchService.selectNodes(repository.getRootHome(), 
                  "app:company_home/app:dictionary/app:email_templates/cm:registration/cm:registration-email_zh_CN.html.ftl", null, 
                  this.namespaceService, false);
      
      if (nodeRefs.size() == 1) 
      {
          // Now localise this
          NodeRef base = nodeRefs.get(0);
          NodeRef local = fileFolderService.getLocalizedSibling(base);
          return local;
      }
      else
      {
          throw new InvitationException("Cannot find the email template!");
      }
  }
  
  
  private Map<String, Serializable> buildMailTextModel(Map<String, String> properties)
  {
      // Set the core model parts
      // Note - the user part is skipped, as that's implied via the run-as
      Map<String, Serializable> model = new HashMap<String, Serializable>();
      model.put(TemplateService.KEY_COMPANY_HOME, repository.getCompanyHome());
//      model.put(TemplateService.KEY_USER_HOME, repository.getUserHome(repository.getPerson()));
      model.put(TemplateService.KEY_PRODUCT_NAME, ModelUtil.getProductName(repoAdminService));

      // Build up the args for rendering inside the template
      Map<String, String> args = buildArgs(properties);
      model.put("args", (Serializable)args);
      
      // All done
      return model;
  }
  
  private Map<String, String> buildArgs(Map<String, String> properties)
  {
      String params = buildUrlParamString(properties);
      String registrationLink = makeLink(properties.get(wfVarServerPath), properties.get(wfVarAcceptUrl), params);
//      String rejectLink = makeLink(properties.get(wfVarServerPath), properties.get(wfVarRejectUrl), params);

      Map<String, String> args = new HashMap<String, String>();
//      args.put("inviteePersonRef", invitee);
//      args.put("inviterPersonRef", inviter);
//      args.put("siteName", getSiteName(properties));
//      args.put("inviteeSiteRole", getRoleName(properties));
//      args.put("inviteeUserName", properties.get(wfVarInviteeUserName));
//      args.put("inviteeGenPassword", properties.get(wfVarInviteeGenPassword));
      args.put("registrationLink", registrationLink);
//      args.put("rejectLink", rejectLink);
      return args;
  }
  
  private String buildUrlParamString(Map<String, String> properties)
  {
      StringBuilder params = new StringBuilder("?inviteId=");
      params.append(properties.get(WF_INVITATION_ID));
//      params.append("&inviteeUserName=");
//      params.append(URLEncoder.encode(properties.get(wfVarInviteeUserName)));
//      params.append("&siteShortName=");
//      params.append(properties.get(wfVarResourceName));
      params.append("&inviteTicket=");
      params.append(properties.get(wfVarInviteTicket));
      params.append("&instanceId=");
      params.append(properties.get(WF_INSTANCE_ID));
      return params.toString();
  }
  
  protected String makeLink(String location, String endpoint, String queryParams)
  {
      location = location.endsWith("/") ? location : location + "/";
      endpoint = endpoint.startsWith("/") ? endpoint.substring(1) : endpoint;
      queryParams = queryParams.startsWith("?") ? queryParams : "?" + queryParams;
      return location + endpoint + queryParams;
  }
  
  private NodeRef getWorkflowPackage(Map<String, String> properties)
  {
      String packageRef = properties.get(WF_PACKAGE);
      return new NodeRef(packageRef);
  }

}
