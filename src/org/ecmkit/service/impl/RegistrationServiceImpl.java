package org.ecmkit.service.impl;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ecmkit.registration.WorkflowModelRegistration;
import org.ecmkit.service.CreateEntityService;
import org.ecmkit.service.RegistrationService;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.admin.SysAdminParams;
import org.alfresco.repo.invitation.WorkflowModelNominatedInvitation;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.security.person.PersonServiceImpl;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.repo.tenant.TenantAdminService;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.repo.workflow.activiti.ActivitiConstants;
import org.alfresco.repo.workflow.jbpm.JBPMEngine;
import org.alfresco.service.cmr.invitation.Invitation;
import org.alfresco.service.cmr.invitation.InvitationException;
import org.alfresco.service.cmr.invitation.InvitationExceptionNotFound;
import org.alfresco.service.cmr.invitation.InvitationExceptionUserError;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.workflow.WorkflowAdminService;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowException;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.alfresco.util.PropertyCheck;
import org.alfresco.util.UrlUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ecmkit.service.CrestSiteService;

import static org.ecmkit.registration.WorkflowModelRegistration.nameSepator;

public class RegistrationServiceImpl implements RegistrationService {
	
	// namespace
    public static final String NAMESPACE_URI = "http://www.alfresco.org/model/workflow/registration/1.0";
	
	// workflow properties
    public static final QName WF_PROP_SERVER_PATH = QName.createQName(NAMESPACE_URI, "serverPath");
    public static final QName WF_PROP_ACCEPT_URL = QName.createQName(NAMESPACE_URI, "acceptUrl");
    public static final QName WF_PROP_REJECT_URL = QName.createQName(NAMESPACE_URI, "rejectUrl");
    public static final QName WF_PROP_INVITE_TICKET = QName.createQName(NAMESPACE_URI, "inviteTicket");
    public static final QName WF_PROP_INVITER_USER_NAME = QName.createQName(NAMESPACE_URI, "inviterUserName");
    public static final QName WF_PROP_INVITEE_USER_NAME = QName.createQName(NAMESPACE_URI, "inviteeUserName");
    public static final QName WF_PROP_INVITEE_EMAIL = QName.createQName(NAMESPACE_URI, "inviteeEmail");
    public static final QName WF_PROP_INVITEE_FIRSTNAME = QName.createQName(NAMESPACE_URI, "inviteeFirstName");
    public static final QName WF_PROP_INVITEE_LASTNAME = QName.createQName(NAMESPACE_URI, "inviteeLastName");
    public static final QName WF_PROP_RESOURCE_TYPE = QName.createQName(NAMESPACE_URI, "resourceType");
    public static final QName WF_PROP_RESOURCE_NAME = QName.createQName(NAMESPACE_URI, "resourceName");
    public static final QName WF_PROP_RESOURCE_TITLE = QName.createQName(NAMESPACE_URI, "resourceTitle");
    public static final QName WF_PROP_RESOURCE_DESCRIPTION = QName.createQName(NAMESPACE_URI, "resourceDescription");   
    public static final QName WF_PROP_INVITEE_ROLE = QName.createQName(NAMESPACE_URI, "inviteeRole");
    public static final QName WF_PROP_INVITEE_GEN_PASSWORD = QName.createQName(NAMESPACE_URI, "inviteeGenPassword");
    
    private SysAdminParams sysAdminParams;
    private PersonService personService;
    private PermissionService permissionService;
    private MutableAuthenticationService authenticationService;
    private NodeService nodeService;
    private WorkflowService workflowService;
    private WorkflowAdminService workflowAdminService;
    private TenantService tenantService;
    private TenantAdminService tenantAdminService;
    private AuthorityService authorityService;
    private static final String SITE_PREFIX = "site_";
    
    private CreateEntityService createEntityService;
	
	/** Logger */
 private static Log logger = LogFactory.getLog(RegistrationServiceImpl.class);
 
 public void init()
 {
     PropertyCheck.mandatory(this, "nodeService", nodeService);
     PropertyCheck.mandatory(this, "WorkflowService", workflowService);
//     PropertyCheck.mandatory(this, "ActionService", actionService);
//     PropertyCheck.mandatory(this, "PersonService", personService);
//     PropertyCheck.mandatory(this, "SiteService", siteService);
//     PropertyCheck.mandatory(this, "AuthenticationService", authenticationService);
//     PropertyCheck.mandatory(this, "PermissionService", permissionService);
//     PropertyCheck.mandatory(this, "NamespaceService", namespaceService);
     PropertyCheck.mandatory(this, "NodeService", nodeService);
//     PropertyCheck.mandatory(this, "UserNameGenerator", usernameGenerator);
//     PropertyCheck.mandatory(this, "PasswordGenerator", passwordGenerator);
//     PropertyCheck.mandatory(this, "PolicyComponent", policyComponent);

     //
//     this.policyComponent.bindClassBehaviour(QName.createQName(NamespaceService.ALFRESCO_URI, "beforeDeleteNode"),
//                 SiteModel.TYPE_SITE, new JavaBehaviour(this, "beforeDeleteNode"));
 }


	public boolean createInvitation(String email) {
		logger.info("User Registration send out!");
        // get the inviter user name (the name of user web script is executed
        // under)
//        String inviterUserName = authenticationService.getCurrentUserName();
        boolean created = false;

//        checkManagerRole(inviterUserName, resourceType, siteShortName);

//        if (logger.isDebugEnabled())
//        {
//            logger.debug("startInvite() inviterUserName=" + inviterUserName + " inviteeUserName=" + inviteeUserName
//                        + " inviteeFirstName=" + inviteeFirstName + " inviteeLastName=" + inviteeLastName
//                        + " inviteeEmail=" + inviteeEmail + " siteShortName=" + siteShortName + " inviteeSiteRole="
//                        + inviteeSiteRole);
//        }
        //
        // if we have not explicitly been passed an existing user's user name
        // then ....
        //
        // if a person already exists who has the given invitee email address
        //
        // 1) obtain invitee user name from first person found having the
        // invitee email address, first name and last name
        // 2) handle error conditions -
        // (invitee already has an invitation in progress for the given site,
        // or he/she is already a member of the given site
        //        
//        if (inviteeUserName == null || inviteeUserName.trim().length() == 0)
//        {
//
//            inviteeUserName = null;
//
//            Set<NodeRef> peopleWithInviteeEmail = personService.getPeopleFilteredByProperty(ContentModel.PROP_EMAIL, email);
//
//            if (peopleWithInviteeEmail.size() > 0)
//            {
//                // get person already existing who has the given
//                // invitee email address
//                for (NodeRef personRef : peopleWithInviteeEmail)
//                {
//                    Serializable firstNameVal = this.getNodeService().getProperty(personRef,
//                                ContentModel.PROP_FIRSTNAME);
//                    Serializable lastNameVal = this.getNodeService().getProperty(personRef, ContentModel.PROP_LASTNAME);
//
//                    String personFirstName = DefaultTypeConverter.INSTANCE.convert(String.class, firstNameVal);
//                    String personLastName = DefaultTypeConverter.INSTANCE.convert(String.class, lastNameVal);
//
//                    if (personFirstName != null && personFirstName.equalsIgnoreCase(firstName))
//                    {
//                        if (personLastName != null && personLastName.equalsIgnoreCase(lastName))
//                        {
//                            // got a match on email, lastname, firstname
//                            // get invitee user name of that person
//                            Serializable userNamePropertyVal = this.getNodeService().getProperty(personRef,
//                                        ContentModel.PROP_USERNAME);
//                            inviteeUserName = DefaultTypeConverter.INSTANCE.convert(String.class, userNamePropertyVal);
//
////                            if (logger.isDebugEnabled())
////                            {
////                                logger
////                                            .debug("not explictly passed username - found matching email, resolved inviteeUserName="
////                                                        + inviteeUserName);
////                            }
//                        }
//                    }
//                }
//            }
//
////            if (inviteeUserName == null)
////            {
////                // This shouldn't normally happen. Due to the fix for ETHREEOH-3268, the link to invite external users
////                // should be disabled when the authentication chain does not allow it.
//////                if (!authenticationService.isAuthenticationCreationAllowed())
//////                {
//////                    throw new InvitationException("invitation.invite.authentication_chain");
//////                }
////                // else there are no existing people who have the given invitee
////                // email address so create new person
////                inviteeUserName = createInviteePerson(inviteeFirstName, inviteeLastName, inviteeEmail);
////                created = true;
////                if (logger.isDebugEnabled())
////                {
////                    logger.debug("not explictly passed username - created new person, inviteeUserName="
////                                + inviteeUserName);
////                }
////            }
//        }

        /**
         * throw exception if person is already a member of the given site
         */
//        if (this.siteService.isMember(siteShortName, inviteeUserName))
//        {
//            if (logger.isDebugEnabled())
//                logger.debug("Failed - invitee user is already a member of the site.");
//
//            Object objs[] = { inviteeUserName, inviteeEmail, siteShortName };
//            throw new InvitationExceptionUserError("invitation.invite.already_member", objs);
//        }

        //
        // If a user account does not already exist for invitee user name
        // then create a disabled user account for the invitee.
        // Hold a local reference to generated password if disabled invitee
        // account
        // is created, otherwise if a user account already exists for invitee
        // user name, then local reference to invitee password will be "null"
        //
//        final String initeeUserNameFinal = inviteeUserName;
        
//        String inviteePassword = created ? AuthenticationUtil.runAs(new RunAsWork<String>()
//        {
//            public String doWork()
//            {
//                return createInviteeDisabledAccount(initeeUserNameFinal);
//            }
//        }, AuthenticationUtil.getSystemUserName()) : null;

        // create a ticket for the invite - this is used
        String inviteTicket = GUID.generate();

        //
        // Start the invite workflow with inviter, invitee and site properties
        //

        WorkflowDefinition wfDefinition = getWorkflowDefinition(true);

        // Get invitee person NodeRef to add as assignee
//        NodeRef inviteeNodeRef = personService.getPerson(inviteeUserName);
//        SiteInfo siteInfo = this.siteService.getSite(siteShortName);
//        String siteDescription = siteInfo.getDescription();
//        if (siteDescription == null)
//        {
//            siteDescription = "";
//        }
//        else if (siteDescription.length() > 255)
//        {
//            siteDescription = siteDescription.substring(0, 255);
//        }
        
        // get the workflow description
//        String workflowDescription = generateWorkflowDescription(siteInfo, "invitation.nominated.workflow.description");
        
        // create workflow properties
        Map<QName, Serializable> workflowProps = new HashMap<QName, Serializable>(32);
        workflowProps.put(WorkflowModel.PROP_WORKFLOW_DESCRIPTION, "It is for registration.");
        workflowProps.put(WF_PROP_INVITER_USER_NAME, "Rock");
//        workflowProps.put(WF_PROP_INVITEE_USER_NAME, firstName + ", " + lastName);
        workflowProps.put(WF_PROP_INVITEE_EMAIL, email);
//        workflowProps.put(WorkflowModel.ASSOC_ASSIGNEE, inviteeNodeRef);
//        workflowProps.put(WF_PROP_INVITEE_FIRSTNAME, firstName);
//        workflowProps.put(WF_PROP_INVITEE_LASTNAME, lastName);
//        workflowProps.put(WF_PROP_INVITEE_GEN_PASSWORD, "123");
//        workflowProps.put(WF_PROP_RESOURCE_NAME, siteShortName);
//        workflowProps.put(WF_PROP_RESOURCE_TITLE, siteInfo.getTitle());
//        workflowProps.put(WF_PROP_RESOURCE_DESCRIPTION, siteDescription);
//        workflowProps.put(WF_PROP_RESOURCE_TYPE, resourceType.toString());
//        workflowProps.put(WF_PROP_INVITEE_ROLE, inviteeSiteRole);
        String serverPath = UrlUtil.getShareUrl(sysAdminParams);
        workflowProps.put(WF_PROP_SERVER_PATH, serverPath);
        workflowProps.put(WF_PROP_ACCEPT_URL, "/page/registration-userinfo");
//        workflowProps.put(WF_PROP_REJECT_URL, rejectUrl);
        workflowProps.put(WF_PROP_INVITE_TICKET, inviteTicket);

//        return (NominatedInvitation) startWorkflow(wfDefinition, workflowProps);
        startWorkflow(wfDefinition, workflowProps);
		return true;
	}
	
	
	private Invitation startWorkflow(final WorkflowDefinition wfDefinition, final Map<QName, Serializable> workflowProps)
    {
        NodeRef wfPackage = workflowService.createPackage(null);
        workflowProps.put(WorkflowModel.ASSOC_PACKAGE, wfPackage);

        // start the workflow
		WorkflowPath wfPath = AuthenticationUtil.runAs(
				new RunAsWork<WorkflowPath>() {
					public WorkflowPath doWork() throws Exception {
						return workflowService.startWorkflow(
								wfDefinition.getId(), workflowProps);

					}
				}, AuthenticationUtil.getSystemUserName());
       

        //
        // complete invite workflow start task to send out the invite email
        //

        // get the workflow tasks
        final String workflowId = wfPath.getInstance().getId();
//        WorkflowTask startTask = workflowService.getStartTask(workflowId);
        
       final WorkflowTask startTask =  AuthenticationUtil.runAs(
				new RunAsWork<WorkflowTask>() {
					public WorkflowTask doWork() throws Exception {
						return workflowService.getStartTask(workflowId);

					}
				}, AuthenticationUtil.getSystemUserName());
        
        // attach empty package to start task, end it and follow with transition
        // that sends out the invite
        if (logger.isDebugEnabled())
            logger.debug("Starting Invite workflow task by attaching empty package...");

        if (logger.isDebugEnabled())
            logger.debug("Transitioning Invite workflow task...");
        try
        {
//            workflowService.endTask(startTask.getId(), null);
            
            AuthenticationUtil.runAs(
    				new RunAsWork<WorkflowTask>() {
    					public WorkflowTask doWork() throws Exception {
    						return workflowService.endTask(startTask.getId(), null);

    					}
    				}, AuthenticationUtil.getSystemUserName());
            
        }
        catch (RuntimeException err)
        {
            if (logger.isDebugEnabled())
                logger.debug("Failed - caught error during Invite workflow transition: " + err.getMessage());
            throw err;
        }
//        Invitation invitation = getInvitation(startTask);
//        return invitation;
        return null;
    }
	
	
    /**
     * Return Activiti workflow definition unless Activiti engine is disabled.
     * @param isNominated TODO
     * @return
     */
    private WorkflowDefinition getWorkflowDefinition(boolean isNominated)
    {
        String workflowName = getNominatedDefinitionName();
        WorkflowDefinition definition = workflowService.getDefinitionByName(workflowName);
        if (definition == null)
        {
            // handle workflow definition does not exist
            Object objs[] = {workflowName};
            throw new InvitationException("invitation.error.noworkflow", objs);
        }
        return definition;
    }
    
    private String getNominatedDefinitionName()
    {
        if(workflowAdminService.isEngineEnabled(ActivitiConstants.ENGINE_ID))
        {
            return WorkflowModelRegistration.WORKFLOW_DEFINITION_NAME_ACTIVITI;
        }
        else if(workflowAdminService.isEngineEnabled(JBPMEngine.ENGINE_ID))
        {
            return WorkflowModelNominatedInvitation.WORKFLOW_DEFINITION_NAME;
        }
        throw new IllegalStateException("None of the Workflow engines supported by teh InvitationService are currently enabled!");
    }

	
	public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public PersonService getPersonService() {
		return personService;
	}


	public void setPersonService(PersonService personService) {
		this.personService = personService;
	}


	public PermissionService getPermissionService() {
		return permissionService;
	}


	public void setPermissionService(PermissionService permissionService) {
		this.permissionService = permissionService;
	}





	public MutableAuthenticationService getAuthenticationService() {
		return authenticationService;
	}


	public void setTenantAdminService(TenantAdminService tenantAdminService) {
		this.tenantAdminService = tenantAdminService;
	}


	public void setAuthenticationService(
			MutableAuthenticationService authenticationService) {
		this.authenticationService = authenticationService;
	}


	public NodeService getNodeService()
    {
        return nodeService;
    }


	public SysAdminParams getSysAdminParams() {
		return sysAdminParams;
	}


	public void setSysAdminParams(SysAdminParams sysAdminParams) {
		this.sysAdminParams = sysAdminParams;
	}


	public WorkflowService getWorkflowService() {
		return workflowService;
	}


	public void setWorkflowService(WorkflowService workflowService) {
		this.workflowService = workflowService;
	}


	public WorkflowAdminService getWorkflowAdminService() {
		return workflowAdminService;
	}


	public void setWorkflowAdminService(WorkflowAdminService workflowAdminService) {
		this.workflowAdminService = workflowAdminService;
	}


	@Override
	public String createUser(String instanceId, final String inviteId, String inviteTicket, String firstName, String lastName, String password) {
		
		final String finalInstanceId = instanceId;
		final WorkflowTask startTask =  AuthenticationUtil.runAs(
				new RunAsWork<WorkflowTask>() {
					public WorkflowTask doWork() throws Exception {
						return workflowService.getStartTask(finalInstanceId);

					}
				}, AuthenticationUtil.getSystemUserName());
//		WorkflowTask startTask = getStartTask(inviteId);
//		 Date inviteDate = startTask.getPath().getInstance().getStartDate();
//         String invitationId = startTask.getPath().getInstance().getId();
//        NominatedInvitation invitation = getNominatedInvitation(startTask);
		 String ticket = (String)startTask.getProperties().get(WF_PROP_INVITE_TICKET);
		 
//		 String firstName = (String)startTask.getProperties().get(WF_PROP_INVITEE_FIRSTNAME);
//		 String lastName = (String)startTask.getProperties().get(WF_PROP_INVITEE_LASTNAME);
//        if(invitation == null)
//        {
//            throw new InvitationException("State error, accept may only be called on a nominated invitation.");
//        }
        // Check invitationId and ticket match
        if(inviteTicket.equals(ticket)==false)
        {
            //TODO localise msg
            String msg = "Response to invite has supplied an invalid ticket. The response to the invitation could thus not be processed";
            throw new InvitationException(msg);
        }
        
       final String email = (String)startTask.getProperties().get(WF_PROP_INVITEE_EMAIL);
        
        boolean isExisted =  checkEmail(email);
		
		if (isExisted) {

            logger.debug("Failed - unable to generate username for registration.");

            Object[] objs = { firstName, lastName, "mail" };
            throw new InvitationException("invitation.invite.unable_generate_id", objs);
        }
        
					 Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
					 properties.put(WF_PROP_INVITEE_FIRSTNAME, firstName);
					 properties.put(WF_PROP_INVITEE_LASTNAME, lastName);
					 properties.put(WF_PROP_INVITEE_USER_NAME, email);
					 properties.put(WF_PROP_INVITEE_GEN_PASSWORD, password);
        boolean isWorkflowCompleted = endInvitation(inviteId,
                null, properties,
                WorkflowModelNominatedInvitation.WF_TASK_INVITE_PENDING, WorkflowModelNominatedInvitation.WF_TASK_ACTIVIT_INVITE_PENDING);
        String executionRusult = null;
        if(isWorkflowCompleted) {
        	executionRusult = firstName + nameSepator + lastName;
        } else {
        	executionRusult = "workflow_expired";
        }
        	return executionRusult;
		
	}
	
	 private WorkflowTask getStartTask(String invitationId)
	    {
	        validateInvitationId(invitationId);
	        WorkflowTask startTask = null;
	        try
	        {
	            startTask = workflowService.getStartTask(invitationId);
	        }
	        catch (WorkflowException we)
	        {
	            // Do nothing
	        }
	        if (startTask == null)
	        {
	            Object objs[] = { invitationId };
	            throw new InvitationExceptionNotFound("invitation.error.not_found", objs);
	        }
	        return startTask;
	    }
	 
	 /**
	     * Validator for invitationId
	     * 
	     * @param invitationId
	     */
	    private void validateInvitationId(String invitationId)
	    {
	        final String ID_SEPERATOR_REGEX = "\\$";
	        String[] parts = invitationId.split(ID_SEPERATOR_REGEX);
	        if (parts.length != 2)
	        {
	            Object objs[] = { invitationId };
	            throw new InvitationExceptionUserError("invitation.error.invalid_inviteId_format", objs);
	        }
	    }
	    
	private boolean endInvitation(String instanceId, String transition,
			Map<QName, Serializable> properties, QName... taskTypes) {
		// List<WorkflowTask> tasks =
		// workflowService.getTasksForWorkflowPath(startTask.getPath().getId());
		List<WorkflowTask> tasks = null;
		boolean isValidate = true;
		try {
			tasks = workflowService.getTasksForWorkflowPath(instanceId);
		} catch (WorkflowException e) {
			isValidate = false;
		}
		//the workflow has been expried.
		if (!isValidate) {
			return false;
		}
		if (tasks != null && tasks.size() == 1) {
			WorkflowTask task = tasks.get(0);
			// if(taskTypeMatches(task, taskTypes))
			// {
			if (properties != null) {
				workflowService
						.updateTask(task.getId(), properties, null, null);
			}
			workflowService.endTask(task.getId(), transition);
			// return;
		}
		return true;
	}
	    
	    private boolean taskTypeMatches(WorkflowTask task, QName... types)
	    {
	        QName taskDefName = task.getDefinition().getMetadata().getName();
	        return Arrays.asList(types).contains(taskDefName);
	    }
	    
	    
	    public String createPerson(String email, String firstName, String lastName, String password)
	    {
//	    	if(true) {
//	    		return "for test";
//	    	}
	        // Attempt to generate user name for invitee
	        // which does not belong to an existing person
	        // Tries up to MAX_NUM_INVITEE_USER_NAME_GEN_TRIES
	        // at which point a web script exception is thrown
//	        String inviteeUserName = null;
//	        int i = 0;
//	        do
//	        {
//	            inviteeUserName = usernameGenerator.generateUserName(inviteeFirstName, inviteeLastName, inviteeEmail, i);
//	            i++;
//	        } while (this.personService.personExists(inviteeUserName) && (i < getMaxUserNameGenRetries()));

	        // if after 10 tries is not able to generate a user name for a
	        // person who doesn't already exist, then throw a web script exception
//	        if (this.personService.personExists(userName))
//	        {
//
//	            logger.debug("Failed - unable to generate username for invitee.");
//
//	            Object[] objs = { firstName, lastName, email };
//	            throw new InvitationException("invitation.invite.unable_generate_id", objs);
//	        }

	        // create a person node for the invitee with generated invitee user name
	        // and other provided person property values
	        final Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
	        
//	        final String finalUserName = PersonServiceImpl.updateUsernameForTenancy(email, tenantService);
	        	
//	        String domain = null;
	        
//	        int idx = email.lastIndexOf(TenantService.SEPARATOR);
//						if ((idx > 0) && (idx < (email.length() - 1))) {
//							domain = email.substring(idx + 1);
//						}

//						final String finalDomain = getEmailDomain(email);;
//						AuthenticationUtil.runAs(new RunAsWork<Object>() {
//						            public Object doWork() throws Exception
//						            {
//						            	tenantService.getTenant(finalDomain);
//						              return null;
//						            }
//
//						}, AuthenticationUtil.getSystemUserName());
//						String domainAdmin = "admin@" + finalDomain;
						final String finalEmail = email;
//						final String finalUserName = (String)AuthenticationUtil.runAs(new RunAsWork<Object>()
//						        {
//						            public Object doWork() throws Exception
//						            {
//						            	String userName = PersonServiceImpl.updateUsernameForTenancy(finalEmail, tenantService);
//						                return userName;
//						            }
//
//						        }, domainAdmin);
            
	        //properties.put(ContentModel.PROP_USERNAME, finalUserName);
			 properties.put(ContentModel.PROP_USERNAME, finalEmail);
	        properties.put(ContentModel.PROP_FIRSTNAME, firstName);
	        properties.put(ContentModel.PROP_LASTNAME, lastName);
	        properties.put(ContentModel.PROP_EMAIL, email);

//	        AuthenticationUtil.runAs(new RunAsWork<Object>()
//	        {
//	            public Object doWork() throws Exception
//	            {
//	                NodeRef person = personService.createPerson(properties);
//	                //permissionService.setPermission(person, finalUserName, PermissionService.ALL_PERMISSIONS, true);
//	                permissionService.setPermission(person, finalEmail, PermissionService.ALL_PERMISSIONS, true);
//
//	                return null;
//	            }
//
//	        }, domainAdmin);
	        
//	        createSiteService.addPerson(siteUrl, finalEmail, firstName, lastName, password);
	        createEntityService.addPerson(email, firstName, lastName, password);
	        String siteTitle = firstName + " " + lastName + "'s Home";
			 String siteDesc = firstName + " " + lastName + "'s private home site.";
			 createEntityService.createSite(email, siteTitle, siteDesc, false);
			 createEntityService.addMember(email, email, SiteModel.SITE_MANAGER);
			 createEntityService.removeMember(email, "admin");
//	        createSiteService.createSite(siteUrl, finalEmail, siteTitle, siteDesc, false);
//	        createSiteService.addMember(siteUrl, finalEmail, finalEmail, SiteModel.SITE_MANAGER);
//	        createSiteService.removeMember(siteUrl, finalEmail, "admin");
//	        AuthenticationUtil.runAs(new RunAsWork<Object>()
//	        {
//	            public Object doWork() throws Exception
//	            {
//	                NodeRef person = personService.createPerson(properties);
//	                //permissionService.setPermission(person, finalUserName, PermissionService.ALL_PERMISSIONS, true);
//	                permissionService.setPermission(person, finalEmail, PermissionService.ALL_PERMISSIONS, true);
//
//	                return null;
//	            }
//
//	        }, AuthenticationUtil.getSystemUserName());
//	        
//	        final String finalPassword = password;
	        
//	        String inviteePassword = AuthenticationUtil.runAs(new RunAsWork<String>()
//	                {
//	                    public String doWork()
//	                    {
//	                        //return createInviteeDisabledAccount(finalUserName, finalPassword);
//	                        return createInviteeDisabledAccount(finalEmail, finalPassword);
//	                    }
//	                }, domainAdmin);
	        

//	        AuthenticationUtil.runAsSystem(new RunAsWork<String>()
//	                {
//                public String doWork()
//                {
//                    //return createInviteeDisabledAccount(finalUserName, finalPassword);
//                    return createInviteeDisabledAccount(finalEmail, finalPassword);
//                }
//            });
	        
	        //create user personal site
//	        String siteTitle = firstName + " " + lastName + "'s Home";
//			String siteDesc = firstName + " " + lastName + "'s private home site.";
//	        createSiteService.createSite(siteUrl, finalEmail, finalPassword, siteTitle, siteDesc, false);
//	        
//	        
//	        AuthenticationUtil.runAsSystem(new RunAsWork<String>() {
//                public String doWork()
//                {
////                	personService.getPerson(finalEmail);
//                	authorityService.addAuthority(getSiteRoleGroup(finalEmail,
//                            SiteModel.SITE_MANAGER, true), finalEmail);
//                	authorityService.removeAuthority(getSiteRoleGroup(finalEmail,
//                            SiteModel.SITE_MANAGER, true), "admin");
////        	        siteService.setMembership(finalEmail, finalEmail, SiteModel.SITE_MANAGER);
//        			if(siteService.isMember(finalEmail, "admin")) {
//        			logger.debug("The admin is a member of site " + finalEmail);
//        			siteService.removeMembership(finalEmail, "admin");
//        		}
//                    return null;
//                }
//            });
	        
//	        createSiteService.createSite(siteUrl, finalEmail, finalPassword, siteTitle, siteDesc, false);
	        
//	        AuthenticationUtil.runAs(new RunAsWork<String>() {
//                public String doWork()
//                {
////                	personService.getPerson(finalEmail);
////                	authorityService.addAuthority(getSiteRoleGroup(finalEmail,
////                            SiteModel.SITE_MANAGER, true), finalEmail);
////                	authorityService.removeAuthority(getSiteRoleGroup(finalEmail,
////                            SiteModel.SITE_MANAGER, true), "admin");
//        	        siteService.setMembership(finalEmail, finalEmail, SiteModel.SITE_MANAGER);
//        			if(siteService.isMember(finalEmail, "admin")) {
//        			logger.debug("The admin is a member of site " + finalEmail);
//        			siteService.removeMembership(finalEmail, "admin");
//        		}
//                    return null;
//                }
//            }, "admin");
	        
	        
//	        AuthenticationUtil.runAs(new RunAsWork<String>() {
//                public String doWork()
//                {
////                	personService.getPerson(finalEmail);
////                	authorityService.addAuthority(getSiteRoleGroup(finalEmail,
////                            SiteModel.SITE_MANAGER, true), finalEmail);
////                	authorityService.removeAuthority(getSiteRoleGroup(finalEmail,
////                            SiteModel.SITE_MANAGER, true), "admin");
////        	        siteService.setMembership(finalEmail, finalEmail, SiteModel.SITE_MANAGER);
//        			if(siteService.isMember(finalEmail, "admin")) {
//        			logger.debug("The admin is a member of site " + finalEmail);
//        			siteService.removeMembership(finalEmail, "admin");
//        		}
//                    return null;
//                }
//            }, finalEmail);


	        //return finalUserName;
	        return finalEmail;
	    }
	    
	    
	    public String getSiteRoleGroup(String shortName, String permission, boolean withGroupPrefix)
	    {
	        return getSiteGroup(shortName, withGroupPrefix) + '_' + permission;
	    }
	    
	    /**
	     * Helper method to get the name of the site group
	     * 
	     * @param shortName     site short name
	     * @return String site group name
	     */
	    public String getSiteGroup(String shortName, boolean withGroupPrefix)
	    {
	        StringBuffer sb = new StringBuffer(64);
	        if (withGroupPrefix == true)
	        {
	            sb.append(PermissionService.GROUP_PREFIX);
	        }
	        sb.append(SITE_PREFIX);
	        sb.append(shortName);
	        return sb.toString();
	    }
	    
	    private String createInviteeDisabledAccount(String inviteeUserName, String password)
	    {
	        // generate password using password generator
	        char[] generatedPassword = password.toCharArray();

	        // create disabled user account for invitee user name with generated
	        // password
	        this.authenticationService.createAuthentication(inviteeUserName, generatedPassword);
	        this.authenticationService.setAuthenticationEnabled(inviteeUserName, true);

	        return String.valueOf(generatedPassword);
	    }


		@Override
		public boolean checkEmail(final String email) {
			boolean isExisted = false;
//			String domain = getEmailDomain(email);
//			if(domain == null) {
//				return true;
//			}
//			boolean isDomainEnable = tenantAdminService.isEnabledTenant(domain);
//			if(isDomainEnable) {
//				final String executor = "admin@" + domain;
//				isExisted = AuthenticationUtil.runAs(
//						new RunAsWork<Boolean>() {
//							public Boolean doWork() throws Exception {
//								return personService.personExists(email);
//
//							}
//						}, executor);
//			} 
			
			isExisted = AuthenticationUtil.runAsSystem(
					new RunAsWork<Boolean>() {
						public Boolean doWork() throws Exception {
							return personService.personExists(email);

						}
					});
			
			return isExisted;
		}
		
	private String getEmailDomain(String email) {
		String domain = null;
		int idx = email.lastIndexOf(TenantService.SEPARATOR);
		if ((idx > 0) && (idx < (email.length() - 1))) {
			domain = email.substring(idx + 1);
		}
		return domain;
	}


		public void setTenantService(TenantService tenantService) {
			this.tenantService = tenantService;
		}


		public AuthorityService getAuthorityService() {
			return authorityService;
		}


		public void setAuthorityService(AuthorityService authorityService) {
			this.authorityService = authorityService;
		}


		public CreateEntityService getCreateEntityService() {
			return createEntityService;
		}


		public void setCreateEntityService(CreateEntityService createEntityService) {
			this.createEntityService = createEntityService;
		}


}
