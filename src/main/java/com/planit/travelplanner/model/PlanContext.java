package com.planit.travelplanner.model;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.planit.journey_common.model.PlanCriteria;
import com.planit.journey_common.model.Plans;
import com.planit.journey_common.model.Point;
import com.planit.travelplanner.service.RouteFinderService;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class PlanContext {
	
	private RouteFinderService routeFinderService;
	private final Map<Point, TravelInfo> travelInfoMap = new ConcurrentHashMap<>();
	private final Plans plans = new Plans();
	private Integer resultCount;
	private PlanCriteria planCriteria;

}
