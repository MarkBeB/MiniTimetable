include "data/IHTC_model.mzn" ;

%array[int] of var opt int: test2 = [0,1,2,<>,4,5];
%var int: test3 = sum(x in test2)(max(0, -2 + x));

output
[ "nurses = [\n" ] ++
[ "\t(\n \(n) \n" | n in nurseVar] ++
[ "];\n\n" ] ++

[ "rooms = [\n"] ++
[ "\t(\n" ++ 
  "\t\tagePenalty: \(rV.penaltyAgeMixing)\n" ++
  "\t\tisOccupied: \(rV.isOccupied)\n" ++
  "\t\tworkload: \(rV.workload)\n" ++
  "\t\tassignedPatients: \(rV.assignedPatients)\n" ++
  "\t\tassignedGender: \(rV.assignedGender)\n" ++
%  "\t\tmaxAgeGroupOccupants: \(rV.maxO)\n" ++
%  "\t\tminAgeGroupOccupants: \(rV.minO)\n" ++
%  "\t\tmaxAgeGroupPatients: \(rV.maxP)\n" ++
%  "\t\tminAgeGroupPatients: \(rV.minP)\n" ++
  "\t\tpenaltyAgeMixing: \(rV.penaltyAgeMixing)\n" ++
  "\t)\n" | id in RoomIds, rV = roomVar[id], rF = roomData[id]] ++
[ "];\n\n" ] ++ 

[ "patients = [\n" ] ++
[ "\t\(p) \n" | p in patientVar] ++
[ "];\n\n" ] ++

[ "surgeons = [\n" ] ++
[ "\t\(surgeonData[id]) \n" ++ "\t\(surgeonVar[id]) \n" | id in SurgeonIds] ++
[ "];\n\n" ] ++

[ "ots = [\n" ] ++
[ "\t\(operatingTheaterData[id]) \n" ++ "\t\(operatingTheaterVar[id]) \n" | id in OperatingTheaterIds] ++
[ "];\n\n" ] ++

[ "Penalties:\n" ] ++
[ "   roomMixedAge         = "++show_int(-6, Penalties.roomMixedAge)        ++"   roomNurseSkill         = "++show_int(-6, Penalties.roomNurseSkill)++ "\n"] ++
[ "   continuityOfCare     = "++show_int(-6, Penalties.continuityOfCare)    ++"   nurseEccessiveWorkload = "++show_int(-6, Penalties.nurseEccessiveWorkload)++ "\n"] ++
[ "   openOperatingTheater = "++show_int(-6, Penalties.openOperatingTheater)++"   surgeonTransfer        = "++show_int(-6, Penalties.surgeonTransfer)++ "\n"] ++
[ "   patientDelay         = "++show_int(-6, Penalties.patientDelay)        ++"   unscheduledOptional    = "++show_int(-6, Penalties.unscheduledOptional)++ "\n"] ++
[ "\n" ] ++
[ "TEST: \(test3)\n"] ++
[ "total score = \(score)\n"]