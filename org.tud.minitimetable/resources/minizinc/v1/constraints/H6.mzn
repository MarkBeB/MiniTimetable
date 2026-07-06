include "../IHTP-model.mzn";

% Global
% H6 Admission day: A patient can be admitted on any day from their release date to their due date.
% Given that optional patients do not have a due date, they can be admitted on any day after their release date.
%

constraint forall(id in PatientIds where patients[id].mandatory)(
    patients[id].earliestPossibleAdmission <= patientVar[id].admissionDay /\ patientVar[id].admissionDay <= patients[id].latestPossibleAdmission
);

constraint forall(id in PatientIds where not patients[id].mandatory)(
    patients[id].earliestPossibleAdmission <= patientVar[id].admissionDay /\ patientVar[id].admissionDay <= lastDay + 1
);