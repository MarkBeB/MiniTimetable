include "../IHTP-model.mzn";

% NRA
% S2 Minimum skill level: The minimum skill level a nurse must have to provide the required care for a patient during each shift of their stay should be met.
% If the skill of the nurse assigned to a patient’s room in a shift does not reach the minimum level required by that patient,
% a penalty equal to the difference between the two skill levels is incurred.
% Note that a nurse with a skill level greater than the minimum required can be assigned to the room at no additional cost.
%

function int: occupantSkillLevel(OccupantIds: id, Days: day, ShiftTypes: shift) = occupantData[id].skillLevelRequired[indexOf(day, shift)];
function var int: patientSkillLevel(PatientIds: id, Days: day, ShiftTypes: shift) = patientData[id].skillLevelRequired[indexOf(id, day, shift)];

constraint forall(day in Days, shift in ShiftTypes)(
  forall(nurseId in NurseIds)(
    if nurseVar[nurseId].isWorking[day, shift] then
      nurseVar[nurseId].penaltySkillLevel[day, shift] = sum(roomId in RoomIds where nurseVar[nurseId].assignedRooms[day, shift, roomId])(
        let { 
          int: nurseLevel = nurseData[nurseId].skillLevel;
          array[_] of int: occupants = [ occupantSkillLevel(id, day, shift) | id in OccupantIds where occupantIsInRoom(id, day, roomId)];
          array[_] of var opt int: patients = [ patientSkillLevel(id, day, shift) | id in PatientIds where patientIsInRoom(id, day, roomId)];
        } in
        sum([max(0, -nurseLevel + level) | level in occupants]) + sum([max(0, -nurseLevel + level) | level in patients])
      )
    else
      nurseVar[nurseId].penaltySkillLevel[day, shift] = 0
    endif
  )
);

constraint Penalties.roomNurseSkill = Weights.roomNurseSkill * sum(nurse in nurseVar, day in Days, shift in ShiftTypes)(nurse.penaltySkillLevel[day, shift])