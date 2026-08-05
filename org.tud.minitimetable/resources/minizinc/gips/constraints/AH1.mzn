include "../IHTP-Model.mzn";

% Additional Hard Constraints
% AH1 No Room Transfer: This rule is stated in section 2.2 of [10]: “Room transfers are not permitted: each patient must remain in the same room for the entire duration of their stay.”
%

constraint forall(p in PatientIds) (
  (sum(r in RoomIds)(isPatientInRoom[p,r]) <= 1)
  :: "Con_AH1 \(p)"
);