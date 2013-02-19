package org.ecmkit.service.impl;

import org.alfresco.repo.domain.tenant.TenantAdminDAO;
import org.alfresco.repo.domain.tenant.TenantEntity;
import org.alfresco.repo.tenant.MultiTServiceImpl;
import org.alfresco.repo.tenant.Tenant;
import org.alfresco.repo.tenant.TenantAdminService;

public class MultiTServiceCloudImpl extends MultiTServiceImpl {
	
	 private TenantAdminDAO tenantAdminDAO;
	 private TenantAdminService tenantAdminService;
	 
	 public void setTenantAdminService(TenantAdminService tenantAdminService)
	    {
	        this.tenantAdminService = tenantAdminService;
	    }
	    
	    public void setTenantAdminDAO(TenantAdminDAO tenantAdminDAO)
	    {
	        this.tenantAdminDAO = tenantAdminDAO;
	    }
	    
	public Tenant getTenant(String tenantDomain)
    {
        TenantEntity tenantEntity = tenantAdminDAO.getTenant(tenantDomain);
        Tenant tenant = null;
        if(tenantEntity == null && !"".equals(tenantDomain)) {
        	tenantAdminService.createTenant(tenantDomain, "password123".toCharArray());
        	tenantEntity = tenantAdminDAO.getTenant(tenantDomain);
        }
        if (tenantEntity != null)
        {
            tenant = new Tenant(tenantEntity.getTenantDomain(), tenantEntity.getEnabled(), tenantEntity.getContentRoot());
        } 
        return tenant;
    }
}
