include "../IHTP-Model.mzn";

% Global
% S8 Unscheduled patients: The number of optional patients who are not admitted within the current decision horizon should be minimized.
%

constraint penaltyUnscheduledOptional = Weights.unscheduledOptional * (
  sum(p in PatientIds where isPatientOptional(p))(1 - bool2int(isPatientAdmitted[p]))
) :: "Con_S8_O";