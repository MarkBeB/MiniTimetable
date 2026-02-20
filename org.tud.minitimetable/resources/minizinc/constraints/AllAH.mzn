include "../data/IHTC_model.mzn";

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%
% Additional Constraints
%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% Derived from competition rules.
% H8 Room assignment: An admitted patient needs to be assigned to a room
%
constraint forall(id in PatientIds)(
  if patientVar[id].isAdmitted then
    sum(day in Days)(patientVar[id].assignedRoom[day] != <>) >= 1
  else
    forall(day in Days)(patientVar[id].assignedRoom[day] == <>)
  endif
);

/* no longer needed
constraint forall(patient in patientVar)(
  (patient.isAdmitted -> patient.assignedRoom = patient.assignedRoomAt[patient.admissionDay])
  /\ (not patient.isAdmitted -> patient.assignedRoom = <>)
);
*/

% Derived from competition rules.
% H9 No Room Transfer: Each patient must remain in the same room for the entire duration of their stay
%
constraint forall(patient in patientVar where patient.isAdmitted)(
  exists(roomId in RoomIds)(
    forall(day in Days)(
      if patient.admissionDay <= day /\ day <= patient.admittedUntil then
        patient.assignedRoom[day] = roomId
      else
        patient.assignedRoom[day] = <>
      endif
    )
  )
);

% Derived from competition rules.
% H10 Surgery on admission day: Patients undergo surgery on the day of their admission
%

% Every admitted patient must be assigned to an OT. Patients who have not been admitted should not be assigned to an OT.
constraint forall(patient in patientVar)(
  patient.isAdmitted -> patient.assignedOT != <>
  /\ not patient.isAdmitted -> patient.assignedOT == <>
);

%  The assigned OT should be available on the day of admission.
constraint forall(patient in patientVar where patient.isAdmitted)(
  exists(otId in OperatingTheaterIds where operatingTheaterData[otId].isAvailable[patient.admissionDay])(
    patient.assignedOT = otId
  )
);