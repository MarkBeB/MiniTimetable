include "../IHTP-Model.mzn";

% SCP
% H4 OT overtime: The duration of all surgeries allocated to an OT on a day must not exceed the OT’s maximum capacity.
%

constraint forall(p in PatientIds) (
  sum(ot in OperatingTheaterIds)(isPatientInOT[p,ot]) = isPatientAdmitted[p]
  :: "Con_H4_A \(p)"
);

constraint forall(ot in OperatingTheaterIds, d in Days) (
  (sum(p in PatientIds) (
%    let {
%      var bool: active = (isPatientAdmittedAt[p,d] /\ patientInOT[p,ot])
%    } in 
    (isPatientAdmittedAt[p,d] /\ isPatientInOT[p,ot]) * surgeryDuration(p)
  ) <= otSurgeryCapacity(ot, d))
  :: "Con_H4_B \(ot) \(d)"
);