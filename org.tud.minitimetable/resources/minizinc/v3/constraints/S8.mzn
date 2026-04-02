include "./../IHTP-model.mzn";

% Global
% S8 Unscheduled patients: The number of optional patients who are not admitted within the current decision horizon should be minimized.
%

var 0..numberOfPatients: numberOfUnscheduledPatients = sum(p in patients where isPatientOptional(p.id))(bool2int(patientVar[p.id].isPostponed));
constraint Penalties.unscheduledOptional = Weights.unscheduledOptional * numberOfUnscheduledPatients;