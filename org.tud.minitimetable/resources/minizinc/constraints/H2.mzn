include "../data/IHTC_model.mzn";

% PAS
% H2 Compatible rooms: Patients can only be assigned to one of their compatible rooms.
%

constraint forall(id in PatientIds, day in Days)(
  not (patientVar[id].assignedRoom[day] in patientData[id].incompatibleRooms)
);

/*
constraint forall(id in PatientIds)(
  not (patientVar[id].assignedRoom in patientData[id].incompatibleRooms)
);
*/