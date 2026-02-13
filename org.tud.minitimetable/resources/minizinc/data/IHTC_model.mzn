%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%
% CONSTANTS
%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

int: minutesPerDay = 24 * 60;
int: maxPenalityValue = 1000000;

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


type PenalityType = record(
  var 0..maxPenalityValue: roomMixedAge,
  var 0..maxPenalityValue: roomNurseSkill,
  var 0..maxPenalityValue: continuityOfCare,
  var 0..maxPenalityValue: nurseEccessiveWorkload,
  var 0..maxPenalityValue: openOperatingTheater,
  var 0..maxPenalityValue: surgeonTransfer,
  var 0..maxPenalityValue: patientDelay,
  var 0..maxPenalityValue: unscheduledOptional,
);


type WorkShift = record(
  Days: day,
  ShiftTypes: shift,
  int: maxLoad,
);


type NurseAssignment = record(
  var Days: day,
  var ShiftTypes: shift,
  array[RoomIds] of var bool: assignedToRooms,
);


type NurseFixData = record(
  NurseIds: id,
  SkillLevels: skillLevel,
  array[Days, ShiftTypes] of int: maxWorkloadPerShift,
);
type NurseVarData = record(
  array[Days, ShiftTypes] of var bool: isWorking,
  array[Days, ShiftTypes, RoomIds] of var bool: assignedRooms,
  array[Days, ShiftTypes] of var int: expectedWorkload,

  array[Days] of 0..maxPenalityValue: penaltySkillLevel,
  array[Days] of 0..maxPenalityValue: penaltyCare,
  array[Days] of 0..maxPenalityValue: penaltyWorkload,
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
  array[Days] of int: availableCapacity,

  %deprecated
  array[Days] of opt Genders: predefinedGenderAssignment,
);
type RoomVarData = record(
  array[Days] of var bool: isOccupied,
  array[Days] of var 0..100: assignedPatients,  
  array[Days] of var Genders: assignedGender,
  array[Days, ShiftTypes] of var int: workload,

  array[Days] of var 0..length(AgeGroups): penaltyAgeMixing,


  array[Days] of var opt AgeGroups: maxAgeGroup,
  array[Days] of var opt AgeGroups: minAgeGroup,
  array[Days] of var opt AgeGroups: maxAgeGroupOccupants,
  array[Days] of var opt AgeGroups: minAgeGroupOccupants,
  array[Days] of var opt AgeGroups: maxAgeGroupPatients,
  array[Days] of var opt AgeGroups: minAgeGroupPatients,
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
type Occupant = OccupantFixData;


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
  var 0..maxDay: admissionDay,
  var 0..maxDay: admittedUntil,
  var bool: isPostponed, 
  var bool: isAdmitted, % isPostponed <-> !isAdmited

  var opt OperatingTheaterIds: assignedOT,
  array[Days] of var opt RoomIds: assignedRoom,
  var 0..maxDay: penaltyAdmissionDelay,
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


var 0..10000000: score = 
    Penalties.roomMixedAge
  + Penalties.roomNurseSkill
  + Penalties.continuityOfCare
  + Penalties.nurseEccessiveWorkload
  + Penalties.openOperatingTheater
  + Penalties.surgeonTransfer
  + Penalties.patientDelay
  + Penalties.unscheduledOptional;


% Can be removed
enum RoomIdOrNotAssigned = {RoomNotAssigned} ++ C_room(RoomIds);
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
%array[OccupantIds] of OccupantVarData: occupantVar;
array[OperatingTheaterIds] of OperatingTheaterVarData: operatingTheaterVar;
array[RoomIds] of RoomVarData: roomVar;

int: numberOfPatients = length(patientData);
int: numberOfOccupants = length(occupantData);

% Usage may cause memory segmentation fault since version 2.9.5, was fine before. Seems to happen when a combined Type has 'too many' fields.
%
%array[NurseIds] of Nurse: nurses ::output = [id: nurseData[id] ++ nurseVar[id] | id in NurseIds];
%array[SurgeonIds] of Surgeon: surgeons ::output = [id: surgeonData[id] ++ surgeonVar[id] | id in SurgeonIds];
%array[PatientIds] of Patient: patients ::output = [id: patientData[id] ++ patientVar[id] | id in PatientIds]; % <-- has too many fields ?!?
%array[OccupantIds] of Occupant: occupants ::output = [id: occupantData[id] | id in OccupantIds];
%array[OperatingTheaterIds] of OperatingTheater: operatingTheaters ::output = [id: operatingTheaterData[id] ++ operatingTheaterVar[id] | id in OperatingTheaterIds];
%array[RoomIds] of Room: rooms ::output = [id: roomData[id] ++ roomVar[id] | id in RoomIds];




%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%
% UTILITY CONSTRAINTS
%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

constraint forall(patient in patientVar)(
  patient.isPostponed <-> not patient.isAdmitted
);

constraint forall(id in PatientIds)(
  patientVar[id].admittedUntil = patientVar[id].admissionDay + patientData[id].lengthOfStay - 1
);

constraint forall(roomId in RoomIds, day in Days)(
  let {
    bool: hasOccupants = exists(occupant in occupantData)(occupant.assignedRoom == roomId /\ day < occupant.lengthOfStay);
    var bool: hasPatients = exists(patient in patientVar)(patient.assignedRoom[day] == roomId);
  } in
  roomVar[roomId].isOccupied[day] <-> hasOccupants \/ hasPatients
);

constraint forall(nurseId in NurseIds, day in Days, shift in ShiftTypes)(
  nurseVar[nurseId].isWorking[day, shift] <-> nurseData[nurseId].maxWorkloadPerShift[day, shift] > 0
);

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


% include "OutConsoleFormat.mzn";
% include "OutJsonFormat.mzn";