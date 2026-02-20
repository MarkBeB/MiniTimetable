package org.tud.minitimetable.model;

import java.util.Arrays;

public final class Room {
	public IhtcModel root;
	public String id;
	public int capacity;

	public Occupant[] _assignedOccupants;
	public int _maxStayOccupant;

	public int[] getAvailableCapacity() {
		var availableCapacity = new int[root.days];
		Arrays.fill(availableCapacity, capacity);
		for (var occupant : root.occupants) {
			if (id.equals(occupant.assignedToRoom)) {
				for (var day = 0; day < occupant.lengthOfStay; ++day) {
					availableCapacity[day] -= 1;
				}
			}
		}
		return availableCapacity;
	}

	public int[] getOccupantsPerDay() {
		var occupantsPerDay = new int[root.days];
		for (var occupant : root.occupants) {
			if (id.equals(occupant.assignedToRoom)) {
				for (var day = 0; day < occupant.lengthOfStay; ++day) {
					occupantsPerDay[day] += 1;
				}
			}
		}
		return occupantsPerDay;
	}

	public String[] getGenderAssignment() {
		var assignments = new String[root.days];

		Occupant maxStayOccupant = null;
		for (var occupant : root.occupants) {
			if (id.equals(occupant.assignedToRoom)) {
				if (maxStayOccupant == null || maxStayOccupant.lengthOfStay < occupant.lengthOfStay) {
					maxStayOccupant = occupant;
				}
			}
		}

		if (maxStayOccupant != null) {
			for (var i = 0; i < maxStayOccupant.lengthOfStay; ++i) {
				assignments[i] = maxStayOccupant.gender;
			}
		}

		return assignments;
	}
}