include "IHTC_model.mzn" ;

output
[ "nurses = [\n" ] ++
[ "\t(\n \(n) \n" | n in nurseVar] ++
[ "];\n\n" ] ++

[ "rooms = [\n"] ++
[ "\t(\n" ++ 
  "\t\tmaxAgeGroup: \(r.maxAgeGroup)\n" ++
  "\t\tminAgeGroup: \(r.minAgeGroup)\n" ++
  "\t\tassignedPatients: \(r.assignedPatients)\n" ++
  "\t)\n" | r in rooms] ++
[ "];\n\n" ] ++ 

[ "patients = [\n" ] ++
[ "\t\(p) \n" | p in patientVar] ++
[ "];\n\n" ] ++

[ "\(Penalties)\n"] ++
[ "score = \(score);\n"] 