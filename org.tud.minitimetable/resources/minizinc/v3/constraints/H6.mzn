include "../IHTP-model.mzn";

% Global
% H6 Admission day: A patient can be admitted on any day from their release date to their due date.
% Given that optional patients do not have a due date, they can be admitted on any day after their release date.
%

constraint forall(p in PatientIds where isPatientMandatory(p))(
    earliestAdmissionDay(p) <= patientVar[p].admissionDay /\ patientVar[p].admissionDay <= latestAdmissionDay(p)
);

constraint forall(p in PatientIds where not isPatientMandatory(p))(
    earliestAdmissionDay(p) <= patientVar[p].admissionDay /\ patientVar[p].admissionDay <= lastDay + 1
);