include "data/IHTC_model.mzn" ;

output
[ "{\n"] ++

[ "  \("score"): \(score),\n" ] ++
[ "  \("penalties"): {\n" ] ++
[ "    \("roomMixedAge"): \(Penalties.roomMixedAge),\n" ] ++
[ "    \("roomNurseSkill"): \(Penalties.roomNurseSkill),\n" ] ++
[ "    \("continuityOfCare"): \(Penalties.continuityOfCare),\n" ] ++
[ "    \("nurseEccessiveWorkload"): \(Penalties.nurseEccessiveWorkload),\n" ] ++
[ "    \("openOperatingTheater"): \(Penalties.openOperatingTheater),\n" ] ++
[ "    \("surgeonTransfer"): \(Penalties.surgeonTransfer),\n" ] ++
[ "    \("patientDelay"): \(Penalties.patientDelay),\n" ] ++
[ "    \("unscheduledOptional"): \(Penalties.unscheduledOptional)\n" ] ++
[ "  },\n" ] ++

[ "  \("patients"): [\n" ] ++
[ "    {\("id"): \("\(id)"), " ++
      "\("admission_day"): "++show_int(3, p.admissionDay)++", " ++ 
      "\("room"): \(format(p.assignedRoom[p.admissionDay])), " ++
      "\("operating_theater"): \("\(p.assignedOT)")},\n" | id in PatientIds, p = patientVar[id] where fix(p.isAdmitted) ] ++
%[ "    {\("id"): \("\(id)"), \("postponed"): true},\n" | id in PatientIds, p = patientVar[id] where not fix(p.isAdmitted) ] ++
[ "  ],\n" ] ++

[ " \("postponedPatients"): [" ] ++
[ "    \("\(id)")," | id in PatientIds, p = patientVar[id] where not fix(p.isAdmitted) ] ++
[ "  ],\n" ] ++


[ "  \("nurses"): [\n" ] ++
[ "    {\n" ++
  "      \("id"): \(format(id)),\n" ++
  "      \("assignments"): [\n" ++
  join(",\n",[
  "        {\("day"): "++show_int(3,day)++", \("shift"): \(format(shift)), \("rooms"): ["++join(", ",[ "\(format(room))" |room in RoomIds where fix(n.assignedRooms[day, shift,room])])++"]}"
    | day in Days, shift in ShiftTypes where fix(n.isWorking[day, shift])]) ++ "\n" ++
  "      ]\n" ++
  "    },\n" 
  | id in NurseIds, n = nurseVar[id], nF = nurseData[id] ] ++
[ "  ],\n" ] ++

[ "}" ]

% "++(if fix(p.assignedOT) = <> then "null" else "\("\(p.assignedOT)")" endif)++"