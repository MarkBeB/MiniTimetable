include "Parameters.mzn";

array[PatientIds, ExtendedDays] of var bool: isPatientAdmittedAt;
array[PatientIds, OperatingTheaterIds] of var bool: isPatientInOT;
array[PatientIds, RoomIds] of var bool: isPatientInRoom;
array[NurseIds, RoomIds, Days, ShiftTypes] of var bool: isNurseInRoom;

constraint forall(p in PatientIds) (
  sum(d in ExtendedDays)(isPatientAdmittedAt[p, d]) == 1
  :: "Con isPatientAdmittedAt \(p) == 1"
);

%constraint forall(p in PatientIds) (
%    sum(ot in OperatingTheaterIds)(isPatientInOT[p, ot]) <= 1
%);



