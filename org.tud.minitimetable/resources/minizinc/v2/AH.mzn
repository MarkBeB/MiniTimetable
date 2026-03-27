include "IHTP-model.mzn";

% Derived from competition rules.
% AH9 Room assignment: An admitted patient needs to be assigned to a room
%

constraint forall(p in PatientIds)(
  isPatientPostponed[p] -> patientRoomAssignment[p] == <> /\
  isPatientAdmitted[p]  -> patientRoomAssignment[p] != <>
);