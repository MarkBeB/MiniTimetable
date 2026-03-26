include "../IHTP-model.mzn";

% Global
% H6 Admission day: A patient can be admitted on any day from their release date to their due date.
% Given that optional patients do not have a due date, they can be admitted on any day after their release date.
%

constraint forall(p in PatientIds)(
  sum(d in ExtendedDays)(isPatientAdmittedAt[p,d]) == 1
);

constraint forall(p in PatientIds)(
  patientAdmissionDay[p] = sum(d in ExtendedDays)(d * isPatientAdmittedAt[p, d])
);

constraint forall(p in PatientIds where isPatientMandatory(p))(
    patients[p].earliestPossibleAdmission <= patientAdmissionDay[p] /\ patientAdmissionDay[p] <= patients[p].latestPossibleAdmission
);

constraint forall(p in PatientIds where isPatientOptional(p))(
    patients[p].earliestPossibleAdmission <= patientAdmissionDay[p] /\ patientAdmissionDay[p] <= lastDay + 1
);

