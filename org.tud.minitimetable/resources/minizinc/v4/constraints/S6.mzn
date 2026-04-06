include "../IHTP-Model.mzn";

% SCP
% S6 Surgeon transfer: The number of different OTs a surgeon is as-signed to per working day should be minimized.
%

array[SurgeonIds, Days, OperatingTheaterIds] of var bool: isSurgeonInOT;
array[SurgeonIds, Days] of var 0..length(OperatingTheaterIds): surgeonTransfers;

constraint forall(p in PatientIds, ot in OperatingTheaterIds, d in Days) (
  let { SurgeonIds: s = assignedSurgeon(p) } in
  isSurgeonInOT[s,d,ot] >= (isPatientAdmittedAt[p,d] /\ isPatientInOT[p,ot])
  :: "Con_S6_A \(p) \(ot) \(d) \(s)"
);

constraint forall(s in SurgeonIds, d in Days) (
  surgeonTransfers[s,d] >= sum(ot in OperatingTheaterIds)(isSurgeonInOT[s,d,ot]) - 1
  :: "Con_S6_B \(s) \(d)"
);

constraint penaltySurgeonTransfer = Weights.surgeonTransfer * (
   sum(d in Days, s in SurgeonIds)( surgeonTransfers[s,d] )
) :: "Con_S6_O";