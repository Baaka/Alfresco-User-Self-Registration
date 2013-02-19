package org.ecmkit.registration;

public interface WorkflowModelRegistration {
			// workflow execution context variable names
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
    public static final String wfFirstName = "regi_inviteeFirstName";
    public static final String wfLastName = "regi_inviteeLastName";
    
    public static final String nameSepator = ", ";
    
    public static final String WORKFLOW_DEFINITION_NAME_ACTIVITI = "activiti$activitiRegistration";
}
