include "../IHTP-Model.mzn";

% PAS
% H1 No gender mix: Patients of different genders may not share a room on any day.
%

% 'Room-Gender' Indicator variables
% True for a given gender g, when room r contains an occupant or patient on day d of gender g
array[RoomIds, Days, Genders] of var bool: hasGender;

% Evaluated at compile time
constraint forall(o in OccupantIds, r in RoomIds, d in Days where isOccupantInRoom(o,d,r))(
  (hasGender[r,d,occupantGender(o)] = 1)
  :: "CON_H1_A \(r) \(d) \(o)"
);

% if patient p is in room r on day d, set the indicator variable for the patient's gender g to true
constraint forall(p in PatientIds, r in compatibleRooms(p), d in Days) (
  (isPatientPresentInRoom[p,r,d] -> hasGender[r,d,patientGender(p)])
  :: "CON_H1_B \(r) \(d) \(p)"
);

% Ensure that only one gender is present in each room each day.
% It is not a problem if an empty room has a gender.
constraint forall(r in RoomIds, d in Days) (
  (sum(g in Genders)(hasGender[r,d,g]) == 1)
  :: "CON_H1_C \(r) \(d)"
);