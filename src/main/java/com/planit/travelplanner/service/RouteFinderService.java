package com.planit.travelplanner.service;

import java.util.List;
import java.util.PriorityQueue;

import com.planit.journey_common.model.RoutePointBean;
import com.planit.travelplanner.model.Planner;

public interface RouteFinderService {

	void addBeanToQueue(RoutePointBean routePointBean);
	
	void createAndExecuteFinderTask(Long planId);

	PriorityQueue<RoutePointBean> getRoutePointPriorityWaitingQueue();
	
	void addBeanToBackTracerQueue(Planner planner);

	void createAndExecuteBackTracerTask(Long planId);

	PriorityQueue<Planner> getPlannerPriorityWaitingQueue();

	List<Double> getWeightOfExecutionTasks();

}
