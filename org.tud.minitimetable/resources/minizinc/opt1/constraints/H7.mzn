include "../IHTP-model.mzn";

% PAS
% H7 Room capacity: The number of patients that occupy a room per day cannot exceed the capacity of the room.
%

constraint forall(r in RoomIds, d in Days)(
  roomAssignedPatients[r,d] = sum(p in PatientIds)(isPatientInRoom(p,d,r))
);

constraint forall(r in RoomIds, d in Days)(  
  roomAssignedPatients[r,d] + rooms[r].assignedOccupants[d] <= rooms[r].capacity
);