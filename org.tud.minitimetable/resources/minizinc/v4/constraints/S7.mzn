include "../IHTP-Model.mzn";

% Global
% S7 Admission delay: The number of days between a patient’s release date and their actual date of admission should be minimized.
%

array[PatientIds] of var 0..maxDay: patientAdmissionDelay;

constraint forall(p in PatientIds where isPatientMandatory(p))(
  patientAdmissionDelay[p] = patientAdmissionDay[p] - earliestAdmissionDay(p)
  :: "Con_S7_A \(p)"
);

constraint forall(p in PatientIds where isPatientOptional(p))(
  patientAdmissionDelay[p] >= (patientAdmissionDay[p] - earliestAdmissionDay(p)) - (1-isPatientAdmitted[p]) * maxDay
  :: "Con_S7_B \(p)"
);

constraint penaltyPatientDelay = Weights.patientDelay  * (
  sum(p in PatientIds)(patientAdmissionDelay[p])
) :: "Con_S7_O";