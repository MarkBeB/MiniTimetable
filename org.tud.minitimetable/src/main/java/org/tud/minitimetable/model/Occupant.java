package org.tud.minitimetable.model;

public final class Occupant {
	public IhtcModel root;
	public String id;
	public String gender;
	public String ageGroup;
	public int lengthOfStay;
	public int[] workloadProduced;
	public int[] skillLevelRequired;
	public String assignedToRoom;
}