include "../IHTC_model.mzn";

% PAS
% H2 Compatible rooms: Patients can only be assigned to one of their compatible rooms.
%

constraint forall(patient in patients)(
  forall(incompatibleRoom in patient.incompatibleRooms)(
    patient.assignedToRoom != incompatibleRoom 
  )
);