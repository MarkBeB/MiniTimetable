include "../IHTP-model.mzn";

% NRA
% S3 Continuity of care: To ensure continuity of care, the total number of distinct nurses providing care to a patient during their entire stay should be minimized.
% The given rosters assume maximum one shift per day for each nurse, hence the number of different nurses who take care of a patient is at least 3,
% thus resulting in a minimum cost for this component of 3 times the number of admitted patients.
%

constraint forall(p in patientVar, d in Days where p.isAdmitted)(
  let{ var opt RoomIds: r = p.assignedRoom[d] } in
  forall(s in ShiftTypes, n in NurseIds)(
    isNurseInRoom[n,d,s,r] -> p.caredByNurse[n]
  )
);

constraint forall(o in OccupantIds, d in Days where isOccupantPresent(o, d))(
  let{ RoomIds: r = occupantRoom(o)} in
  forall(s in ShiftTypes, n in NurseIds)(
    isNurseInRoom[n,d,s,r] -> occupantVar[o].caredByNurse[n]
  )
);

%constraint Penalties.continuityOfCare = Weights.continuityOfCare * sum(patient in patientVar)
%  (max(0, sum(nurseId in NurseIds)(patient.caredByNurse[nurseId]) - length(ShiftTypes)));

constraint Penalties.continuityOfCare = Weights.continuityOfCare * (
    count(p in patientVar, n in NurseIds)(p.caredByNurse[n])
  + count(o in occupantVar, n in NurseIds)(o.caredByNurse[n])
);

constraint Penalties.continuityOfCare >= Weights.continuityOfCare * length(ShiftTypes) * (count(p in patientVar)(p.isAdmitted) + length(OccupantIds));