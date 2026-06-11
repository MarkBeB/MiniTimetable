include "Parameters.mzn";
include "Variables.mzn";
include "ParFunctions.mzn";

predicate isPatientAdmitted(PatientIds: p) = sum(d in Days)(isPatientAdmittedAt[p,d]) > 0;
predicate isPatientInRoom(PatientIds: p, RoomIds: r) = isPatientInRoom[p,r];
predicate isPatientPresent(PatientIds: p, Days: d) = exists(b in Days)(isPatientAdmittedAt[p,b] /\ (b <= d) /\ (d < b + patientLengthOfStay(p)));
predicate isPatientPresentInRoom(PatientIds: p, RoomIds: r, Days: d) = isPatientInRoom(p,r) /\ isPatientPresent(p,d);



function ExtendedDays: patientAdmissionDayOutput(PatientIds: p) =
  let { array[int] of ExtendedDays: result = [ d | d in ExtendedDays where fix(isPatientAdmittedAt[p,d]) ] } in
  result[1];

function bool: isPatientAdmittedOutput(PatientIds: p) = fix(sum(d in Days)(isPatientAdmittedAt[p,d]) > 0);

function opt RoomIds: patientRoomAssignment(PatientIds: p) = 
  let { array[int] of RoomIds: result = [ r | r in RoomIds where fix(isPatientInRoom[p,r])] } in
  if length(result) == 0 then <> else result[1] endif;

function opt OperatingTheaterIds: patientOTAssignment(PatientIds: p) = 
  let { array[int] of OperatingTheaterIds: result = [ ot | ot in OperatingTheaterIds where fix(isPatientInOT[p,ot])] } in
  if length(result) == 0 then <> else result[1] endif;

function opt NurseIds: roomNurseAssignment(RoomIds: r, Days: d, ShiftTypes: s) = 
  let { array[int] of NurseIds: result = [ n | n in NurseIds where fix(isNurseInRoom[n,r,d,s])] } in
  if length(result) == 0 then <> else result[1] endif;