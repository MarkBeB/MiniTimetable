include "../IHTP-Model.mzn";

% NRA
% S3 Continuity of care: To ensure continuity of care, the total number of distinct nurses providing care to a patient during their entire stay should be minimized.
% The given rosters assume maximum one shift per day for each nurse, hence the number of different nurses who take care of a patient is at least 3,
% thus resulting in a minimum cost for this component of 3 times the number of admitted patients.
%

array[PatientIds, NurseIds] of var bool: patientCaredByNurse;
array[OccupantIds, NurseIds] of var bool: occupantCaredByNurse;

constraint forall(p in PatientIds, n in NurseIds, r in compatibleRooms(p)) (
  forall(d in Days, s in ShiftTypes) (
    patientCaredByNurse[p, n] >= (isPatientPresent[p,d] /\ isPatientInRoom[p,r] /\ isNurseInRoom[n,r,d,s])
    :: "Con_S3_A \(p) \(n) \(r) \(d) \(s)"
  ) 
);

constraint forall(o in OccupantIds, n in NurseIds) (
  let { RoomIds: r = occupantRoom(o) } in
  forall(d in Days, s in ShiftTypes) (
    if isOccupantPresent(o,d) then
      occupantCaredByNurse[o, n] >= isNurseInRoom[n,r,d,s]
    endif
    :: "Con_S3_B \(o) \(n) \(r) \(d) \(s)"
  ) 
);

constraint penaltyContinuityOfCare = Weights.continuityOfCare * (
    sum(p in PatientIds, n in NurseIds) (patientCaredByNurse[p, n]) +
    sum(occ in OccupantIds, n in NurseIds) (occupantCaredByNurse[occ, n])
) :: "Con_S3_O";