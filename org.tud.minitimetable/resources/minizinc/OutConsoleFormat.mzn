include "IHTC_model.mzn" ;

output
[ "nurses = [\n" ] ++
[ "\t(\n \(n) \n" | n in nurseVar] ++
[ "];\n\n" ] ++

[ "rooms = [\n"] ++
[ "\t(\n" ++ 
  "\t\tagePenalty: \(rV.penaltyAgeMixing)\n" ++
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

[ "\(Penalties)\n"] ++
[ "score = \(score);\n"] 