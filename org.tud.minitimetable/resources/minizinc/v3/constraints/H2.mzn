include "../IHTP-Model.mzn";

% PAS
% H2 Compatible rooms: Patients can only be assigned to one of their compatible rooms.
%

constraint forall(p in PatientIds, d in Days)(
  not (patientRoom[p,d] in incompatibleRooms(p))
);