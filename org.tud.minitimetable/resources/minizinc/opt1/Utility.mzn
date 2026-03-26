include "IHTP-model.mzn";

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%
% UTILITY CONSTRAINTS
%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

constraint forall(p in PatientIds)( 
  patientAdmittedUntil[p] = patientAdmissionDay[p] + patientData[p].lengthOfStay - 1
);

constraint forall(r in RoomIds, d in Days)(
  let {
    int: numOccupants = sum(o in occupantData)(isOccupantInRoom(o,d,r));
    array[PatientIds] of var bool: patientsInRoom = [ isPatientInRoom(p,d,r) | p in PatientIds ];
  } in
  sum(patientsInRoom) + numOccupants <= roomData[r].capacity * isRoomOccupied[r,d] /\ isRoomOccupied[r,d] <= sum(patientsInRoom) + numOccupants
);

constraint forall(r in RoomIds, d in Days)(
  let {
    bool: hasOccupants = exists(o in occupantData)(isOccupantInRoom(o,d,r));
  } in
  if hasOccupants then
    isRoomOccupied[r,d] = true
  else
    let {
      array[PatientIds] of var bool: patientsInRoom = [ isPatientInRoom(p,d,r) | p in PatientIds ];
    } in
    sum(patientsInRoom) <= roomData[r].capacity * isRoomOccupied[r,d] /\ isRoomOccupied[r,d] <= sum(patientsInRoom)
  endif
);
