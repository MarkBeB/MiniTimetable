include "../IHTP-model.mzn";

% SCP
% H4 OT overtime: The duration of all surgeries allocated to an OT on a day must not exceed the OT’s maximum capacity.
%

constraint forall(ot in OperatingTheaterIds, d in Days where isOTAvailable(ot,d))(
  otSurgeryTime[ot,d] = sum(p in PatientIds)(isPatientInOT[p,ot,d] * surgeryDuration(p))
);

constraint forall(ot in OperatingTheaterIds, d in Days)(
  otSurgeryTime[ot,d] <= getOTAvailability(ot,d)
);