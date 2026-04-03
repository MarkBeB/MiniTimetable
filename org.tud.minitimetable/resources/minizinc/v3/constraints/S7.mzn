include "../IHTP-model.mzn";

% Global
% S7 Admission delay: The number of days between a patient’s release date and their actual date of admission should be minimized.
%

array[PatientIds] of var int: patientAdmissionDelay;

constraint forall(p in PatientIds)(
  patientAdmissionDelay[p] = patientAdmissionDay[p] - earliestAdmissionDay(p)
);

constraint Penalties.patientDelay = Weights.patientDelay * sum(p in PatientIds where isPatientAdmitted[p])(patientAdmissionDelay[p]);