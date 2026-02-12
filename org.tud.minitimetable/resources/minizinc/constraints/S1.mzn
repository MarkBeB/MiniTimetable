include "../data/IHTC_model.mzn";

% PAS
% S1 Age groups: For each day of the decision horizon and for each room, the maximum difference between age groups of patients sharing the room should be minimized.
%

/*
constraint forall(room in rooms, day in Day)(
  let {
    array[int] of var AgeGroup: assignedOccupants = [occupant.ageGroup | occupant in occupants 
      where occupant.assignedToRoom == room.id /\ day <= occupant.lengthOfStay];
    array[int] of var opt AgeGroup: assignedPatients = [patient.ageGroup | patient in patients 
      where patient.assignedRoom[day] == room.id];

    var opt AgeGroup: maxOccupantAge = max_weak(assignedOccupants);
    var opt AgeGroup: minOccupantAge = min_weak(assignedOccupants);    
    var opt AgeGroup: maxPatientAge = max_weak(assignedPatients);
    var opt AgeGroup: minPatientAge = min_weak(assignedPatients);

  } in room.maxAgeGroup[day] = max_weak([maxOccupantAge, maxPatientAge])
    /\ room.minAgeGroup[day] = min_weak([minOccupantAge, minPatientAge])
);
*/

constraint forall(room in rooms, day in Day)(
  room.maxAgeGroupOccupants[day] = max_weak(occupant in occupants where occupant.assignedToRoom == room.id /\ day <= occupant.lengthOfStay)(occupant.ageGroup)
);

constraint forall(room in rooms, day in Day)(
  room.minAgeGroupOccupants[day] = min_weak(occupant in occupants where occupant.assignedToRoom == room.id /\ day <= occupant.lengthOfStay)(occupant.ageGroup)
);

constraint forall(room in rooms, day in Day)(
  room.maxAgeGroupPatients[day] = max_weak(patient in patients where patient.assignedRoom[day] == room.id)(patient.ageGroup)
);

constraint forall(room in rooms, day in Day)(
  room.minAgeGroupPatients[day] = min_weak(patient in patients where patient.assignedRoom[day] == room.id)(patient.ageGroup)
);

constraint forall(room in rooms, day in Day)(
  room.maxAgeGroup[day] = max_weak([room.maxAgeGroupOccupants[day], room.maxAgeGroupPatients[day]])
);

constraint forall(room in rooms, day in Day)(
  room.minAgeGroup[day] = min_weak([room.minAgeGroupOccupants[day], room.minAgeGroupPatients[day]])
);

constraint forall(room in rooms, day in Day)(
  room.agePenalty[day] = enum2int(room.maxAgeGroup[day]) - enum2int(room.minAgeGroup[day])
);

constraint Penalties.roomMixedAge == Weights.roomMixedAge * sum(room in rooms, day in Day)(enum2int(room.maxAgeGroup[day]) - enum2int(room.minAgeGroup[day]));