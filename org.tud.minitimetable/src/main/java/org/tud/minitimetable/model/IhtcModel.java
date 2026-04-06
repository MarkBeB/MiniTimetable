package org.tud.minitimetable.model;

import java.util.Map;
import java.util.stream.Stream;

public final class IhtcModel {

	public int days;

	public int skillLevels;

	public String[] shiftTypes;

	public String[] ageGroups;

	public Map<String, Integer> weights;

	public Nurse[] nurses;

	public Surgeon[] surgeons;

	public Patient[] patients;

	public Occupant[] occupants;

	public OperatingTheater[] operatingTheaters;

	public Room[] rooms;

	public int getLastDay() {
		return days - 1;
	}

	public int getMaxDay() {
		var lastDay = getLastDay();
		return Stream.of(patients) //
				.mapToInt(p -> p.mandatory ? p.surgeryDueDay + p.lengthOfStay : lastDay + p.lengthOfStay) //
				.max().orElse(lastDay);
	}

}
