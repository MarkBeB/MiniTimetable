include "../IHTP-model.mzn";

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%
% Additional Constraints
%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% Derived from competition rules.
% AH9 Room assignment: An admitted patient needs to be assigned to a room
%
/*
constraint forall(id in PatientIds)(
  if patientVar[id].isAdmitted then
    sum(day in Days)(patientVar[id].assignedRoom[day] != <>) >= 1
  else
    forall(day in Days)(patientVar[id].assignedRoom[day] == <>)
  endif
) :: constraint_name("Con_AH9");
*/

constraint forall(id in PatientIds)(
     patientVar[id].isAdmitted -> sum(day in Days)(patientVar[id].assignedRoom[day] != <>) >= 1
  /\ not patientVar[id].isAdmitted -> forall(day in Days)(patientVar[id].assignedRoom[day] == <>)
) :: constraint_name("Con_AH9_A");

/*
constraint forall(id in PatientIds)(
  not patientVar[id].isPostponed -> sum(day in Days)(patientVar[id].assignedRoom[day] != <>) >= 1
) :: constraint_name("Con_AH9_A");

constraint forall(id in PatientIds)(
  patientVar[id].isPostponed -> forall(day in Days)(patientVar[id].assignedRoom[day] == <>)
) :: constraint_name("Con_AH9_B");
*/

/* no longer needed
constraint forall(patient in patientVar)(
  (patient.isAdmitted -> patient.assignedRoom = patient.assignedRoomAt[patient.admissionDay])
  /\ (not patient.isAdmitted -> patient.assignedRoom = <>)
);
*/

% Derived from competition rules.
% AH10 No Room Transfer: Each patient must remain in the same room for the entire duration of their stay
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
) :: constraint_name("Con_AH10");

% Derived from competition rules.
% AH11 Surgery on admission day: Patients undergo surgery on the day of their admission
%

% Every admitted patient must be assigned to an OT. Patients who have not been admitted should not be assigned to an OT.
constraint forall(patient in patientVar)(
  patient.isAdmitted -> patient.assignedOT != <>
  /\ not patient.isAdmitted -> patient.assignedOT == <>
) :: constraint_name("Con_AH11_A");

%  The assigned OT should be available on the day of admission.
constraint forall(patient in patientVar where patient.isAdmitted)(
  exists(ot in operatingTheaterData where ot.isAvailable[patient.admissionDay])(
    patient.assignedOT = ot.id
  )
) :: constraint_name("Con_AH11_B");
