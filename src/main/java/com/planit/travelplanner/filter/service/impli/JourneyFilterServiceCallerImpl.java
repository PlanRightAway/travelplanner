package com.planit.travelplanner.filter.service.impli;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.planit.journey_common.model.Journey;
import com.planit.travelplanner.filter.service.JourneyFilter;
import com.planit.travelplanner.servicecommon.AbstractService;

@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class JourneyFilterServiceCallerImpl extends AbstractService implements JourneyFilter {

	@Override
	public boolean check(Journey journey, Long planId) {
		// Implement call to specified filter service check api
		return false;
	}

}
