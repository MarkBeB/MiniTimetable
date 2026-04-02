include "../IHTP-model.mzn";

% Global
% H6 Admission day: A patient can be admitted on any day from their release date to their due date.
% Given that optional patients do not have a due date, they can be admitted on any day after their release date.
%

constraint forall(id in PatientIds where isPatientMandatory(id))(
    earliestAdmissionDay(id) <= patientVar[id].admissionDay /\ patientVar[id].admissionDay <= latestAdmissionDay(id)
);

constraint forall(id in PatientIds where not isPatientMandatory(id))(
    earliestAdmissionDay(id) <= patientVar[id].admissionDay /\ patientVar[id].admissionDay <= lastDay + 1
);