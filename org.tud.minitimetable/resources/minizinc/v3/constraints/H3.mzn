include "../IHTP-model.mzn";

% SCP
% H3 Surgeon overtime: The maximum daily surgery time of a surgeon must not be exceeded.
%

constraint forall(s in SurgeonIds, d in Days)(
  surgeonTime[s,d] = sum(p in PatientIds where patientOfSurgeon(p, s))(
    if patientVar[p].admissionDay == d then surgeryDuration(p) else 0 endif
  )    
);

constraint forall(s in SurgeonIds, d in Days)(
  surgeonTime[s,d] <= surgeonSurgeryCapacity(s,d)
);