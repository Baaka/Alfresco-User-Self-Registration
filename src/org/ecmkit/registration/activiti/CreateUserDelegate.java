package org.ecmkit.registration.activiti;

import java.util.Map;

import org.activiti.engine.delegate.DelegateExecution;

public class CreateUserDelegate extends AbstractRegistrationDelegate {

	public void execute(DelegateExecution execution) throws Exception {
		Map<String, Object> executionVariables = execution.getVariables();
		registrationHelper.createUser(executionVariables);
		
	}

}
