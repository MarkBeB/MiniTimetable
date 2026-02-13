package org.tud.minitimetable.model;

public final class Nurse {
	public IhtcModel root;

	public String id;
	public int skillLevel;
	public WorkShift[] workingShifts;

	private int indexOf(String[] arr, String value) {
		for (var i = 0; i < arr.length; ++i)
			if (arr[i].equals(value))
				return i;
		return -1;
	}

	public int[][] getLoadPerWorkdayShift() {
		var workShiftPerDay = new int[root.days][root.shiftTypes.length];
		for (var workshift : workingShifts) {
			workShiftPerDay[workshift.day][indexOf(root.shiftTypes, workshift.shift)] = workshift.maxLoad;
		}
		return workShiftPerDay;
	}
}