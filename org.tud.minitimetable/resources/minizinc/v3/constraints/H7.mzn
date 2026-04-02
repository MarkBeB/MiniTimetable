include "../IHTP-model.mzn";

% PAS
% H7 Room capacity: The number of patients that occupy a room per day cannot exceed the capacity of the room.
%

constraint forall(r in RoomIds, d in Days)(
  roomVar[r].assignedPatients[d] = count(p in patientVar)(p.assignedRoom[d] == r)
);

constraint forall(r in RoomIds, d in Days)(  
  roomVar[r].assignedPatients[d] + roomData[r].assignedOccupants[d] <= roomCapacity(r)
);

%constraint forall(roomId in RoomIds, day in Days)(
%  roomVar[roomId].assignedPatients[day] <= roomData[roomId].availableCapacity[day]
%);