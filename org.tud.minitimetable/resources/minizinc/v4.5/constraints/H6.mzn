include "../IHTP-Model.mzn";

% Global
% H6 Admission day: A patient can be admitted on any day from their release date to their due date.
% Given that optional patients do not have a due date, they can be admitted on any day after their release date.
%

constraint forall(p in PatientIds where isPatientMandatory(p))(
     forall(d in Days where d < earliestAdmissionDay(p))( isPatientAdmittedAt[p,d] = 0 )
  /\ forall(d in Days where d > latestAdmissionDay(p))( isPatientAdmittedAt[p,d] = 0 )
  :: "Con_H6_A \(p)"
);

constraint forall(p in PatientIds where isPatientOptional(p))(
  forall(d in Days where d < earliestAdmissionDay(p))( isPatientAdmittedAt[p,d] = 0 )
  :: "Con_H6_B \(p)"
);

constraint forall(p in PatientIds)(
  forall(d in Days diff validAdmissionDays(p))( isPatientAdmittedAt[p,d] = 0 )
  :: "Con_P_A \(p)"
);
