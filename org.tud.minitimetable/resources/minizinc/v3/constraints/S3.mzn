include "../IHTP-model.mzn";

% NRA
% S3 Continuity of care: To ensure continuity of care, the total number of distinct nurses providing care to a patient during their entire stay should be minimized.
% The given rosters assume maximum one shift per day for each nurse, hence the number of different nurses who take care of a patient is at least 3,
% thus resulting in a minimum cost for this component of 3 times the number of admitted patients.
%

constraint forall(patient in patientVar, day in Days where patient.isAdmitted)(
  let{ var opt RoomIds: roomId = patient.assignedRoom[day] } in
  forall(shift in ShiftTypes, nurseId in NurseIds)(
    isNurseInRoom[nurseId,day, shift, roomId] -> patient.caredByNurse[nurseId]
  )
);

constraint forall(occupantId in OccupantIds, day in Days where isOccupantPresent(occupantId, day))(
  let{ RoomIds: roomId = occupantData[occupantId].assignedRoom } in
  forall(shift in ShiftTypes, nurseId in NurseIds)(
    isNurseInRoom[nurseId,day, shift, roomId] -> occupantVar[occupantId].caredByNurse[nurseId]
  )
);

%constraint Penalties.continuityOfCare = Weights.continuityOfCare * sum(patient in patientVar)
%  (max(0, sum(nurseId in NurseIds)(patient.caredByNurse[nurseId]) - length(ShiftTypes)));

constraint Penalties.continuityOfCare = Weights.continuityOfCare * (
    count(patient in patientVar, nurseId in NurseIds)(patient.caredByNurse[nurseId])
  + count(occupant in occupantVar, nurseId in NurseIds)(occupant.caredByNurse[nurseId])
);

constraint Penalties.continuityOfCare >= Weights.continuityOfCare * length(ShiftTypes) * (count(patient in patientVar)(patient.isAdmitted) + length(OccupantIds));