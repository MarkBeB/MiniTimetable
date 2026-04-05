include "../IHTP-Model.mzn";

% SCP
% H3 Surgeon overtime: The maximum daily surgery time of a surgeon must not be exceeded.
%

array[SurgeonIds, Days] of var DayTime: surgeonTime;

constraint forall(s in SurgeonIds, d in Days)(
  surgeonTime[s,d] = sum(p in PatientIds where isPatientOfSurgeon(p, s))(
    if patientAdmissionDay[p] == d then surgeryDuration(p) else 0 endif
  )    
);

constraint forall(s in SurgeonIds, d in Days)(
  surgeonTime[s,d] <= surgeonSurgeryCapacity(s,d)
);