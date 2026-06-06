include "../IHTP-Model.mzn";

% NRA
% H8 For each shift, each occupied room must be allocated to a nurse who is on duty during this shift.
%

array[RoomIds, Days] of var bool: isRoomOccupied;

constraint forall(r in RoomIds, d in Days)(
  if roomHasOccupants(r,d) then
    isRoomOccupied[r,d] = 1
  else
    isRoomOccupied[r,d] <-> exists(p in PatientIds where r in compatibleRooms(p))(isPatientPresentInRoom[p,r,d])
  endif
  :: "Var RoomOccupied \(r) \(d)"
);

/*
constraint forall(r in RoomIds, d in Days, s in ShiftTypes)(
     (isRoomOccupied[r,d] -> (roomNurse[r,d,s] in activeNurses[d,s]))
  /\ (not isRoomOccupied[r,d] -> absent(roomNurse[r,d,s]))
  :: "Con_H8_A \(r) \(d) \(s)"
);
*/

/*
constraint forall(r in RoomIds, d in Days, s in ShiftTypes)(
     (isRoomOccupied[r,d] -> occurs(roomNurse[r,d,s]))
  /\ (not isRoomOccupied[r,d] -> absent(roomNurse[r,d,s]))
  :: "Con_H8_A \(r) \(d) \(s)"
);
*/

/*
constraint forall(r in RoomIds, d in Days)(
  (isRoomOccupied[r,d] -> sum(s in ShiftTypes)(occurs(roomNurse[r,d,s])) == length(ShiftTypes))
  /\ (not isRoomOccupied[r,d] -> sum(s in ShiftTypes)(absent(roomNurse[r,d,s])) == length(ShiftTypes))
  :: "Con_H8_A \(r) \(d)"
);
*/

/*
constraint forall(r in RoomIds, d in Days, s in ShiftTypes)(
  (roomNurse[r,d,s] in activeNurses[d,s]) \/ absent(roomNurse[r,d,s])
  :: "Con_H8_D \(r) \(d) \(s)"
);
*/

constraint forall(r in RoomIds, d in Days, s in ShiftTypes)(
  isRoomOccupied[r,d] == sum(n in activeNurses[d,s])(isNurseInRoom[n,r,d,s])
  :: "Con_H8_A \(r) \(d) \(s)"
);

constraint forall(d in Days, s in ShiftTypes, n in NurseIds where not (n in activeNurses[d,s])) (
  forall(r in RoomIds)(
    isNurseInRoom[n,r,d,s] = 0
    :: "Con_H8_B \(r) \(d) \(s) \(n)"
  )
);

/*
constraint forall(r in RoomIds, d in Days, s in ShiftTypes) (
  sum(n in NurseIds)(isNurseInRoom[n,r,d,s]) <= 1
  :: "Con_H8_B \(r) \(d) \(s)"
);
*/

constraint forall(r in RoomIds, d in Days, s in ShiftTypes, n in NurseIds) (
  isNurseInRoom[n,r,d,s] <-> (roomNurse[r,d,s] = n)
  :: "Con_H8_C \(n) \(r) \(d) \(s)"
);

%constraint forall(r in RoomIds, d in Days, s in ShiftTypes) (
%  (roomNurse[r, d, s] in activeNurses[d, s] \/ absent(roomNurse[r, d, s]))
%  :: "Con_H8_B \(r) \(d) \(s)"
%);