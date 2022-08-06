package com.planit.travelplanner.task;

import java.util.List;
import java.util.concurrent.Callable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.planit.journey_common.api.JourneyApi;
import com.planit.journey_common.model.Journey;
import com.planit.journey_common.model.RoutePointBean;
import com.planit.travelplanner.filter.service.JourneyFilter;
import com.planit.travelplanner.heuristic.service.HeuristificationService;
import com.planit.travelplanner.model.TravelInfo;
import com.planit.travelplanner.service.PlanManagementService;
import com.planit.travelplanner.service.RouteFinderService;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@AllArgsConstructor
@RequiredArgsConstructor
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Data
public class RoutePointFinderTask implements Callable<Void> {

	@Autowired
	private PlanManagementService planManagementService;

	@Autowired
	@Qualifier("JourneyServices")
	private List<JourneyApi> journeyApis;
	
	@Autowired
	@Qualifier("JourneyFilters")
	private List<JourneyFilter> journeyFilters;

	@Autowired
	@Qualifier("Heuristification")
	private HeuristificationService heuristificationService;

	private RouteFinderService routeFinderService;
	private Long planId;

	public Void call() throws Exception {
		if (planManagementService.validForExecution(routeFinderService, planId)) {
			RoutePointBean sourceRoutePointBean = routeFinderService.getRoutePointPriorityWaitingQueue().poll();
			routeFinderService.getWeightOfExecutionTasks().add(sourceRoutePointBean.getReachingWeight());
			if (!planManagementService.isRoutePointVisited(sourceRoutePointBean.getPoint(), planId)) {
				System.out.println("Point " + sourceRoutePointBean.getPoint()
						+ " is not searched. Finding routes!!! for PlanId:" + planId);
				findRoutes(sourceRoutePointBean);
				// setting point as visited
				planManagementService.getPointTravelInfo(sourceRoutePointBean.getPoint(), planId).setVisited(true);
			} else if (planManagementService.isRoutePointOptimizable(sourceRoutePointBean, planId)) {
				System.out.println("Point " + sourceRoutePointBean.getPoint()
						+ " is already searched. Optimizing routes!!! for PlanId:" + planId);
				optimizeRoutes(sourceRoutePointBean);
			}
			routeFinderService.getWeightOfExecutionTasks().remove(sourceRoutePointBean.getReachingWeight());
			planManagementService.resumeFetchingPlans(planId);
		}
		return null;
	}

	private void optimizeRoutes(RoutePointBean sourceRoutePointBean) {
		TravelInfo sourceTravelInfo = planManagementService.getPointTravelInfo(sourceRoutePointBean.getPoint(), planId);
		sourceTravelInfo.getDestinationTravelInfoList().stream()
				.filter(travelJorneyInfo -> !travelJorneyInfo.isConnected()).map(travelJorneyInfo -> {
					Journey journey = travelJorneyInfo.getSourceTravelJourneyInfoList().stream()
							.filter(tj -> sourceTravelInfo.equals(tj.getTravelInfo())).findFirst().get().getJourney();
					journey.setReachingWeight(sourceRoutePointBean.getReachingWeight());
					return journey;
				})
				// .filter(journey ->
				// planManagementService.feedAndCheckMoreExplorationRequired(journey, planId))
				.map(journey -> journey.buildRoutePoint())
				.map(destinationRoutePoint -> heuristificationService.heuristify(destinationRoutePoint, planId))
				.forEach(destinationRoutePoint -> routeFinderService.addBeanToQueue(destinationRoutePoint));

	}

	private void findRoutes(RoutePointBean sourceRoutePointBean) {
		try {
		journeyApis.stream().flatMap(journeyService -> {
			return journeyService.getJourneyRoutePoints(sourceRoutePointBean, planId).stream();
		}).map(journey -> {
			return planManagementService.journeyFiller(journey, sourceRoutePointBean, planId);
		}).filter(journey -> {
			return journeyFilters.stream().allMatch(journeyFilter -> journeyFilter.check(journey, planId));
		}).filter(journey -> {
			return planManagementService.feedAndCheckMoreExplorationRequired(journey, planId);
		}).map(journey -> {
			return journey.buildRoutePoint();
		}).map(destinationRoutePoint -> {
			return heuristificationService.heuristify(destinationRoutePoint, planId);
		}).forEach(destinationRoutePoint -> {
			routeFinderService.addBeanToQueue(destinationRoutePoint);
		});
		routeFinderService.createAndExecuteFinderTask(planId);
		} catch (Exception e) {
			System.out.println(e);
		}
	}

}
