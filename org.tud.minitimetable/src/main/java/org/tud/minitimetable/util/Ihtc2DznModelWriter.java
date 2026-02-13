package org.tud.minitimetable.util;

import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.tud.minitimetable.model.IhtcModel;
import org.tud.minitimetable.model.Nurse;
import org.tud.minitimetable.model.Occupant;
import org.tud.minitimetable.model.OperatingTheater;
import org.tud.minitimetable.model.Patient;
import org.tud.minitimetable.model.Room;
import org.tud.minitimetable.model.Surgeon;
import org.tud.minitimetable.model.WorkShift;

public class Ihtc2DznModelWriter {

	private static String nullable(Object nullable) {
		if (nullable == null)
			return "<>";
		return nullable.toString();
	}

	private static boolean notEmpty(int[] arr) {
		return arr.length > 0;
	}

	private static <T> boolean notEmpty(T[] arr) {
		return arr.length > 0;
	}

	private static int[] copy(int[] arr, int length) {
		return Arrays.copyOf(arr, length);
	}

	private static String toString(int[] arr) {
		StringBuilder sb = new StringBuilder();
		for (var i = 0; i < arr.length; ++i) {
			sb.append(Integer.toString(arr[i]));
			if (i < arr.length - 1) {
				sb.append(", ");
			}
		}
		return sb.toString();
	}

	public synchronized void write(IhtcModel model, Writer writer) throws IOException {

		{
			writer.indentation().write("Weights").write(" = ").write("(").newLine().increaseIndentation();
			Pattern p = Pattern.compile("_(\\w)");
			for (var entry : model.weights.entrySet()) {
				var matcher = p.matcher(entry.getKey());
				var newKey = matcher.replaceAll(result -> result.group(1).toUpperCase());
				writer.indentation().write(newKey).write(": ").write(entry.getValue()).write(",").newLine();
			}
			writer.decreaseIndentation().indentation().write(");").newLine().newLine();
		}

		writer.indentation().write("Genders").write(" = ").write("{") //
				.write("A").write(", ").write("B") //
				.write("};").newLine();

		writer.indentation().write("ShiftTypes").write(" = ").write("{") //
				.write(model.shiftTypes, (value, hasMore) -> {
					writer.write(value).write(", ", hasMore);
				}).write("};").newLine();

		writer.indentation().write("AgeGroups").write(" = ").write("{") //
				.write(model.ageGroups, (value, hasMore) -> {
					writer.write(value).write(", ", hasMore);
				}).write("};").newLine();

		writer.indentation().write("NurseIds").write(" = ").write("{") //
				.write(model.nurses, (value, hasMore) -> {
					writer.write(value.id).write(", ", hasMore);
				}).write("};").newLine();

		writer.indentation().write("SurgeonIds").write(" = ").write("{") //
				.write(model.surgeons, (value, hasMore) -> {
					writer.write(value.id).write(", ", hasMore);
				}).write("};").newLine();

		writer.indentation().write("OperatingTheaterIds").write(" = ").write("{") //
				.write(model.operatingTheaters, (value, hasMore) -> {
					writer.write(value.id).write(", ", hasMore);
				}).write("};").newLine();

		writer.indentation().write("RoomIds").write(" = ").write("{") //
				.write(model.rooms, (value, hasMore) -> {
					writer.write(value.id).write(", ", hasMore);
				}).write("};").newLine();

		writer.indentation().write("PatientIds").write(" = ").write("{") //
				.write(model.patients, (value, hasMore) -> {
					writer.write(value.id).write(", ", hasMore);
				}).write("};").newLine();

		writer.indentation().write("OccupantIds").write(" = ").write("{") //
				.write(model.occupants, (value, hasMore) -> {
					writer.write(value.id).write(", ", hasMore);
				}).write("};").newLine();

		var lastDay = model.days - 1;
		var maxDay = Stream.of(model.patients) //
				.mapToInt(p -> p.mandatory ? p.surgeryDueDay + p.lengthOfStay : lastDay + p.lengthOfStay) //
				.max().orElse(lastDay);

		writer.newLine();
//		writer.indentation().write("days").write(" = ").write(model.days).write(";").newLine();
//		writer.indentation().write("firstDay").write(" = ").write(firstDay).write(";").newLine();
		writer.indentation().write("lastDay").write(" = ").write(lastDay).write(";").newLine();
		writer.indentation().write("maxDay").write(" = ").write(maxDay).write(";").newLine();
		writer.indentation().write("maxSkillLevel").write(" = ").write(model.skillLevels - 1).write(";").newLine();
//		writer.indentation().write("SkillLevels").write(" = ") //
//				.write("0.." + (model.skillLevels - 1)).write(";").newLine();
		writer.newLine();

		writer.write("nurseData").write(" = ").write("[").newLine().increaseIndentation()
				.write(model.nurses, (value, hasMore) -> {
					write(value, writer);
					writer.write(",", hasMore).newLine();
				}).decreaseIndentation().write("];").newLine().newLine();

		writer.write("surgeonData").write(" = ").write("[").newLine().increaseIndentation()
				.write(model.surgeons, (value, hasMore) -> {
					write(value, writer);
					writer.write(",", hasMore).newLine();
				}).decreaseIndentation().write("];").newLine().newLine();

		writer.write("patientData").write(" = ").write("[").newLine().increaseIndentation()
				.write(model.patients, (value, hasMore) -> {
					write(value, writer);
					writer.write(",", hasMore).newLine();
				}).decreaseIndentation().write("];").newLine().newLine();

		writer.write("occupantData").write(" = ").write("[").newLine().increaseIndentation()
				.write(model.occupants, (value, hasMore) -> {
					write(value, writer);
					writer.write(",", hasMore).newLine();
				}).decreaseIndentation().write("];").newLine().newLine();

		writer.write("operatingTheaterData").write(" = ").write("[").newLine().increaseIndentation()
				.write(model.operatingTheaters, (value, hasMore) -> {
					write(value, writer);
					writer.write(",", hasMore).newLine();
				}).decreaseIndentation().write("];").newLine().newLine();

		writer.write("roomData").write(" = ").write("[").newLine().increaseIndentation()
				.write(model.rooms, (value, hasMore) -> {
					write(value, writer);
					writer.write(",", hasMore).newLine();
				}).decreaseIndentation().write("];").newLine().newLine();

	}

	private void write(Surgeon obj, Writer writer) throws IOException {
		writer.indentation().write("(") //
				.write("id").write(": ").write(obj.id).write(", ") //
				.write("maxSurgeryTime").write(": ").write("[").write("0:", notEmpty(obj.maxSurgeryTime)) //
				.write(obj.maxSurgeryTime, (value, hasMore) -> {
					writer.write(value).write(", ", hasMore);
				}).write("]") //
				.write(")");
	}

	private void write(Patient obj, Writer writer) throws IOException {
		writer.indentation().write("(").increaseIndentation().newLine();
		writer.indentation().write("id").write(": ").write(obj.id).write(", ").newLine();
		writer.indentation().write("gender").write(": ").write(obj.gender).write(", ").newLine();
		writer.indentation().write("ageGroup").write(": ").write(obj.ageGroup).write(", ").newLine();
		writer.indentation().write("lengthOfStay").write(": ").write(obj.lengthOfStay).write(", ").newLine();
		writer.indentation().write("workloadProduced").write(": ") //
				.write("[").write("0:", notEmpty(obj.workloadProduced)) //
				.write(obj.workloadProduced, (value, hasMore) -> {
					writer.write(value).write(", ", hasMore);
				}).write("]").write(",").newLine();
		writer.indentation().write("skillLevelRequired").write(": ") //
				.write("[").write("0:", notEmpty(obj.skillLevelRequired)) //
				.write(obj.skillLevelRequired, (value, hasMore) -> {
					writer.write(value).write(", ", hasMore);
				}).write("]").write(",").newLine();
		writer.indentation().write("mandatory").write(": ").write(obj.mandatory).write(", ").newLine();
		writer.indentation().write("earliestPossibleAdmission").write(": ") //
				.write(obj.surgeryReleaseDay).write(", ").newLine();
		writer.indentation().write("latestPossibleAdmission").write(": ") //
				.write(obj.surgeryDueDay).write(", ").newLine();
		writer.indentation().write("surgeryDuration").write(": ").write(obj.surgeryDuration).write(", ").newLine();
		writer.indentation().write("assignedSurgeon").write(": ").write(obj.surgeonId).write(", ").newLine();
		writer.indentation().write("incompatibleRooms").write(": ") //
				.write("[") //
				.write(obj.incompatibleRooms, (value, hasMore) -> {
					writer.write(value).write(", ", hasMore);
				}).write("]").write(",").newLine();
		writer.decreaseIndentation().indentation().write(")");
	}

	private void write(Occupant obj, Writer writer) throws IOException {
		writer.indentation().write("(").increaseIndentation().newLine();
		writer.indentation().write("id").write(": ").write(obj.id).write(", ").newLine();
		writer.indentation().write("gender").write(": ").write(obj.gender).write(", ").newLine();
		writer.indentation().write("ageGroup").write(": ").write(obj.ageGroup).write(", ").newLine();
		writer.indentation().write("lengthOfStay").write(": ").write(obj.lengthOfStay).write(", ").newLine();
		writer.indentation().write("workloadProduced").write(": ") //
				.write("[").write("0:", notEmpty(obj.workloadProduced)) //
				.write(obj.workloadProduced, (value, hasMore) -> {
					writer.write(value).write(", ", hasMore);
				}).write("]").write(",").newLine();
		writer.indentation().write("skillLevelRequired").write(": ") //
				.write("[").write("0:", notEmpty(obj.skillLevelRequired)) //
				.write(obj.skillLevelRequired, (value, hasMore) -> {
					writer.write(value).write(", ", hasMore);
				}).write("]").write(",").newLine();
		writer.indentation().write("assignedRoom").write(": ").write(obj.assignedToRoom).write(", ").newLine();
		writer.decreaseIndentation().indentation().write(")");
	}

	private void write(OperatingTheater obj, Writer writer) throws IOException {
		writer.indentation().write("(").increaseIndentation().newLine();
		writer.indentation().write("id").write(": ").write(obj.id).write(", ").newLine();
		writer.indentation().write("availability").write(": ")//
				.write("[").write("0:", notEmpty(obj.availability)) //
				.write(obj.availability, (value, hasMore) -> {
					writer.write(value).write(", ", hasMore);
				}).write("]").write(",").newLine();
		writer.indentation().write("isAvailable").write(": ")//
				.write("[").write("0:", notEmpty(obj.availability)) //
				.write(obj.availability, (value, hasMore) -> {
					writer.write(value > 0).write(", ", hasMore);
				}).write("]").write(",").newLine();
		writer.decreaseIndentation().indentation().write(")");
	}

	private void write(Room obj, Writer writer) throws IOException {
		var availableCapacity = obj.getAvailableCapacity();
		var genderAssignment = obj.getGenderAssignment();

		writer.indentation().write("(") //
				.write("id").write(": ").write(obj.id).write(", ") //
				.write("capacity").write(": ").write(obj.capacity).write(", ") //
				.write("availableCapacity").write(": ") //
				.write("[").write("0:", notEmpty(availableCapacity)) //
				.write(availableCapacity, (value, hasMore) -> {
					writer.write(value).write(", ", hasMore);
				}).write("]").write(", ") //
				.write("predefinedGenderAssignment").write(": ") //
				.write("[").write("0:", notEmpty(genderAssignment)) //
				.write(genderAssignment, (value, hasMore) -> {
					writer.write(nullable(value)).write(", ", hasMore);
				}).write("]") //
				.write(")");
	}

	private void write(Nurse obj, Writer writer) throws IOException {
		var maxWorkloadPerShift = obj.getMaxWorkloadPerShift();

		writer.indentation().write("(").newLine().increaseIndentation();
		writer.indentation().write("id").write(": ").write(obj.id).write(",").newLine();
		writer.indentation().write("skillLevel").write(": ").write(obj.skillLevel).write(",").newLine();

//		writer.indentation().write("workingShifts").write(": ").write("[").newLine().increaseIndentation()
//				.write(obj.workingShifts, (value, hasMore) -> {
//					write(value, writer);
//					writer.write(", ", hasMore).newLine();
//				}).decreaseIndentation().indentation().write("]").write(",").newLine();

		writer.indentation().write("maxWorkloadPerShift").write(": ") //
				.write("array2d(Days, ShiftTypes, [").newLine().increaseIndentation() //
				.write(maxWorkloadPerShift, (value, hasMore) -> {
					writer.indentation().write(toString(value)).write(", ", hasMore).newLine();
				}).decreaseIndentation().indentation().write("])").write(",").newLine();

		writer.decreaseIndentation().indentation().write(")");
	}

	private void write(WorkShift obj, Writer writer) throws IOException {
		writer.indentation().write("(") //
				.write("day").write(": ").write(obj.day).write(", ") //
				.write("shift").write(": ").write(obj.shift).write(", ") //
				.write("maxLoad").write(": ").write(obj.maxLoad) //
				.write(")");
	}

}
