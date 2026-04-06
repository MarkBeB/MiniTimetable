include "../IHTP-Model.mzn";

% PAS
% H1 No gender mix: Patients of different genders may not share a room on any day.
%

array[RoomIds, Days, Genders] of var bool: hasGender;
constraint forall(o in OccupantIds, r in RoomIds, d in Days where isOccupantInRoom(o,d,r))(
  (hasGender[r,d,occupantGender(o)] = 1)
  :: "CON_H1_A \(r) \(d) \(o)"
);

constraint forall(p in PatientIds, r in compatibleRooms(p), d in Days) (
  (hasGender[r,d,patientGender(p)] >= isPatientPresentInRoom[p,r,d])
  :: "CON_H1_B \(r) \(d) \(p)"
);

constraint forall(r in RoomIds, d in Days) (
  (sum(g in Genders)(hasGender[r,d,g]) == 1)
  :: "CON_H1_C \(r) \(d)"
);