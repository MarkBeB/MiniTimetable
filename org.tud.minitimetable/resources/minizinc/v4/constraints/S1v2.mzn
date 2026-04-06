include "../IHTP-Model.mzn";

% PAS
% S1 Age groups: For each day of the decision horizon and for each room, the maximum difference between age groups of patients sharing the room should be minimized.
%

var int: agePenalty = sum(d in Days, r in RoomIds) (
  let {
    var set of AgeGroups: patientAges = { patientAgeGroup(p) | p in PatientIds where isPatientPresentInRoom[p,r,d] };       
    set of AgeGroups: occupantAges = { occupantAgeGroup(o) | o in OccupantIds where isOccupantInRoom(o,d,r) };
    var set of AgeGroups: allAges = patientAges union occupantAges;
  } in 
  if card(allAges) <= 1 then 0 else enum2int(max(allAges)) - enum2int(min(allAges)) endif
) :: "Con_S1_A";

constraint penaltyRoomMixedAge = Weights.roomMixedAge * agePenalty
  :: "Con_S1_O";