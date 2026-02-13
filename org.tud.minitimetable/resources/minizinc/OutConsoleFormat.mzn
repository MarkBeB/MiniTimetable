include "data/IHTC_model.mzn" ;

output
[ "nurses = [\n" ] ++
[ "\t(\n \(n) \n" | n in nurseVar] ++
[ "];\n\n" ] ++

[ "rooms = [\n"] ++
[ "\t(\n" ++ 
  "\t\tagePenalty: \(rV.penaltyAgeMixing)\n" ++
  "\t\tisOccupied: \(rV.isOccupied)\n" ++
  "\t\tworkload: \(rV.workload)\n" ++
  "\t\tmaxAgeGroup: \(rV.maxAgeGroup)\n" ++
  "\t\tminAgeGroup: \(rV.minAgeGroup)\n" ++
  "\t\tassignedPatients: \(rV.assignedPatients)\n" ++
  "\t\tassignedGender: \(rV.assignedGender)\n" ++
  "\t\tmaxAgeGroupOccupants: \(rV.maxAgeGroupOccupants)\n" ++
  "\t\tminAgeGroupOccupants: \(rV.minAgeGroupOccupants)\n" ++
  "\t\tmaxAgeGroupPatients: \(rV.maxAgeGroupPatients)\n" ++
  "\t\tminAgeGroupPatients: \(rV.minAgeGroupPatients)\n" ++
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
[ "score = \(score)\n"]