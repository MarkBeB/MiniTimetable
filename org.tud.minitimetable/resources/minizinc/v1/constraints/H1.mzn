include "../IHTP-Model.mzn";

% PAS
% H1 No gender mix: Patients of different genders may not share a room on any day.
%

constraint forall(roomId in RoomIds)(
  forall(occupantId in OccupantIds where occupants[occupantId].assignedRoom == roomId)(
    forall(day in Days where day <= occupants[occupantId].lengthOfStay)(
      roomVar[roomId].assignedGender[day] = occupants[occupantId].gender
    )
  )
);

constraint forall(roomId in RoomIds)(
  forall(patientId in PatientIds)(
    forall(day in Days where patientVar[patientId].assignedRoom[day] == roomId)(
      roomVar[roomId].assignedGender[day] == patients[patientId].gender
    )
  )
);