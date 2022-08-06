package com.planit.travelplanner.service;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.planit.journey_common.model.PlanCriteria;
import com.planit.journey_common.model.Plans;
import com.planit.journey_common.model.RoutePointBean;

@Service
@Scope(value=ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class TravelPlannerServiceImpl implements TravelPlannerService {
	
	@Autowired
	private ObjectFactory<RouteFinderService> routeFinderServiceFactory;
	
	@Autowired
	private PlanManagementService planManagementService;

	public Long startPlanning(PlanCriteria planCriteria) {
		Long planId = planManagementService.registerPlan(planCriteria);
		RoutePointBean routePointBean = new RoutePointBean(planCriteria.getSourcePoint(), 0.0);
		RouteFinderService routeFinderService = routeFinderServiceFactory.getObject();
		routeFinderService.addBeanToQueue(routePointBean);
		planManagementService.registerRouteFinderService(routeFinderService, planId);
		return planId;
	}

	public Plans getPlans(Long planId, int pageSize, int pageNumber) {
		try {
			return planManagementService.requestPlans(planId, pageSize, pageNumber);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}

}
