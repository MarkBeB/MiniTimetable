include "../IHTP-model.mzn";

% PAS
% H1 No gender mix: Patients of different genders may not share a room on any day.
%

% There is no difference between = and == in MiniZinc

array[RoomIds, Days] of var Genders: roomGender;

% fix room gender to occupant gender
constraint forall(r in RoomIds, d in Days, o in OccupantIds where isOccupantPresentInRoom(o,d,r))(
  roomGender[r, d] = occupantGender(o)
);

% only patients with the same gender should share a room
%constraint forall(r in RoomIds, d in Days, p in PatientIds)(
%  (isPatientPresent[p,d] /\ isPatientInRoom[p,r]) -> (roomGender[r, d] == patientGender(p))
%);

constraint forall(r in RoomIds, d in Days, p in PatientIds)(
  isPatientPresentInRoom[p,d,r] -> (roomGender[r, d] == patientGender(p))
);