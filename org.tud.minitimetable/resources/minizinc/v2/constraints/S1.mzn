include "../IHTP-model.mzn";

% PAS
% S1 Age groups: For each day of the decision horizon and for each room, the maximum difference between age groups of patients sharing the room should be minimized.
%

array[RoomIds, Days] of var AgeGroups: roomMaxAge;
array[RoomIds, Days] of var AgeGroups: roomMinAge;
array[RoomIds, Days] of var 0..card(AgeGroups): roomAgeDifference;

/*
constraint forall(p in PatientIds, d in Days)(
    let { 
        AgeGroups: age = patientAgeGroup(p)
    } in
    forall(r in RoomIds)(         
        isPatientPresentInRoom[p,d,r] -> (
            roomMaxAge[r, d] >= age /\
            roomMinAge[r, d] <= age
        )
    )
);

constraint forall(r in RoomIds, d in Days)(
    let { 
        array[int] of AgeGroups: occupantAges = [ occupantAgeGroup(o) | o in OccupantIds where isOccupantPresentInRoom(o,d,r)] 
    } in
    if length(occupantAges) > 0 then
        roomMaxAge[r, d] >= max(occupantAges) /\
        roomMinAge[r, d] <= min(occupantAges)
    endif
);

constraint forall(r in RoomIds, d in Days)(
    roomAgeDifference[r, d] = isRoomOccupied[r,d] * (enum2int(roomMaxAge[r, d]) - enum2int(roomMinAge[r, d]))
);
*/


constraint forall(r in RoomIds, d in Days)(
  let {
    array[int] of AgeGroups: occupantAges = [ occupantAgeGroup(o) | o in OccupantIds 
      where isOccupantPresentInRoom(o,d,r) ];
    
    array[int] of var opt AgeGroups: patientAges = [ patientAgeGroup(p) | p in PatientIds 
      where isPatientPresentInRoom[p,d,r] ];
    
    var opt AgeGroups: maxAge = max_weak(occupantAges ++ patientAges);
    var opt AgeGroups: minAge = min_weak(occupantAges ++ patientAges);  
  } in
  
  if isRoomOccupied[r,d] then
    roomAgeDifference[r,d] = enum2int(maxAge) - enum2int(minAge)
  else
    roomAgeDifference[r,d] = 0
  endif

);


constraint Penalties.roomMixedAge = Weights.roomMixedAge * sum(r in RoomIds, d in Days)(roomAgeDifference[r, d]);