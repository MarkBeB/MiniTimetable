include "Parameters.mzn";
include "ParFunctions.mzn";


array[PatientIds] of var bool: isPatientAdmitted;
array[PatientIds] of var ExtendedDays: patientAdmissionDay;
array[PatientIds, ExtendedDays] of var bool: isPatientAdmittedAt;
constraint forall(p in PatientIds, d in ExtendedDays)(
  isPatientAdmittedAt[p,d] -> (patientAdmissionDay[p]==d)
  :: "Var isPatientAdmittedAt \(p) \(d)"
);
constraint forall(p in PatientIds) (
  sum(d in ExtendedDays)(isPatientAdmittedAt[p, d]) == 1
  :: "Con isPatientAdmittedAt \(p) == 1"
);


array[PatientIds] of var opt OperatingTheaterIds: patientOT;
array[PatientIds, OperatingTheaterIds] of var bool: isPatientInOT;
constraint forall(p in PatientIds, ot in OperatingTheaterIds)(
  isPatientInOT[p,ot] <-> (patientOT[p]==ot)
  :: "Var isPatientInOT \(p) \(ot)"
);
%constraint forall(p in PatientIds) (
%    sum(ot in OperatingTheaterIds)(isPatientInOT[p, ot]) <= 1
%);

array[PatientIds] of var opt RoomIds: patientRoom;
array[PatientIds, RoomIds] of var bool: isPatientInRoom;
constraint forall(p in PatientIds, r in RoomIds)(
  isPatientInRoom[p,r] <-> (patientRoom[p]==r)
);

array[PatientIds, ExtendedDays] of var bool: isPatientPresent;
constraint forall(p in PatientIds, d in Days) (
  (isPatientPresent[p, d] <-> (
       (patientAdmissionDay[p] <= d)
    /\ (d < patientAdmissionDay[p] + patientLengthOfStay(p))
  )) :: "Var isPatientPresent \(p) \(d)"
);
/*
constraint forall(p in PatientIds, d in Days) (
  (isPatientPresent[p, d] >= 
       isPatientAdmitted[p] 
    + (patientAdmissionDay[p] <= d) 
    + (d < patientAdmissionDay[p] + patientLengthOfStay(p))
    - 2  
  ) :: "Var isPatientPresent \(p) \(d)"
);
*/

array[PatientIds, RoomIds, Days] of var bool: isPatientPresentInRoom = [(p,r,d):
  isPatientPresent[p,d] /\ isPatientInRoom[p,r]
  | p in PatientIds, r in RoomIds, d in Days
];

array[NurseIds, RoomIds, Days, ShiftTypes] of var bool: isNurseInRoom;

array[RoomIds, Days, ShiftTypes] of var opt NurseIds: roomNurse;



%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%
% UTILITY CONSTRAINTS
%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

array[Days, ShiftTypes] of set of NurseIds: activeNurses = [(d,s): availableNurses(d,s) | d in Days, s in ShiftTypes ];
array[PatientIds] of set of ExtendedDays: validAdmissionDays = [ validAdmissionDays(p) | p in PatientIds ];

%array[Days, ShiftTypes] of set of NurseIds: activeNurses = [(d,s):
%    { n | n in NurseIds where isNurseAvailable(n,d,s) }
%    | d in Days, s in ShiftTypes
%];



%array[PatientIds] of set of int: validAdmissionDays = [
%  { extendedDay } % postponed
%  union
%  { d | d in earliestAdmissionDay(p)..latestAdmissionDay(p) where isPatientOpAvailable(p, d) }
%  | p in PatientIds
%];


