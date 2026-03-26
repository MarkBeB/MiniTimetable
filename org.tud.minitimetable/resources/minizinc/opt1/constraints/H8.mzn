include "../IHTP-model.mzn";

% NRA
% H8 For each shift, each occupied room must be allocated to a nurse who is on duty during this shift.
%

constraint forall(r in RoomIds, d in Days, s in ShiftTypes)(
  isRoomOccupied[r,d] == count(n in NurseIds)(isNurseInRoom[n,d,s,r]) 
);

constraint forall(n in NurseIds, d in Days, s in ShiftTypes, r in RoomIds)(
  isNurseInRoom[n,d,s,r] -> isNurseWorking(n,d,s)
);

constraint forall(n in NurseIds, d in Days, s in ShiftTypes, r in RoomIds)(
  isNurseInRoom(n,d,s,r) <-> roomNurseAssignment[r,d,s] == n
);