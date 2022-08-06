package com.planit.travelplanner.model;

import com.planit.journey_common.model.Journey;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class TravelJourneyInfo {
	
	private TravelInfo travelInfo;
	private boolean connected;
	private Journey journey;
}
