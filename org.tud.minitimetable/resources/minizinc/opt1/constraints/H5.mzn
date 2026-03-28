include "../IHTP-model.mzn";

% Global
% H5 Mandatory versus optional patients: All mandatory patients must be admitted within the decision horizon, whereas optional patients may be postponed to future periods.
%

constraint forall(p in PatientIds where isPatientMandatory(p))(
  isPatientPostponed[p] = false
);

constraint forall(p in PatientIds where isPatientOptional(p))(
  isPatientPostponed[p] = isPatientAdmittedAt[p,postponedDay]
);

/*
% Mandatory patients are always admitted within Days, while optional patients may be admitted within Days+1
constraint forall(p in PatientIds)(
  isPatientPostponed[p] = sum(d in Days)(isPatientAdmittedAt[p,d])
);
*/