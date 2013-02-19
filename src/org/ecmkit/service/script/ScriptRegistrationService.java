package org.ecmkit.service.script;

import org.alfresco.repo.jscript.BaseScopableProcessorExtension;
import org.alfresco.service.ServiceRegistry;
import org.ecmkit.service.RegistrationService;

public class ScriptRegistrationService  extends BaseScopableProcessorExtension {
	/** Service Registry */
	private ServiceRegistry serviceRegistry;
	
	private RegistrationService registrationService;

	public ServiceRegistry getServiceRegistry() {
		return serviceRegistry;
	}

	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		this.serviceRegistry = serviceRegistry;
	}

	public RegistrationService getRegistrationService() {
		return registrationService;
	}

	public void setRegistrationService(RegistrationService registrationService) {
		this.registrationService = registrationService;
	}
	
	public boolean createInvitation(String email) {
		boolean result = registrationService.createInvitation(email);
		return result;
	}
	
	public String createUser(String instanceId, String inviteId, String inviteTicket, String firstName, String lastName, String password) {
		return registrationService.createUser(instanceId, inviteId, inviteTicket, firstName, lastName, password);
		
	}
	
	public boolean checkEmail(String email) {
		return registrationService.checkEmail(email);
	}
	
	

}
