include "../IHTP-Model.mzn";

% SCP
% S5 Open OTs: The number of occupied OTs on each day should be minimized. Note that if an OT has no patients assigned for a particular day, it should not open on that day.
%

array[OperatingTheaterIds, Days] of var bool: isOTOpen;

constraint forall(ot in OperatingTheaterIds, d in Days) (
  isOTOpen[ot, d] = exists(p in PatientIds)(isPatientAdmittedAt[p,d] /\ isPatientInOT[p,ot])
  :: "Con_S5_A \(ot) \(d)" 
);

constraint violationsOpenOperatingTheater = sum(ot in OperatingTheaterIds, d in Days)(isOTOpen[ot,d]) :: "Con_S5_O";