include "../IHTP-model.mzn";

% NRA
% H8 For each shift, each occupied room must be allocated to a nurse who is on duty during this shift.
%

constraint forall(r in RoomIds, d in Days, s in ShiftTypes)(
  bool2int(roomVar[r].isOccupied[d]) == count(n in NurseIds)( nurseVar[n].assignedRooms[d, s, r] ) 
);

constraint forall(n in NurseIds, d in Days, s in ShiftTypes, r in RoomIds)(
  nurseVar[n].assignedRooms[d, s, r] -> isNurseAvailable(n, d, s)
);

constraint forall(n in NurseIds, d in Days, s in ShiftTypes, r in RoomIds)(
  nurseVar[n].assignedRooms[d, s, r] <-> roomVar[r].assignedNurse[d, s] == n
);