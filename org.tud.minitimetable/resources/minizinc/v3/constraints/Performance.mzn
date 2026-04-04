include "../IHTP-Model.mzn";

%
% Performance constraints (Reduces search time to ~14seconds (before: 6+ minutes))
%

% Admissions should only be allowed on days when at least one OT is available.
set of ExtendedDays: daysWithOTCapacity = {extendedDay} union { d | d in Days where exists(ot in OperatingTheaterIds)(isOTAvailable(ot, d))};
constraint redundant_constraint(forall(p in PatientIds)(
  patientAdmissionDay[p] in daysWithOTCapacity 
));

% Patients can only be admitted on days when their assigned surgeon is available.
constraint redundant_constraint(forall(p in PatientIds)( 
  let {
    SurgeonIds: s = assignedSurgeon(p);
    set of ExtendedDays: daysWithAvailableSurgeon = {extendedDay} union { d | d in Days where isSurgeonAvailable(s, d)};
  } in
  patientAdmissionDay[p] in daysWithAvailableSurgeon
));
