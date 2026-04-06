include "IHTP-Model.mzn" ;

output :: "json"
[ "{\n"] ++

[ "  \("score"): \(score),\n" ] ++
[ "  \("penalties"): {\n" ] ++
[ "    \("RoomAgeMix"): \(penaltyRoomMixedAge),\n" ] ++
[ "    \("RoomSkillLevel"): \(penaltyRoomNurseSkill),\n" ] ++
[ "    \("ContinuityOfCare"): \(penaltyContinuityOfCare),\n" ] ++
[ "    \("ExcessiveNurseWorkload"): \(penaltyNurseEccessiveWorkload),\n" ] ++
[ "    \("OpenOperatingTheater"): \(penaltyOpenOperatingTheater),\n" ] ++
[ "    \("SurgeonTransfer"): \(penaltySurgeonTransfer),\n" ] ++
[ "    \("PatientDelay"): \(penaltyPatientDelay),\n" ] ++
[ "    \("ElectiveUnscheduledPatients"): \(penaltyUnscheduledOptional)\n" ] ++
[ "  },\n" ] ++

[ " \("noOfPatients"): \(sum(p in PatientIds)(1)),\n" ] ++
[ " \("noOfAdmitted"): \(count(p in PatientIds)(isPatientAdmitted[p])),\n" ] ++
[ " \("noOfPostponed"): \(count(p in PatientIds)(not isPatientAdmitted[p])),\n" ] ++

[ "  \("patients"): [\n" ] ++
[ "    {\("id"): \("\(p)"), " ++
%      "\("no_nurses"): \(count(n in NurseIds)(patientCaredByNurse[p, n])), " ++
      "\("admission_day"): "++show_int(3, patientAdmissionDay[p])++", " ++ 
      "\("room"): \(format(patientRoom[p])), " ++
      "\("operating_theater"): \("\(patientOT[p])")},\n" | p in PatientIds where fix(isPatientAdmitted[p]) ] ++
[ "  ],\n" ] ++
/*
[ "  \("occupants"): [\n" ] ++
[ "    {\("id"): \("\(o)"), "  ++
      "\("no_nurses"): \(count(n in NurseIds)(occupantCaredByNurse[o, n])), " ++ 
      "\("room"): \(format(occupantData[o].assignedRoom))},\n" | o in OccupantIds ] ++
[ "  ],\n" ] ++
*/

[ " \("postponedPatients"): [" ] ++
[ "    \("\(p)")," | p in PatientIds where not fix(isPatientAdmitted[p]) ] ++
[ "  ],\n" ] ++


[ "  \("nurses"): [\n" ] ++
[ "    {\n" ++
  "      \("id"): \(format(n)),\n" ++
  "      \("assignments"): [\n" ++
  join(",\n",[
  "        {\("day"): "++show_int(3,d)++", \("shift"): \(format(s)), \("rooms"): ["++join(", ",[ "\(format(r))" |r in RoomIds where fix(isNurseInRoom[n,r,d,s])])++"]}"
    | d in Days, s in ShiftTypes where isNurseAvailable(n,d,s)]) ++ "\n" ++
  "      ]\n" ++
  "    },\n" 
  | n in NurseIds ] ++
[ "  ],\n" ] ++

[ "}" ]

% "++(if fix(p.assignedOT) = <> then "null" else "\("\(p.assignedOT)")" endif)++"