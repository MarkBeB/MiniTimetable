include "../IHTC_model.mzn";

% Global
% S7 Admission delay: The number of days between a patient’s release date and their actual date of admission should be minimized.
%

constraint forall(patient in patients)(
  patient.admissionDelay == patient.admissionDay - patient.earliestPossibleAdmission
);

constraint Penalties.patientDelay == Weights.patientDelay * sum(patient in patients)(patient.admissionDelay);