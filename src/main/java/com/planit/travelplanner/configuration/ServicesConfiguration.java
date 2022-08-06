package com.planit.travelplanner.configuration;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient;

import com.planit.journey_common.api.JourneyApi;
import com.planit.travelplanner.filter.service.JourneyFilter;
import com.planit.travelplanner.filter.service.impli.JourneyFilterServiceCallerImpl;
import com.planit.travelplanner.heuristic.service.HeuristificationService;
import com.planit.travelplanner.heuristic.service.impl.HeuristificationServiceCallerImpl;
import com.planit.travelplanner.journey.service.impl.JourneyApiCallerImpl;
import com.planit.travelplanner.weighing.service.WeighingService;
import com.planit.travelplanner.weighing.service.WeighingServiceCallerImpl;

@Configuration
public class ServicesConfiguration {

	@Autowired
	private ObjectFactory<JourneyApiCallerImpl> journeyApiCallerFactory;
	
	@Autowired
	private ObjectFactory<JourneyFilterServiceCallerImpl> journeyFilterServiceCallerFactory;
	
	@Autowired
	private ObjectFactory<WeighingServiceCallerImpl> weighingServiceCallerFactory;
	
	@Autowired
	private ObjectFactory<HeuristificationServiceCallerImpl> heuristificationServiceCallerFactory;
	
	@Autowired
	private WebClient.Builder webClientBuilder;

	@Bean("JourneyServices")
	public List<JourneyApi> journeyServiceListConfiguration() {
		return webClientBuilder.baseUrl(String.format("http://%s", "JOURNEY-SERVICE-PROVIDER")).build().get()
				.uri(String.format("/getJourneyServices")).retrieve()
				.bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {
				}).block().stream().sorted((m1, m2) -> (int) m1.get("order") - (int) m2.get("order")).map(m -> {
					JourneyApiCallerImpl journeyCaller = journeyApiCallerFactory.getObject();
					journeyCaller.setServiceName(m.get("serviceName").toString());
					return journeyCaller;
				}).collect(Collectors.toList());
	}
	
	@Bean("JourneyFilters")
	public List<JourneyFilter> journeyFilterListConfiguration() {
		return webClientBuilder.baseUrl(String.format("http://%s", "JOURNEY-SERVICE-PROVIDER")).build().get()
				.uri(String.format("/getJourneyFilters")).retrieve()
				.bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {
				}).block().stream().sorted((m1, m2) -> (int) m1.get("order") - (int) m2.get("order")).map(m -> {
					JourneyFilterServiceCallerImpl journeyFilterServiceCallerImpl = journeyFilterServiceCallerFactory.getObject();
					journeyFilterServiceCallerImpl.setServiceName(m.get("serviceName").toString());
					return journeyFilterServiceCallerImpl;
				}).collect(Collectors.toList());
	}
	
	@Bean("Weighing")
	public WeighingService WeighingServiceConfiguration() {
		String serviceName = webClientBuilder.baseUrl(String.format("http://%s", "JOURNEY-SERVICE-PROVIDER")).build().get()
		.uri(String.format("/getWeighingService")).retrieve()
		.bodyToMono(String.class).block();
		if (serviceName != null && !serviceName.isBlank()) {
			WeighingServiceCallerImpl weighingServiceCallerImpl = weighingServiceCallerFactory.getObject();
			weighingServiceCallerImpl.setServiceName(serviceName);
			return weighingServiceCallerImpl;
		} else {
			throw new RuntimeException("Unable to configure Weighing Service. Can't initiate Travell planner!!");
		}
	}
	
	@Bean("Heuristification")
	public HeuristificationService HeuristificationServiceConfiguration() {
		String serviceName = webClientBuilder.baseUrl(String.format("http://%s", "JOURNEY-SERVICE-PROVIDER")).build().get()
		.uri(String.format("/getHeuristicService")).retrieve()
		.bodyToMono(String.class).block();
		if (serviceName != null && !serviceName.isBlank()) {
			HeuristificationServiceCallerImpl heuristificationServiceCallerImpl = heuristificationServiceCallerFactory.getObject();
			heuristificationServiceCallerImpl.setServiceName(serviceName);
			return heuristificationServiceCallerImpl;
		} else {
			throw new RuntimeException("Unable to configure Heuristification Service. Can't initiate Travell planner!!");
		}
	}

}
