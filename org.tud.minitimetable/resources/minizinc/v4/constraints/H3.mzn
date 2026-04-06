include "../IHTP-Model.mzn";

% SCP
% H3 Surgeon overtime: The maximum daily surgery time of a surgeon must not be exceeded.
%

constraint forall(s in SurgeonIds, d in Days) (
  (sum(p in PatientIds where isPatientOfSurgeon(p,s)) (
    isPatientAdmittedAt[p,d] * surgeryDuration(p)
  ) <= surgeonSurgeryCapacity(s, d))
  :: "Con_H3_A \(s) \(d)"
);