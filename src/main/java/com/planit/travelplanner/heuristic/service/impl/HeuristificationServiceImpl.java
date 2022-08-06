package com.planit.travelplanner.heuristic.service.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.planit.journey_common.model.PlanCriteria;
import com.planit.journey_common.model.RoutePointBean;
import com.planit.travelplanner.heuristic.bean.HeuristicMeta;
import com.planit.travelplanner.heuristic.constants.HeuristificationConstants;
import com.planit.travelplanner.heuristic.service.HeuristificationService;
import com.planit.travelplanner.servicecommon.AbstractService;

@Service
public class HeuristificationServiceImpl extends AbstractService implements HeuristificationService {

	private final Map<Long, HeuristicMeta> heuristicMetaMap = new ConcurrentHashMap<>();

	@Override
	public RoutePointBean heuristify(RoutePointBean routePointBean, Long planId) {
		HeuristicMeta heuristicMeta = heuristicMetaMap.get(planId);
		routePointBean.setHeuristicValue(Math.sqrt(
				Math.pow(heuristicMeta.getDestinationPoint().getLang() - routePointBean.getPoint().getLang(), 2) + Math
						.pow(heuristicMeta.getDestinationPoint().getLongi() - routePointBean.getPoint().getLongi(), 2))
				* heuristicMeta.getHeuristicWeightPerKM());
		System.out.println("Heuristified routePointBean:" + routePointBean);
		return routePointBean;
	}

	@Override
	public void registerPlan(Long planId, PlanCriteria planCriteria) {
		heuristicMetaMap.put(planId,
				HeuristicMeta.builder().destinationPoint(planCriteria.getDestinationPoint()).heuristicWeightPerKM(
						(planCriteria.getCostWeightage() * HeuristificationConstants.HEURISTIC_COST_INR_PER_KM
								+ planCriteria.getTimeWeightage() * HeuristificationConstants.HEURISTIC_TIME_SEC_PER_KM)
								/ HeuristificationConstants.PER_KM_GPS)
						.build());

	}

}
