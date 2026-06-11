include "../IHTP-Model.mzn";

% PAS
% H1 No gender mix: Patients of different genders may not share a room on any day.
%

array[RoomIds, Days, Genders] of var bool: hasGender;

constraint forall(r in RoomIds, d in Days, g in Genders)(
  totalIndividuals * hasGender[r,d,g] >= 
      sum(o in OccupantIds where isOccupantInRoom(o,d,r))(occupantGender(o) == g)
    + sum(p in PatientIds where isPatientPresentInRoom(p,r,d))(patientGender(p) == g)
  :: "CON_H1_A \(r) \(d) \(g)"
);

constraint forall(r in RoomIds, d in Days) (
  (sum(g in Genders)(hasGender[r,d,g]) == 1)
  :: "CON_H1_B \(r) \(d)"
);