include "../IHTP-model.mzn";

% Global
% S8 Unscheduled patients: The number of optional patients who are not admitted within the current decision horizon should be minimized.
%

var 0..numberOfPatients: numberOfUnscheduledPatients = sum(p in PatientIds where not patients[p].mandatory)(isPatientPostponed(p));

constraint Penalties.unscheduledOptional = Weights.unscheduledOptional * numberOfUnscheduledPatients;