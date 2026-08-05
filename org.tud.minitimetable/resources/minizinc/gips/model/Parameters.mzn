%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%
% CONSTANTS
%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

int: minutesPerDay = 24 * 60;

% Upper limit required for Big-M
int: maxViolationsPerSoftConstraint = 10^6;

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%
% TYPES
%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

type DayTime = 0..minutesPerDay;

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
  array[int] of int: workloadProduced,
  array[int] of SkillLevels: skillLevelRequired,
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
  set of RoomIds: incompatibleRooms,
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

int: firstDay = 0; % first day of the planning horizon
int: lastDay; % last day of the planning horizon
int: extendedDay = lastDay + 1; % used for postponed (first day of the 'next' planning horizon) 
int: maxDay; % lastDay + the longest stay. Used to avoid out-of-index accesses.
set of int: Days = 0..lastDay;
set of int: ExtendedDays = 0..extendedDay;
set of int: MaxDays = 0..maxDay;

array[NurseIds] of Nurse: nurses;
array[SurgeonIds] of Surgeon: surgeons;
array[PatientIds] of Patient: patients;
array[OccupantIds] of Occupant: occupants;
array[OperatingTheaterIds] of OperatingTheater: operatingTheaters;
array[RoomIds] of Room: rooms;

int: totalIndividuals = length(PatientIds) + length(OccupantIds);
