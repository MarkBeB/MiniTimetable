include "../IHTP-Model.mzn";

% PAS
% S1 Age groups: For each day of the decision horizon and for each room, the maximum difference between age groups of patients sharing the room should be minimized.
%

array[RoomIds, Days] of var AgeGroups: roomMaxAge;
array[RoomIds, Days] of var AgeGroups: roomMinAge;
array[RoomIds, Days] of var 0..card(AgeGroups): roomAgeDifference;

constraint forall(p in PatientIds, d in Days)(
    let { 
        AgeGroups: age = patientData[p].ageGroup
    } in
    forall(r in RoomIds)(
        patientRoom[p,d] == r -> (
            roomMaxAge[r, d] >= age /\
            roomMinAge[r, d] <= age
        )
    )
) :: constraint_name("Con_S1_A");

constraint forall(r in RoomIds, d in Days)(
    let { 
        array[int] of AgeGroups: occupantAges = [ occupantAgeGroup(o) | o in OccupantIds where isOccupantInRoom(o,d,r)] 
    } in
    if length(occupantAges) > 0 then
        roomMaxAge[r, d] >= max(occupantAges) /\
        roomMinAge[r, d] <= min(occupantAges)
    endif
) :: constraint_name("Con_S1_B");

constraint forall(r in RoomIds, d in Days)(
    roomAgeDifference[r, d] = enum2int(roomMaxAge[r, d]) - enum2int(roomMinAge[r, d])
) :: constraint_name("Con_S1_C");

constraint Penalties.roomMixedAge = Weights.roomMixedAge * sum(r in RoomIds, d in Days)(roomAgeDifference[r, d])  :: constraint_name("Con_S1_O");