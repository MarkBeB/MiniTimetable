include "../IHTP-model.mzn";

% Derived from competition rules.
% AH9 Room assignment: An admitted patient needs to be assigned to a room
%
constraint forall(p in PatientIds)(
  isPatientAdmitted[p]  -> patientRoomAssignment[p] != <>
% /\ isPatientPostponed[p] -> patientRoomAssignment[p] == <> 

);


% Derived from competition rules.
% AH10 No Room Transfer: Each patient must remain in the same room for the entire duration of their stay
%
% Already handeld, because patientRoomAssignment[p] is either empty or has one RoomId


% Derived from competition rules.
% AH11 Surgery on admission day: Patients undergo surgery on the day of their admission
%
constraint forall(p in PatientIds, d in Days)(
  isPatientAdmitted[p] -> patientOTAssignment[p] != <>
%  /\ isPatientPostponed[p] -> patientOTAssignment[p] == <>
);