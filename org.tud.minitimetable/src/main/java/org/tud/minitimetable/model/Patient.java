package org.tud.minitimetable.model;

public final class Patient {
	public IhtcModel root;
	public String id;
	public String gender;
	public String ageGroup;
	public int lengthOfStay;
	public int[] workloadProduced;
	public int[] skillLevelRequired;
	public boolean mandatory;
	public int surgeryReleaseDay;
	public int surgeryDueDay;
	public int surgeryDuration;
	public String surgeonId;
	public String[] incompatibleRooms;
}