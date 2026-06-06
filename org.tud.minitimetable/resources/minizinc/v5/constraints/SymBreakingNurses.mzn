include "../IHTP-Model.mzn";

%include "lex_lesseq.mzn";

function bool: areNursesIdentical(NurseIds: n1, NurseIds: n2) = (
     nurseSkillLevel(n1) == nurseSkillLevel(n2)
  /\ forall(d in Days, s in ShiftTypes) (isNurseAvailable(n1,d,s) == isNurseAvailable(n2,d,s))
);

constraint forall(n1, n2 in NurseIds where n1 < n2 /\ areNursesIdentical(n1, n2)) (
     sum(r in RoomIds, d in Days, s in ShiftTypes) (isNurseInRoom[n1,r,d,s]) 
  >= sum(r in RoomIds, d in Days, s in ShiftTypes) (isNurseInRoom[n2,r,d,s])
);