include "../IHTP-Model.mzn";

% NRA
% S3 Continuity of care: To ensure continuity of care, the total number of distinct nurses providing care to a patient during their entire stay should be minimized.
% The given rosters assume maximum one shift per day for each nurse, hence the number of different nurses who take care of a patient is at least 3,
% thus resulting in a minimum cost for this component of 3 times the number of admitted patients.
%

% Huge improvement over v3



/*
constraint forall(n in NurseIds, r in RoomIds, d in Days) (
  nurseInRoomOnDay[n,r,d] >= exists(s in ShiftTypes) (
    isNurseInRoom[n,r,d,s]
  ) :: "Con_S3_A \(n) \(r) \(d)"
);
*/

/*
constraint forall(n in NurseIds, r in RoomIds, d in Days, s in ShiftTypes where isNurseAvailable(n,d,s)) (
  isNurseInRoom[n,r,d,s] -> nurseInRoomOnDay[n,r,d]
  :: "Con_S3_A \(n) \(r) \(d)"
);
*/

/*
constraint forall(n in NurseIds, r in RoomIds, d in Days) (
  if isNurseAvailable(n,d) then
    nurseInRoomOnDay[n,r,d] <-> exists(s in ShiftTypes) (
      isNurseInRoom[n,r,d,s]
    )
  else
    nurseInRoomOnDay[n,r,d] = 0
  endif
  :: "Con_S3_A \(n) \(r) \(d)"
);
*/

array[PatientIds, NurseIds] of var bool: patientCaredByNurse;
array[OccupantIds, NurseIds] of var bool: occupantCaredByNurse;
array[NurseIds, RoomIds, Days] of var bool: nurseInRoomOnDay;

constraint forall(n in NurseIds, r in RoomIds, d in Days where isNurseAvailable(n,d)) (
  nurseInRoomOnDay[n,r,d] >= exists(s in ShiftTypes) (
    isNurseInRoom[n,r,d,s]
  ) :: "Con_S3_A \(n) \(r) \(d)"
);

constraint forall(p in PatientIds, n in NurseIds, r in compatibleRooms(p)) (
  patientCaredByNurse[p,n] >= (
    isPatientInRoom[p,r] /\ 
    exists(d in Days where withinStayWindow(p,d)) (
        nurseInRoomOnDay[n,r,d] /\ isPatientPresent[p,d]
    )
  ) :: "Con_S3_B \(p) \(n) \(r)"
);

constraint forall(o in OccupantIds, n in NurseIds) (
  let { RoomIds: r = occupantRoom(o) } in
  occupantCaredByNurse[o,n] >= exists(d in Days where d < occupantLengthOfStay(o)) (
    nurseInRoomOnDay[n,r,d]
  ) :: "Con_S3_C \(o) \(n) \(r)"
);

constraint penaltyContinuityOfCare = Weights.continuityOfCare * (
    sum(p in PatientIds, n in NurseIds) (patientCaredByNurse[p,n])
  + sum(o in OccupantIds, n in NurseIds) (occupantCaredByNurse[o,n])
) :: "Con_S3_O";