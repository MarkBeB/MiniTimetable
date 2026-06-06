include "../IHTP-Model.mzn";

% PAS
% H2 Compatible rooms: Patients can only be assigned to one of their compatible rooms.
%

% patient can not be assigned to incompatible rooms
constraint forall(p in PatientIds, r in incompatibleRooms(p))(
  (isPatientInRoom[p,r] = 0)
  :: "Con_H2_A \(p) \(r)"
);

% If a patient is admitted, they get exactly one compatible room
constraint forall(p in PatientIds) (
  (sum(r in compatibleRooms(p))(isPatientInRoom[p,r]) = isPatientAdmitted[p])
  :: "Con_H2_B \(p)"
);