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
  sum(patientsInRoom) + numOccupants <= roomCapacity(r) * isRoomOccupied[r,d] /\ isRoomOccupied[r,d] <= sum(patientsInRoom) + numOccupants
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
    sum(patientsInRoom) <= roomCapacity(r) * isRoomOccupied[r,d] /\ isRoomOccupied[r,d] <= sum(patientsInRoom)
  endif
);



array[NurseIds, Days, RoomIds] of var bool: nurseAssignedToRoom;
constraint forall(n in NurseIds, d in Days, s in ShiftTypes, r in RoomIds where isNurseWorking(n,d,s)) (
  nurseAssignedToRoom[n,d,r] >= isNurseInRoom[n,d,s,r]
);
