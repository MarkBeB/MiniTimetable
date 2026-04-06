include "../IHTP-Model.mzn";

% PAS
% S1 Age groups: For each day of the decision horizon and for each room, the maximum difference between age groups of patients sharing the room should be minimized.
%

array[RoomIds, Days, AgeGroups] of var bool: agePresent;
array[RoomIds, Days] of var AgeGroups: maxRoomAge;
array[RoomIds, Days] of var AgeGroups: minRoomAge;

constraint forall(r in RoomIds, d in Days, g in AgeGroups) (
  agePresent[r, d, g] <-> (
    exists(p in PatientIds where patientAgeGroup(p) == g /\ r in compatibleRooms(p))(isPatientPresentInRoom[p,r,d])
    \/
    exists(o in OccupantIds where occupantAgeGroup(o) == g)(isOccupantInRoom(o,d,r))
  ) :: "Con_S1_A \(r) \(d) \(g)"
);

constraint forall(r in RoomIds , d in Days, g in AgeGroups) (
  (agePresent[r,d,g] -> maxRoomAge[r,d] >= g) :: "Con_S1_B \(r) \(d) \(g)"
);

constraint forall(r in RoomIds , d in Days, g in AgeGroups) (
  (agePresent[r,d,g] -> minRoomAge[r,d] <= g) :: "Con_S1_C \(r) \(d) \(g)"
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
) :: "Con_S1_C";

constraint penaltyRoomMixedAge = Weights.roomMixedAge * agePenalty  :: constraint_name("Con_S1_O");