include "../IHTP-model.mzn";

% Global
% H5 Mandatory versus optional patients: All mandatory patients must be admitted within the decision horizon, whereas optional patients may be postponed to future periods.
%

constraint forall(id in PatientIds where isPatientMandatory(id))(
  patientVar[id].isPostponed = false
);

constraint forall(id in PatientIds where not isPatientMandatory(id))(
  patientVar[id].isPostponed = (lastDay < patientVar[id].admissionDay)
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