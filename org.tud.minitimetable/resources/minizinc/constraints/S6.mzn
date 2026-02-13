include "../data/IHTC_model.mzn";

% SCP
% S6 Surgeon transfer: The number of different OTs a surgeon is as-signed to per working day should be minimized.
%

constraint forall(patientId in PatientIds where patientVar[patientId].isAdmitted)(
  forall(otId in OperatingTheaterIds, day in Days)(
    (patientVar[patientId].assignedOT == otId /\ patientVar[patientId].admissionDay == day)
      -> surgeonVar[patientData[patientId].assignedSurgeon].worksInOT[day, otId]
  )
);

constraint Penalties.surgeonTransfer = Weights.surgeonTransfer * 
  sum(day in Days, surgeon in surgeonVar)( max(0, sum(otId in OperatingTheaterIds)(surgeon.worksInOT[day, otId])-1) );