include "../IHTP-Model.mzn";

% Global
% H5 Mandatory versus optional patients: All mandatory patients must be admitted within the decision horizon, whereas optional patients may be postponed to future periods.
%

constraint forall(p in PatientIds where isPatientMandatory(p)) (
  isPatientAdmitted[p] = true
  :: "Con_H5_B \(p)"
);

constraint forall(p in PatientIds where isPatientOptional(p)) (
  isPatientAdmitted[p] <-> (patientAdmissionDay[p] <= lastDay)
  :: "Con_H5_B \(p)"
);