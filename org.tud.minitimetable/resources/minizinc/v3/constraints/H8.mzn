include "../IHTP-model.mzn";

% NRA
% H8 For each shift, each occupied room must be allocated to a nurse who is on duty during this shift.
%

constraint forall(roomId in RoomIds, day in Days, shift in ShiftTypes)(
  bool2int(roomVar[roomId].isOccupied[day]) == count(nurseId in NurseIds)( isNurseInRoom[nurseId, day, shift, roomId] ) 
);

constraint forall(nurseId in NurseIds, day in Days, shift in ShiftTypes, roomId in RoomIds)(
  isNurseInRoom[nurseId, day, shift, roomId] -> isNurseAvailable[nurseId,day, shift]
);

constraint forall(nurseId in NurseIds, day in Days, shift in ShiftTypes, roomId in RoomIds)(
  isNurseInRoom[nurseId, day, shift, roomId] <-> roomVar[roomId].assignedNurse[day, shift] == nurseId
); 