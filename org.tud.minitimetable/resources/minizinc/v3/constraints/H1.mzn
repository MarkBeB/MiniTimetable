include "../IHTP-model.mzn";

% PAS
% H1 No gender mix: Patients of different genders may not share a room on any day.
%

constraint forall(r in RoomIds)(
  forall(o in OccupantIds where isOccupantInRoom(o, r))(
    forall(d in Days where d <= occupantLengthOfStay(o))(
      roomVar[r].assignedGender[d] = occupantGender(o)
    )
  )
);

constraint forall(r in RoomIds)(
  forall(p in PatientIds)(
    forall(d in Days where patientVar[p].assignedRoom[d] == r)(
      roomVar[r].assignedGender[d] == patientGender(p)
    )
  )
);