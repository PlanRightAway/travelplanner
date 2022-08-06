package com.planit.travelplanner.weighing.service;

import com.planit.journey_common.model.Journey;
import com.planit.journey_common.servicecommon.AbstractServiceInterface;

public interface WeighingService extends AbstractServiceInterface {

	Double getWeight(Journey journey, Long planId);

}
