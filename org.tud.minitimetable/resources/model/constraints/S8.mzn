include "../IHTC_model.mzn";

% Global
% S8 Unscheduled patients: The number of optional patients who are not admitted within the current decision horizon should be minimized.
%

constraint Penalties.unscheduledOptional == Weights.unscheduledOptional * sum(patient in patients where not patient.mandatory /\ patient.isPostponed)(1);