include "../IHTP-Model.mzn";

% SCP
% S5 Open OTs: The number of occupied OTs on each day should be minimized. Note that if an OT has no patients assigned for a particular day, it should not open on that day.
%

array[OperatingTheaterIds, Days] of var bool: isOTOpen;

constraint forall(p in PatientIds, ot in OperatingTheaterIds, d in Days) (
  isOTOpen[ot, d] >= (isPatientAdmittedAt[p,d] /\ isPatientInOT[p,ot])
  :: "Con_S5_A \(p) \(ot) \(d)" 
);

constraint penaltyOpenOperatingTheater = Weights.openOperatingTheater * (
  sum(ot in OperatingTheaterIds, d in Days)(isOTOpen[ot,d]) 
) :: "Con_S5_O";