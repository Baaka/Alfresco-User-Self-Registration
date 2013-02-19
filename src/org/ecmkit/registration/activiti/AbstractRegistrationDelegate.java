package org.ecmkit.registration.activiti;

import org.alfresco.repo.workflow.activiti.BaseJavaDelegate;
import org.ecmkit.registration.RegistrationHelper;

public abstract class AbstractRegistrationDelegate extends BaseJavaDelegate {
	protected RegistrationHelper registrationHelper;

	public RegistrationHelper getRegistrationHelper() {
		return registrationHelper;
	}

	public void setRegistrationHelper(RegistrationHelper registrationHelper) {
		this.registrationHelper = registrationHelper;
	}
}
