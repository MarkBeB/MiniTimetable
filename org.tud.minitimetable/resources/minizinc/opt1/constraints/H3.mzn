include "../IHTP-model.mzn";

% SCP
% H3 Surgeon overtime: The maximum daily surgery time of a surgeon must not be exceeded.
%

constraint forall(s in SurgeonIds, d in Days)(
  surgeonSurgeryTime[s, d] = sum(p in PatientIds where hasPatientSurgeon(p,s))(
    surgeryDuration(p) * isPatientAdmittedAt[p,d]
  )
);

constraint forall(s in SurgeonIds, d in Days)(
  surgeonSurgeryTime[s, d] <= maxSurgeryTime(s,d)
);