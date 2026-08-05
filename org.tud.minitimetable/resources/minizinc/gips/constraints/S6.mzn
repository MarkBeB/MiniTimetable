include "../IHTP-Model.mzn";

% SCP
% S6 Surgeon transfer: The number of different OTs a surgeon is as-signed to per working day should be minimized.
%

array[SurgeonIds, Days, OperatingTheaterIds] of var bool: isSurgeonInOT;
array[SurgeonIds, Days] of var 0..length(OperatingTheaterIds): surgeonTransfers;

constraint forall(sr in SurgeonIds, d in Days, ot in OperatingTheaterIds)(
  isSurgeonInOT[sr,d,ot] = exists(p in PatientIds where isPatientOfSurgeon(p,sr))(isPatientAdmittedAt[p,d] /\ isPatientInOT[p,ot])
  :: "Con_S6_A \(sr) \(d) \(ot)"
);

constraint forall(s in SurgeonIds, d in Days) (
  surgeonTransfers[s,d] = max(sum(ot in OperatingTheaterIds)(isSurgeonInOT[s,d,ot]) - 1, 0)
  :: "Con_S6_B \(s) \(d)"
);

constraint violationsSurgeonTransfer = sum(d in Days, s in SurgeonIds)( surgeonTransfers[s,d] ) :: "Con_S6_O";