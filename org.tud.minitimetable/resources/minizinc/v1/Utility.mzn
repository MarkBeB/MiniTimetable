%include "IHTP-model.mzn";

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

constraint forall(nurseId in NurseIds, day in Days, shift in ShiftTypes)(
  nurseVar[nurseId].isWorking[day, shift] <-> nurseData[nurseId].maxWorkloadPerShift[day, shift] > 0
);

