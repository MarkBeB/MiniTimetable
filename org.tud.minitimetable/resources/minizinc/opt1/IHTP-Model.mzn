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
int: numberOfPatients = length(patientData);
%deprecated
int: numberOfOccupants = length(occupantData);

array[NurseIds] of Nurse: nurseData;
array[SurgeonIds] of Surgeon: surgeonData;
array[PatientIds] of Patient: patientData;
array[OccupantIds] of Occupant: occupantData;
array[OperatingTheaterIds] of OperatingTheater: operatingTheaterData;
array[RoomIds] of Room: roomData;

array[NurseIds] of Nurse: nurses = nurseData;
array[SurgeonIds] of Surgeon: surgeons = surgeonData;
array[PatientIds] of Patient: patients = patientData;
array[OccupantIds] of Occupant: occupants = occupantData;
array[OperatingTheaterIds] of OperatingTheater: operatingTheaters = operatingTheaterData;
array[RoomIds] of Room: rooms = roomData;

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%
% DECISION VARIABLES
%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

array[PatientIds, ExtendedDays] of var bool: isPatientAdmittedAt;
array[PatientIds, Days, RoomIds] of var bool: isPatientInRoom;
array[PatientIds, OperatingTheaterIds, Days] of var bool: isPatientInOT;
array[PatientIds, Days] of var bool: isPatientPresent;
array[PatientIds] of var bool: isPatientPostponed;
array[PatientIds] of var opt RoomIds: patientRoomAssignment;
array[PatientIds] of var opt OperatingTheaterIds: patientOTAssignment;
array[PatientIds] of var ExtendedDays: patientAdmissionDay;
array[PatientIds] of var MaxDays: patientAdmittedUntil;
array[PatientIds, Days, NurseIds] of var bool: patientNurseAssignment;
array[PatientIds, NurseIds] of var bool: patientCaredByNurse;
array[OccupantIds, NurseIds] of var bool: occupantCaredByNurse;
array[PatientIds, Days, ShiftTypes] of var PenaltyRange: patientSkillDifference;
array[OccupantIds, Days, ShiftTypes] of var PenaltyRange: occupantSkillDifference;

array[NurseIds, Days, ShiftTypes, RoomIds] of var bool: isNurseInRoom;
array[NurseIds, Days, ShiftTypes] of var 0..1000: nurseWorkload;
array[SurgeonIds, Days, OperatingTheaterIds] of var bool: surgeonUsesOT;
array[SurgeonIds, Days] of var DayTime: surgeonSurgeryTime;

array[OperatingTheaterIds, Days] of var DayTime: otSurgeryTime;

array[RoomIds, Days] of var bool: isRoomOccupied;
array[RoomIds, Days] of var Genders: roomGender;
array[RoomIds, Days, ShiftTypes] of var 0..1000: roomWorkload;
array[RoomIds, Days, ShiftTypes] of var opt NurseIds: roomNurseAssignment;
array[RoomIds, Days] of var AgeGroups: roomMaxAge;
array[RoomIds, Days] of var AgeGroups: roomMinAge;
array[RoomIds, Days] of var 0..card(AgeGroups): roomAgeDifference;

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%
% UTILITY FUNCTIONS
%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% Types with many arguments can lead to segmentation faults if used as function arguments 


% DYNAMIC EXPRESSION

function var bool: isPatientInRoom(PatientIds: p, Days: d, RoomIds: r) = 
  isPatientInRoom[p, d, r];

function var bool: isNurseInRoom(NurseIds: n, Days: d, ShiftTypes: s, RoomIds: r) = 
  isNurseInRoom[n, d, s, r];

function var bool: isPatientPresent(PatientIds: p, Days: d) = 
  patientAdmissionDay[p] <= d /\ d <= patientAdmittedUntil[p];
  
function var bool: isPatientInOT(PatientIds: p, OperatingTheaterIds: ot, Days: d) =
  isPatientInOT[p,ot,d];

function var bool: isPatientPostponed(PatientIds: p) =
  isPatientPostponed[p];
  
function var bool: isPatientAdmitted(PatientIds: p) = 
  not isPatientPostponed[p];

function var bool: isRoomOccupied(RoomIds: r, Days: d) = 
  isRoomOccupied[r,d];

function var int: patientDailyIndex(PatientIds: id, Days: day, ShiftTypes: shift) =
  enum2int(shift) - 1 + (enum2int(day) - patientAdmissionDay[id]) * length(ShiftTypes);

% CONSTANT EXPRESSIONS

function bool: isOccupantInRoom(OccupantIds: o, Days: d,  RoomIds: r) =
  isOccupantInRoom(occupantData[o], d, r);

function bool: isOccupantInRoom(Occupant: o, Days: d, RoomIds: r) = 
  (o.assignedRoom == r) /\ (d < o.lengthOfStay);

function bool: isOccupantPresent(OccupantIds: o, Days: d) = 
  d < occupants[o].lengthOfStay;

function bool: isNurseWorking(NurseIds: n, Days: d, ShiftTypes: s) =
  nurses[n].maxWorkloadPerShift[d, s] > 0;

function bool: isOTAvailable(OperatingTheaterIds: ot, Days: d) = 
  operatingTheaters[ot].availability[d] > 0;
  
function int: getOTAvailability(OperatingTheaterIds: ot, Days: d) =
  operatingTheaters[ot].availability[d];
  
function bool: isSurgeonAvailable(SurgeonIds: s, Days: d) = 
  surgeons[s].maxSurgeryTime[d] > 0;

function bool: isPatientMandatory(PatientIds: p) = 
  patients[p].mandatory;
  
function bool: isPatientOptional(PatientIds: p) = 
  not patients[p].mandatory;
  
function bool: hasPatientSurgeon(PatientIds: p, SurgeonIds: s) =
  patients[p].assignedSurgeon == s;
  
function int: maxSurgeryTime(SurgeonIds: s, Days: d) =
  surgeons[s].maxSurgeryTime[d];
  
function int: surgeryDuration(PatientIds: p) =
  patients[p].surgeryDuration;
  
function SurgeonIds: assignedSurgeon(PatientIds: p) =
  patients[p].assignedSurgeon;
  
function int: roomCapacity(RoomIds: r) =
  rooms[r].capacity;
  
function AgeGroups: patientAgeGroup(PatientIds: p) =
  patients[p].ageGroup;
  
function AgeGroups: occupantAgeGroup(OccupantIds: o) =
  occupants[o].ageGroup;
  
function set of RoomIds: incompatibleRooms(PatientIds: p) =
  array2set(patients[p].incompatibleRooms);

function int: nurseSkillLevel(NurseIds: n) =
  nurses[n].skillLevel;
  
function int: occupantDailyIndex(Days: day, ShiftTypes: shift) = 
  enum2int(shift) - 1 + enum2int(day) * length(ShiftTypes);