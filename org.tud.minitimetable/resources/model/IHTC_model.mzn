int: minutesPerDay = 24 * 60;
type MinuteOfDay = 0..minutesPerDay;
int: maxPenalityValue = 1000000;

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

var int: score = 
    Penalties.roomMixedAge
  + Penalties.roomNurseSkill
  + Penalties.continuityOfCare
  + Penalties.nurseEccessiveWorkload
  + Penalties.openOperatingTheater
  + Penalties.surgeonTransfer
  + Penalties.patientDelay
  + Penalties.unscheduledOptional;


type WorkShift = record(
  Day: day,
  ShiftType: shift,
  int: maxLoad,
);

type NurseAssignment = record(
  var Day: day,
  var ShiftType: shift,
  array[RoomId] of var bool: assignedToRooms,
);


type NurseFixData = record(
  NurseId: id,
  SkillLevel: skillLevel,
  array[_] of WorkShift: workingShifts,
);
type NurseVarData = record(
  array[Day] of var NurseAssignment: assignments,
);
type Nurse = NurseFixData ++ NurseVarData;


type SurgeonFixData = record(
  SurgeonId: id,
  array[Day] of MinuteOfDay: maxSurgeryTime,
);
type SurgeonVarData = record(
  array[Day] of var MinuteOfDay: assignedSurgeryTime
);
type Surgeon = SurgeonFixData ++ SurgeonVarData;


type OperatingTheaterFixData = record(
  OperatingTheaterId: id,
  array[Day] of MinuteOfDay: availability,
);
type OperatingTheaterVarData = record(
  array[Day] of var bool: isAvailable,
  array[Day] of var MinuteOfDay: assignedTime,
);
type OperatingTheater = OperatingTheaterFixData ++ OperatingTheaterVarData;


type RoomFixData = record(
  RoomId: id,
  int: capacity,
  array[Day] of int: availableCapacity,
  array[Day] of opt Gender: fixGenderAssignment,
);
type RoomVarData = record(
  array[Day] of var 0..100: assignedPatients,  
  array[Day] of var opt Gender: assignedGender,
  array[Day] of var int: agePenalty,
  array[Day] of var opt AgeGroup: maxAgeGroup,
  array[Day] of var opt AgeGroup: minAgeGroup,
);
type Room = RoomFixData ++ RoomVarData;


type OccupantFixData = record (
  OccupantId: id,
  Gender: gender,
  AgeGroup: ageGroup,
  int: lengthOfStay,
  array[_] of int: workloadProduced, %length = lengthOfStay * length(ShiftType)
  array[_] of SkillLevel: skillLevelRequired, %length = lengthOfStay * length(ShiftType)
  RoomId: assignedToRoom,
);
type Occupant = OccupantFixData;


type PatientFixData = record (
  PatientId: id,
  bool: mandatory,
  Gender: gender,
  AgeGroup: ageGroup,
  int: lengthOfStay,
  Day: earliestPossibleAdmission,
  Day: latestPossibleAdmission,
  int: surgeryDuration,
  SurgeonId: surgeonId,
  array[_] of RoomId: incompatibleRooms,
  array[_] of int: workloadProduced,
  array[_] of SkillLevel: skillLevelRequired,
);
type PatientVarData = record (
  var 0..maxDay: admissionDay,
  var bool: isPostponed,
  var bool: isAdmitted,
  var opt RoomId: assignedToRoom,
  var opt OperatingTheaterId: assignedOT,

  array[Day] of var opt RoomId: assignedRoom,
  var 0..maxDay: admissionDelay,
);
type Patient = PatientFixData ++ PatientVarData;


WeightType: Weights;
enum Gender;
enum ShiftType;
enum AgeGroup;
enum NurseId;
enum SurgeonId;
enum OperatingTheaterId;
enum RoomId;
enum PatientId;
enum OccupantId;

%Gender: NotAssignedGender = min(Gender);

int: skillLevels;
set of int: SkillLevel = 0..skillLevels-1;
% enum SkillLevel = _(0..skillLevels-1);
% int: days;
int: firstDay = 0;
int: lastDay;
int: maxDay;
set of int: Day = 0..lastDay;
% enum Day = _(firstDay..lastDay);

enum RoomIdOrNotAssigned = {RoomNotAssigned} ++ C_room(RoomId);
enum GenderOrNotAssigned = {GenderNotAssigned} ++ C_gender(Gender);



array[_] of NurseFixData: nurseData;
array[_] of SurgeonFixData: surgeonData;
array[_] of PatientFixData: patientData;
array[_] of OccupantFixData: occupantData;
array[_] of OperatingTheaterFixData: operatingTheaterData;
array[_] of RoomFixData: roomData;


array[index_set(nurseData)] of NurseVarData: nurseVar;
array[index_set(surgeonData)] of SurgeonVarData: surgeonVar;
array[index_set(patientData)] of PatientVarData: patientVar;
%array[index_set(occupantData)] of OccupantVarData: occupantVar;
array[index_set(operatingTheaterData)] of OperatingTheaterVarData: operatingTheaterVar;
array[index_set(roomData)] of RoomVarData: roomVar;
 
array[NurseId] of Nurse: nurses ::output = [nurseData[id] ++ nurseVar[id] | id in index_set(NurseId)];
array[SurgeonId] of Surgeon: surgeons ::output = [surgeonData[id] ++ surgeonVar[id] | id in index_set(SurgeonId)];
array[PatientId] of Patient: patients ::output = [patientData[id] ++ patientVar[id] | id in index_set(PatientId)];
array[_] of Occupant: occupants ::output = array1d(OccupantId, occupantData);% [occupantData[id] | id in index_set(OccupantId)];
array[OperatingTheaterId] of OperatingTheater: operatingTheaters ::output = [operatingTheaterData[id] ++ operatingTheaterVar[id] | id in index_set(OperatingTheaterId)];
array[RoomId] of Room: rooms ::output = [roomData[id] ++ roomVar[id] | id in index_set(RoomId)];

int: NumberOfPatients = length(patientData);
int: NumberOfOccupants = length(occupantData);

var PenalityType: Penalties;

output
[ "score = \(score);\n\n"] ++

[ "patients = [\n" ] ++
[ "\t\(p) \n" | p in patientVar] ++
[ "];\n\n" ] ++

[ "rooms = [\n"] ++
[ "\t(\n" ++ 
  "\t\tmaxAgeGroup: \(r.maxAgeGroup)\n" ++
  "\t\tminAgeGroup: \(r.minAgeGroup)\n" ++
  "\t\tassignedPatients: \(r.assignedPatients)\n" ++
  "\t)\n" | r in rooms] ++

%[ "\t\(r) \n" | r in rooms] ++
[ "];\n\n" ] ++ 

[ "nurses = [\n" ] ++
[ "\t(\n \(n) \n" | n in nurseVar] ++
[ "];\n\n" ] ++
[ "\(Penalties)"]
