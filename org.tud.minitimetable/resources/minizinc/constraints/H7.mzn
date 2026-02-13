include "../data/IHTC_model.mzn";

% PAS
% H7 Room capacity: The number of patients that occupy a room per day cannot exceed the capacity of the room.
%

/* Old
constraint forall(room in rooms, day in Day)(
  room.assignedPatients[day] == sum(patient in patients where occurs(patient.assignedRoom[day]) /\ patient.assignedRoom[day] == room.id)(1) 
  /\ room.assignedPatients[day] <= room.availableCapacity[day]
);
*/

constraint forall(roomId in RoomIds, day in Days)(
  roomVar[roomId].assignedPatients[day] = count(patient in patientVar)(patient.assignedRoom[day] == roomId)
);

constraint forall(roomId in RoomIds, day in Days)(
  roomVar[roomId].assignedPatients[day] <= roomData[roomId].availableCapacity[day]
);