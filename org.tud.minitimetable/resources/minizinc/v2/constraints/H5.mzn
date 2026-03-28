include "../IHTP-model.mzn";

% Global
% H5 Mandatory versus optional patients: All mandatory patients must be admitted within the decision horizon, whereas optional patients may be postponed to future periods.
%

% fix isAdmitted for mandatory patients to true, optional patients still can be true/false
constraint forall(p in PatientIds where isPatientMandatory(p))(
  isPatientAdmitted[p] = true
);

