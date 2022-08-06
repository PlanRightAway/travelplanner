package com.planit.travelplanner.servicecommon;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.reactive.function.client.WebClient;

import com.planit.journey_common.model.PlanCriteria;
import com.planit.journey_common.servicecommon.AbstractServiceInterface;

import lombok.Data;

@Data
public abstract class AbstractService implements AbstractServiceInterface {

	protected String serviceName;

	@Autowired
	protected WebClient.Builder webClientBuilder;

	@Override
	public void registerPlan(Long planId, PlanCriteria planCriteria) {
		webClientBuilder.baseUrl(String.format("http://%s", serviceName)).build().post()
				.uri(String.format("/registerPlan/?planId=%d", planId)).bodyValue(planCriteria).retrieve()
				.bodyToMono(Void.class).block();

	}

}
