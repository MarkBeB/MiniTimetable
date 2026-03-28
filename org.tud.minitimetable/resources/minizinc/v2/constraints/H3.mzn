include "../IHTP-model.mzn";

% SCP
% H3 Surgeon overtime: The maximum daily surgery time of a surgeon must not be exceeded.
%

array[SurgeonIds, Days] of var 0..1000: surgeonSurgeryTime;

constraint forall(s in SurgeonIds, d in Days)(
  surgeonSurgeryTime[s,d] = sum(p in PatientIds where patientOfSurgeon(p,s) /\ (patientAdmissionDay[p] == d))(
    surgeryDuration(p)
  )
);

/*
constraint forall(s in SurgeonIds, d in Days)(
  surgeonSurgeryTime[s,d] = sum(p in PatientIds where patientOfSurgeon(p,s))(
    surgeryDuration(p) * (patientAdmissionDay[p] == d)
  )
);
*/

/*
constraint forall(s in SurgeonIds, d in Days)(
  surgeonSurgeryTime[s,d] = sum(p in PatientIds where patientOfSurgeon(p,s))(
    surgeryDuration(p) * isPatientAdmittedAt[p,d]
  )
);
*/

constraint forall(s in SurgeonIds, d in Days)(
  surgeonSurgeryTime[s,d] <= surgeonSurgeryCapacity(s,d)
);