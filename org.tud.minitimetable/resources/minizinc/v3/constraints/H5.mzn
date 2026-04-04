include "../IHTP-Model.mzn";

% Global
% H5 Mandatory versus optional patients: All mandatory patients must be admitted within the decision horizon, whereas optional patients may be postponed to future periods.
%

constraint forall(p in PatientIds where isPatientMandatory(p))(
  isPatientPostponed[p] = false
);

constraint forall(p in PatientIds where not isPatientMandatory(p))(
  isPatientPostponed[p] = (lastDay < patientAdmissionDay[p])
);

/*
constraint forall(id in PatientIds)(
  if patientData[id].mandatory then
    patientVar[id].isPostponed = false
  else
    % patientVar[id].admissionDay <= lastDay <-> patientVar[id].isPostponed = false
    patientVar[id].isPostponed = (lastDay < patientVar[id].admissionDay)
  endif
);
*/