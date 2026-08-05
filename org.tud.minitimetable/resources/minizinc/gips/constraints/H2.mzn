include "../IHTP-Model.mzn";

% PAS
% H2 Compatible rooms: Patients can only be assigned to one of their compatible rooms.
%

% patient can not be assigned to incompatible rooms
constraint forall(p in PatientIds, r in incompatibleRooms(p))(
  (isPatientInRoom[p,r] = 0)
  :: "Con_H2_A \(p) \(r)"
);