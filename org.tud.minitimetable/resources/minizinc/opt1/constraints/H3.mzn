include "../IHTP-model.mzn";

% SCP
% H3 Surgeon overtime: The maximum daily surgery time of a surgeon must not be exceeded.
%

constraint forall(s in SurgeonIds, d in Days)(
    surgeonSurgeryTime[s, d] = sum(p in PatientIds where patients[p].assignedSurgeon == s)(
        patients[p].surgeryDuration * bool2int(patientAdmissionDay[p] == d)
    )
);

constraint forall(s in SurgeonIds, d in Days)(
  surgeonSurgeryTime[s, d] <= surgeons[s].maxSurgeryTime[d]
);