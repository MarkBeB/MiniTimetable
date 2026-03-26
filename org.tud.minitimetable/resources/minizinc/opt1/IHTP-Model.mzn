%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%
% CONSTANTS
%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

int: minutesPerDay = 24 * 60;
int: maxPenalityValue = 1000000;
int: maxScoreValue = 100000000;

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%
% TYPES
%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

type DayTime = 0..minutesPerDay;
type PenaltyRange = 0..maxPenalityValue;

type WeightType = record(
  int: roomMixedAge,
  int: roomNurseSkill,
  int: continuityOfCare,
  int: nurseEccessiveWorkload,
  int: openOperatingTheater,
  int: surgeonTransfer,
  int: patientDelay,
  int: unscheduledOptional,
);


type PenalityType = record(
  var PenaltyRange: roomMixedAge,
  var PenaltyRange: roomNurseSkill,
  var PenaltyRange: continuityOfCare,
  var PenaltyRange: nurseEccessiveWorkload,
  var PenaltyRange: openOperatingTheater,
  var PenaltyRange: surgeonTransfer,
  var PenaltyRange: patientDelay,
  var PenaltyRange: unscheduledOptional,
);


type NurseFixData = record(
  NurseIds: id,
  SkillLevels: skillLevel,
  array[Days, ShiftTypes] of int: maxWorkloadPerShift,
);
type NurseVarData = record(
  array[Days, ShiftTypes, RoomIds] of var bool: assignedRooms,
  array[Days, ShiftTypes] of var int: expectedWorkload,

  array[Days, ShiftTypes] of var 0..maxPenalityValue: penaltySkillLevel,
);
type Nurse = NurseFixData ++ NurseVarData;


type SurgeonFixData = record(
  SurgeonIds: id,
  array[Days] of DayTime: maxSurgeryTime,
);
type SurgeonVarData = record(
  array[Days] of var DayTime: usedTime,
  array[Days, OperatingTheaterIds] of var bool: worksInOT,
);
type Surgeon = SurgeonFixData ++ SurgeonVarData;


type OperatingTheaterFixData = record(
  OperatingTheaterIds: id,
  array[Days] of DayTime: availability,
  array[Days] of bool: isAvailable,
);
type OperatingTheaterVarData = record(  
  array[Days] of var DayTime: usedTime,
);
type OperatingTheater = OperatingTheaterFixData ++ OperatingTheaterVarData;


type RoomFixData = record(
  RoomIds: id,
  int: capacity,
  array[Days] of int: assignedOccupants,
);
type RoomVarData = record(
  array[Days] of var bool: isOccupied, % utility constraint
  array[Days] of var 0..100: assignedPatients,  
  array[Days] of var Genders: assignedGender,
  array[Days, ShiftTypes] of var int: workload,
  array[Days, ShiftTypes] of var opt NurseIds: assignedNurse,

  array[Days] of var 0..length(AgeGroups): penaltyAgeMixing,
);
type Room = RoomFixData ++ RoomVarData;


type OccupantFixData = record (
  OccupantIds: id,
  Genders: gender,
  AgeGroups: ageGroup,
  int: lengthOfStay,
  array[int] of int: workloadProduced, %length = lengthOfStay * length(ShiftType)
  array[int] of SkillLevels: skillLevelRequired, %length = lengthOfStay * length(ShiftType)
  RoomIds: assignedRoom,
);
type OccupantVarData = record (
  array[NurseIds] of var bool: caredByNurse
);
type Occupant = OccupantFixData ++ OccupantVarData;


type PatientFixData = record (
  PatientIds: id,
  bool: mandatory,
  Genders: gender,
  AgeGroups: ageGroup,
  int: lengthOfStay,
  Days: earliestPossibleAdmission,
  Days: latestPossibleAdmission,
  int: surgeryDuration,
  SurgeonIds: assignedSurgeon,
  array[int] of RoomIds: incompatibleRooms,
  array[int] of int: workloadProduced,
  array[int] of SkillLevels: skillLevelRequired,
);

array[PatientIds, Days, RoomIds] of var bool: patientRoomAssignment;
array[PatientIds, Days, NurseIds] of var bool: patientNurseAssignment;

type PatientVarData = record (
  var MaxDays: admissionDay,
  var MaxDays: admittedUntil,
  var bool: isPostponed, 
  var bool: isAdmitted, % isPostponed <-> !isAdmited

  var opt OperatingTheaterIds: assignedOT,
  array[NurseIds] of var bool: caredByNurse,
  
  var MaxDays: penaltyAdmissionDelay,
);
type Patient = PatientFixData ++ PatientVarData;

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%
% DATA ENTRIES
%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

WeightType: Weights;
var PenalityType: Penalties;
enum Genders;
enum ShiftTypes;
enum AgeGroups;
enum NurseIds;
enum SurgeonIds;
enum OperatingTheaterIds;
enum RoomIds;
enum PatientIds;
enum OccupantIds;

int: maxSkillLevel;
set of int: SkillLevels = 0..maxSkillLevel;

int: firstDay = 0;
int: lastDay;
int: postponedDay = lastDay + 1; 
int: maxDay;
set of int: Days = 0..lastDay;
set of int: ExtendedDays = 0..postponedDay;
set of int: MaxDays = 0..maxDay;
% enum Days = _(0..lastDay);


var 0..maxScoreValue: score = 
    Penalties.roomMixedAge
  + Penalties.roomNurseSkill
  + Penalties.continuityOfCare
  + Penalties.nurseEccessiveWorkload
  + Penalties.openOperatingTheater
  + Penalties.surgeonTransfer
  + Penalties.patientDelay
  + Penalties.unscheduledOptional;


%deprecated
enum RoomIdOrNotAssigned = {RoomNotAssigned} ++ C_room(RoomIds);
%deprecated
enum GenderOrNotAssigned = {GenderNotAssigned} ++ C_gender(Genders);


array[NurseIds] of NurseFixData: nurseData;
array[SurgeonIds] of SurgeonFixData: surgeonData;
array[PatientIds] of PatientFixData: patientData;
array[OccupantIds] of OccupantFixData: occupantData;
array[OperatingTheaterIds] of OperatingTheaterFixData: operatingTheaterData;
array[RoomIds] of RoomFixData: roomData;


array[NurseIds] of NurseVarData: nurseVar;
array[SurgeonIds] of SurgeonVarData: surgeonVar;
array[PatientIds] of PatientVarData: patientVar;
array[OccupantIds] of OccupantVarData: occupantVar;
array[OperatingTheaterIds] of OperatingTheaterVarData: operatingTheaterVar;
array[RoomIds] of RoomVarData: roomVar;

%deprecated
int: numberOfPatients = length(patientData);
%deprecated
int: numberOfOccupants = length(occupantData);

% Usage may cause memory segmentation fault since version 2.9.5, was fine before. Seems to happen when a combined Type has 'too many' fields.
%
%array[NurseIds] of Nurse: nurses ::output = [id: nurseData[id] ++ nurseVar[id] | id in NurseIds];
%array[SurgeonIds] of Surgeon: surgeons ::output = [id: surgeonData[id] ++ surgeonVar[id] | id in SurgeonIds];
%array[PatientIds] of Patient: patients ::output = [id: patientData[id] ++ patientVar[id] | id in PatientIds]; % <-- has too many fields ?!?
%array[OccupantIds] of Occupant: occupants ::output = [id: occupantData[id] | id in OccupantIds];
%array[OperatingTheaterIds] of OperatingTheater: operatingTheaters ::output = [id: operatingTheaterData[id] ++ operatingTheaterVar[id] | id in OperatingTheaterIds];
%array[RoomIds] of Room: rooms ::output = [id: roomData[id] ++ roomVar[id] | id in RoomIds];

array[NurseIds] of NurseFixData: nurses = nurseData;
array[SurgeonIds] of SurgeonFixData: surgeons = surgeonData;
array[PatientIds] of PatientFixData: patients = patientData;
array[OccupantIds] of OccupantFixData: occupants = occupantData;
array[OperatingTheaterIds] of OperatingTheaterFixData: operatingTheaters = operatingTheaterData;
array[RoomIds] of RoomFixData: rooms = roomData;

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%
% UTILITY FUNCTIONS
%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

function var bool: isPatientInRoom(PatientIds: p, Days: d, RoomIds: r) = 
  patientRoomAssignment[p, d, r];

function bool: isOccupantInRoom(OccupantIds: o, Days: d, RoomIds: r) =
  isOccupantInRoom(occupantData[o], d, r);

function bool: isOccupantInRoom(OccupantFixData: o, Days: d, RoomIds: r) = 
  (o.assignedRoom == r) /\ (d < o.lengthOfStay);

function bool: isOccupantPresent(OccupantIds: o, Days: d) = 
  d < occupantData[o].lengthOfStay;

function var bool: isPatientPresent(PatientIds: p, Days: d) = 
  patientVar[p].admissionDay <= d /\ d <= patientVar[p].admittedUntil;

function bool: isWorking(NurseIds: n, Days: d, ShiftTypes: s) ::cache_result =
  nurseData[n].maxWorkloadPerShift[d, s] > 0; 

/*
constraint forall(nurseId in NurseIds, day in Days, shift in ShiftTypes)(
  nurseVar[nurseId].isWorking[day, shift] = exists(workingShift in nurseData[nurseId].workingShifts)(
      workingShift.day == day /\ workingShift.shift == shift)
);

constraint forall(nurseId in NurseIds, day in Days, shift in ShiftTypes)(
  nurseVar[nurseId].isWorking[day, shift] -> exists(workingShift in nurseData[nurseId].workingShifts)(
      workingShift.day == day /\ workingShift.shift == shift 
      /\ nurseVar[nurseId].maxLoad[day, shift] = workingShift.maxLoad
    )
);
*/

function var int: indexOf(PatientIds: id, Days: day, ShiftTypes: shift) =
  enum2int(shift) - 1 + (enum2int(day) - patientVar[id].admissionDay) * length(ShiftTypes);

function int: indexOf(Days: day, ShiftTypes: shift) =
  enum2int(shift) - 1 + enum2int(day) * length(ShiftTypes);

% include "OutConsoleFormat.mzn";
% include "OutJsonFormat.mzn";