include "../IHTP-Model.mzn";

% SCP
% H4 OT overtime: The duration of all surgeries allocated to an OT on a day must not exceed the OT’s maximum capacity.
%

constraint forall(ot in OperatingTheaterIds, d in Days) (
  (sum(p in PatientIds) (
    (isPatientAdmittedAt[p,d] /\ isPatientInOT[p,ot]) * surgeryDuration(p)
  ) <= otSurgeryCapacity(ot,d))
  :: "Con_H4_A \(ot) \(d)"
);