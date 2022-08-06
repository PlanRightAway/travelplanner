package com.planit.travelplanner.model;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TravelInfo {
	
	private final List<TravelJourneyInfo> sourceTravelJourneyInfoList = new ArrayList<>();
	private final List<TravelInfo> destinationTravelInfoList = new ArrayList<>();
	private final List<Planner> planners= new ArrayList<>();
	
	private boolean connected;
	private boolean visited;
	private boolean source;

}
