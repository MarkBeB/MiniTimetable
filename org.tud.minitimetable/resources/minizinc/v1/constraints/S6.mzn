include "../IHTP-model.mzn";

% SCP
% S6 Surgeon transfer: The number of different OTs a surgeon is as-signed to per working day should be minimized.
%

constraint forall(patientId in PatientIds where patientVar[patientId].isAdmitted)(
  let { SurgeonIds: surgeonId = patientData[patientId].assignedSurgeon } in
  forall(otId in OperatingTheaterIds, day in Days)(
    (patientVar[patientId].assignedOT == otId /\ patientVar[patientId].admissionDay == day) -> surgeonVar[surgeonId].worksInOT[day, otId]
  )
);

constraint Penalties.surgeonTransfer = Weights.surgeonTransfer * 
  sum(day in Days, surgeon in surgeonVar)( max(0, sum(otId in OperatingTheaterIds)(surgeon.worksInOT[day, otId])-1) );

/*
constraint forall(p in PatientIds where patientVar[p].isAdmitted)(
    let { 
        SurgeonIds: s = patientData[p].assignedSurgeon,
        var MaxDays: d = patientVar[p].admissionDay,
        var opt OperatingTheaterIds: ot = patientVar[p].assignedOT
    } in 
    surgeonVar[s].worksInOT[d, ot] = true
);

array[Days, SurgeonIds] of var 0..card(OperatingTheaterIds): surgeonTransfersPerDay;
constraint forall(d in Days, s in SurgeonIds)(
    surgeonTransfersPerDay[d, s] >= sum(ot in OperatingTheaterIds)(surgeonVar[s].worksInOT[d, ot]) - 1
);

constraint Penalties.surgeonTransfer = Weights.surgeonTransfer * sum(d in Days, s in SurgeonIds)(surgeonTransfersPerDay[d, s]);
*/