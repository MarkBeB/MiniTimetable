include "IHTP-model.mzn";

% PAS
% H7 Room capacity: The number of patients that occupy a room per day cannot exceed the capacity of the room.
%
constraint forall(r in RoomIds, d in Days)(
  let {
    int: occupantsInRoom = sum(o in OccupantIds)(isOccupantInRoom(o,d,r));
    var int: patientsInRoom = sum(p in PatientIds)(isPatientPresent[p,d] /\ isPatientInRoom[p,r]);
  } in
  (occupantsInRoom + patientsInRoom) <= roomCapacity(r)
);