include "IHTP-Model.mzn";

array[NurseIds, Days, ShiftTypes, RoomIds] of var bool: isNurseInRoom;

%array[NurseIds, Days, ShiftTypes] of var int: nursePenaltySkillLevel;

array[SurgeonIds, Days] of var DayTime: surgeonTime;
array[SurgeonIds,Days, OperatingTheaterIds] of var bool: isSurgeonInOT;

array[OperatingTheaterIds, Days] of var DayTime: otTime;

array[RoomIds, Days] of var bool: isRoomOccupied;
array[RoomIds, Days] of var Genders: roomGender;
array[RoomIds, Days, ShiftTypes] of var int: roomWorkload;


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%
% UTILITY CONSTRAINTS
%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

constraint forall(p in patientVar)(
  p.isPostponed <-> not p.isAdmitted
);

constraint forall(p in PatientIds)( 
  patientVar[p].admittedUntil = patientVar[p].admissionDay + patientLengthOfStay(p) - 1
);

constraint forall(r in RoomIds, d in Days)(
  let {
    bool: hasOccupants = exists(o in OccupantIds)(isOccupantInRoom(o,d,r));
    var bool: hasPatients = exists(patient in patientVar)(patient.assignedRoom[d] == r);
  } in
  isRoomOccupied[r,d] <-> hasOccupants \/ hasPatients
);
