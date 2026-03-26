include "../IHTP-model.mzn";

% PAS
% H2 Compatible rooms: Patients can only be assigned to one of their compatible rooms.
%

constraint forall(p in PatientIds)(
  let {
    set of RoomIds: compatibleRooms = RoomIds diff incompatibleRooms(p);
  } in
  patientRoomAssignment[p] in compatibleRooms
);