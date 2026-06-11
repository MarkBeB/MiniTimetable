include "../IHTP-Model.mzn";

% PAS
% H1 No gender mix: Patients of different genders may not share a room on any day.
%

array[RoomIds, Days, Genders] of var bool: roomGender;

constraint forall(r in RoomIds, d in Days, g in Genders)(
  totalIndividuals * roomGender[r,d,g] >= 
      sum(o in OccupantIds where isOccupantGender(o,g))(isOccupantInRoom(o,d,r))
    + sum(p in PatientIds where isPatientGender(p,g))(isPatientPresentInRoom[p,r,d])
  :: "CON_H1_A \(r) \(d) \(g)"
);

constraint forall(r in RoomIds, d in Days) (
  (sum(g in Genders)(roomGender[r,d,g]) <= 1)
  :: "CON_H1_B \(r) \(d)"
);