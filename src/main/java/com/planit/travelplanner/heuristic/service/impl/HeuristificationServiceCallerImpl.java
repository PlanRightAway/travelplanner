package com.planit.travelplanner.heuristic.service.impl;

import org.springframework.stereotype.Service;

import com.planit.journey_common.model.RoutePointBean;
import com.planit.travelplanner.heuristic.service.HeuristificationService;
import com.planit.travelplanner.servicecommon.AbstractService;

@Service
public class HeuristificationServiceCallerImpl extends AbstractService implements HeuristificationService {

	@Override
	public RoutePointBean heuristify(RoutePointBean routePointBean, Long planId) {
		RoutePointBean processedRoutePointBean = webClientBuilder.baseUrl(String.format("http://%s", serviceName)).build().post()
				.uri(String.format("/heuristify/?planId=%d", planId)).bodyValue(routePointBean).retrieve()
				.bodyToMono(RoutePointBean.class).block();
		routePointBean.setReachingWeight(0.0);
		return processedRoutePointBean == null ? routePointBean : processedRoutePointBean;
	}

}
