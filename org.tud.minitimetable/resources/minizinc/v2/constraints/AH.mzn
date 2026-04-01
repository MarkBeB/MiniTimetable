include "../IHTP-model.mzn";

% Derived from competition rules.
% AH9 Room assignment: An admitted patient needs to be assigned to a room
%
%constraint forall(p in PatientIds)(
%  isPatientAdmitted[p]  -> occurs(patientRoomAssignment[p])
% /\ isPatientPostponed[p] -> patientRoomAssignment[p] == <> 
%);
constraint forall(p in PatientIds)(
  isPatientAdmitted[p] -> sum(r in RoomIds)(isPatientInRoom[p,r]) > 0
  /\ isPatientPostponed[p] -> sum(r in RoomIds)(isPatientInRoom[p,r]) == 0
);


% Derived from competition rules.
% AH10 No Room Transfer: Each patient must remain in the same room for the entire duration of their stay
%
constraint forall(p in PatientIds)(
  %Can be 0 if patient is not admitted
  sum(r in RoomIds)(isPatientInRoom[p,r]) <= 1
);


% Derived from competition rules.
% AH11 Surgery on admission day: Patients undergo surgery on the day of their admission
%
constraint forall(p in PatientIds, d in Days)(
  isPatientAdmitted[p] -> occurs(patientOTAssignment[p])
  /\ isPatientPostponed[p] -> absent(patientOTAssignment[p])
);