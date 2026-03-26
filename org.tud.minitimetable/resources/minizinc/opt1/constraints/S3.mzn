include "../IHTP-model.mzn";

% NRA
% S3 Continuity of care: To ensure continuity of care, the total number of distinct nurses providing care to a patient during their entire stay should be minimized.
% The given rosters assume maximum one shift per day for each nurse, hence the number of different nurses who take care of a patient is at least 3,
% thus resulting in a minimum cost for this component of 3 times the number of admitted patients.
%

array[NurseIds, Days, RoomIds] of var bool: nurseAssignedToRoom;
constraint forall(n in NurseIds, d in Days, s in ShiftTypes, r in RoomIds where isNurseWorking(n,d,s)) (
  nurseAssignedToRoom[n,d,r] >= isNurseInRoom[n,d,s,r]
);

constraint forall(p in PatientIds, d in Days, r in RoomIds, n in NurseIds) (
    patientCaredByNurse[p,n] >= isPatientInRoom[p,d,r] + nurseAssignedToRoom[n,d,r] - 1
);

constraint forall(o in OccupantIds, d in Days, r in RoomIds, n in NurseIds) (
    occupantCaredByNurse[o,n] >= isOccupantInRoom(o,d,r) + nurseAssignedToRoom[n,d,r] - 1
);

constraint Penalties.continuityOfCare = Weights.continuityOfCare * (
    sum(p in PatientIds, n in NurseIds)(patientCaredByNurse[p, n])
  + sum(o in OccupantIds, n in NurseIds)(occupantCaredByNurse[o, n])
);

constraint Penalties.continuityOfCare >= Weights.continuityOfCare * card(ShiftTypes) * (count(p in PatientIds)(isPatientAdmitted(p)) + card(OccupantIds));