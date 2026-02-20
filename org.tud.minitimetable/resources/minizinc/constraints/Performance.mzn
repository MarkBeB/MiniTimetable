include "../data/IHTC_model.mzn";

%
% Performance constraints (Reduces search time to ~14seconds (before: 6+ minutes))
%

% Admissions should only be allowed on days when at least one OT is available.
set of ExtendedDays: daysWithOTCapacity = {postponedDay} union { day | day in Days where exists(ot in operatingTheaterData)(ot.isAvailable[day])};
constraint forall(patient in patientVar)(
  patient.admissionDay in daysWithOTCapacity
);

% Patients can only be admitted on days when their assigned surgeon is available.
constraint forall(patientId in PatientIds)( let {
    set of ExtendedDays: daysWithAvailableSurgeon = {postponedDay} union { day | day in Days where surgeonData[patientData[patientId].assignedSurgeon].maxSurgeryTime[day] > 0}
  } in
  patientVar[patientId].admissionDay in daysWithAvailableSurgeon
);