include "../IHTP-Model.mzn";

% NRA
% H8 For each shift, each occupied room must be allocated to a nurse who is on duty during this shift.
%

% First: determine which room is occupied
array[RoomIds, Days] of var bool: isRoomOccupied;

constraint forall(r in RoomIds, d in Days)(
  if roomHasOccupants(r,d) then
    isRoomOccupied[r,d] = 1
  else
    isRoomOccupied[r,d] <-> exists(p in PatientIds where r in compatibleRooms(p))(isPatientPresentInRoom[p,r,d])
  endif
  :: "Var RoomOccupied \(r) \(d)"
);

% If a room IS occupied, assign exactly one available nurse for every shift s
constraint forall(r in RoomIds, d in Days, s in ShiftTypes)(
  isRoomOccupied[r,d] == sum(n in availableNurses(d,s))(isNurseInRoom[n,r,d,s])
  :: "Con_H8_A \(r) \(d) \(s)"
);

% Nurses who are not working on Day d at Shift s cannot be assigned to a room at that time. Set the core decision variable to zero preemptively.
constraint forall(d in Days, s in ShiftTypes, n in NurseIds diff availableNurses(d,s)) (
  forall(r in RoomIds)(
    isNurseInRoom[n,r,d,s] = 0
    :: "Con_H8_B \(r) \(d) \(s) \(n)"
  )
);