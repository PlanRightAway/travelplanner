package com.planit.travelplanner.service;

import com.planit.journey_common.model.Journey;
import com.planit.journey_common.model.PlanCriteria;
import com.planit.journey_common.model.Plans;
import com.planit.journey_common.model.Point;
import com.planit.journey_common.model.RoutePointBean;
import com.planit.travelplanner.model.Planner;
import com.planit.travelplanner.model.TravelInfo;

public interface PlanManagementService {

	Plans requestPlans(Long planId, int pageSize, int pageNumber) throws InterruptedException;

	Long registerPlan(PlanCriteria planCriteria);

	void registerRouteFinderService(RouteFinderService routeFinderService, Long planId);

	boolean validForExecution(RouteFinderService routeFinderService, Long planId);

	boolean feedAndCheckMoreExplorationRequired(Journey journey, Long planId);

	boolean isRoutePointVisited(Point sourcePoint, Long planId);

	boolean isRoutePointOptimizable(RoutePointBean sourceRoutePointBean, Long planId);

	RouteFinderService getRouteFinderService(Long planId);

	TravelInfo getPointTravelInfo(Point point, Long planId);

	void addInPlans(Planner planner, Long planId);

	boolean validForBackTracerExecution(RouteFinderService routeFinderService, Long planId);

	void resumeFetchingPlans(Long planId);

	Journey journeyFiller(Journey journey, RoutePointBean sourceRoutePointBean, Long planId);

}
