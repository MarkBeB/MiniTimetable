include "../IHTP-model.mzn";

% PAS
% H2 Compatible rooms: Patients can only be assigned to one of their compatible rooms.
%

%constraint forall(p in PatientIds)(
%  patientRoomAssignment[p] in compatibleRooms(p) \/ absent(patientRoomAssignment[p])
%);

constraint forall(p in PatientIds, r in incompatibleRooms(p))(
  isPatientInRoom[p,r] = false
);

/*
constraint forall(p in PatientIds)(
  not (patientRoomAssignment[p] in patientData[p].incompatibleRooms)
);

constraint forall(p in PatientIds, r in incompatibleRooms(p))(
  isPatientInRoom[p,r] = false
);
*/
