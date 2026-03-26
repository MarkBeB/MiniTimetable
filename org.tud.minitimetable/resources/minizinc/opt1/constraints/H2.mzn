include "../IHTP-model.mzn";

% PAS
% H2 Compatible rooms: Patients can only be assigned to one of their compatible rooms.
%

constraint forall(p in PatientIds, d in Days , r in patientData[p].incompatibleRooms)(
  patientRoomAssignment[p,d,r] = false
);

constraint forall(p in PatientIds, d in Days )(
  sum(r in RoomIds)(patientRoomAssignment[p,d,r]) <= 1
);