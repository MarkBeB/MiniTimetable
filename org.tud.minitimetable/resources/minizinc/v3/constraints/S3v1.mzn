include "../IHTP-model.mzn";

% NRA
% S3 Continuity of care: To ensure continuity of care, the total number of distinct nurses providing care to a patient during their entire stay should be minimized.
% The given rosters assume maximum one shift per day for each nurse, hence the number of different nurses who take care of a patient is at least 3,
% thus resulting in a minimum cost for this component of 3 times the number of admitted patients.
%

constraint forall(p in PatientIds, d in Days where isPatientAdmitted[p])(
  let{ var opt RoomIds: r = patientRoom[p,d] } in
  forall(s in ShiftTypes, n in NurseIds)(
    isNurseInRoom[n,d,s,r] -> patientCaredByNurse[p,n]
  )
);

constraint forall(o in OccupantIds, d in Days where isOccupantPresent(o, d))(
  let{ RoomIds: r = occupantRoom(o)} in
  forall(s in ShiftTypes, n in NurseIds)(
    isNurseInRoom[n,d,s,r] -> occupantCaredByNurse[o,n]
  )
);

constraint Penalties.continuityOfCare = Weights.continuityOfCare * (
    count(p in PatientIds, n in NurseIds)(patientCaredByNurse[p,n])
  + count(o in OccupantIds, n in NurseIds)(occupantCaredByNurse[o,n])
);

constraint Penalties.continuityOfCare >= Weights.continuityOfCare * length(ShiftTypes) * (count(p in PatientIds)(isPatientAdmitted[p]) + length(OccupantIds));