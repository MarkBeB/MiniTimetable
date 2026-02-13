include "../data/IHTC_model.mzn";

% NRA
% H8 For each shift, each occupied room must be allocated to a nurse who is on duty during this shift.
%

constraint forall(roomId in RoomIds, day in Days, shift in ShiftTypes)(
  bool2int(roomVar[roomId].isOccupied[day]) == count(nurseId in NurseIds)( nurseVar[nurseId].assignedRooms[day, shift, roomId] ) 
);

constraint forall(nurseId in NurseIds, day in Days, shift in ShiftTypes, roomId in RoomIds)(
  nurseVar[nurseId].assignedRooms[day, shift, roomId] -> nurseVar[nurseId].isWorking[day, shift]
);