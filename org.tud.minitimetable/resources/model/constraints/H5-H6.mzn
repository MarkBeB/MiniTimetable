include "../IHTC_model.mzn";

% Global
% H5 Mandatory versus optional patients: All mandatory patients must be admitted within the decision horizon, whereas optional pa-tients may be postponed to future periods.
%
% This constraint ensures that no patient is admitted prior to the earliest available time slot for their surgery.
% It also ensures that mandatory patients must be admitted within their surgery time window.
% Optional patients admitted after the decision horizon count as postponed.
%

constraint forall(patient in patients)(
  if patient.mandatory then
    patient.earliestPossibleAdmission <= patient.admissionDay /\ patient.admissionDay <= patient.latestPossibleAdmission
  else
    patient.earliestPossibleAdmission <= patient.admissionDay /\ patient.admissionDay <= lastDay + 1    
  endif
);

constraint forall(patient in patients)(
  if patient.mandatory then
    patient.isPostponed == false
  else
    lastDay < patient.admissionDay <-> patient.isPostponed
  endif
);

constraint forall(patient in patients)(
  patient.isPostponed <-> not patient.isAdmitted
);

% Global
% H6 Admission day: A patient can be admitted on any day from their release date to their due date.
% Given that optional patients do not have a due date, they can be admitted on any day after their release date.
%
% Solved by H5