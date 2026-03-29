%include "./../IHTP-model.mzn";

% Global
% S8 Unscheduled patients: The number of optional patients who are not admitted within the current decision horizon should be minimized.
%

var 0..numberOfPatients: numberOfUnscheduledPatients = sum(patient in patients where not patient.mandatory)(bool2int(patientVar[patient.id].isPostponed));
constraint Penalties.unscheduledOptional = Weights.unscheduledOptional * numberOfUnscheduledPatients;