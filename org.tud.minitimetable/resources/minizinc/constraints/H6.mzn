include "../data/IHTC_model.mzn";

% Global
% H6 Admission day: A patient can be admitted on any day from their release date to their due date.
% Given that optional patients do not have a due date, they can be admitted on any day after their release date.
%

/* old
constraint forall(patient in patients)(
  if patient.mandatory then
    patient.earliestPossibleAdmission <= patient.admissionDay /\ patient.admissionDay <= patient.latestPossibleAdmission
  else
    patient.earliestPossibleAdmission <= patient.admissionDay /\ patient.admissionDay <= lastDay + 1
  endif
);
*/

constraint forall(id in PatientIds)(
  if patientData[id].mandatory then
    patientData[id].earliestPossibleAdmission <= patientVar[id].admissionDay /\ patientVar[id].admissionDay <= patientData[id].latestPossibleAdmission
  else
    patientData[id].earliestPossibleAdmission <= patientVar[id].admissionDay /\ patientVar[id].admissionDay <= lastDay + 1
  endif
);