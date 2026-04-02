include "IHTP-model.mzn" ;

output :: "json"
[ "{\n"] ++

[ "  \("score"): \(score),\n" ] ++
[ "  \("penalties"): {\n" ] ++
[ "    \("RoomAgeMix"): \(Penalties.roomMixedAge),\n" ] ++
[ "    \("RoomSkillLevel"): \(Penalties.roomNurseSkill),\n" ] ++
[ "    \("ContinuityOfCare"): \(Penalties.continuityOfCare),\n" ] ++
[ "    \("ExcessiveNurseWorkload"): \(Penalties.nurseEccessiveWorkload),\n" ] ++
[ "    \("OpenOperatingTheater"): \(Penalties.openOperatingTheater),\n" ] ++
[ "    \("SurgeonTransfer"): \(Penalties.surgeonTransfer),\n" ] ++
[ "    \("PatientDelay"): \(Penalties.patientDelay),\n" ] ++
[ "    \("ElectiveUnscheduledPatients"): \(Penalties.unscheduledOptional)\n" ] ++
[ "  },\n" ] ++

[ "  \("patients"): [\n" ] ++
[ "    {\("id"): \("\(id)"), " ++
      "\("no_nurses"): \(count(nurseId in NurseIds)(p.caredByNurse[nurseId])), " ++
      "\("admission_day"): "++show_int(3, p.admissionDay)++", " ++ 
      "\("room"): \(format(p.assignedRoom[p.admissionDay])), " ++
      "\("operating_theater"): \("\(p.assignedOT)")},\n" | id in PatientIds, p = patientVar[id] where fix(p.isAdmitted) ] ++
[ "  ],\n" ] ++

[ "  \("occupants"): [\n" ] ++
[ "    {\("id"): \("\(id)"), "  ++
      "\("no_nurses"): \(count(nurseId in NurseIds)(o.caredByNurse[nurseId])), " ++ 
      "\("room"): \(format(occupantData[id].assignedRoom))},\n" | id in OccupantIds, o = occupantVar[id] ] ++
[ "  ],\n" ] ++

[ " \("postponedPatients"): [" ] ++
[ "    \("\(id)")," | id in PatientIds, p = patientVar[id] where not fix(p.isAdmitted) ] ++
[ "  ],\n" ] ++


[ "  \("nurses"): [\n" ] ++
[ "    {\n" ++
  "      \("id"): \(format(n)),\n" ++
  "      \("assignments"): [\n" ++
  join(",\n",[
  "        {\("day"): "++show_int(3,d)++", \("shift"): \(format(s)), \("rooms"): ["++join(", ",[ "\(format(r))" |r in RoomIds where fix(nV.assignedRooms[d, s, r])])++"]}"
    | d in Days, s in ShiftTypes where isNurseAvailable(n,d,s)]) ++ "\n" ++
  "      ]\n" ++
  "    },\n" 
  | n in NurseIds, nV = nurseVar[n], nF = nurseData[n] ] ++
[ "  ],\n" ] ++

[ "}" ]

% "++(if fix(p.assignedOT) = <> then "null" else "\("\(p.assignedOT)")" endif)++"