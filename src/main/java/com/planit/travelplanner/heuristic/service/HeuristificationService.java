package com.planit.travelplanner.heuristic.service;

import com.planit.journey_common.model.RoutePointBean;
import com.planit.journey_common.servicecommon.AbstractServiceInterface;

public interface HeuristificationService extends AbstractServiceInterface {

	RoutePointBean heuristify(RoutePointBean routePointBean, Long planId);

}
