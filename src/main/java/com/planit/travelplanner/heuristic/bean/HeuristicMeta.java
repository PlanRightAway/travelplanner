package com.planit.travelplanner.heuristic.bean;

import com.planit.journey_common.model.Point;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class HeuristicMeta {

	private Double heuristicWeightPerKM;
	private Point destinationPoint;
}
