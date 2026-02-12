include "../data/IHTC_model.mzn";

% PAS
% H1 No gender mix: Patients of different genders may not share a room on any day.
%

constraint forall(roomId in RoomIds)(
  forall(occupantId in OccupantIds where occupantData[occupantId].assignedRoom == roomId)(
    forall(day in Days where day <= occupantData[occupantId].lengthOfStay)(
      roomVar[roomId].assignedGender[day] = occupantData[occupantId].gender
    )
  )
);

constraint forall(roomId in RoomIds)(
  forall(patientId in PatientIds)(
    forall(day in Days where patientVar[patientId].assignedRoomAt[day] == roomId)(
      roomVar[roomId].assignedGender[day] = patientData[patientId].gender
    )
  )
);