include "../data/IHTC_model.mzn";

% NRA
% S3 Continuity of care: To ensure continuity of care, the total number of distinct nurses providing care to a patient during their entire stay should be minimized.
% The given rosters assume maximum one shift per day for each nurse, hence the number of different nurses who take care of a patient is at least 3,
% thus resulting in a minimum cost for this component of 3 times the number of admitted patients.
%

constraint forall(patient in patientVar, day in Days where patient.isAdmitted)(
  let{ var opt RoomIds: roomId = patient.assignedRoom[day] } in
  forall(shift in ShiftTypes)(
    forall(nurseId in NurseIds)(
      nurseVar[nurseId].assignedRooms[day, shift, roomId] -> patient.caredByNurse[nurseId]
    )
  )
);

constraint Penalties.continuityOfCare = Weights.continuityOfCare * sum(patient in patientVar)(max(0, sum(nurseId in NurseIds)(patient.caredByNurse[nurseId]) - length(ShiftTypes)));