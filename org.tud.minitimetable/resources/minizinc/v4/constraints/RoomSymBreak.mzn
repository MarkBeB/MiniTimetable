include "../IHTP-Model.mzn";

%include "lex_lesseq.mzn";

array[RoomIds] of var int: roomUsage = [
  sum(p in PatientIds, d in Days) (isPatientPresent[p,d] /\ isPatientInRoom[p,r] )
  | r in RoomIds
];

function bool: areRoomsIdentical(RoomIds: r1, RoomIds: r2) = (
     roomCapacity(r1) == roomCapacity(r2)
  /\ forall(d in Days)(roomOccupants(r1,d) == roomOccupants(r2,d))
  /\ forall(p in PatientIds)(r1 in compatibleRooms(p) == r2 in compatibleRooms(p))
);

% Symmetrie breaker: if r and r+1 are identical, fill r before r+1
constraint forall(r in 1..length(RoomIds)-1) (
  if areRoomsIdentical(r, r+1) then
    roomUsage[r] >= roomUsage[r+1]
  endif
);

/*
ann: prio = priority(
  [isPatientInRoom[p, r] | p in PatientIds, r in RoomIds], 
  [length(RoomIds) - r | p in PatientIds, r in RoomIds]
);
*/