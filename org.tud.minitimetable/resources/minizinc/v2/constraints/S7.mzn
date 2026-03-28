include "../IHTP-model.mzn";

% Global
% S7 Admission delay: The number of days between a patient’s release date and their actual date of admission should be minimized.
%

var int: admissionDelay;
constraint admissionDelay >= 0;
constraint admissionDelay <= length(Days) * length(PatientIds);
constraint admissionDelay = sum(p in PatientIds)(isPatientAdmitted[p] * (patientAdmissionDay[p] - earliestAdmissionDay(p)));
constraint Penalties.patientDelay = Weights.patientDelay * admissionDelay;