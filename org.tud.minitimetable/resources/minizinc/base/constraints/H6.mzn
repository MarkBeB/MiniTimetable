include "../IHTP-model.mzn";

% Global
% H6 Admission day: A patient can be admitted on any day from their release date to their due date.
% Given that optional patients do not have a due date, they can be admitted on any day after their release date.
%

constraint forall(id in PatientIds where patientData[id].mandatory)(
    patientData[id].earliestPossibleAdmission <= patientVar[id].admissionDay /\ patientVar[id].admissionDay <= patientData[id].latestPossibleAdmission
);

constraint forall(id in PatientIds where not patientData[id].mandatory)(
    patientData[id].earliestPossibleAdmission <= patientVar[id].admissionDay /\ patientVar[id].admissionDay <= lastDay + 1
);