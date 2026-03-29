include "../IHTP-model.mzn";

% SCP
% H4 OT overtime: The duration of all surgeries allocated to an OT on a day must not exceed the OT’s maximum capacity.
%

constraint forall(otId in OperatingTheaterIds, day in Days where isOTAvailable(otId, day))(
  operatingTheaterVar[otId].usedTime[day] = sum(patientId in PatientIds where patientVar[patientId].assignedOT == otId /\ patientVar[patientId].admissionDay == day)
    (patientData[patientId].surgeryDuration)
);

constraint forall(otId in OperatingTheaterIds, day in Days)(
  operatingTheaterVar[otId].usedTime[day] <= operatingTheaterData[otId].availability[day]
);