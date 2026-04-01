include "../IHTP-model.mzn";

% NRA
% S3 Continuity of care: To ensure continuity of care, the total number of distinct nurses providing care to a patient during their entire stay should be minimized.
% The given rosters assume maximum one shift per day for each nurse, hence the number of different nurses who take care of a patient is at least 3,
% thus resulting in a minimum cost for this component of 3 times the number of admitted patients.
%

array[PatientIds] of var PenaltyRange: patientContinuityPenalty ::output = [p: sum(n in NurseIds)(patientCaredByNurse[p, n]) | p in PatientIds];
array[OccupantIds] of var PenaltyRange: occupantContinuityPenalty ::output = [o: sum(n in NurseIds)(occupantCaredByNurse[o, n]) | o in OccupantIds];

constraint Penalties.continuityOfCare =
    Weights.continuityOfCare * sum(p in PatientIds)(patientContinuityPenalty[p])
  + Weights.continuityOfCare * sum(o in OccupantIds)(occupantContinuityPenalty[o]);

var int: lowerBound;
constraint let {
  int: numberOfOccupants = card(OccupantIds);
  int: numberOfMandatoryPatients = sum(p in PatientIds where isPatientMandatory(p))(1);
  var int: numberOfOptionalPatients = sum(p in PatientIds where isPatientOptional(p))(isPatientAdmitted[p]);
} in lowerBound = Weights.continuityOfCare * card(ShiftTypes) * (numberOfOccupants + numberOfMandatoryPatients + numberOfOptionalPatients);
