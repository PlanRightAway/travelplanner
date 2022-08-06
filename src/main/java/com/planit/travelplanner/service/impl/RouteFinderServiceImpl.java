package com.planit.travelplanner.service.impl;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.ExecutorService;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.planit.journey_common.model.RoutePointBean;
import com.planit.travelplanner.model.Planner;
import com.planit.travelplanner.service.RouteFinderService;
import com.planit.travelplanner.task.BackTracerTask;
import com.planit.travelplanner.task.RoutePointFinderTask;

@Service
@Scope(value=ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class RouteFinderServiceImpl implements RouteFinderService {
	
	@Autowired
	@Qualifier("routeFinder")
	private ExecutorService routeFinderExecutorService;
	
	@Autowired
	@Qualifier("backTracer")
	private ExecutorService backTracerExecutorService;
	
	@Autowired
	private ObjectFactory<RoutePointFinderTask> routePointFinderTaskFactory;
	
	@Autowired
	private ObjectFactory<BackTracerTask> backTracerTaskFactory;
	
	private final PriorityQueue<RoutePointBean> routePointPriorityWaitingQueue = new PriorityQueue<>();
	
	private final PriorityQueue<Planner> planPriorityWaitingQueue = new PriorityQueue<>();
	
	private final List<Double> weightsOfExecutingTasks = Collections.synchronizedList(new LinkedList<>());

	public void addBeanToQueue(RoutePointBean routePointBean) {
		System.out.println("RoutePoiintBean: "+routePointBean+" added to routePointPriorityWaitingQueue !!");
		routePointPriorityWaitingQueue.add(routePointBean);
	}
	
	public void createAndExecuteFinderTask(Long planId) {
		RoutePointFinderTask routePointFinderTask = routePointFinderTaskFactory.getObject();
		routePointFinderTask.setRouteFinderService(this);
		routePointFinderTask.setPlanId(planId);
		routeFinderExecutorService.submit(routePointFinderTask);
		System.out.println("routeFinderExecutorService started for planId: "+planId+" !!");
	}

	public PriorityQueue<RoutePointBean> getRoutePointPriorityWaitingQueue() {
		return routePointPriorityWaitingQueue;
	}

	@Override
	public void createAndExecuteBackTracerTask(Long planId) {
		BackTracerTask backTracerTask = backTracerTaskFactory.getObject();
		backTracerTask.setPlanId(planId);
		backTracerExecutorService.submit(backTracerTask);
		
	}

	@Override
	public void addBeanToBackTracerQueue(Planner planner) {
		planPriorityWaitingQueue.add(planner);
		
	}

	@Override
	public PriorityQueue<Planner> getPlannerPriorityWaitingQueue() {
		return planPriorityWaitingQueue;
		
	}

	@Override
	public List<Double> getWeightOfExecutionTasks() {
		return weightsOfExecutingTasks;
	}
	
	

}
