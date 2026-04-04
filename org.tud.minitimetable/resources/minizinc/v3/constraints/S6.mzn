include "../IHTP-Model.mzn";

% SCP
% S6 Surgeon transfer: The number of different OTs a surgeon is as-signed to per working day should be minimized.
%

array[SurgeonIds,Days, OperatingTheaterIds] of var bool: isSurgeonInOT;

constraint forall(p in PatientIds where isPatientAdmitted[p])(
  let { SurgeonIds: s = assignedSurgeon(p) } in
  forall(ot in OperatingTheaterIds, d in Days)(
    (patientOT[p] == ot /\ patientAdmissionDay[p] == d) -> isSurgeonInOT[s,d,ot]
  )
);

constraint Penalties.surgeonTransfer = Weights.surgeonTransfer * 
  sum(d in Days, s in SurgeonIds)( max(0, sum(ot in OperatingTheaterIds)(isSurgeonInOT[s,d,ot])-1) );