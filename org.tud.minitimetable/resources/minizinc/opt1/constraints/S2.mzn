%include "../IHTP-model.mzn";

% NRA
% S2 Minimum skill level: The minimum skill level a nurse must have to provide the required care for a patient during each shift of their stay should be met.
% If the skill of the nurse assigned to a patient’s room in a shift does not reach the minimum level required by that patient,
% a penalty equal to the difference between the two skill levels is incurred.
% Note that a nurse with a skill level greater than the minimum required can be assigned to the room at no additional cost.
%

function int: occupantSkillIndex(Days: day, ShiftTypes: shift) =
  enum2int(shift) - 1 + enum2int(day) * length(ShiftTypes);
  
function var int: patientSkillIndex(PatientIds: id, Days: day, ShiftTypes: shift) =
  enum2int(shift) - 1 + (enum2int(day) - patientVar[id].admissionDay) * length(ShiftTypes);

function int: getRequiredOccupantSkill(OccupantIds: o, Days: d, ShiftTypes: s) =
  occupantData[o].skillLevelRequired[occupantSkillIndex(d, s)];
  
function var int: getRequiredPatientSkill(PatientIds: p, Days: d, ShiftTypes: s) =
  patientData[p].skillLevelRequired[ patientSkillIndex(p, d, s) ];

array[PatientIds, Days, ShiftTypes] of var PenaltyRange: patientSkillDifference;
array[OccupantIds, Days, ShiftTypes] of var PenaltyRange: occupantSkillDifference;

constraint forall(p in PatientIds, d in Days, s in ShiftTypes)(
  forall(n in NurseIds, r in RoomIds where isNurseWorking(n,d,s))(
    (isPatientInRoom(p,d,r) /\ nurseVar[n].assignedRooms[d, s, r]) -> (
      patientSkillDifference[p, d, s] >= getRequiredPatientSkill(p, d, s) - nurseData[n].skillLevel
    )
  )
);

constraint forall(o in OccupantIds, d in Days, s in ShiftTypes)(
    forall(n in NurseIds, r in RoomIds where isNurseWorking(n,d,s) /\ isOccupantInRoom(o,d,r))(
      nurseVar[n].assignedRooms[d, s, r] -> (
        occupantSkillDifference[o, d, s] >= getRequiredOccupantSkill(o, d, s) - nurseData[n].skillLevel
      )
    )
);

constraint Penalties.roomNurseSkill = Weights.roomNurseSkill * (
    sum(p in PatientIds, d in Days, s in ShiftTypes)(patientSkillDifference[p, d, s]) +
    sum(o in OccupantIds, d in Days, s in ShiftTypes)(occupantSkillDifference[o, d, s])
);