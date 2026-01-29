package org.tud.minitimetable.model;

import java.util.Map;

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

}
