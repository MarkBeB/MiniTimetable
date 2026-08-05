include "../IHTP-Model.mzn";

% Additional Hard Constraints
% AH2 Room Assignment on Admittance: A patient admitted on any given day must also be assigned a room, while a postponed patient does not require a room.
%

% If a patient is admitted, they get exactly one compatible room
constraint forall(p in PatientIds) (
  (sum(r in compatibleRooms(p))(isPatientInRoom[p,r]) = isPatientAdmitted[p])
  :: "Con_AH2 \(p)"
);