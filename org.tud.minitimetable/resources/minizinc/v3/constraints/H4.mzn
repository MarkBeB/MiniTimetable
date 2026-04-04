include "../IHTP-Model.mzn";

% SCP
% H4 OT overtime: The duration of all surgeries allocated to an OT on a day must not exceed the OT’s maximum capacity.
%

constraint forall(ot in OperatingTheaterIds, d in Days where isOTAvailable(ot, d))(
  otTime[ot,d] = sum(p in PatientIds where patientOT[p] == ot /\ patientAdmissionDay[p] == d)
    (surgeryDuration(p))
);

constraint forall(ot in OperatingTheaterIds, d in Days)(
  otTime[ot,d] <= otSurgeryCapacity(ot, d)
);