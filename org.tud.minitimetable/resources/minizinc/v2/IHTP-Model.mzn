%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%
% CONSTANTS
%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

int: minutesPerDay = 24 * 60;
int: minPenaltyValue = 0;
int: maxPenaltyValue = 10^4;
int: minScoreValue = 0;
int: maxScoreValue = 10^5;

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%
% TYPES
%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

type DayTime = 0..minutesPerDay;
type PenaltyRange = minPenaltyValue..maxPenaltyValue;
type ScoreRange = minScoreValue..maxScoreValue;
type PosInteger = 0..10^8;

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

type Nurse = record(
  NurseIds: id,
  SkillLevels: skillLevel,
  array[Days, ShiftTypes] of int: maxWorkloadPerShift,
);

type Surgeon = record(
  SurgeonIds: id,
  array[Days] of DayTime: maxSurgeryTime,
);

type OperatingTheater = record(
  OperatingTheaterIds: id,
  array[Days] of DayTime: availability,
);

type Room = record(
  RoomIds: id,
  int: capacity,
  array[Days] of int: assignedOccupants,
);

type Occupant = record (
  OccupantIds: id,
  Genders: gender,
  AgeGroups: ageGroup,
  int: lengthOfStay,
  array[int] of int: workloadProduced, %length = lengthOfStay * length(ShiftType)
  array[int] of SkillLevels: skillLevelRequired, %length = lengthOfStay * length(ShiftType)
  RoomIds: assignedRoom,
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
  array[int] of SkillLevels: skillLevelRequired,
);



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

% objective function, will be minimised
var ScoreRange: score = 
    Penalties.roomMixedAge
  + Penalties.roomNurseSkill
  + Penalties.continuityOfCare
  + Penalties.nurseEccessiveWorkload
  + Penalties.openOperatingTheater
  + Penalties.surgeonTransfer
  + Penalties.patientDelay
  + Penalties.unscheduledOptional;

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%
% DECISION VARIABLES
%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% These variables are used to decide in which room a patient is.
%array[PatientIds, Days, RoomIds] of var bool: isPatientInRoom; % true if patient p is at day d in room r

array[PatientIds, RoomIds] of var bool: isPatientInRoom;
array[PatientIds] of var opt RoomIds: patientRoomAssignment; % set to room r if patient p is admitted
% link arrays
constraint forall(p in PatientIds, r in RoomIds)(
  isPatientInRoom[p,r] == (patientRoomAssignment[p] == r)
);


array[PatientIds] of var ExtendedDays: patientAdmissionDay;
array[PatientIds, Days] of var bool: isPatientPresent;
% link arrays
constraint forall(p in PatientIds, d in Days)(
  isPatientPresent[p,d] == (patientAdmissionDay[p] <= d /\ d <= patientAdmissionDay[p] + patientLengthOfStay(p))
);

array[PatientIds, ExtendedDays] of var bool: isPatientAdmittedAt;
% link arrays
constraint forall(p in PatientIds, d in ExtendedDays)(
  isPatientAdmittedAt[p,d] == (patientAdmissionDay[p] == d)
);

array[PatientIds] of var bool: isPatientAdmitted;
array[PatientIds] of var bool: isPatientPostponed = [ not isPatientAdmitted[p] | p in PatientIds ];
constraint forall(p in PatientIds)(
  sum(d in Days)(isPatientAdmittedAt[p,d]) == isPatientAdmitted[p]
);

array[PatientIds, OperatingTheaterIds] of var bool: isPatientInOT;

% ROOM




%array[RoomIds, Days] of var 0..1000: roomUtilization;
%array[RoomIds, Days] of var 0..1000: roomWorkload;

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%
% CONSTANT FUNCTIONS
%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% these functions are used to access parameters. It looks cleaner.

function bool: isOccupantInRoom(OccupantIds: o, Days: d, RoomIds: r) = 
  (occupantData[o].assignedRoom == r) /\ (d < occupantData[o].lengthOfStay);

function bool: isOccupantPresent(OccupantIds: id, Days: day) = 
  day < occupantData[id].lengthOfStay;

function AgeGroups: occupantAgeGroup(OccupantIds: o) =
  occupantData[o].ageGroup;

function Genders: occupantGender(OccupantIds: o) =
  occupantData[o].gender;

function int: requiredOccupantSkill(OccupantIds: o, Days: d, ShiftTypes: s) =
  occupantData[o].skillLevelRequired[occupantDailyIndex(d,s)];

function int: occupantDailyIndex(Days: d, ShiftTypes: s) =
  enum2int(s) - 1 + enum2int(d) * length(ShiftTypes);


function bool: patientOfSurgeon(PatientIds: p, SurgeonIds: s) =
  assignedSurgeon(p) == s;

function SurgeonIds: assignedSurgeon(PatientIds: p) =
  patientData[p].assignedSurgeon;

function int: surgeryDuration(PatientIds: p) =
  patientData[p].surgeryDuration;

function AgeGroups: patientAgeGroup(PatientIds: p) =
  patientData[p].ageGroup;

function Genders: patientGender(PatientIds: p) =
  patientData[p].gender;

function int: patientLengthOfStay(PatientIds: p) =
  patientData[p].lengthOfStay;

function set of RoomIds: incompatibleRooms(PatientIds: p) =
  array2set(patientData[p].incompatibleRooms);

function set of RoomIds: compatibleRooms(PatientIds: p) =
  RoomIds diff incompatibleRooms(p);

function bool: isPatientMandatory(PatientIds: p) = 
  patientData[p].mandatory;

function bool: isPatientOptional(PatientIds: p) = 
  not patientData[p].mandatory;
  
function var int: requiredPatientSkill(PatientIds: p, Days: d, ShiftTypes: s) =
  patientData[p].skillLevelRequired[patientDailyIndex(p,d,s)];

function var int: patientDailyIndex(PatientIds: p, Days: d, ShiftTypes: s) =
  enum2int(s) - 1 + (enum2int(d) - patientAdmissionDay[p]) * length(ShiftTypes);




function int: nurseWorkloadCapacity(NurseIds: n, Days: d, ShiftTypes: s) =
  nurseData[n].maxWorkloadPerShift[d, s];

function int: nurseSkillLevel(NurseIds: n) =
  nurseData[n].skillLevel;




function bool: isSurgeonAvailable(SurgeonIds: s, Days: d) = 
  surgeonSurgeryCapacity(s,d) > 0;

function int: surgeonSurgeryCapacity(SurgeonIds: s, Days: d) = 
  surgeonData[s].maxSurgeryTime[d];

function bool: isNurseAvailable(NurseIds: n, Days: d, ShiftTypes: s) =
  nurseWorkloadCapacity(n,d,s) > 0;  




function bool: isOTAvailable(OperatingTheaterIds: ot, Days: d) = 
  otSurgeryCapacity(ot,d) > 0;

function int: otSurgeryCapacity(OperatingTheaterIds: ot, Days: d) = 
  operatingTheaterData[ot].availability[d];




function int: roomCapacity(RoomIds: r) =
  roomData[r].capacity;
 
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%
% HELPER FUNCTIONS - REPLACE WITH HELPER VARIABLES TO REDUCE MODEL SIZE
%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
 
predicate isPatientInHospital(PatientIds: p) = false