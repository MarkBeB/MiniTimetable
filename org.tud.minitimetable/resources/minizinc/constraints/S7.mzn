include "../data/IHTC_model.mzn";

% Global
% S7 Admission delay: The number of days between a patient’s release date and their actual date of admission should be minimized.
%
/* old
constraint forall(patient in patients)(
  patient.penaltyAdmissionDelay == patient.admissionDay - patient.earliestPossibleAdmission
);
*/

constraint forall(id in PatientIds)(
  patientVar[id].penaltyAdmissionDelay = patientVar[id].admissionDay - patientData[id].earliestPossibleAdmission
);

constraint Penalties.patientDelay = Weights.patientDelay * sum(patient in patientVar where patient.isAdmitted)(patient.penaltyAdmissionDelay);