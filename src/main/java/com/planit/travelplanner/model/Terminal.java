package com.planit.travelplanner.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Terminal {
	private String name;
	private String campusName;
	private String address;
}
