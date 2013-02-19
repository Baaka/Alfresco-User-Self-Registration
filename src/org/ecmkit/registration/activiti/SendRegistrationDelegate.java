package org.ecmkit.registration.activiti;

import java.util.Map;

import org.activiti.engine.delegate.DelegateExecution;
import org.alfresco.repo.workflow.activiti.ActivitiConstants;

public class SendRegistrationDelegate  extends AbstractRegistrationDelegate {

	@Override
	public void execute(DelegateExecution execution) throws Exception {
		 String instanceId = ActivitiConstants.ENGINE_ID + "$" +
		 execution.getProcessInstanceId();
		// send sub process id, not parent process id.
		String invitationId = ActivitiConstants.ENGINE_ID + "$"
				+ execution.getId();
		Map<String, Object> variables = execution.getVariables();
		registrationHelper.sendNominatedInvitation(instanceId, invitationId, variables);
	}

}
