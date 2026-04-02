include "IHTP-Model.mzn";

array[NurseIds, Days, ShiftTypes, RoomIds] of var bool: isNurseInRoom;
array[NurseIds, Days, ShiftTypes] of var int: nurseWorkload;
%array[NurseIds, Days, ShiftTypes] of var int: nursePenaltySkillLevel;

array[SurgeonIds, Days] of var DayTime: surgeonTime;
array[SurgeonIds,Days, OperatingTheaterIds] of var bool: isSurgeonInOT;

%array[OperatingTheaterIds, Days] of var DayTime: otTime;

%array[RoomIds, Days] of var bool: isRoomOccupied;
array[RoomIds, Days] of var Genders: roomGender;


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%
% UTILITY CONSTRAINTS
%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

constraint forall(patient in patientVar)(
  patient.isPostponed <-> not patient.isAdmitted
);

constraint forall(id in PatientIds)( 
  patientVar[id].admittedUntil = patientVar[id].admissionDay + patientData[id].lengthOfStay - 1
);

constraint forall(roomId in RoomIds, day in Days)(
  let {
    bool: hasOccupants = exists(occupant in occupantData)(occupant.assignedRoom == roomId /\ day < occupant.lengthOfStay);
    var bool: hasPatients = exists(patient in patientVar)(patient.assignedRoom[day] == roomId);
  } in
  roomVar[roomId].isOccupied[day] <-> hasOccupants \/ hasPatients
);
