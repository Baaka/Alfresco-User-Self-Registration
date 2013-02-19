package org.ecmkit.service;

public interface RegistrationService {
	public boolean createInvitation(String email);
	public String createUser(String instanceId, String inviteId, String inviteTicket, String firstName, String lastName, String password);
	public String createPerson(String email, String firstName, String lastName, String password);
	public boolean checkEmail(String email);

}
