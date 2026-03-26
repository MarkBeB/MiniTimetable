include "../IHTP-model.mzn";

% PAS
% S1 Age groups: For each day of the decision horizon and for each room, the maximum difference between age groups of patients sharing the room should be minimized.
%

constraint forall(p in PatientIds, d in Days)(
    let { 
        AgeGroups: age = patientAgeGroup(p)
    } in
    forall(r in RoomIds)(
        isPatientInRoom[p,d,r] -> (
            roomMaxAge[r, d] >= age /\
            roomMinAge[r, d] <= age
        )
    )
);

constraint forall(r in RoomIds, d in Days)(
    let { 
        array[int] of AgeGroups: occupantAges = [ occupantAgeGroup(o) | o in OccupantIds where isOccupantInRoom(o,d,r)] 
    } in
    if length(occupantAges) > 0 then
        roomMaxAge[r, d] >= max(occupantAges) /\
        roomMinAge[r, d] <= min(occupantAges)
    endif
);

constraint forall(r in RoomIds, d in Days)(
    roomAgeDifference[r, d] = enum2int(roomMaxAge[r, d]) - enum2int(roomMinAge[r, d])
);

constraint Penalties.roomMixedAge = Weights.roomMixedAge * sum(r in RoomIds, d in Days)(roomAgeDifference[r, d]);