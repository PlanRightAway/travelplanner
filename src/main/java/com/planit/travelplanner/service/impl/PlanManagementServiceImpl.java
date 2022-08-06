package com.planit.travelplanner.service.impl;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.planit.journey_common.api.JourneyApi;
import com.planit.journey_common.model.Journey;
import com.planit.journey_common.model.PlanCriteria;
import com.planit.journey_common.model.Plans;
import com.planit.journey_common.model.Point;
import com.planit.journey_common.model.RoutePointBean;
import com.planit.travelplanner.filter.service.JourneyFilter;
import com.planit.travelplanner.heuristic.service.HeuristificationService;
import com.planit.travelplanner.model.PlanContext;
import com.planit.travelplanner.model.Planner;
import com.planit.travelplanner.model.TravelInfo;
import com.planit.travelplanner.model.TravelJourneyInfo;
import com.planit.travelplanner.service.PlanManagementService;
import com.planit.travelplanner.service.RouteFinderService;
import com.planit.travelplanner.weighing.service.WeighingService;

@Service
public class PlanManagementServiceImpl implements PlanManagementService {

	@Autowired
	@Qualifier("Heuristification")
	private HeuristificationService heuristificationService;

	@Autowired
	@Qualifier("JourneyServices")
	private List<JourneyApi> journeyServices;
	
	@Autowired
	@Qualifier("JourneyFilters")
	private List<JourneyFilter> journeyFilters;
	
	@Autowired
	@Qualifier("Weighing")
	private WeighingService weighingService;

	Map<Long, PlanContext> planContextMap = new ConcurrentHashMap<>();

	@Override
	public Plans requestPlans(Long planId, int pageSize, int pageNumber) throws InterruptedException {
		PlanContext planContext = planContextMap.get(planId);
		synchronized (planContext.getPlans()) {
			RouteFinderService routeFinderService = planContext.getRouteFinderService();
			planContext.setResultCount(pageNumber * pageSize);
			while (planContext.getPlans().getSize() < pageNumber * pageSize
					&& !(plannerWaitingQueueDoneCondition(planContext, routeFinderService)
							&& routePointWaitingQueueDoneCondition(planContext, routeFinderService)
							&& weightOfExecutionTasksDoneCondition(planContext, routeFinderService))) {
				planContext.getRouteFinderService().createAndExecuteFinderTask(planId);
				planContext.getRouteFinderService().createAndExecuteBackTracerTask(planId);
				System.out.println("Going to wait for results for planID: " + planId);
				planContext.getPlans().wait();
				System.out.println("Got notified for resume plan fetching for planId: " + planId);
			}
			return planContext.getPlans();
		}
	}

	@Override
	public Long registerPlan(PlanCriteria planCriteria) {
		PlanContext planContext = new PlanContext();
		planContext.setPlanCriteria(planCriteria);
		TravelInfo travelInfo = TravelInfo.builder().source(true).build();
		travelInfo.getSourceTravelJourneyInfoList().add(TravelJourneyInfo.builder()
				.journey(Journey.builder().reachingWeight(0.0).journeyWeight(0.0).build()).build());
		planContext.getTravelInfoMap().put(planCriteria.getSourcePoint(), travelInfo);
		TravelInfo destinationTravelInfo = TravelInfo.builder().visited(true).connected(true).build();
		Planner planner = new Planner();
		planner.setWeight(0.0);
		planner.setCurrentPoint(planCriteria.getDestinationPoint());
		destinationTravelInfo.getPlanners().add(planner);
		planContext.getTravelInfoMap().put(planCriteria.getDestinationPoint(), destinationTravelInfo);
		Long uuid = (long) planContext.hashCode();
		heuristificationService.registerPlan(uuid, planCriteria);
		weighingService.registerPlan(uuid, planCriteria);
		journeyServices.stream().forEach(journeyService -> journeyService.registerPlan(uuid, planCriteria));
		journeyFilters.stream().forEach(journeyFilter -> journeyFilter.registerPlan(uuid, planCriteria));
		planContextMap.put(uuid, planContext);
		return uuid;
	}

	@Override
	public void registerRouteFinderService(RouteFinderService routeFinderService, Long planId) {
		planContextMap.get(planId).setRouteFinderService(routeFinderService);

	}

	@Override
	public boolean validForExecution(RouteFinderService routeFinderService, Long planId) {
		return !routeFinderService.getRoutePointPriorityWaitingQueue().isEmpty()
				&& routeFinderService.getRoutePointPriorityWaitingQueue().peek().getReachingWeight() < planContextMap
						.get(planId).getPlans().getMaxWeight();
	}

	@Override
	public boolean feedAndCheckMoreExplorationRequired(Journey journey, Long planId) {
		/*
		 * Will feed the journey details and check: -> if destination point visited:
		 * then it will add the destination in findRouteQueue for optimization -> if
		 * destination point is connected point: then will start back-tracking task
		 */
		if (!isSourcePoint(journey.getDestinationPoint(), planId)) {
			PlanContext planContext = planContextMap.get(planId);
			TravelJourneyInfo sourceTravelJourneyInfo = TravelJourneyInfo.builder()
					.travelInfo(planContext.getTravelInfoMap().get(journey.getSourcePoint())).journey(journey).build();
			TravelInfo destinationTravelInfo = null;
			if (!isRoutePointVisited(journey.getDestinationPoint(), planId)) {
				// Destination Point Is not visited
				System.out.println("destination point: " + journey.getDestinationPoint() + " not visited !!");
				destinationTravelInfo = new TravelInfo();
				planContext.getTravelInfoMap().put(journey.getDestinationPoint(), destinationTravelInfo);
				destinationTravelInfo.getSourceTravelJourneyInfoList().add(sourceTravelJourneyInfo);
				sourceTravelJourneyInfo.getTravelInfo().getDestinationTravelInfoList().add(destinationTravelInfo);
				System.out.println("setting destination point: " + journey.getDestinationPoint() + " visited");
				return true;
			} else {
				// Destination point is visited
				destinationTravelInfo = planContext.getTravelInfoMap().get(journey.getDestinationPoint());
				if (destinationTravelInfo.isConnected()) {
					System.out.println("Connected point reached !!: "+journey.getDestinationPoint()+" marking point: "+ journey.getSourcePoint()+" for backtracing !!");
					// Destination point is connected to final destination
					destinationTravelInfo.getPlanners().stream().forEach(planner -> {
						Planner newPlanner = new Planner();
						newPlanner.setCurrentPoint(journey.getSourcePoint());
						newPlanner.setWeight(planner.getWeight() + journey.getJourneyWeight());
						newPlanner.setOptimisticTotalWeight(newPlanner.getWeight() + journey.getReachingWeight());
						newPlanner.getJournies().addAll(planner.getJournies());
						newPlanner.getJournies().add(journey);
						newPlanner.getLoopBreakerSet().addAll(planner.getLoopBreakerSet());
						newPlanner.getLoopBreakerSet().add(journey);
						planContext.getRouteFinderService().addBeanToBackTracerQueue(newPlanner);
					});
					planContext.getRouteFinderService().createAndExecuteBackTracerTask(planId);
				}
				destinationTravelInfo.getSourceTravelJourneyInfoList().add(sourceTravelJourneyInfo);
				sourceTravelJourneyInfo.getTravelInfo().getDestinationTravelInfoList().add(destinationTravelInfo);
				planContext.getRouteFinderService()
						.addBeanToQueue(heuristificationService.heuristify(journey.buildRoutePoint(), planId));
				planContext.getRouteFinderService().createAndExecuteFinderTask(planId);
			}
		}
		return false;
	}

	public boolean isSourcePoint(Point point, Long planId) {
		return planContextMap.get(planId).getTravelInfoMap().containsKey(point)
				&& planContextMap.get(planId).getTravelInfoMap().get(point).isSource();
	}

	@Override
	public boolean isRoutePointVisited(Point point, Long planId) {
		return planContextMap.get(planId).getTravelInfoMap().containsKey(point)
				&& planContextMap.get(planId).getTravelInfoMap().get(point).isVisited();
	}

	@Override
	public TravelInfo getPointTravelInfo(Point point, Long planId) {
		return planContextMap.get(planId).getTravelInfoMap().get(point);
	}

	@Override
	public boolean isRoutePointOptimizable(RoutePointBean sourceRoutePointBean, Long planId) {
		Journey journey = planContextMap.get(planId).getTravelInfoMap().get(sourceRoutePointBean.getPoint())
				.getSourceTravelJourneyInfoList().stream().map(tj -> tj.getJourney())
				.min((j1, j2) -> (int) (j2.getJourneyWeight() + j2.getReachingWeight() - j1.getJourneyWeight()
						- j1.getReachingWeight()))
				.get();
		return journey.getJourneyWeight() + journey.getReachingWeight() == sourceRoutePointBean.getReachingWeight();
	}

	@Override
	public RouteFinderService getRouteFinderService(Long planId) {
		return planContextMap.get(planId).getRouteFinderService();
	}

	@Override
	public void addInPlans(Planner planner, Long planId) {
		PlanContext planContext = planContextMap.get(planId);
		planContext.getPlans().addPlan(planner);
		if (planContext.getPlans().getPlans().size() >= planContext.getResultCount()) {
			planContext.getPlans().setMaxWeight(planContext.getPlans().getPlans().stream()
					.skip(planContext.getResultCount()).findFirst().get().getWeight());
		}

	}

	@Override
	public boolean validForBackTracerExecution(RouteFinderService routeFinderService, Long planId) {
		return !routeFinderService.getPlannerPriorityWaitingQueue().isEmpty()
				&& routeFinderService.getPlannerPriorityWaitingQueue().peek().getWeight() < planContextMap.get(planId)
						.getPlans().getMaxWeight();
	}

	@Override
	public void resumeFetchingPlans(Long planId) {
		PlanContext planContext = planContextMap.get(planId);
		synchronized (planContext.getPlans()) {
			RouteFinderService routeFinderService = planContext.getRouteFinderService();
			if (plannerWaitingQueueDoneCondition(planContext, routeFinderService)
					&& routePointWaitingQueueDoneCondition(planContext, routeFinderService)
					&& weightOfExecutionTasksDoneCondition(planContext, routeFinderService)) {
				System.out.println("done searching !! ready to notifing for search compltete");
				planContext.getPlans().notifyAll();
			}
		}

	}

	private boolean weightOfExecutionTasksDoneCondition(PlanContext planContext,
			RouteFinderService routeFinderService) {
		return routeFinderService.getWeightOfExecutionTasks().isEmpty() || Collections
				.min(routeFinderService.getWeightOfExecutionTasks()) >= planContext.getPlans().getMaxWeight();
	}

	private boolean routePointWaitingQueueDoneCondition(PlanContext planContext,
			RouteFinderService routeFinderService) {
		return routeFinderService.getRoutePointPriorityWaitingQueue().isEmpty()
				|| routeFinderService.getRoutePointPriorityWaitingQueue().peek().getReachingWeight() >= planContext
						.getPlans().getMaxWeight();
	}

	private boolean plannerWaitingQueueDoneCondition(PlanContext planContext, RouteFinderService routeFinderService) {
		return routeFinderService.getPlannerPriorityWaitingQueue().isEmpty() || routeFinderService
				.getPlannerPriorityWaitingQueue().peek().getWeight() >= planContext.getPlans().getMaxWeight();
	}

	@Override
	public Journey journeyFiller(Journey journey, RoutePointBean sourceRoutePointBean, Long planId) {
		journey.setReachingWeight(sourceRoutePointBean.getReachingWeight());
//		journey.setJourneyWeight(planCriteria.getTimeWeightage()
//				* (journey.getDurationInMillies() + journey.getWaitingTimeInMillies()
//						+ journey.getEndBufferTimeInMillies() + journey.getStartBufferTimeInMillies())
//				+ planCriteria.getCostWeightage() * journey.getTravellingCost());
		journey.setJourneyWeight(weighingService.getWeight(journey, planId));
		return journey;
	}

}
