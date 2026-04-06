include "../IHTP-Model.mzn";

% PAS
% S1 Age groups: For each day of the decision horizon and for each room, the maximum difference between age groups of patients sharing the room should be minimized.
%

% Huge improvement over v3

array[RoomIds, Days, AgeGroups] of var bool: agePresent;
array[RoomIds, Days] of var AgeGroups: maxRoomAge;
array[RoomIds, Days] of var AgeGroups: minRoomAge;

constraint forall(r in RoomIds, d in Days, g in AgeGroups) (
  if exists(o in OccupantIds where isOccupantAgeGroup(o,g))(isOccupantInRoom(o,d,r)) then
    agePresent[r, d, g] = 1
  else
    agePresent[r, d, g] <-> exists(p in PatientIds where isPatientAgeGroup(p,g) /\ r in compatibleRooms(p))(isPatientPresentInRoom[p,r,d])
  endif
  :: "Con_S1_A \(r) \(d) \(g)"
);

constraint forall(r in RoomIds, d in Days, g in AgeGroups) (
  maxRoomAge[r, d] >= agePresent[r,d,g] * enum2int(g)
  :: "Con_S1_B \(r) \(d) \(g)"
);

constraint forall(r in RoomIds, d in Days, g in AgeGroups) (
  minRoomAge[r, d] <= enum2int(g) + (1-agePresent[r,d,g]) * length(AgeGroups)
  :: "Con_S1_C \(r) \(d) \(g)"
);

var int: agePenalty = sum(r in RoomIds, d in Days) (
  let {
      var bool: occupied = exists(g in AgeGroups) (agePresent[r,d,g]);
  } in 
  if occupied then 
    enum2int(maxRoomAge[r,d]) - enum2int(minRoomAge[r,d]) 
  else 
    0 
  endif
) :: "Con_S1_D";

constraint penaltyRoomMixedAge = Weights.roomMixedAge * (
 agePenalty
) :: constraint_name("Con_S1_O");