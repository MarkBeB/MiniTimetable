include "IHTP-Model.mzn";

array[NurseIds, Days, ShiftTypes, RoomIds] of var bool: isNurseInRoom;

array[PatientIds] of var MaxDays: patientAdmissionDay;
array[PatientIds] of var MaxDays: patientAdmittedUntil;
array[PatientIds] of var bool: isPatientPostponed;
array[PatientIds] of var bool: isPatientAdmitted = [p: not isPatientPostponed[p] | p in PatientIds];
array[PatientIds, NurseIds] of var bool: patientCaredByNurse;
array[PatientIds,Days] of var opt RoomIds: patientRoom;
array[PatientIds] of var opt OperatingTheaterIds: patientOT;

array[OccupantIds, NurseIds] of var bool: occupantCaredByNurse;

array[OperatingTheaterIds, Days] of var DayTime: otTime;


array[RoomIds, Days] of var bool: isRoomOccupied;
array[RoomIds, Days, ShiftTypes] of var opt NurseIds: roomNurseAssignment;


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%
% UTILITY CONSTRAINTS
%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

constraint forall(p in PatientIds)( 
  patientAdmittedUntil[p] = patientAdmissionDay[p] + patientLengthOfStay(p) - 1
);

constraint forall(r in RoomIds, d in Days)(
  let {
    bool: hasOccupants = exists(o in OccupantIds)(isOccupantInRoom(o,d,r));
    var bool: hasPatients = exists(p in PatientIds)(patientRoom[p,d] == r);
  } in
  isRoomOccupied[r,d] <-> hasOccupants \/ hasPatients
);
