include "model/IHTC_model.mzn";

%PAS

%H1 No gender mix: Patients of different genders may not share a room on any day.

% PAS
% H2 Compatible rooms: Patients can only be assigned to one of their compatible rooms.
%
constraint forall(patient in patients)(
  forall(incompatibleRoom in patient.incompatibleRooms)(
    patient.assignedToRoom != incompatibleRoom 
  )
);

% PAS
% S1 Age groups: For each day of the decision horizon and for each room, the maximum difference between age groups of patients sharing the room should be minimized.
%
constraint Penalties.roomMixedAge == Weights.roomMixedAge * 
  sum(day in Day)(
    sum(room in rooms)(
      sum(occupant in occupants where occupant.lengthOfStay <= 1 /\ occupant.assignedToRoom == room.id)(1)
    )
);

constraint forall(day in Day)(
  forall(patientA, patientB in patients where patientA != patientB /\ patientA.assignedRoom[day] == patientB.assignedRoom[day])(
    true %patientA.gender != patientB.gender
  )
);

%function var int: maxAgeInRoom(array[_] of Patient: patiens, Day: day, RoomId: roomId) = 
%  enum2int(max (patient in patiens where patient.assigned[day] == roomId)(patient.ageGroup));

%function var int: minAgeInRoom(array[_] of Patient: patiens, Day: day, RoomId: roomId) = 
%  enum2int(min (patient in patiens where patient.assigned[day] == roomId)(patient.ageGroup));


% PAS
% H7 Room capacity: The number of patients that occupy a room per day cannot exceed the capacity of the room.
%
constraint forall(room in rooms, day in Day)(
  room.assignedPatients[day] = sum(patient in patients where room.id == patient.assignedRoom[day])(1)
);

constraint forall(room in rooms, day in Day)(
  room.assignedPatients[day] <= room.availableCapacity[day]
);



% PAS
% Ensure the admission day of each patient lies within valid range of days.
% suregeryReleaseDay is the earliest day a patient can be admissed to a room
% surgeryDueDay is the latest day a patient must be admissed to a room, not all patients have a must day.
constraint forall(patient in patients)(
  if patient.mandatory then
    patient.earliestPossibleAdmission <= patient.admissionDay /\ patient.admissionDay <= patient.latestPossibleAdmission
  else
    patient.earliestPossibleAdmission <= patient.admissionDay /\ patient.admissionDay <= lastDay + 1
  endif
);

constraint forall(patient in patients)(
  if patient.mandatory then
    patient.assignedToRoom != <>
  else
    (patient.admissionDay <= lastDay -> patient.assignedToRoom != <>) /\ (lastDay < patient.admissionDay -> patient.assignedToRoom == <>)
  endif
);

constraint forall(patient in patients)(
  if occurs(patient.assignedToRoom) then
    forall(day in Day where patient.admissionDay <= day /\ day <= patient.admissionDay + patient.lengthOfStay)(
      patient.assignedRoom[day] == patient.assignedToRoom
    )
  endif
);

% PAS
% Calculate penalty for every optional patient which was not assigned
constraint Penalties.unscheduledOptional == Weights.unscheduledOptional * sum(patient in patients where not patient.mandatory)
  (if lastDay < patient.admissionDay then 1 else 0 endif);

constraint forall(room in rooms, occupant in occupants)(
  if(room.id == occupant.assignedToRoom) then
    forall(day in Day where day < occupant.lengthOfStay)(
      room.assignedGender[day] == occupant.gender 
    )
  endif
);











constraint forall(operatingTheater in operatingTheaters, day in Day)(
  operatingTheater.isAvailable[day] = (operatingTheater.availability[day] > 0)
);

%surgical case planning
%constraint forall(surgeon in surgeons, day in index_set(Day))(
%  surgeon.maxSurgeryTime[day] <= surgeon.assignedSurgeryTime[day]
  % /\ surgeon.assignedSurgeryTime[day] >= 0
%);


%constraint forall(operatingTheater in operatingTheaters, day in index_set(Day))(
%  operatingTheater.availability[day] <= operatingTheater.assignedTime[day]
  % /\ operatingTheater.assignedTime[day] >= 0
%);

solve minimize score;