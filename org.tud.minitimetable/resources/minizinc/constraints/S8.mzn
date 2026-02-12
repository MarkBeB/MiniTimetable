include "../data/IHTC_model.mzn";

% Global
% S8 Unscheduled patients: The number of optional patients who are not admitted within the current decision horizon should be minimized.
%

constraint Penalties.unscheduledOptional == Weights.unscheduledOptional * sum(id in PatientIds where not patientData[id].mandatory)(patientVar[id].isPostponed);