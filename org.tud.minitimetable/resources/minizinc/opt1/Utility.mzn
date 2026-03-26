include "IHTP-model.mzn";

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

constraint forall(r in RoomIds, d in Days)(
  let {
    int: numOccupants = sum(o in occupantData)(isOccupantInRoom(o,d,r));
    array[PatientIds] of var bool: patientsInRoom = [ isPatientInRoom(p,d,r) | p in PatientIds ];
  } in
  sum(patientsInRoom) + numOccupants <= roomData[r].capacity * roomVar[r].isOccupied[d] /\ roomVar[r].isOccupied[d] <= sum(patientsInRoom) + numOccupants
);

constraint forall(r in RoomIds, d in Days)(
  let {
    bool: hasOccupants = exists(o in occupantData)(isOccupantInRoom(o,d,r));
  } in
  if hasOccupants then
    roomVar[r].isOccupied[d] = true
  else
    let {
      array[PatientIds] of var bool: patientsInRoom = [ isPatientInRoom(p,d,r) | p in PatientIds ];
    } in
    sum(patientsInRoom) <= roomData[r].capacity * roomVar[r].isOccupied[d] /\ roomVar[r].isOccupied[d] <= sum(patientsInRoom)
  endif
);

constraint forall(nurseId in NurseIds, day in Days, shift in ShiftTypes)(
  nurseVar[nurseId].isWorking[day, shift] <-> nurseData[nurseId].maxWorkloadPerShift[day, shift] > 0
);

