include "../IHTP-Model.mzn";

% PAS
% H1 No gender mix: Patients of different genders may not share a room on any day.
%

array[RoomIds, Days, Genders] of var bool: roomGender;
%array[PatientIds, Days, RoomIds] of var bool: isPatientInRoom;

constraint forall(r in RoomIds, d in Days, g in Genders)(
  roomGender[r,d,g] * (length(PatientIds) + length(OccupantIds)) 
    >= sum(o in OccupantIds where occupantGender(o) == g)(isOccupantInRoom(o,d,r)) 
     + sum(p in PatientIds where patientGender(p) == g)(isPatientInRoom[p,d,r])
);

constraint forall(r in RoomIds, d in Days)( sum(g in Genders ) (roomGender[r,d,g]) <= 1 );