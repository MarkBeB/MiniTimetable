package org.tud.minitimetable.model.util;

import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.Collectors;

import org.tud.minitimetable.model.IhtcModel;
import org.tud.minitimetable.model.Nurse;
import org.tud.minitimetable.model.Occupant;
import org.tud.minitimetable.model.OperatingTheater;
import org.tud.minitimetable.model.Patient;
import org.tud.minitimetable.model.Room;
import org.tud.minitimetable.model.Surgeon;
import org.tud.minitimetable.model.WorkShift;

import com.alibaba.fastjson2.JSONObject;

public class Json2IhtcModelParser {

	@FunctionalInterface
	private static interface BuildFunction<ContextType, InputType, OutputType> {

		public OutputType build(ContextType context, InputType input);

	}

	@SuppressWarnings("unchecked")
	private <C, I, O> O[] buildArray(C context, JSONObject json, String value,
			BuildFunction<? super C, ? super I, O> transform, IntFunction<O[]> gen) {
		return json.getJSONArray(value).stream() //
				.map(e -> transform.build(context, (I) e)) //
				.toArray(gen);
	}

	@SuppressWarnings("unchecked")
	private <T, I> T[] buildArray(JSONObject json, String value, Function<I, T> transform, IntFunction<T[]> gen) {
		return json.getJSONArray(value).stream().map(e -> (I) e).map(transform).toArray(gen);
	}

	private int[] buildIntArray(JSONObject json, String value) {
		return json.getJSONArray(value).stream().mapToInt(e -> (Integer) e).toArray();
	}

	public IhtcModel read(JSONObject json) {
		IhtcModel obj = new IhtcModel();
		obj.days = json.getInteger("days");
		obj.skillLevels = json.getIntValue("skill_levels");
		obj.shiftTypes = json.getJSONArray("shift_types").toArray(n -> new String[n]);
		obj.ageGroups = json.getJSONArray("age_groups").toArray(n -> new String[n]);
		obj.weights = json.getJSONObject("weights").entrySet().stream().collect( //
				Collectors.toMap( //
						Entry::getKey, //
						entry -> (Integer) entry.getValue()) //
		);

		obj.nurses = buildArray(obj, json, "nurses", this::buildNurse, n -> new Nurse[n]);
		obj.surgeons = buildArray(obj, json, "surgeons", this::buildSurgeon, n -> new Surgeon[n]);
		obj.patients = buildArray(obj, json, "patients", this::buildPatient, n -> new Patient[n]);
		obj.occupants = buildArray(obj, json, "occupants", this::buildOccupant, n -> new Occupant[n]);
		obj.operatingTheaters = buildArray(obj, json, "operating_theaters", this::buildOperatingTheater,
				n -> new OperatingTheater[n]);
		obj.rooms = buildArray(obj, json, "rooms", this::buildRoom, n -> new Room[n]);

		return obj;
	}

	private Nurse buildNurse(IhtcModel parent, JSONObject json) {
		Nurse obj = new Nurse();
		obj.root = parent;
		obj.id = json.getString("id");
		obj.skillLevel = json.getIntValue("skill_level");
		obj.workingShifts = buildArray(obj, json, "working_shifts", this::buildWorkShift, n -> new WorkShift[n]);

		return obj;
	}

	private Surgeon buildSurgeon(IhtcModel parent, JSONObject json) {
		Surgeon obj = new Surgeon();
		obj.root = parent;
		obj.id = json.getString("id");
		obj.maxSurgeryTime = buildIntArray(json, "max_surgery_time");

		return obj;
	}

	private Room buildRoom(IhtcModel parent, JSONObject json) {
		Room obj = new Room();
		obj.root = parent;
		obj.id = json.getString("id");
		obj.capacity = json.getIntValue("capacity");

		return obj;
	}

	private OperatingTheater buildOperatingTheater(IhtcModel parent, JSONObject json) {
		OperatingTheater obj = new OperatingTheater();
		obj.root = parent;
		obj.id = json.getString("id");
		obj.availability = buildIntArray(json, "availability");

		return obj;
	}

	private WorkShift buildWorkShift(Nurse parent, JSONObject json) {
		WorkShift obj = new WorkShift();
		obj.root = parent;
		obj.day = json.getIntValue("day");
		obj.shift = json.getString("shift");
		obj.maxLoad = json.getIntValue("max_load");

		return obj;
	}

	private Occupant buildOccupant(IhtcModel parent, JSONObject json) {
		Occupant obj = new Occupant();
		obj.root = parent;
		obj.id = json.getString("id");
		obj.gender = json.getString("gender");
		obj.ageGroup = json.getString("age_group");
		obj.lengthOfStay = json.getInteger("length_of_stay");
		obj.workloadProduced = buildIntArray(json, "workload_produced");
		obj.skillLevelRequired = buildIntArray(json, "skill_level_required");
		obj.assignedToRoom = json.getString("room_id");

		return obj;
	}

	private Patient buildPatient(IhtcModel parent, JSONObject json) {
		Patient obj = new Patient();
		obj.root = parent;
		obj.id = json.getString("id");
		obj.gender = json.getString("gender");
		obj.ageGroup = json.getString("age_group");
		obj.lengthOfStay = json.getInteger("length_of_stay");
		obj.workloadProduced = buildIntArray(json, "workload_produced");
		obj.skillLevelRequired = buildIntArray(json, "skill_level_required");

		obj.mandatory = json.getBooleanValue("mandatory");
		obj.surgeryReleaseDay = json.getInteger("surgery_release_day");
		obj.surgeryDueDay = json.getIntValue("surgery_due_day", 0);
		obj.surgeryDuration = json.getInteger("surgery_duration");
		obj.surgeonId = json.getString("surgeon_id");
		obj.incompatibleRooms = buildArray(json, "incompatible_room_ids", Object::toString, n -> new String[n]);

		return obj;
	}

}
