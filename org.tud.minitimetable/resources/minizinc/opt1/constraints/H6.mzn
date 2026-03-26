include "../IHTP-model.mzn";

% Global
% H6 Admission day: A patient can be admitted on any day from their release date to their due date.
% Given that optional patients do not have a due date, they can be admitted on any day after their release date.
%

constraint forall(p in PatientIds where patients[p].mandatory)(
    patients[p].earliestPossibleAdmission <= patientAdmissionDay[p] /\ patientAdmissionDay[p] <= patients[p].latestPossibleAdmission
);

constraint forall(p in PatientIds where not patientData[p].mandatory)(
    patients[p].earliestPossibleAdmission <= patientAdmissionDay[p] /\ patientAdmissionDay[p] <= lastDay + 1
);