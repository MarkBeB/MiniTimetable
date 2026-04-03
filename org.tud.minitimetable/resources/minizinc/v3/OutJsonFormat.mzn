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
[ "    {\("id"): \("\(p)"), " ++
      "\("no_nurses"): \(count(n in NurseIds)(patientCaredByNurse[p, n])), " ++
      "\("admission_day"): "++show_int(3, patientAdmissionDay[p])++", " ++ 
      "\("room"): \(format(patientRoom[p,patientAdmissionDay[p]])), " ++
      "\("operating_theater"): \("\(patientOT[p])")},\n" | p in PatientIds where fix(isPatientAdmitted[p]) ] ++
[ "  ],\n" ] ++

[ "  \("occupants"): [\n" ] ++
[ "    {\("id"): \("\(o)"), "  ++
      "\("no_nurses"): \(count(n in NurseIds)(occupantCaredByNurse[o, n])), " ++ 
      "\("room"): \(format(occupantData[o].assignedRoom))},\n" | o in OccupantIds ] ++
[ "  ],\n" ] ++

[ " \("postponedPatients"): [" ] ++
[ "    \("\(p)")," | p in PatientIds where not fix(isPatientAdmitted[p]) ] ++
[ "  ],\n" ] ++


[ "  \("nurses"): [\n" ] ++
[ "    {\n" ++
  "      \("id"): \(format(n)),\n" ++
  "      \("assignments"): [\n" ++
  join(",\n",[
  "        {\("day"): "++show_int(3,d)++", \("shift"): \(format(s)), \("rooms"): ["++join(", ",[ "\(format(r))" |r in RoomIds where fix(isNurseInRoom[n,d,s,r])])++"]}"
    | d in Days, s in ShiftTypes where isNurseAvailable(n,d,s)]) ++ "\n" ++
  "      ]\n" ++
  "    },\n" 
  | n in NurseIds ] ++
[ "  ],\n" ] ++

[ "}" ]

% "++(if fix(p.assignedOT) = <> then "null" else "\("\(p.assignedOT)")" endif)++"