include "../IHTP-Model.mzn";

% Global
% S8 Unscheduled patients: The number of optional patients who are not admitted within the current decision horizon should be minimized.
%

var 0..length(PatientIds): numberOfUnscheduledPatients = sum(p in PatientIds where isPatientOptional(p))(bool2int(isPatientPostponed[p]))
 :: "Con_S8_A";

constraint (
  Penalties.unscheduledOptional = Weights.unscheduledOptional * numberOfUnscheduledPatients
) :: "Con_S8_O";