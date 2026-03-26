include "../IHTP-model.mzn";

% PAS
% H1 No gender mix: Patients of different genders may not share a room on any day.
%

constraint forall(r in RoomIds, d in Days, o in OccupantIds where isOccupantInRoom(o,d,r))(
  % fix room gender to occupant gender
  roomGender[r, d] = occupants[o].gender
);

constraint forall(r in RoomIds, d in Days, p in PatientIds where isPatientInRoom[p,d,r])(
  % only patients with the same gender should share a room
  roomGender[r, d] == patients[p].gender
);