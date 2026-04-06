include "../IHTP-Model.mzn";

% NRA
% H8 For each shift, each occupied room must be allocated to a nurse who is on duty during this shift.
%

constraint forall(r in RoomIds, d in Days, s in ShiftTypes)(
  bool2int(isRoomOccupied[r,d]) == count(n in NurseIds)( isNurseInRoom[n,d,s,r] ) 
) :: constraint_name("Con_H8_A");

constraint forall(n in NurseIds, d in Days, s in ShiftTypes, r in RoomIds)(
  isNurseInRoom[n,d,s,r] -> isNurseAvailable(n,d,s)
) :: constraint_name("Con_H8_B");

constraint forall(n in NurseIds, d in Days, s in ShiftTypes, r in RoomIds)(
  isNurseInRoom[n,d,s,r] <-> roomNurseAssignment[r,d,s] == n
) :: constraint_name("Con_H8_C");