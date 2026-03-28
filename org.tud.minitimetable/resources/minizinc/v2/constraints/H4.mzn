include "../IHTP-model.mzn";

% SCP
% H4 OT overtime: The duration of all surgeries allocated to an OT on a day must not exceed the OT’s maximum capacity.
%

array[OperatingTheaterIds, Days] of var 0..1000: otSurgeryTime;

constraint forall(ot in OperatingTheaterIds, d in Days)(
 otSurgeryTime[ot,d] = sum(p in PatientIds where isPatientAdmittedAt[p,d] /\ isPatientInOT[p,ot])(
      surgeryDuration(p)
    )
);

constraint forall(ot in OperatingTheaterIds, d in Days)(
  otSurgeryTime[ot,d] <= otSurgeryCapacity(ot,d)
);