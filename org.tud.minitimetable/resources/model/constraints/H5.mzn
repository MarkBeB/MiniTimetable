include "../IHTC_model.mzn";

% Global
% H5 Mandatory versus optional patients: All mandatory patients must be admitted within the decision horizon, whereas optional pa-tients may be postponed to future periods.
%

constraint forall(patient in patients)(
  if patient.mandatory then
    patient.isPostponed == false
  else
    patient.admissionDay == lastDay + 1 <-> patient.isPostponed == true
  endif  
);