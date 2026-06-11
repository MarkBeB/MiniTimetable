include "../IHTP-Model.mzn";

% NRA
% H8 For each shift, each occupied room must be allocated to a nurse who is on duty during this shift.
%

array[RoomIds, Days] of var bool: isRoomOccupied; 
constraint forall(r in RoomIds, d in Days)(
  isRoomOccupied[r,d] <-> (
       roomHasOccupants(r,d) 
    \/ exists(p in PatientIds where r in compatibleRooms(p))(isPatientPresentInRoom(p,r,d)))
);

constraint forall(r in RoomIds, d in Days, s in ShiftTypes)(
  isRoomOccupied[r,d] == sum(n in availableNurses(d,s))(isNurseInRoom[n,r,d,s])
  :: "Con_H8_A \(r) \(d) \(s)"
);

constraint forall(d in Days, s in ShiftTypes, n in unavailableNurses(d,s)) (
  forall(r in RoomIds)(
    isNurseInRoom[n,r,d,s] = 0
    :: "Con_H8_B \(r) \(d) \(s) \(n)"
  )
);