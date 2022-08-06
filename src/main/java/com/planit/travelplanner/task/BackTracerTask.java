package com.planit.travelplanner.task;

import java.util.concurrent.Callable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.planit.travelplanner.model.Planner;
import com.planit.travelplanner.model.TravelInfo;
import com.planit.travelplanner.service.PlanManagementService;
import com.planit.travelplanner.service.RouteFinderService;

import lombok.Data;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Data
public class BackTracerTask implements Callable<Void> {

	@Autowired
	private PlanManagementService planManagementService;

	private Long planId;

	@Override
	public Void call() throws Exception {
		RouteFinderService routeFinderService = planManagementService.getRouteFinderService(planId);
		if (planManagementService.validForBackTracerExecution(routeFinderService, planId)) {
			Planner planner = routeFinderService.getPlannerPriorityWaitingQueue().poll();
			TravelInfo travelInfo = planManagementService.getPointTravelInfo(planner.getCurrentPoint(), planId);
			travelInfo.setConnected(true);
			travelInfo.getPlanners().add(planner);
			if (!travelInfo.isSource() && !travelInfo.getSourceTravelJourneyInfoList().isEmpty()) {
				System.out.println("Bactracing point: "+planner.getCurrentPoint());
				travelInfo.getSourceTravelJourneyInfoList().stream().filter(
						travelJourneyInfo -> !planner.getLoopBreakerSet().contains(travelJourneyInfo.getJourney()))
						.forEach(travelJourneyInfo -> {
//							travelJourneyInfo.setConnected(true);
							Planner newPlanner = new Planner();
							newPlanner
									.setWeight(planner.getWeight() + travelJourneyInfo.getJourney().getJourneyWeight());
							newPlanner.setOptimisticTotalWeight(
									newPlanner.getWeight() + travelJourneyInfo.getJourney().getReachingWeight());
							newPlanner.getJournies().addAll(planner.getJournies());
							newPlanner.getJournies().add(travelJourneyInfo.getJourney());
							newPlanner.setCurrentPoint(travelJourneyInfo.getJourney().getSourcePoint());
							travelJourneyInfo.getTravelInfo().getPlanners().add(newPlanner);
							routeFinderService.addBeanToBackTracerQueue(newPlanner);
						});
				routeFinderService.createAndExecuteBackTracerTask(planId);
			} else {
				planManagementService.addInPlans(planner, planId);
				planManagementService.resumeFetchingPlans(planId);
			}
		}
		return null;
	}

}
