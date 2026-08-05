include "../IHTP-Model.mzn";

% Additional Hard Constraints
% AH3 OT Assignment on Admittance: The existing hard constraints (H1–H8) do not guarantee that an admitted patient is assigned to an OT for surgery.
% Conversely, a postponed patient requires no OT allocation.
%

constraint forall(p in PatientIds) (
  sum(ot in OperatingTheaterIds)(isPatientInOT[p,ot]) = isPatientAdmitted[p]
  :: "Con_AH3 \(p)"
);