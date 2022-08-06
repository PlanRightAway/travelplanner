package com.planit.travelplanner.model;

import java.util.HashSet;
import java.util.Set;

import com.planit.journey_common.model.Journey;
import com.planit.journey_common.model.Plan;
import com.planit.journey_common.model.Point;

import lombok.Data;

@Data
public class Planner extends Plan implements Comparable<Planner> {
	private Point currentPoint;
	private Double optimisticTotalWeight;
	private final Set<Journey> loopBreakerSet  = new HashSet<>();
	
	@Override
	public int compareTo(Planner o) {
		return this.optimisticTotalWeight.compareTo(o.optimisticTotalWeight);
	}
}
