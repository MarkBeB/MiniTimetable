include "../IHTP-Model.mzn";

% Global
% H6 Admission day: A patient can be admitted on any day from their release date to their due date.
% Given that optional patients do not have a due date, they can be admitted on any day after their release date.
%

constraint forall(p in PatientIds where isPatientMandatory(p))(
  (earliestAdmissionDay(p) <= patientAdmissionDay[p] /\ patientAdmissionDay[p] <= latestAdmissionDay(p))
  :: "Con_H6_A \(p)"
);

constraint forall(p in PatientIds where isPatientOptional(p))(
  (earliestAdmissionDay(p) <= patientAdmissionDay[p] /\ patientAdmissionDay[p] <= extendedDay)
  :: "Con_H6_B \(p)"
);

constraint forall(p in PatientIds)(
  patientAdmissionDay[p] in validAdmissionDays[p]
  :: "Con_P_A \(p)"
);
