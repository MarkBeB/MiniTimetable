include "../IHTP-model.mzn";

% PAS
% S1 Age groups: For each day of the decision horizon and for each room, the maximum difference between age groups of patients sharing the room should be minimized.
%

constraint forall(roomId in RoomIds, day in Days)(
  let {
    array[int] of AgeGroups: occupantAges = [ occupant.ageGroup | occupant in occupantData 
      where occupant.assignedRoom == roomId /\ day < occupant.lengthOfStay ];
    
    array[int] of var opt AgeGroups: patientAges = [ patientData[id].ageGroup | id in PatientIds 
      where patientVar[id].assignedRoom[day] == roomId ];
    
    var opt AgeGroups: maxAge = max_weak(occupantAges ++ patientAges);
    var opt AgeGroups: minAge = min_weak(occupantAges ++ patientAges);  
  } in
  
  if roomVar[roomId].isOccupied[day] then
    roomVar[roomId].penaltyAgeMixing[day] = enum2int(maxAge) - enum2int(minAge)
  else
    roomVar[roomId].penaltyAgeMixing[day] = 0
  endif

);

constraint Penalties.roomMixedAge == Weights.roomMixedAge * sum(room in roomVar, day in Days)(room.penaltyAgeMixing[day]);