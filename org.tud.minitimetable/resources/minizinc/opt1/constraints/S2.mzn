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
          array[_] of int: occupants = [ occupantSkillLevel(id, day, shift) | id in OccupantIds where isOccupantInRoom(id, day, roomId)];
          array[_] of var opt int: patients = [ patientSkillLevel(id, day, shift) | id in PatientIds where isPatientInRoom(id, day, roomId)];
        } in
        sum([max(0, -nurseLevel + level) | level in occupants]) + sum([max(0, -nurseLevel + level) | level in patients])
      )
    else
      nurseVar[nurseId].penaltySkillLevel[day, shift] = 0
    endif
  )
);

constraint Penalties.roomNurseSkill = Weights.roomNurseSkill * sum(nurse in nurseVar, day in Days, shift in ShiftTypes)(nurse.penaltySkillLevel[day, shift]);



array[Days, ShiftTypes, RoomIds, NurseIds] of var bool: nurseAssignedToRoom;
constraint forall(d in Days, s in ShiftTypes, r in RoomIds)(
    sum(n in NurseIds)(nurseAssignedToRoom[d, s, r, n]) <= 1
);



array[PatientIds, Days, ShiftTypes] of var PenaltyRange: skillDifference;

constraint forall(p in PatientIds, d in Days, s in ShiftTypes)(
    let { 
        int: required = patientSkillLevel(p, d, s)
    } in
    forall(n in NurseIds, r in RoomIds where isWorking(n,d,s))(
        % Kopplung: Wenn Nurse n den Patienten p in Schicht s betreut...
        % ...dann muss die Strafe mindestens (Required - NurseLevel) sein.
        
        nurseVar[n].assignedRooms[d, s, r] -> (
            skillDifference[p, d, s] >= required - nurseData[n].skillLevel
        )
    )
);