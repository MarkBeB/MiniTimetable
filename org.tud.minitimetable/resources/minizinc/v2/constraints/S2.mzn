include "../IHTP-model.mzn";

% NRA
% S2 Minimum skill level: The minimum skill level a nurse must have to provide the required care for a patient during each shift of their stay should be met.
% If the skill of the nurse assigned to a patient’s room in a shift does not reach the minimum level required by that patient,
% a penalty equal to the difference between the two skill levels is incurred.
% Note that a nurse with a skill level greater than the minimum required can be assigned to the room at no additional cost.
%


array[PatientIds, NurseIds, Days, ShiftTypes, Days] of int: precomputedSkillDiff = array5d(PatientIds, NurseIds, Days, ShiftTypes, Days, [
  (p,n,d,s,k):
  let { int: relativeDay = d - k } in
  if relativeDay >= 0 /\ relativeDay < patientData[p].lengthOfStay then
    max(0, patientSkillLevel(p,relativeDay,s) - nurseSkillLevel(n))
  else
    0
  endif
  | p in PatientIds, n in NurseIds, d in Days, s in ShiftTypes, k in Days
]);

constraint forall(d in Days, s in ShiftTypes, r in RoomIds, p in PatientIds)(
  patientSkillDifference[p,d,s] >= sum(n in NurseIds, k in Days)(
    % Nur wenn: Patient in Raum r UND Nurse n in Raum r UND Aufnahme war an Tag a
    (isPatientPresentInRoom[p,d,r] /\ isNursePresentInRoom[n,d,s,r] /\ isPatientAdmittedAt[p,k]) * precomputedSkillDiff[p,n,d,s,k]
  )
);

array[PatientIds, Days, ShiftTypes] of var PenaltyRange: patientSkillDifference;
array[OccupantIds, Days, ShiftTypes] of var PenaltyRange: occupantSkillDifference;

constraint forall(o in OccupantIds, d in Days, s in ShiftTypes, n in NurseIds, r in RoomIds)(
  if isNurseAvailable(n,d,s) /\ isOccupantPresentInRoom(o,d,r) then
    isNursePresentInRoom[n,d,s,r] -> (
      occupantSkillDifference[o,d,s] = max(0, requiredOccupantSkill(o,d,s) - nurseSkillLevel(n))
    )
  endif
);

constraint Penalties.roomNurseSkill = (
    Weights.roomNurseSkill * sum(p in PatientIds, d in Days, s in ShiftTypes)(patientSkillDifference[p,d,s])
  + Weights.roomNurseSkill * sum(o in OccupantIds, d in Days, s in ShiftTypes)(occupantSkillDifference[o,d,s])
);



/*
array[PatientIds, Days, ShiftTypes] of var PenaltyRange: patientSkillDifference;
array[OccupantIds, Days, ShiftTypes] of var PenaltyRange: occupantSkillDifference;

constraint forall(d in Days, s in ShiftTypes)(
  forall(n in NurseIds)(
    if isNurseAvailable(n,d,s) then
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

constraint Penalties.roomNurseSkill = (
    Weights.roomNurseSkill * sum(o in OccupantIds, d in Days, s in ShiftTypes)(occupantSkillDifference[o,d,s])
  + Weights.roomNurseSkill * sum(o in OccupantIds, d in Days, s in ShiftTypes)(occupantSkillDifference[o,d,s])
);
*/