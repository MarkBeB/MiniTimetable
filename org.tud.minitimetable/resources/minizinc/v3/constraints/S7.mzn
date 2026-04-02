include "../IHTP-model.mzn";

% Global
% S7 Admission delay: The number of days between a patient’s release date and their actual date of admission should be minimized.
%

constraint forall(p in PatientIds)(
  patientVar[p].penaltyAdmissionDelay = patientVar[p].admissionDay - earliestAdmissionDay(p)
);

constraint Penalties.patientDelay = Weights.patientDelay * sum(p in patientVar where p.isAdmitted)(p.penaltyAdmissionDelay);