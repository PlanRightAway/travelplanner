package com.planit.travelplanner.endpoint;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.planit.journey_common.model.PlanCriteria;
import com.planit.journey_common.model.Plans;
import com.planit.journey_common.model.TopPlans;
import com.planit.travelplanner.service.TravelPlannerService;

@RestController
public class RestServiceController {
	
	@Autowired
	private TravelPlannerService travelPlannerService;
	
	@PostMapping("/makePlan")
	public TopPlans makePlan(@RequestParam int pageSize, @RequestParam int pageNumber, @RequestBody PlanCriteria planCriteria) {
		
		Long planReqId = travelPlannerService.startPlanning(planCriteria);
		return TopPlans.builder().plans(travelPlannerService.getPlans(planReqId, pageSize, pageNumber)).planId(planReqId).build();
	}

}
