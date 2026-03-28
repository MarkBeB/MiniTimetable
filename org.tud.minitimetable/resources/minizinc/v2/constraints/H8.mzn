include "../IHTP-model.mzn";

% NRA
% H8 For each shift, each occupied room must be allocated to a nurse who is on duty during this shift.
%


constraint forall(r in RoomIds, d in Days)(
  isRoomOccupied[r,d] -> forall(s in ShiftTypes)(occurs(roomNurseAssignment[r,d,s]))
);

/*
constraint forall(r in RoomIds, d in Days, s in ShiftTypes)(
  isRoomOccupied[r,d] -> roomNurseAssignment[r,d,s] != <>
);

constraint forall(r in RoomIds, d in Days, s in ShiftTypes)(
  isRoomOccupied[r,d] == sum(n in NurseIds)(isNurseInRoomAt[n,d,r,s])
);
*/

constraint forall(d in Days, s in ShiftTypes)(
  let {
    set of NurseIds: availableNurses = { n | n in NurseIds where isNurseAvailable(n,d,s)};
  } in
  forall(r in RoomIds)(
    roomNurseAssignment[r,d,s] in availableNurses \/ absent(roomNurseAssignment[r,d,s])
  )  
);

/*
constraint forall(r in RoomIds, n in NurseIds, d in Days, s in ShiftTypes)(
  isNurseInRoomAt[n,d,r,s] == (roomNurseAssignment[r,d,s] == n)
);
*/