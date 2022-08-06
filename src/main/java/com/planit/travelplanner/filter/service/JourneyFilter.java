package com.planit.travelplanner.filter.service;

import com.planit.journey_common.model.Journey;
import com.planit.journey_common.servicecommon.AbstractServiceInterface;

public interface JourneyFilter extends AbstractServiceInterface {

	public boolean check(Journey journey, Long planId);

}
