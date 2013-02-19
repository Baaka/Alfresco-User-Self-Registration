package org.ecmkit.service;

public interface CreateEntityService {
	public boolean createSite(String shortName, String title, String description, boolean isPublic);
	public boolean removeMember(String shortName, String authority);
	public boolean addMember(String shortName, String authority, String role);
	public boolean addPerson(String email, String firstName, String lastName, String password);
}
