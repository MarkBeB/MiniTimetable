include "Parameters.mzn";
include "ParFunctions.mzn";

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%
% OBJECTIVE
%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

type Violation = 0..maxViolationsPerSoftConstraint;

var Violation: violationsRoomMixedAge;
var Violation: violationsRoomNurseSkill;
var Violation: violationsContinuityOfCare;
var Violation: violationsNurseEccessiveWorkload;
var Violation: violationsOpenOperatingTheater;
var Violation: violationsSurgeonTransfer;
var Violation: violationsPatientDelay;
var Violation: violationsUnscheduledOptional;

var int: score = 
    Weights.roomMixedAge           * violationsRoomMixedAge
  + Weights.roomNurseSkill         * violationsRoomNurseSkill
  + Weights.continuityOfCare       * violationsContinuityOfCare
  + Weights.nurseEccessiveWorkload * violationsNurseEccessiveWorkload
  + Weights.openOperatingTheater   * violationsOpenOperatingTheater
  + Weights.surgeonTransfer        * violationsSurgeonTransfer
  + Weights.patientDelay           * violationsPatientDelay
  + Weights.unscheduledOptional    * violationsUnscheduledOptional;

constraint score >= 0;

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%
% CORE DECISIONS
%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

array[PatientIds, ExtendedDays] of var bool: isPatientAdmittedAt;
array[PatientIds, OperatingTheaterIds] of var bool: isPatientInOT;
array[PatientIds, RoomIds] of var bool: isPatientInRoom;
array[NurseIds, RoomIds, Days, ShiftTypes] of var bool: isNurseInRoom;

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%
% HELPERS
%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% Set in H5, also used in AH2 and AH3
array[PatientIds] of var bool: isPatientAdmitted;

array[PatientIds] of var ExtendedDays: patientAdmissionDay;
constraint forall(p in PatientIds, d in ExtendedDays)(
  isPatientAdmittedAt[p,d] <-> (patientAdmissionDay[p]==d)
  :: "Var isPatientAdmittedAt \(p) \(d)"
);

array[PatientIds] of var opt OperatingTheaterIds: patientOT;
constraint forall(p in PatientIds, ot in OperatingTheaterIds)(
  isPatientInOT[p,ot] <-> (patientOT[p]==ot)
  :: "Var isPatientInOT \(p) \(ot)"
);

array[PatientIds] of var opt RoomIds: patientRoom;
constraint forall(p in PatientIds, r in RoomIds)(
  isPatientInRoom[p,r] <-> (patientRoom[p]==r)
);

array[PatientIds, Days] of var bool: isPatientPresent = [(p,d):
  (patientAdmissionDay[p] <= d) /\ (d < patientAdmissionDay[p] + patientLengthOfStay(p))
  | p in PatientIds, d in Days
];

array[PatientIds, RoomIds, Days] of var bool: isPatientPresentInRoom = [(p,r,d):
  isPatientPresent[p,d] /\ isPatientInRoom[p,r]
  | p in PatientIds, r in RoomIds, d in Days
];

% still used?
%array[RoomIds, Days, ShiftTypes] of var opt NurseIds: roomNurse;
