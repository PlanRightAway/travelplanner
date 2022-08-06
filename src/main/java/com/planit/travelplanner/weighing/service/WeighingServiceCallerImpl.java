package com.planit.travelplanner.weighing.service;

import org.springframework.stereotype.Service;

import com.planit.journey_common.model.Journey;
import com.planit.travelplanner.servicecommon.AbstractService;

@Service
public class WeighingServiceCallerImpl extends AbstractService implements WeighingService {

	@Override
	public Double getWeight(Journey journey, Long planId) {
		Double weight = webClientBuilder.baseUrl(String.format("http://%s", serviceName)).build().post()
				.uri(String.format("/getWeight/?planId=%d", planId)).bodyValue(journey).retrieve()
				.bodyToMono(Double.class).block();
		return weight == null ? 0 : weight;
	}

}
