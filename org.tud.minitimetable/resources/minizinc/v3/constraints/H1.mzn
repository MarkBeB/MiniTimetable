include "../IHTP-Model.mzn";

% PAS
% H1 No gender mix: Patients of different genders may not share a room on any day.
%

array[RoomIds, Days] of var Genders: roomGender;

constraint forall(r in RoomIds, o in OccupantIds, d in Days where isOccupantInRoom(o, d, r))(
  roomGender[r,d] = occupantGender(o) :: "CON_H1_A \(r) \(d) \(o)"
);

constraint forall(r in RoomIds)(
  forall(p in PatientIds)(
    forall(d in Days where patientRoom[p,d] == r)(
      roomGender[r,d] == patientGender(p) :: "CON_H1_B \(r) \(d) \(p)"
    )
  )
);