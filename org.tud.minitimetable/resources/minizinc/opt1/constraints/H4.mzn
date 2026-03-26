include "../IHTP-model.mzn";

% SCP
% H4 OT overtime: The duration of all surgeries allocated to an OT on a day must not exceed the OT’s maximum capacity.
%

constraint forall(ot in OperatingTheaterIds, d in Days where operatingTheaters[ot].isAvailable[d])(
  otSurgeryTime[ot,d] = sum(p in PatientIds where isPatientPresent(p,d))
    (patientData[p].surgeryDuration)
);

constraint forall(ot in OperatingTheaterIds, d in Days)(
  otSurgeryTime[ot,d] <= operatingTheaters[ot].availability[d]
);