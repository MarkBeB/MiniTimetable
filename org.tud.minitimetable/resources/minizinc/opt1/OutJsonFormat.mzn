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
[ "    {\("id"): \(format(p)), " ++
      "\("no_nurses"): \(count(n in NurseIds)(patientCaredByNurse[p,n])), " ++
      "\("admission_day"): "++show_int(3, patientAdmissionDay[p])++", " ++ 
      "\("room"): \(format(patientRoomAssignment[p])), " ++
      "\("operating_theater"): \(format(patientOTAssignment[p]))},\n" | p in PatientIds where not fix(isPatientPostponed[p]) ] ++
[ "  ],\n" ] ++

[ "  \("rooms"): [\n" ] ++
[ "    {\n" ++
  "      \("id"): \(format(r)),\n" ++
  "      \("ageDiff"): \(format(sum(d in Days)(roomAgeDifference[r,d]))),\n" ++
  "      \("ages"): [\n" ++
  join(",\n",[
  "        {\("day"): "++show_int(3,d)++", \("diff"): \(roomAgeDifference[r,d]), \("ages"): [" ++ join(", ",["\(format(patientAgeGroup(p)))" | p in PatientIds where fix(isPatientInRoom[p,d,r])]) ++ "]}"
  | d in Days]) ++ "\n" ++
  "      ]\n" ++
  "    },\n"
  | r in RoomIds ] ++ 
[ "  ],\n" ] ++

[ "  \("occupants"): [\n" ] ++
[ "    {\("id"): \(format(o)), "  ++
      "\("no_nurses"): \(count(n in NurseIds)(occupantCaredByNurse[o, n])), " ++ 
      "\("room"): \(format(occupants[o].assignedRoom))},\n" | o in OccupantIds ] ++
[ "  ],\n" ] ++

[ " \("postponedPatients"): [" ] ++
[ "\(format(p))," | p in PatientIds where fix(isPatientPostponed[p])] ++
[ "  ],\n" ] ++


[ "  \("nurses"): [\n" ] ++
[ "    {\n" ++
  "      \("id"): \(format(n)),\n" ++
  "      \("assignments"): [\n" ++
  join(",\n",[
  "        {\("day"): "++show_int(3,d)++", \("shift"): \(format(s)), \("rooms"): ["++join(", ",[ "\(format(r))" |r in RoomIds where fix(isNurseInRoom[n,d,s,r])])++"]}"
    | d in Days, s in ShiftTypes where fix(isNurseWorking(n,d,s))]) ++ "\n" ++
  "      ]\n" ++
  "    },\n" 
  | n in NurseIds] ++
[ "  ],\n" ] ++

[ "}" ]

% "++(if fix(p.assignedOT) = <> then "null" else "\("\(p.assignedOT)")" endif)++"