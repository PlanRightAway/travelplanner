package com.planit.travelplanner.service;

import com.planit.journey_common.model.PlanCriteria;
import com.planit.journey_common.model.Plans;

public interface TravelPlannerService {

	Long startPlanning(PlanCriteria planCriteria);

	Plans getPlans(Long planId, int pageSize, int pageNumber);

}
