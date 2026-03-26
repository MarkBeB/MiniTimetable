include "../IHTP-model.mzn";

% Global
% H5 Mandatory versus optional patients: All mandatory patients must be admitted within the decision horizon, whereas optional patients may be postponed to future periods.
%

constraint forall(p in PatientIds where patients[p].mandatory)(
  isPatientPostponed[p] = false
);

constraint forall(p in PatientIds where not patients[p].mandatory)(
  isPatientPostponed[p] = (lastDay < patientAdmissionDay[p])
);