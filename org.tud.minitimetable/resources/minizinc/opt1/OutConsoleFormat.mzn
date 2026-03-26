include "IHTP-model.mzn" ;

%array[int] of var opt int: test2 = [0,1,2,<>,4,5];
%var int: test3 = sum(x in test2)(max(0, -2 + x));

output
%[ "nurses = [\n" ] ++
%[ "\t(\n \(n) \n" | n in NurseIds] ++
%[ "];\n\n" ] ++

[ "rooms = [\n"] ++
[ "  (\n" ++ 
  "    Patients:[\n" ++
  join("\n",[
  "       \(d):" 
  | d in Days]) ++ "\n" ++
  "    ],\n" ++
  "    Age:[\n" ++
  join("\n",[
  "      \(d): \(roomMaxAge[r,d])-\(roomMinAge[r,d])->\(roomAgeDifference[r,d])" 
  | d in Days ]) ++ "\n" ++
  "    ],\n" ++
  "    AgeDifference: \(sum(d in Days)(roomAgeDifference[r,d])),\n" ++
  "  )\n" | r in RoomIds] ++
[ "];\n\n" ] ++ 

[ "Penalties:\n" ] ++
[ "   roomMixedAge         = "++show_int(-6, Penalties.roomMixedAge)        ++"   roomNurseSkill         = "++show_int(-6, Penalties.roomNurseSkill)++ "\n"] ++
[ "   continuityOfCare     = "++show_int(-6, Penalties.continuityOfCare)    ++"   nurseEccessiveWorkload = "++show_int(-6, Penalties.nurseEccessiveWorkload)++ "\n"] ++
[ "   openOperatingTheater = "++show_int(-6, Penalties.openOperatingTheater)++"   surgeonTransfer        = "++show_int(-6, Penalties.surgeonTransfer)++ "\n"] ++
[ "   patientDelay         = "++show_int(-6, Penalties.patientDelay)        ++"   unscheduledOptional    = "++show_int(-6, Penalties.unscheduledOptional)++ "\n"] ++
[ "\n" ] ++
[ "total score = \(score)\n"]