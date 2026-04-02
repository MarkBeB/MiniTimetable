include "../IHTP-model.mzn";

% SCP
% H4 OT overtime: The duration of all surgeries allocated to an OT on a day must not exceed the OT’s maximum capacity.
%

constraint forall(ot in OperatingTheaterIds, d in Days where isOTAvailable(ot, d))(
  operatingTheaterVar[ot].usedTime[d] = sum(p in PatientIds where patientVar[p].assignedOT == ot /\ patientVar[p].admissionDay == d)
    (surgeryDuration(p))
);

constraint forall(ot in OperatingTheaterIds, d in Days)(
  operatingTheaterVar[ot].usedTime[d] <= otSurgeryCapacity(ot, d)
);