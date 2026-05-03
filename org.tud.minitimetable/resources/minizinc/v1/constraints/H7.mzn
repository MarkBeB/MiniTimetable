include "../IHTP-model.mzn";

% PAS
% H7 Room capacity: The number of patients that occupy a room per day cannot exceed the capacity of the room.
%

constraint forall(roomId in RoomIds, day in Days)(
  roomVar[roomId].assignedPatients[day] = count(patient in patientVar)(patient.assignedRoom[day] == roomId)
);

constraint forall(roomId in RoomIds, day in Days)( 
  let { int: occupantsInRoom = sum(o in OccupantIds)(isOccupantInRoom(o,day,roomId)) } in
  roomVar[roomId].assignedPatients[day] + occupantsInRoom <= roomData[roomId].capacity
);

%constraint forall(roomId in RoomIds, day in Days)(
%  roomVar[roomId].assignedPatients[day] <= roomData[roomId].availableCapacity[day]
%);