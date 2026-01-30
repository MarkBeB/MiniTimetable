include "../IHTC_model.mzn";

% PAS
% S1 Age groups: For each day of the decision horizon and for each room, the maximum difference between age groups of patients sharing the room should be minimized.
%

constraint forall(room in rooms, day in Day)(
  room.minAgeGroup[day] == min(AgeGroup) % min(patient in patients where patient.assignedRoom[day] == room.id)(patient.ageGroup)
);

constraint forall(room in rooms, day in Day)(
  room.maxAgeGroup[day] == max(AgeGroup)
);

constraint Penalties.roomMixedAge == Weights.roomMixedAge * sum(room in rooms, day in Day)(enum2int(room.maxAgeGroup[day]) - enum2int(room.minAgeGroup[day]));