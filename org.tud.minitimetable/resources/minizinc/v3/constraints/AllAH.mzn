include "../IHTP-Model.mzn";

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

constraint forall(p in PatientIds)(
     isPatientAdmitted[p] -> sum(d in Days)(patientRoom[p,d] != <>) >= 1
  /\ isPatientPostponed[p] -> forall(d in Days)(patientRoom[p,d] == <>)
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

constraint forall(p in PatientIds where isPatientAdmitted[p])(
  exists(r in RoomIds)(
    forall(d in Days)(
      if patientAdmissionDay[p] <= d /\ d <= patientAdmittedUntil[p] then
        patientRoom[p,d] = r
      else
        patientRoom[p,d] = <>
      endif
    )
  )
);

% Derived from competition rules.
% AH11 Surgery on admission day: Patients undergo surgery on the day of their admission
%

% Every admitted patient must be assigned to an OT. Patients who have not been admitted should not be assigned to an OT.
constraint forall(p in PatientIds)(
     isPatientAdmitted[p] -> patientOT[p] != <>
  /\ isPatientPostponed[p] -> patientOT[p] == <>
);

%  The assigned OT should be available on the day of admission.
constraint forall(p in PatientIds where isPatientAdmitted[p])(
  exists(ot in operatingTheaterData where ot.availability[patientAdmissionDay[p]] > 0)(
    patientOT[p] = ot.id
  )
);
