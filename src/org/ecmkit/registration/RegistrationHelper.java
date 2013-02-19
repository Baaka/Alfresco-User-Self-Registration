package org.ecmkit.registration;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.util.collections.CollectionUtils;
import org.ecmkit.service.RegistrationService;
import org.springframework.beans.factory.InitializingBean;

import static org.ecmkit.registration.WorkflowModelRegistration.wfVarAcceptUrl;
import static org.ecmkit.registration.WorkflowModelRegistration.wfVarInviterUserName;
import static org.ecmkit.registration.WorkflowModelRegistration.wfVarInviteeUserName;
import static org.ecmkit.registration.WorkflowModelRegistration.wfVarResourceName;
import static org.ecmkit.registration.WorkflowModelRegistration.wfVarResourceTitle;
import static org.ecmkit.registration.WorkflowModelRegistration.wfVarResourceDescription;
import static org.ecmkit.registration.WorkflowModelRegistration.wfVarResourceType;
import static org.ecmkit.registration.WorkflowModelRegistration.wfVarWorkflowInstanceId;
import static org.ecmkit.registration.WorkflowModelRegistration.wfVarRole;
import static org.ecmkit.registration.WorkflowModelRegistration.wfVarInviteTicket;
import static org.ecmkit.registration.WorkflowModelRegistration.wfVarServerPath;
import static org.ecmkit.registration.WorkflowModelRegistration.wfVarRejectUrl;
import static org.ecmkit.registration.WorkflowModelRegistration.wfVarInviteeGenPassword;
import static org.ecmkit.registration.WorkflowModelRegistration.wfEmail;
import static org.ecmkit.registration.WorkflowModelRegistration.wfFirstName;
import static org.ecmkit.registration.WorkflowModelRegistration.wfLastName;



public class RegistrationHelper  implements InitializingBean {
	
	private NamespaceService namespaceService;
	
	
	private static final Collection<String> sendInvitePropertyNames = Arrays.asList(wfVarInviteeUserName,//
//            wfVarResourceName,//
            wfVarInviterUserName,//
            wfVarInviteeUserName,//
//            wfVarRole,//
            wfVarInviteeGenPassword,//
//            wfVarResourceName,//
            wfVarInviteTicket,//
            wfVarServerPath,//
            wfVarAcceptUrl,//
            wfEmail, 
//            wfVarRejectUrl,
            RegistrationSender.WF_INVITATION_ID);
	
	private RegistrationSender sender;
	private ServiceRegistry serviceRegistry;
	private Repository repositoryHelper;
	private RegistrationService registrationService;
	
	@Override
	public void afterPropertiesSet()
    {

        this.namespaceService = serviceRegistry.getNamespaceService();
        this.sender = new RegistrationSender(serviceRegistry, repositoryHelper);
    }
	
	public  void sendNominatedInvitation(String instanceId, String inviteId, Map<String, Object> executionVariables)
    {
       
    Map<String, String> properties = makePropertiesFromContextVariables(executionVariables, sendInvitePropertyNames);

    String packageName = WorkflowModel.ASSOC_PACKAGE.toPrefixString(namespaceService).replace(":", "_");
    ScriptNode packageNode = (ScriptNode) executionVariables.get(packageName);
    String packageRef = packageNode.getNodeRef().toString();
    properties.put(RegistrationSender.WF_PACKAGE, packageRef);
    
    properties.put(RegistrationSender.WF_INVITATION_ID, inviteId);
    properties.put(RegistrationSender.WF_INSTANCE_ID, instanceId);

    
    sender.sendMail(properties);
        
    }
	
	public void createUser(Map<String, Object> executionVariables) {
		//String userName = (String)executionVariables.get("regi_inviteeUserName");
		String password = (String)executionVariables.get("regi_inviteeGenPassword");
		String firstName = (String)executionVariables.get(wfFirstName);
		String lastName = (String)executionVariables.get(wfLastName);
		String email = (String)executionVariables.get(wfEmail);
		registrationService.createPerson(email, firstName, lastName, password);
		
	}
	
	private Map<String, String> makePropertiesFromContextVariables(Map<?, ?> executionVariables, Collection<String> propertyNames)
    {
        return CollectionUtils.filterKeys((Map<String, String>) executionVariables, CollectionUtils.containsFilter(propertyNames));
    }

	public ServiceRegistry getServiceRegistry() {
		return serviceRegistry;
	}

	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		this.serviceRegistry = serviceRegistry;
	}

	public Repository getRepositoryHelper() {
		return repositoryHelper;
	}

	public void setRepositoryHelper(Repository repositoryHelper) {
		this.repositoryHelper = repositoryHelper;
	}

	public RegistrationService getRegistrationService() {
		return registrationService;
	}

	public void setRegistrationService(RegistrationService registrationService) {
		this.registrationService = registrationService;
	}

}
