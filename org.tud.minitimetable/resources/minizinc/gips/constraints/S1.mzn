include "../IHTP-Model.mzn";

% PAS
% S1 Age groups: For each day of the decision horizon and for each room, the maximum difference between age groups of patients sharing the room should be minimized.
%

constraint violationsRoomMixedAge = sum(d in Days, r in RoomIds) (
    let {
        var set of AgeGroups: patientAges = { patientAgeGroup(p) | p in PatientIds where isPatientPresentInRoom[p,r,d]  };       
        set of AgeGroups: occupantAges = { occupantAgeGroup(o) | o in OccupantIds where isOccupantInRoom(o,d,r) };
        var set of AgeGroups: allPresentAges = patientAges union occupantAges;
    } in 
    if card(allPresentAges) == 0 then 0 else enum2int(max(allPresentAges)) - enum2int(min(allPresentAges)) endif
) :: "Con_S1_O";

% constraint violationsRoomMixedAge = sum(d in Days, r in RoomIds) (
%     let {
%         array[int] of var opt AgeGroups: patientAges = [ patientAgeGroup(p) | p in PatientIds where isPatientPresentInRoom[p,r,d] ];       
%         array[int] of AgeGroups: occupantAges = [ occupantAgeGroup(o) | o in OccupantIds where isOccupantInRoom(o,d,r) ];
%         array[int] of var opt AgeGroups: allPresentAges = patientAges ++ occupantAges;
%         var bool: roomIsEmpty = exists(i in allPresentAges)(occurs(i)) == false;
%     } in 
%     if roomIsEmpty then 0 else enum2int(max(allPresentAges)) - enum2int(min(allPresentAges)) endif
% ) :: "Con_S1_O";