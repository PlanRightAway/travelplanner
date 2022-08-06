package com.planit.travelplanner.journey.service.impl;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import com.planit.journey_common.api.JourneyApi;
import com.planit.journey_common.model.Journey;
import com.planit.journey_common.model.RoutePointBean;
import com.planit.travelplanner.servicecommon.AbstractService;

@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class JourneyApiCallerImpl extends AbstractService implements JourneyApi {

	@Override
	public List<Journey> getJourneyRoutePoints(RoutePointBean routePointBean, Long planId) {
		List<Journey> journeys = webClientBuilder.baseUrl(String.format("http://%s", serviceName)).build().post()
				.uri(String.format("/getJourneyRoutePoints/?planId=%d", planId)).bodyValue(routePointBean).retrieve()
				.bodyToMono(new ParameterizedTypeReference<List<Journey>>() {
				}).block();
		return journeys == null ? Collections.emptyList() : journeys;
	}

}