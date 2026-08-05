include "../IHTP-Model.mzn";

% NRA
% S3 Continuity of care: To ensure continuity of care, the total number of distinct nurses providing care to a patient during their entire stay should be minimized.
% The given rosters assume maximum one shift per day for each nurse, hence the number of different nurses who take care of a patient is at least 3,
% thus resulting in a minimum cost for this component of 3 times the number of admitted patients.
%

array[NurseIds, RoomIds, Days] of var bool: nurseInRoomOnDay;
array[PatientIds, NurseIds] of var bool: patientCaredByNurse;
array[OccupantIds, NurseIds] of var bool: occupantCaredByNurse;

% help variable, which is true, if a nurse n was in room r on day d at any shift s
constraint forall(n in NurseIds, r in RoomIds, d in Days) (
  if isNurseAvailable(n,d) then
    nurseInRoomOnDay[n,r,d] = exists(s in ShiftTypes) (
      isNurseInRoom[n,r,d,s]
    ) :: "Con_S3_A \(n) \(r) \(d)"
  else
    nurseInRoomOnDay[n,r,d] = 0
  endif
);

constraint forall(p in PatientIds, n in NurseIds) (
  patientCaredByNurse[p,n] = exists(r in compatibleRooms(p), d in Days where withinStayWindow(p,d))(
    isPatientInRoom[p,r] /\ nurseInRoomOnDay[n,r,d] /\ isPatientPresent[p,d]
  ) :: "Con_S3_B \(p) \(n)"
);

constraint forall(o in OccupantIds, n in NurseIds) (
  let { RoomIds: r = occupantRoom(o) } in
  occupantCaredByNurse[o,n] = exists(d in Days where d < occupantLengthOfStay(o)) (
    nurseInRoomOnDay[n,r,d]
  ) :: "Con_S3_C \(o) \(n)"
);

constraint violationsContinuityOfCare = (
    sum(p in PatientIds, n in NurseIds) (patientCaredByNurse[p,n])
  + sum(o in OccupantIds, n in NurseIds) (occupantCaredByNurse[o,n])
) :: "Con_S3_O";