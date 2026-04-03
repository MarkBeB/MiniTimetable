include "../IHTP-model.mzn";

% PAS
% S1 Age groups: For each day of the decision horizon and for each room, the maximum difference between age groups of patients sharing the room should be minimized.
%

array[RoomIds, Days, AgeGroups] of var bool: agePresent;
array[RoomIds, Days] of var AgeGroups: maxRoomAge;
array[RoomIds, Days] of var AgeGroups: minRoomAge;

constraint forall(r in RoomIds, d in Days, g in AgeGroups) (
    agePresent[r, d, g] <-> (
        exists(p in PatientIds where patientAgeGroup(p) == g)(patientRoom[p,d] == r)
        \/
        exists(o in OccupantIds where occupantAgeGroup(o) == g)(isOccupantInRoom(o,d,r))
    )
);

constraint forall(r in RoomIds , d in Days) (
    forall(g in AgeGroups) (agePresent[r,d,g] -> maxRoomAge[r,d] >= g) /\
    forall(g in AgeGroups) (agePresent[r,d,g] -> minRoomAge[r,d] <= g)
);

var int: agePenalty = sum(r in RoomIds , d in Days) (
    let {
        var bool: occupied = exists(g in AgeGroups) (agePresent[r,d,g]);
    } in 
    if occupied then 
      enum2int(maxRoomAge[r,d]) - enum2int(minRoomAge[r,d]) 
    else 
      0 
    endif
);

constraint Penalties.roomMixedAge = Weights.roomMixedAge * agePenalty;