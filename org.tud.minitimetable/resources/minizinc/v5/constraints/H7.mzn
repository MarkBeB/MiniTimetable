include "../IHTP-Model.mzn";

% PAS
% H7 Room capacity: The number of patients that occupy a room per day cannot exceed the capacity of the room.
%

constraint forall(r in RoomIds, d in Days)(
  let {
    var int: numberofPatients = sum(p in PatientIds where r in compatibleRooms(p))(isPatientPresentInRoom[p,r,d]);
    int: numberOfOccupants = sum(o in OccupantIds)(isOccupantInRoom(o,d,r));
  } in
  numberofPatients + numberOfOccupants <= roomCapacity(r)
  :: "Con_H7_A \(r) \(d)"
);