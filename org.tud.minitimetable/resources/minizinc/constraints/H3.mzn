include "../data/IHTC_model.mzn";

% SCP
% H3 Surgeon overtime: The maximum daily surgery time of a surgeon must not be exceeded.
%

constraint forall(surgeonId in SurgeonIds, day in Days)(
  surgeonVar[surgeonId].usedTime[day] = sum(patientId in PatientIds where patientData[patientId].assignedSurgeon == surgeonId)(
    if patientVar[patientId].admissionDay == day then patientData[patientId].surgeryDuration else 0 endif
  )    
);

constraint forall(surgeonId in SurgeonIds, day in Days)(
  surgeonVar[surgeonId].usedTime[day] <= surgeonData[surgeonId].maxSurgeryTime[day]
);