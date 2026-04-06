include "../IHTP-Model.mzn";

% NRA
% S3 Continuity of care: To ensure continuity of care, the total number of distinct nurses providing care to a patient during their entire stay should be minimized.
% The given rosters assume maximum one shift per day for each nurse, hence the number of different nurses who take care of a patient is at least 3,
% thus resulting in a minimum cost for this component of 3 times the number of admitted patients.
%

constraint forall(p in PatientIds, d in Days, s in ShiftTypes, n in NurseIds where patientRoom[p,d] != <>)(
    patientCaredByNurse[p, n] >= isNurseInRoom[n, d, s, patientRoom[p,d]]
) :: constraint_name("Con_S3_A");

constraint forall(o in OccupantIds, d in Days, s in ShiftTypes, n in NurseIds where isOccupantPresent(o,d))(
    occupantCaredByNurse[o, n] >= isNurseInRoom[n, d, s, occupantRoom(o)]
) :: constraint_name("Con_S3_B");

constraint penaltyContinuityOfCare = Weights.continuityOfCare * (
    sum(p in PatientIds, n in NurseIds)(patientCaredByNurse[p,n])
  + sum(o in OccupantIds, n in NurseIds)(occupantCaredByNurse[o,n])
) :: constraint_name("Con_S3_O");

%constraint Penalties.continuityOfCare >= Weights.continuityOfCare * length(ShiftTypes) * (count(p in patientVar)(p.isAdmitted) + length(OccupantIds));