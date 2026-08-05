include "IHTP-Model.mzn" ;

output :: "json"
[ "{\n"] ++

[ "  \("score"): \(score),\n" ] ++
[ "  \("penalties"): {\n" ] ++
[ "    \("RoomAgeMix"): \(Weights.roomMixedAge * violationsRoomMixedAge),\n" ] ++
[ "    \("RoomSkillLevel"): \(Weights.roomNurseSkill * violationsRoomNurseSkill),\n" ] ++
[ "    \("ContinuityOfCare"): \(Weights.continuityOfCare * violationsContinuityOfCare),\n" ] ++
[ "    \("ExcessiveNurseWorkload"): \(Weights.nurseEccessiveWorkload * violationsNurseEccessiveWorkload),\n" ] ++
[ "    \("OpenOperatingTheater"): \(Weights.openOperatingTheater * violationsOpenOperatingTheater),\n" ] ++
[ "    \("SurgeonTransfer"): \(Weights.surgeonTransfer * violationsSurgeonTransfer),\n" ] ++
[ "    \("PatientDelay"): \(Weights.patientDelay * violationsPatientDelay),\n" ] ++
[ "    \("ElectiveUnscheduledPatients"): \(Weights.unscheduledOptional * violationsUnscheduledOptional)\n" ] ++
[ "  },\n" ] ++

% [ " \("noOfPatients"): \(sum(p in PatientIds)(1)),\n" ] ++
% [ " \("noOfAdmitted"): \(count(p in PatientIds)(isPatientAdmitted[p])),\n" ] ++
% [ " \("noOfPostponed"): \(count(p in PatientIds)(not isPatientAdmitted[p])),\n" ] ++

[ "  \("patients"): [\n" ] ++
[ "    {\("id"): \("\(p)"), " ++
      "\("admission_day"): "++show_int(3, patientAdmissionDay[p])++", " ++ 
      "\("room"): \(format(patientRoom[p])), " ++
      "\("operating_theater"): \("\(patientOT[p])"), "  ++
      "},\n" | p in PatientIds where fix(isPatientAdmitted[p]) ] ++
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