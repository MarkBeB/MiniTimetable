include "../IHTP-model.mzn";

% PAS
% H1 No gender mix: Patients of different genders may not share a room on any day.
%

constraint forall(r in RoomIds, d in Days, o in OccupantIds where isOccupantInRoom(o, d, r))(
  roomVar[r].assignedGender[d] = occupantData[o].gender
);

constraint forall(r in RoomIds, d in Days, p in PatientIds where isPatientInRoom(p, d, r))(
  roomVar[r].assignedGender[d] == patientData[p].gender
);