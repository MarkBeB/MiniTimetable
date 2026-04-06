%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%
% CONSTANTS
%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

int: minutesPerDay = 24 * 60;
int: minPenaltyValue = 0;
int: maxPenaltyValue = 10^8;
int: minScoreValue = 0;
int: maxScoreValue = 10^8;

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%
% TYPES
%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

type DayTime = 0..minutesPerDay;
type PenaltyRange = minPenaltyValue..maxPenaltyValue;
type ScoreRange = minScoreValue..maxScoreValue;

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

type Nurse = record(
  NurseIds: id,
  SkillLevels: skillLevel,
  array[Days, ShiftTypes] of int: maxWorkloadPerShift
);

type Surgeon = record(
  SurgeonIds: id,
  array[Days] of DayTime: maxSurgeryTime
);

type OperatingTheater = record(
  OperatingTheaterIds: id,
  array[Days] of DayTime: availability
);

type Room = record(
  RoomIds: id,
  int: capacity
);

type Occupant = record (
  OccupantIds: id,
  Genders: gender,
  AgeGroups: ageGroup,
  int: lengthOfStay,
  array[int] of int: workloadProduced, %length = lengthOfStay * length(ShiftType)
  array[int] of SkillLevels: skillLevelRequired, %length = lengthOfStay * length(ShiftType)
  RoomIds: assignedRoom
);

type Patient = record (
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
  array[int] of SkillLevels: skillLevelRequired
);



%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%
% DATA ENTRIES
%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

WeightType: Weights;

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
int: extendedDay = lastDay + 1; 
int: maxDay;
set of int: Days = 0..lastDay;
set of int: ExtendedDays = 0..extendedDay;
set of int: MaxDays = 0..maxDay;

array[NurseIds] of Nurse: nurseData;
array[SurgeonIds] of Surgeon: surgeonData;
array[PatientIds] of Patient: patientData;
array[OccupantIds] of Occupant: occupantData;
array[OperatingTheaterIds] of OperatingTheater: operatingTheaterData;
array[RoomIds] of Room: roomData;


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%
% OBJECTIVE
%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

var PenaltyRange: penaltyRoomMixedAge;
var PenaltyRange: penaltyRoomNurseSkill;
var PenaltyRange: penaltyContinuityOfCare;
var PenaltyRange: penaltyNurseEccessiveWorkload;
var PenaltyRange: penaltyOpenOperatingTheater;
var PenaltyRange: penaltySurgeonTransfer;
var PenaltyRange: penaltyPatientDelay;
var PenaltyRange: penaltyUnscheduledOptional;

var int: score = 
    penaltyRoomMixedAge
  + penaltyRoomNurseSkill
  + penaltyContinuityOfCare
  + penaltyNurseEccessiveWorkload
  + penaltyOpenOperatingTheater
  + penaltySurgeonTransfer
  + penaltyPatientDelay
  + penaltyUnscheduledOptional;

constraint score >= 0;
constraint penaltyRoomMixedAge >= 0;
constraint penaltyRoomNurseSkill >= 0;
constraint penaltyContinuityOfCare >= 0;
constraint penaltyNurseEccessiveWorkload >= 0;
constraint penaltyOpenOperatingTheater >= 0;
constraint penaltySurgeonTransfer >= 0;
constraint penaltyPatientDelay >= 0;
constraint penaltyUnscheduledOptional >= 0;
