include "../IHTP-model.mzn";

% PAS
% H7 Room capacity: The number of patients that occupy a room per day cannot exceed the capacity of the room.
%

constraint forall(r in RoomIds, d in Days)(
  let {
    int: occupantsInRoom = rooms[r].assignedOccupants[d]; % count(o in Occupants)(occupants[o].assignedRoom == r)
    var int: patientsInRoom = sum(p in PatientIds)(isPatientInRoom[p,d,r]);
  } in
  occupantsInRoom + patientsInRoom <= roomCapacity(r)
);