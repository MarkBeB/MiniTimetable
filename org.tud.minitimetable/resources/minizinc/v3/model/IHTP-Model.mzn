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
%  array[Days, ShiftTypes] of var bool: isWorking,
  array[Days, ShiftTypes, RoomIds] of var bool: assignedRooms,
  array[Days, ShiftTypes] of var int: expectedWorkload,
  array[Days, ShiftTypes] of var PenaltyRange: penaltySkillLevel,
   var bool: empty
);
type Nurse = NurseFixData ++ NurseVarData;


type SurgeonFixData = record(
  SurgeonIds: id,
  array[Days] of DayTime: maxSurgeryTime,
);
type SurgeonVarData = record(
  array[Days] of var DayTime: usedTime,
  array[Days, OperatingTheaterIds] of var bool: worksInOT,
  var bool: empty
);
type Surgeon = SurgeonFixData ++ SurgeonVarData;


type OperatingTheaterFixData = record(
  OperatingTheaterIds: id,
  array[Days] of DayTime: availability,
);
type OperatingTheaterVarData = record(  
  array[Days] of var DayTime: usedTime,
  var bool: empty
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
%  array[Days] of var Genders: assignedGender,
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
type PatientVarData = record (
  var MaxDays: admissionDay,
  var MaxDays: admittedUntil,
  var bool: isPostponed, 
  var bool: isAdmitted, % isPostponed <-> !isAdmited

  var opt OperatingTheaterIds: assignedOT,
  array[Days] of var opt RoomIds: assignedRoom,
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
%array[SurgeonIds] of SurgeonVarData: surgeonVar;
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


function var bool: patientIsInRoom(PatientIds: id, Days: day, RoomIds: roomId) = 
  patientVar[id].assignedRoom[day] == roomId;

function var bool: patientIsPresent(PatientIds: id, Days: day) = 
  patientVar[id].admissionDay <= day /\ day <= patientVar[id].admittedUntil;

function var bool: isPatientInRoom(PatientIds: p, Days: d, RoomIds: r) = 
  patientIsInRoom(p, d, r);

function bool: isInRoom(OccupantFixData: occupant, RoomFixData: room, Days: day) =
  occupant.assignedRoom == room.id /\ day < occupant.lengthOfStay;

function var bool: isInRoom(PatientFixData: patient, RoomFixData: room, var Days: day) =
  patientVar[patient.id].assignedRoom[day] == room.id;

function bool: isPresent(OccupantFixData: occupant, Days: day) =
  day < occupant.lengthOfStay;

function var bool: isPresent(PatientFixData: patient, Days: day) =
  patientVar[patient.id].admissionDay <= day /\ day <= patientVar[patient.id].admittedUntil;

function bool: isWorking(NurseFixData: nurse, Days: day, ShiftTypes: shift) =
  nurse.maxWorkloadPerShift[day, shift] > 0;

array[RoomIds, Days] of bool: roomHasFixedOccupants = [(room.id, day): exists(occupant in occupants)(isInRoom(occupant, room, day)) | room in rooms, day in Days ];
function var bool: isOccupied(RoomFixData: room, Days: day) =
  roomHasFixedOccupants[room.id, day] \/ exists(patient in patients)(isInRoom(patient, room, day));

function var bool: isPostponed(PatientFixData: patient) = patientVar[patient.id].isPostponed;
function var bool: isAdmitted(PatientFixData: patient) = not patientVar[patient.id].isPostponed;

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


function bool: isNurseWorking(NurseIds: n, Days: d, ShiftTypes: s) =
  nurseData[n].maxWorkloadPerShift[d, s] > 0; 