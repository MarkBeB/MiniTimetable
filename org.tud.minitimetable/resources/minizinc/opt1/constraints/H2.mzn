include "../IHTP-model.mzn";

% PAS
% H2 Compatible rooms: Patients can only be assigned to one of their compatible rooms.
%

array[PatientIds] of set of RoomIds: patientCompatibleRooms = [
    RoomIds diff array2set(p.incompatibleRooms) | p in patients
];

%array[PatientIds] of RoomIds: patientCompatibleRooms2 = [ r | p in patients, r in RoomIds where not (r in p.incompatibleRooms) ];

constraint forall(p in PatientIds)(
  patientRoomAssignment[p] in patientCompatibleRooms[p]
);