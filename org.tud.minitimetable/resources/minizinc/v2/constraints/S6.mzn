include "../IHTP-model.mzn";

% SCP
% S6 Surgeon transfer: The number of different OTs a surgeon is as-signed to per working day should be minimized.
%

array[SurgeonIds, Days, OperatingTheaterIds] of var bool: surgeonUsesOT;
array[SurgeonIds, Days] of var 0..card(OperatingTheaterIds): surgeonTransfersPerDay;

constraint forall(p in PatientIds)(
    let { 
        SurgeonIds: s = patientData[p].assignedSurgeon,
        var MaxDays: d = patientAdmissionDay[p],
        var opt OperatingTheaterIds: ot = patientOTAssignment[p]
    } in 
    isPatientAdmitted[p] -> surgeonUsesOT[s, d, ot] = true
);

constraint forall(d in Days, s in SurgeonIds)(
    surgeonTransfersPerDay[s, d] >= sum(ot in OperatingTheaterIds)(surgeonUsesOT[s, d, ot]) - 1
);

constraint Penalties.surgeonTransfer = Weights.surgeonTransfer * 
    sum(d in Days, s in SurgeonIds)(surgeonTransfersPerDay[s, d]);