include "../data/IHTC_model.mzn";

% Global
% H5 Mandatory versus optional patients: All mandatory patients must be admitted within the decision horizon, whereas optional patients may be postponed to future periods.
%

/* old
constraint forall(patient in patients)(
  if patient.mandatory then
    patient.isPostponed == false
  else
    patient.admissionDay == lastDay + 1 <-> patient.isPostponed == true
  endif  
);
*/

constraint forall(id in PatientIds)(
  if patientData[id].mandatory then
    patientVar[id].isPostponed = false
  else
    % patientVar[id].admissionDay <= lastDay <-> patientVar[id].isPostponed = false
    patientVar[id].isPostponed = (lastDay < patientVar[id].admissionDay)
  endif
);