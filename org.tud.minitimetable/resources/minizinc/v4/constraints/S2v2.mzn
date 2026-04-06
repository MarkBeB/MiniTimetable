include "../IHTP-Model.mzn";

% NRA
% S2 Minimum skill level: The minimum skill level a nurse must have to provide the required care for a patient during each shift of their stay should be met.
% If the skill of the nurse assigned to a patient’s room in a shift does not reach the minimum level required by that patient,
% a penalty equal to the difference between the two skill levels is incurred.
% Note that a nurse with a skill level greater than the minimum required can be assigned to the room at no additional cost.
%

array[PatientIds, Days, ShiftTypes] of var 0..maxSkillLevel: patientSkillDifference;
array[OccupantIds, Days, ShiftTypes] of var 0..maxSkillLevel: occupantSkillDifference;

constraint forall(p in PatientIds, d in Days, s in ShiftTypes, r in compatibleRooms(p)) (
  let {
    var int: reqSkill = sum(start in validAdmissionDays[p] where start <= d /\ d < start + patientLengthOfStay(p)) (
      (patientAdmissionDay[p] == start) * patientSkillRequired(p,d,s,start)
    )
  } in
    forall(n in NurseIds) (
      (isPatientPresent[p,d] /\ isPatientInRoom[p,r] /\ isNurseInRoom[n,r,d,s]) -> (
        patientSkillDifference[p,d,s] >= reqSkill - nurseSkillLevel(n)
      )
      :: "Con_S2_A \(p) \(d) \(s) \(r) \(n)"
    )
);

constraint forall(o in OccupantIds, d in Days, s in ShiftTypes) (
  if d < occupantLengthOfStay(o) then 
    let {
      RoomIds: r = occupantRoom(o);
      int: reqSkill = occupantSkillRequired(o,d,s);
    } in 
    forall(n in NurseIds where nurseSkillLevel(n) < reqSkill) (
      isNurseInRoom[n,r,d,s] -> (
        occupantSkillDifference[o,d,s] >= reqSkill - nurseSkillLevel(n)
      )
      :: "Con_S2_B \(o) \(d) \(s) \(r) \(n)"
    )
  else
    occupantSkillDifference[o,d,s] = 0 
  endif
);

constraint penaltyRoomNurseSkill = Weights.roomNurseSkill * (
    sum(p in PatientIds, d in Days, s in ShiftTypes) (patientSkillDifference[p,d,s])
  + sum(o in OccupantIds, d in Days, s in ShiftTypes) (occupantSkillDifference[o,d,s])
) :: "Con_S2_O";



