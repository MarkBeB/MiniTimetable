include "../IHTP-Model.mzn";

% NRA
% S2 Minimum skill level: The minimum skill level a nurse must have to provide the required care for a patient during each shift of their stay should be met.
% If the skill of the nurse assigned to a patient’s room in a shift does not reach the minimum level required by that patient,
% a penalty equal to the difference between the two skill levels is incurred.
% Note that a nurse with a skill level greater than the minimum required can be assigned to the room at no additional cost.
%
  
array[PatientIds, Days, ShiftTypes] of var PenaltyRange: patientSkillDifference;
array[OccupantIds, Days, ShiftTypes] of var PenaltyRange: occupantSkillDifference;

constraint forall(p in PatientIds, d in Days, s in ShiftTypes)(
  forall(n in NurseIds, r in RoomIds where isNurseAvailable(n,d,s))(
    (patientRoom[p,d] == r /\ isNurseInRoom[n,d,s,r]) -> (
      patientSkillDifference[p,d,s] = max(0, patientSkillRequired(p, d-patientAdmissionDay[p], s) - nurseSkillLevel(n))
    )
  ) 
);

constraint forall(o in OccupantIds, d in Days, s in ShiftTypes)(
  forall(n in NurseIds, r in RoomIds where isNurseAvailable(n,d,s) /\ isOccupantInRoom(o,d,r))(
    isNurseInRoom[n,d,s,r] -> (
      occupantSkillDifference[o,d,s] = max(0, occupantSkillRequired(o,d,s) - nurseSkillLevel(n))
    )
  )
);

constraint (Penalties.roomNurseSkill = 
    Weights.roomNurseSkill * sum(p in PatientIds, d in Days, s in ShiftTypes)(patientSkillDifference[p,d,s]) +
    Weights.roomNurseSkill * sum(o in OccupantIds, d in Days, s in ShiftTypes)(occupantSkillDifference[o,d,s])
);